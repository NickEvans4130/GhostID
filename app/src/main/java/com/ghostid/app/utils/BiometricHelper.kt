package com.ghostid.app.utils

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * Wraps the AndroidX BiometricPrompt API.
 * Supports fingerprint, face unlock, and device PIN/pattern as fallback.
 */
class BiometricHelper(private val activity: FragmentActivity) {

    private val allowedAuthenticators = BIOMETRIC_STRONG or DEVICE_CREDENTIAL

    /** Returns true if the device has any authentication method enrolled and available. */
    fun canAuthenticate(): Boolean =
        BiometricManager.from(activity).canAuthenticate(allowedAuthenticators) ==
            BiometricManager.BIOMETRIC_SUCCESS

    /** Human-readable explanation of why authentication is unavailable. */
    fun unavailabilityReason(): String = when (
        BiometricManager.from(activity).canAuthenticate(allowedAuthenticators)
    ) {
        BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> "No biometric hardware found"
        BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> "Biometric hardware unavailable"
        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> "No biometric or PIN enrolled in device settings"
        BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> "A security update is required"
        else -> "Authentication unavailable"
    }

    /**
     * Shows the system biometric / PIN prompt.
     *
     * [onSuccess] — called on the main thread when the user authenticates.
     * [onError]   — called with a message only on hard errors (not on user-cancelled).
     */
    fun authenticate(
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        val executor = ContextCompat.getMainExecutor(activity)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                val isCancelled = errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                    errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
                    errorCode == BiometricPrompt.ERROR_CANCELED
                if (!isCancelled) {
                    onError(errString.toString())
                }
            }

            override fun onAuthenticationFailed() {
                // Single failed attempt — system shows its own feedback; no action needed here.
            }
        }

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock GhostID")
            .setSubtitle("Verify your identity to continue")
            .setAllowedAuthenticators(allowedAuthenticators)
            .build()

        BiometricPrompt(activity, executor, callback).authenticate(promptInfo)
    }
}
