package com.ritiksc.remoteaccess.devices

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.ritiksc.remoteaccess.common.Constants
import com.ritiksc.remoteaccess.common.DeviceUtils
import com.ritiksc.remoteaccess.common.Result
import com.ritiksc.remoteaccess.common.model.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class DeviceRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val uid: String get() = auth.currentUser?.uid ?: ""

    suspend fun registerDevice(deviceId: String, fcmToken: String): Result<DeviceInfo> {
        return try {
            val device = DeviceInfo(
                deviceId = deviceId,
                deviceName = DeviceUtils.getDeviceName(),
                ownerUid = uid,
                model = DeviceUtils.getModel(),
                manufacturer = DeviceUtils.getManufacturer(),
                androidVersion = DeviceUtils.getAndroidVersion(),
                appVersion = "1.0.0",
                fcmToken = fcmToken,
                registeredAt = System.currentTimeMillis(),
                lastSeenAt = System.currentTimeMillis(),
                isOnline = true,
                status = DeviceStatus.ONLINE
            )

            firestore.collection(Constants.COLLECTION_DEVICES)
                .document(deviceId)
                .set(device)
                .await()

            Result.success(device)
        } catch (e: Exception) {
            Result.error("Failed to register device: ${e.message}", e)
        }
    }

    suspend fun updateDeviceStatus(deviceId: String, status: DeviceStatus, isOnline: Boolean): Result<Unit> {
        return try {
            firestore.collection(Constants.COLLECTION_DEVICES)
                .document(deviceId)
                .update(
                    mapOf(
                        "status" to status.name,
                        "isOnline" to isOnline,
                        "lastSeenAt" to System.currentTimeMillis()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error("Failed to update device status: ${e.message}", e)
        }
    }

    suspend fun updateFcmToken(deviceId: String, token: String): Result<Unit> {
        return try {
            firestore.collection(Constants.COLLECTION_DEVICES)
                .document(deviceId)
                .update("fcmToken", token)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error("Failed to update FCM token: ${e.message}", e)
        }
    }

    fun observePairedDevices(): Flow<List<PairedDevice>> = callbackFlow {
        val listener: ListenerRegistration = firestore.collection(Constants.COLLECTION_PAIRINGS)
            .whereEqualTo("ownerUid", uid)
            .whereEqualTo("isActive", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                val devices = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(PairedDevice::class.java)
                } ?: emptyList()
                trySend(devices)
            }
        awaitClose { listener.remove() }
    }

    suspend fun getDeviceInfo(deviceId: String): Result<DeviceInfo> {
        return try {
            val doc = firestore.collection(Constants.COLLECTION_DEVICES)
                .document(deviceId)
                .get()
                .await()

            val device = doc.toObject(DeviceInfo::class.java)
            if (device != null) {
                Result.success(device)
            } else {
                Result.error("Device not found")
            }
        } catch (e: Exception) {
            Result.error("Failed to get device info: ${e.message}", e)
        }
    }

    suspend fun removePairedDevice(deviceIdOrPairingId: String): Result<Unit> {
        return try {
            val directDoc = firestore.collection(Constants.COLLECTION_PAIRINGS)
                .document(deviceIdOrPairingId).get().await()

            if (directDoc.exists()) {
                firestore.collection(Constants.COLLECTION_PAIRINGS)
                    .document(deviceIdOrPairingId)
                    .update("isActive", false)
                    .await()
            } else {
                val query = firestore.collection(Constants.COLLECTION_PAIRINGS)
                    .whereEqualTo("ownerUid", uid)
                    .whereEqualTo("remoteDeviceId", deviceIdOrPairingId)
                    .get()
                    .await()

                val batch = firestore.batch()
                query.documents.forEach { doc ->
                    batch.update(doc.reference, "isActive", false)
                }
                batch.commit().await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error("Failed to remove device: ${e.message}", e)
        }
    }

    suspend fun getConnectionHistory(): Result<List<ConnectionHistory>> {
        return try {
            val snapshot = firestore.collection(Constants.COLLECTION_HISTORY)
                .whereEqualTo("ownerUid", uid)
                .orderBy("startedAt", Query.Direction.DESCENDING)
                .limit(50)
                .get()
                .await()

            // Note: Firestore requires a composite index for this query.
            // The index will be created automatically if you follow the link
            // in the error message when this query first runs.

            val history = snapshot.documents.mapNotNull { doc ->
                doc.toObject(ConnectionHistory::class.java)
            }
            Result.success(history)
        } catch (e: Exception) {
            Result.error("Failed to load history: ${e.message}", e)
        }
    }

    fun observeDeviceStatus(deviceId: String): Flow<DeviceInfo?> = callbackFlow {
        val listener = firestore.collection(Constants.COLLECTION_DEVICES)
            .document(deviceId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }
                val device = snapshot?.toObject(DeviceInfo::class.java)
                trySend(device)
            }
        awaitClose { listener.remove() }
    }
}
