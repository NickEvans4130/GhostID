package com.ghostid.app.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.ghostid.app.domain.model.Account
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeepLinkLauncher @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun launchSignup(account: Account) {
        val platform = account.platform

        // Copy password to clipboard with auto-clear
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("${platform.displayName} password", account.password))
        ClipboardClearService.startWithTimeout(context, 30_000L)

        // Try app deep link first, fall back to browser
        val intent = platform.appDeepLink
            ?.let { deepLink ->
                Intent(Intent.ACTION_VIEW, Uri.parse(deepLink)).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }.takeIf { it.resolveActivity(context.packageManager) != null }
            }
            ?: browserIntent(platform.signupUrl)

        context.startActivity(intent)
    }

    private fun browserIntent(url: String): Intent =
        Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
}
