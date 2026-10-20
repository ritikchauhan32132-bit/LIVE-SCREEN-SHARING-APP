package com.ritiksc.remoteaccess.pairing

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.ritiksc.remoteaccess.common.CodeGenerator
import com.ritiksc.remoteaccess.common.Constants
import com.ritiksc.remoteaccess.common.DeviceUtils
import com.ritiksc.remoteaccess.common.Result
import com.ritiksc.remoteaccess.common.model.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class PairingRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val uid: String get() = auth.currentUser?.uid ?: ""

    suspend fun generatePairingCode(deviceId: String): Result<PairingCode> {
        return try {
            val now = System.currentTimeMillis()

            // Rate limiting: check recent pairing code generation count
            val recentCodes = firestore.collection(Constants.COLLECTION_PAIRING_CODES)
                .whereEqualTo("deviceId", deviceId)
                .whereGreaterThan("createdAt", now - Constants.PAIRING_RATE_LIMIT_WINDOW_MS)
                .get()
                .await()

            if (recentCodes.size() >= Constants.MAX_PAIRING_ATTEMPTS) {
                return Result.error("Too many pairing attempts. Please wait 15 minutes before trying again.")
            }

            val code = CodeGenerator.generatePairingCode()
            val pairingCode = PairingCode(
                code = code,
                deviceId = deviceId,
                deviceName = DeviceUtils.getDeviceName(),
                ownerUid = uid,
                createdAt = now,
                expiresAt = now + Constants.PAIRING_CODE_EXPIRY_MS,
                isUsed = false
            )

            firestore.collection(Constants.COLLECTION_PAIRING_CODES)
                .document(code)
                .set(pairingCode)
                .await()

            Result.success(pairingCode)
        } catch (e: Exception) {
            Result.error("Failed to generate pairing code: ${e.message}", e)
        }
    }

    suspend fun validateAndUsePairingCode(
        code: String,
        controllerDeviceId: String,
        controllerDeviceName: String
    ): Result<PairedDevice> {
        return try {
            val doc = firestore.collection(Constants.COLLECTION_PAIRING_CODES)
                .document(code.uppercase().trim())
                .get()
                .await()

            val pairingCode = doc.toObject(PairingCode::class.java)
                ?: return Result.error("Invalid pairing code")

            // Validate code
            val now = System.currentTimeMillis()
            when {
                pairingCode.isUsed -> return Result.error("This pairing code has already been used")
                pairingCode.expiresAt < now -> return Result.error("This pairing code has expired")
                pairingCode.ownerUid != uid -> return Result.error("This code belongs to a different account")
                pairingCode.deviceId == controllerDeviceId ->
                    return Result.error("Cannot pair a device with itself")
            }

            // Mark code as used
            firestore.collection(Constants.COLLECTION_PAIRING_CODES)
                .document(code.uppercase().trim())
                .update("isUsed", true)
                .await()

            // Create pairing
            val pairingId = "${controllerDeviceId}_${pairingCode.deviceId}"
            val pairedDevice = PairedDevice(
                pairingId = pairingId,
                controllerDeviceId = controllerDeviceId,
                remoteDeviceId = pairingCode.deviceId,
                controllerDeviceName = controllerDeviceName,
                remoteDeviceName = pairingCode.deviceName,
                ownerUid = uid,
                pairedAt = now,
                isActive = true
            )

            firestore.collection(Constants.COLLECTION_PAIRINGS)
                .document(pairingId)
                .set(pairedDevice)
                .await()

            Result.success(pairedDevice)
        } catch (e: Exception) {
            Result.error("Pairing failed: ${e.message}", e)
        }
    }

    fun observePairingRequests(deviceId: String): Flow<List<PairedDevice>> = callbackFlow {
        val listener: ListenerRegistration = firestore.collection(Constants.COLLECTION_PAIRINGS)
            .whereEqualTo("remoteDeviceId", deviceId)
            .whereEqualTo("isActive", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                val pairings = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(PairedDevice::class.java)
                } ?: emptyList()
                trySend(pairings)
            }
        awaitClose { listener.remove() }
    }

    suspend fun generateQrCodeData(deviceId: String): Result<String> {
        return when (val result = generatePairingCode(deviceId)) {
            is Result.Success -> {
                // QR data format: ritiksc://pair?code=<CODE>&uid=<UID>
                val qrData = "ritiksc://pair?code=${result.data.code}&uid=$uid"
                Result.success(qrData)
            }
            is Result.Error -> Result.error(result.message, result.exception)
            is Result.Loading -> Result.loading()
        }
    }

    fun parsePairingQrData(data: String): Pair<String, String>? {
        return try {
            if (!data.startsWith("ritiksc://pair?")) return null
            val params = data.substringAfter("?").split("&").associate {
                val (key, value) = it.split("=")
                key to value
            }
            val code = params["code"] ?: return null
            val uidFromQr = params["uid"] ?: return null
            Pair(code, uidFromQr)
        } catch (e: Exception) {
            null
        }
    }
}
