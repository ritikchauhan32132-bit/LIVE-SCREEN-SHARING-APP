package com.ritiksc.remoteaccess.connection

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Firebase Cloud Messaging service for receiving push notifications
 * about connection requests, session updates, and pairing events.
 */
class RitikMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "RitikMessaging"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token received")
        // Token will be updated when the app next starts and registers the device
        // We don't handle it here to avoid race conditions with device registration
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "Message received: ${message.data}")

        val data = message.data
        when (data["type"]) {
            "connection_request" -> {
                val sessionId = data["sessionId"] ?: return
                val controllerName = data["controllerName"] ?: "Unknown Device"
                showConnectionRequestNotification(sessionId, controllerName)
            }
            "session_update" -> {
                val sessionId = data["sessionId"] ?: return
                val status = data["status"] ?: return
                Log.d(TAG, "Session $sessionId updated to $status")
            }
            "pairing_request" -> {
                val deviceName = data["deviceName"] ?: "Unknown Device"
                showPairingNotification(deviceName)
            }
        }
    }

    private fun showConnectionRequestNotification(sessionId: String, controllerName: String) {
        // Notification will be shown through the app's notification manager
        // when the user opens the connection request screen
        Log.d(TAG, "Connection request from $controllerName for session $sessionId")
    }

    private fun showPairingNotification(deviceName: String) {
        Log.d(TAG, "Pairing request from $deviceName")
    }
}
