package com.ritiksc.remoteaccess.common

import android.os.Build
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import java.security.SecureRandom

object Constants {
    // Pairing
    const val PAIRING_CODE_LENGTH = 8
    const val PAIRING_CODE_EXPIRY_MS = 5 * 60 * 1000L // 5 minutes
    const val MAX_PAIRING_ATTEMPTS = 5
    const val PAIRING_RATE_LIMIT_WINDOW_MS = 15 * 60 * 1000L // 15 minutes

    // Connection
    const val CONNECTION_TIMEOUT_MS = 30_000L
    const val HEARTBEAT_INTERVAL_MS = 10_000L
    const val SESSION_MAX_DURATION_MS = 4 * 60 * 60 * 1000L // 4 hours
    const val RECONNECT_MAX_ATTEMPTS = 5
    const val RECONNECT_BASE_DELAY_MS = 1000L
    const val RECONNECT_MAX_DELAY_MS = 30_000L

    // WebRTC
    const val VIDEO_WIDTH = 1280
    const val VIDEO_HEIGHT = 720
    const val VIDEO_FPS = 30
    const val VIDEO_BITRATE = 2_000_000 // 2 Mbps

    // Firestore collections
    const val COLLECTION_USERS = "users"
    const val COLLECTION_DEVICES = "devices"
    const val COLLECTION_PAIRINGS = "pairings"
    const val COLLECTION_PAIRING_CODES = "pairing_codes"
    const val COLLECTION_SESSIONS = "sessions"
    const val COLLECTION_SIGNALING = "signaling"
    const val COLLECTION_HISTORY = "connection_history"

    // STUN/TURN - These are public Google STUN servers.
    // For production, you MUST configure your own TURN server.
    val DEFAULT_ICE_SERVERS = listOf(
        "stun:stun.l.google.com:19302",
        "stun:stun1.l.google.com:19302"
    )
}

object DeviceUtils {
    fun getDeviceName(): String {
        val manufacturer = Build.MANUFACTURER.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        }
        val model = Build.MODEL
        return if (model.startsWith(manufacturer, ignoreCase = true)) {
            model
        } else {
            "$manufacturer $model"
        }
    }

    fun getAndroidVersion(): String = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
    fun getManufacturer(): String = Build.MANUFACTURER
    fun getModel(): String = Build.MODEL
}

object TimeUtils {
    private val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    private val fullFormat = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault())

    fun formatDate(timestamp: Long): String = dateFormat.format(Date(timestamp))
    fun formatTime(timestamp: Long): String = timeFormat.format(Date(timestamp))
    fun formatFull(timestamp: Long): String = fullFormat.format(Date(timestamp))

    fun formatDuration(durationMs: Long): String {
        val seconds = (durationMs / 1000) % 60
        val minutes = (durationMs / (1000 * 60)) % 60
        val hours = durationMs / (1000 * 60 * 60)
        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            minutes > 0 -> "${minutes}m ${seconds}s"
            else -> "${seconds}s"
        }
    }

    fun formatRelativeTime(timestamp: Long): String {
        val diff = System.currentTimeMillis() - timestamp
        return when {
            diff < 60_000 -> "Just now"
            diff < 3_600_000 -> "${diff / 60_000}m ago"
            diff < 86_400_000 -> "${diff / 3_600_000}h ago"
            diff < 604_800_000 -> "${diff / 86_400_000}d ago"
            else -> formatDate(timestamp)
        }
    }
}

object CodeGenerator {
    private val secureRandom = SecureRandom()
    private const val CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789" // No I, O, 0, 1 to avoid confusion

    fun generatePairingCode(length: Int = Constants.PAIRING_CODE_LENGTH): String {
        return buildString {
            repeat(length) {
                append(CODE_CHARS[secureRandom.nextInt(CODE_CHARS.length)])
            }
        }
    }

    fun generateDeviceId(): String = UUID.randomUUID().toString()
    fun generateSessionId(): String = UUID.randomUUID().toString()
}
