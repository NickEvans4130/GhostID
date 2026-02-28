package com.ghostid.app.utils

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.ghostid.app.GhostIDApp.Companion.CLIPBOARD_CHANNEL_ID
import com.ghostid.app.GhostIDApp.Companion.CLIPBOARD_NOTIFICATION_ID
import com.ghostid.app.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ClipboardClearService : Service() {

    companion object {
        const val EXTRA_TIMEOUT_MS = "timeout_ms"
        const val ACTION_CLEAR_NOW = "com.ghostid.app.ACTION_CLEAR_NOW"
        private const val DEFAULT_TIMEOUT_MS = 30_000L

        fun startWithTimeout(context: Context, timeoutMs: Long) {
            context.startService(Intent(context, ClipboardClearService::class.java).apply {
                putExtra(EXTRA_TIMEOUT_MS, timeoutMs)
            })
        }
    }

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var clearJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_CLEAR_NOW) {
            clearClipboard()
            stopSelf()
            return START_NOT_STICKY
        }

        val timeoutMs = intent?.getLongExtra(EXTRA_TIMEOUT_MS, DEFAULT_TIMEOUT_MS) ?: DEFAULT_TIMEOUT_MS
        showNotification(timeoutMs)

        clearJob?.cancel()
        clearJob = scope.launch {
            delay(timeoutMs)
            clearClipboard()
            dismissNotification()
            stopSelf()
        }

        return START_NOT_STICKY
    }

    private fun clearClipboard() {
        val cm = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("", ""))
    }

    private fun dismissNotification() {
        getSystemService(NotificationManager::class.java)?.cancel(CLIPBOARD_NOTIFICATION_ID)
    }

    private fun showNotification(timeoutMs: Long) {
        val clearIntent = PendingIntent.getService(
            this, 0,
            Intent(this, ClipboardClearService::class.java).apply { action = ACTION_CLEAR_NOW },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val seconds = timeoutMs / 1000
        val notification = NotificationCompat.Builder(this, CLIPBOARD_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_ghost)
            .setContentTitle(getString(R.string.clipboard_notification_title))
            .setContentText(getString(R.string.clipboard_notification_body))
            .setSubText("Auto-clears in ${seconds}s")
            .setContentIntent(clearIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        getSystemService(NotificationManager::class.java)
            ?.notify(CLIPBOARD_NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        clearJob?.cancel()
        super.onDestroy()
    }
}
