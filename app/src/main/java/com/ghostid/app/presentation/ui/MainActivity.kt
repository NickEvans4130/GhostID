package com.ghostid.app.presentation.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ghostid.app.presentation.navigation.NavGraph
import com.ghostid.app.presentation.ui.screens.LockScreen
import com.ghostid.app.presentation.ui.theme.GhostIDTheme
import com.ghostid.app.presentation.viewmodel.SettingsViewModel
import com.ghostid.app.utils.BiometricHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var biometricHelper: BiometricHelper

    // Survive configuration changes within the same session but reset on process death / re-launch
    private var isAuthenticated by mutableStateOf(false)
    private var authError by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        biometricHelper = BiometricHelper(this)

        setContent {
            val settingsVm: SettingsViewModel = hiltViewModel()
            val theme by settingsVm.theme.collectAsState()
            val appLock by settingsVm.appLock.collectAsState()

            GhostIDTheme(appTheme = theme) {
                if (appLock && !isAuthenticated) {
                    LockScreen(
                        onUnlockClick = { promptBiometric() },
                        errorMessage = authError,
                    )
                } else {
                    NavGraph()
                }
            }
        }

        // Auto-prompt biometric on launch if app lock is enabled
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                val settingsVm = androidx.lifecycle.ViewModelProvider(this@MainActivity)
                    .get(SettingsViewModel::class.java)
                val appLock = settingsVm.appLock.first()
                if (appLock && !isAuthenticated) {
                    promptBiometric()
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // Lock again whenever the app goes to background (if app lock is on)
        lifecycleScope.launch {
            val settingsVm = androidx.lifecycle.ViewModelProvider(this@MainActivity)
                .get(SettingsViewModel::class.java)
            if (settingsVm.appLock.first()) {
                isAuthenticated = false
                authError = null
            }
        }
    }

    private fun promptBiometric() {
        if (!biometricHelper.canAuthenticate()) {
            // No biometrics enrolled — unlock without auth (setting will be disabled in UI)
            isAuthenticated = true
            return
        }
        authError = null
        biometricHelper.authenticate(
            onSuccess = {
                isAuthenticated = true
                authError = null
            },
            onError = { message ->
                authError = message
            },
        )
    }
}
