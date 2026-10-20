package com.ritiksc.remoteaccess

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.google.firebase.FirebaseApp

class RitikScApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val channels = listOf(
            NotificationChannel(
                CHANNEL_SCREEN_CAPTURE,
                "Screen Capture",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows when screen sharing is active"
            },
            NotificationChannel(
                CHANNEL_CONNECTION,
                "Connection Requests",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts for incoming connection and pairing requests"
            },
            NotificationChannel(
                CHANNEL_GENERAL,
                "General",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "General notifications from RITIK SC"
            }
        )

        val notificationManager = getSystemService(NotificationManager::class.java)
        channels.forEach { notificationManager.createNotificationChannel(it) }
    }

    companion object {
        const val CHANNEL_SCREEN_CAPTURE = "ritik_sc_screen_capture"
        const val CHANNEL_CONNECTION = "ritik_sc_connection"
        const val CHANNEL_GENERAL = "ritik_sc_notifications"
    }
}
