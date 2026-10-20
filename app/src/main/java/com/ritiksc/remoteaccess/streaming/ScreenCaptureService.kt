package com.ritiksc.remoteaccess.streaming

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.ritiksc.remoteaccess.MainActivity
import com.ritiksc.remoteaccess.R
import com.ritiksc.remoteaccess.RitikScApplication

/**
 * Foreground service for screen capture.
 *
 * Android requires a foreground service with type "mediaProjection"
 * to perform screen capture. This service displays a persistent notification
 * while screen sharing is active.
 */
class ScreenCaptureService : Service() {

    companion object {
        const val ACTION_START = "com.ritiksc.remoteaccess.START_CAPTURE"
        const val ACTION_STOP = "com.ritiksc.remoteaccess.STOP_CAPTURE"
        const val EXTRA_SESSION_ID = "session_id"
        private const val NOTIFICATION_ID = 1001
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val sessionId = intent.getStringExtra(EXTRA_SESSION_ID) ?: "unknown"
                startForegroundWithNotification(sessionId)
            }
            ACTION_STOP -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun startForegroundWithNotification(sessionId: String) {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingOpenApp = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopIntent = Intent(this, ScreenCaptureService::class.java).apply {
            action = ACTION_STOP
        }
        val pendingStop = PendingIntent.getService(
            this, 1, stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, RitikScApplication.CHANNEL_SCREEN_CAPTURE)
            .setContentTitle("RITIK SC - Screen Sharing Active")
            .setContentText("Your screen is being shared. Tap to open the app.")
            .setSmallIcon(android.R.drawable.ic_menu_share)
            .setOngoing(true)
            .setContentIntent(pendingOpenApp)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Stop Sharing",
                pendingStop
            )
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // The WebRtcManager should handle actual resource cleanup
        // This service only manages the foreground notification
    }
}
