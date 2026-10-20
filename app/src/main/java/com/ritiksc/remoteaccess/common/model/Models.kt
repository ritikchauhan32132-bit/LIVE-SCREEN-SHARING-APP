package com.ritiksc.remoteaccess.common.model

import kotlinx.serialization.Serializable

@Serializable
data class DeviceInfo(
    val deviceId: String = "",
    val deviceName: String = "",
    val ownerUid: String = "",
    val model: String = "",
    val manufacturer: String = "",
    val androidVersion: String = "",
    val appVersion: String = "",
    val fcmToken: String = "",
    val registeredAt: Long = 0L,
    val lastSeenAt: Long = 0L,
    val isOnline: Boolean = false,
    val status: DeviceStatus = DeviceStatus.OFFLINE
)

@Serializable
enum class DeviceStatus {
    ONLINE,
    OFFLINE,
    SHARING_SCREEN,
    CONNECTED,
    BUSY
}

@Serializable
data class PairedDevice(
    val pairingId: String = "",
    val controllerDeviceId: String = "",
    val remoteDeviceId: String = "",
    val controllerDeviceName: String = "",
    val remoteDeviceName: String = "",
    val ownerUid: String = "",
    val pairedAt: Long = 0L,
    val lastConnectedAt: Long = 0L,
    val isActive: Boolean = true
)

@Serializable
data class PairingCode(
    val code: String = "",
    val deviceId: String = "",
    val deviceName: String = "",
    val ownerUid: String = "",
    val createdAt: Long = 0L,
    val expiresAt: Long = 0L,
    val isUsed: Boolean = false
)

@Serializable
data class ConnectionSession(
    val sessionId: String = "",
    val controllerDeviceId: String = "",
    val remoteDeviceId: String = "",
    val ownerUid: String = "",
    val startedAt: Long = 0L,
    val endedAt: Long = 0L,
    val status: SessionStatus = SessionStatus.PENDING,
    val isViewOnly: Boolean = true,
    val endReason: String = ""
)

@Serializable
enum class SessionStatus {
    PENDING,
    AWAITING_APPROVAL,
    APPROVED,
    CONNECTING,
    CONNECTED,
    RECONNECTING,
    DISCONNECTED,
    REJECTED,
    EXPIRED,
    FAILED
}

@Serializable
data class ConnectionHistory(
    val sessionId: String = "",
    val controllerDeviceName: String = "",
    val remoteDeviceName: String = "",
    val startedAt: Long = 0L,
    val endedAt: Long = 0L,
    val duration: Long = 0L,
    val wasViewOnly: Boolean = true,
    val endReason: String = ""
)

@Serializable
data class SignalingMessage(
    val type: SignalingType = SignalingType.OFFER,
    val sessionId: String = "",
    val senderDeviceId: String = "",
    val targetDeviceId: String = "",
    val payload: String = "",
    val timestamp: Long = 0L
)

@Serializable
enum class SignalingType {
    OFFER,
    ANSWER,
    ICE_CANDIDATE,
    SESSION_REQUEST,
    SESSION_APPROVED,
    SESSION_REJECTED,
    SESSION_END,
    HEARTBEAT
}
