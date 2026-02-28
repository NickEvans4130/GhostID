package com.ghostid.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class GhostIDApp : Application() {

    companion object {
        const val CLIPBOARD_CHANNEL_ID = "clipboard_guard"
        const val CLIPBOARD_NOTIFICATION_ID = 1001
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val channel = NotificationChannel(
            CLIPBOARD_CHANNEL_ID,
            "Clipboard Guard",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Alerts when a GhostID password is in the clipboard"
        }
        getSystemService(NotificationManager::class.java)
            ?.createNotificationChannel(channel)
    }
}
