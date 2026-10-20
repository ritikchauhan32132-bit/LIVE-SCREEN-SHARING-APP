package com.ritiksc.remoteaccess.connection

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.ritiksc.remoteaccess.common.CodeGenerator
import com.ritiksc.remoteaccess.common.Constants
import com.ritiksc.remoteaccess.common.Result
import com.ritiksc.remoteaccess.common.model.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class SignalingRepository {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val uid: String get() = auth.currentUser?.uid ?: ""

    suspend fun createSession(
        controllerDeviceId: String,
        remoteDeviceId: String,
        isViewOnly: Boolean = true
    ): Result<ConnectionSession> {
        return try {
            // Verify both devices belong to the user
            val controllerDoc = firestore.collection(Constants.COLLECTION_DEVICES)
                .document(controllerDeviceId).get().await()
            val remoteDoc = firestore.collection(Constants.COLLECTION_DEVICES)
                .document(remoteDeviceId).get().await()

            val controllerDevice = controllerDoc.toObject(DeviceInfo::class.java)
            val remoteDevice = remoteDoc.toObject(DeviceInfo::class.java)

            if (controllerDevice == null || remoteDevice == null) {
                return Result.error("One or both devices not found")
            }
            if (controllerDevice.ownerUid != uid || remoteDevice.ownerUid != uid) {
                return Result.error("Unauthorized: devices do not belong to your account")
            }

            // Check for existing active sessions
            val existingSessions = firestore.collection(Constants.COLLECTION_SESSIONS)
                .whereEqualTo("remoteDeviceId", remoteDeviceId)
                .whereEqualTo("status", SessionStatus.CONNECTED.name)
                .get().await()

            if (!existingSessions.isEmpty) {
                return Result.error("This device already has an active session")
            }

            val sessionId = CodeGenerator.generateSessionId()
            val session = ConnectionSession(
                sessionId = sessionId,
                controllerDeviceId = controllerDeviceId,
                remoteDeviceId = remoteDeviceId,
                ownerUid = uid,
                startedAt = System.currentTimeMillis(),
                status = SessionStatus.AWAITING_APPROVAL,
                isViewOnly = isViewOnly
            )

            firestore.collection(Constants.COLLECTION_SESSIONS)
                .document(sessionId)
                .set(session)
                .await()

            Result.success(session)
        } catch (e: Exception) {
            Result.error("Failed to create session: ${e.message}", e)
        }
    }

    suspend fun updateSessionStatus(sessionId: String, status: SessionStatus, endReason: String = ""): Result<Unit> {
        return try {
            val updates = mutableMapOf<String, Any>(
                "status" to status.name
            )
            if (status == SessionStatus.DISCONNECTED || status == SessionStatus.FAILED) {
                updates["endedAt"] = System.currentTimeMillis()
                updates["endReason"] = endReason
            }
            firestore.collection(Constants.COLLECTION_SESSIONS)
                .document(sessionId)
                .update(updates)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error("Failed to update session: ${e.message}", e)
        }
    }

    suspend fun sendSignalingMessage(message: SignalingMessage): Result<Unit> {
        return try {
            firestore.collection(Constants.COLLECTION_SIGNALING)
                .document("${message.sessionId}_${message.type.name}_${message.timestamp}")
                .set(message)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error("Failed to send signaling message: ${e.message}", e)
        }
    }

    fun observeSignalingMessages(sessionId: String, forDeviceId: String): Flow<SignalingMessage> = callbackFlow {
        val listener: ListenerRegistration = firestore.collection(Constants.COLLECTION_SIGNALING)
            .whereEqualTo("sessionId", sessionId)
            .whereEqualTo("targetDeviceId", forDeviceId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                snapshot?.documentChanges?.forEach { change ->
                    if (change.type == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                        val message = change.document.toObject(SignalingMessage::class.java)
                        trySend(message)
                    }
                }
            }
        awaitClose { listener.remove() }
    }

    fun observeSessionRequests(deviceId: String): Flow<ConnectionSession?> = callbackFlow {
        val listener = firestore.collection(Constants.COLLECTION_SESSIONS)
            .whereEqualTo("remoteDeviceId", deviceId)
            .whereEqualTo("status", SessionStatus.AWAITING_APPROVAL.name)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val session = snapshot?.documents?.firstOrNull()?.toObject(ConnectionSession::class.java)
                trySend(session)
            }
        awaitClose { listener.remove() }
    }

    fun observeSession(sessionId: String): Flow<ConnectionSession?> = callbackFlow {
        val listener = firestore.collection(Constants.COLLECTION_SESSIONS)
            .document(sessionId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }
                val session = snapshot?.toObject(ConnectionSession::class.java)
                trySend(session)
            }
        awaitClose { listener.remove() }
    }

    suspend fun saveConnectionHistory(session: ConnectionSession, controllerName: String, remoteName: String): Result<Unit> {
        return try {
            val history = ConnectionHistory(
                sessionId = session.sessionId,
                controllerDeviceName = controllerName,
                remoteDeviceName = remoteName,
                startedAt = session.startedAt,
                endedAt = session.endedAt,
                duration = if (session.endedAt > 0) session.endedAt - session.startedAt else 0,
                wasViewOnly = session.isViewOnly,
                endReason = session.endReason
            )

            firestore.collection(Constants.COLLECTION_HISTORY)
                .document(session.sessionId)
                .set(history)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.error("Failed to save history: ${e.message}", e)
        }
    }

    suspend fun cleanupSignaling(sessionId: String): Result<Unit> {
        return try {
            val messages = firestore.collection(Constants.COLLECTION_SIGNALING)
                .whereEqualTo("sessionId", sessionId)
                .get().await()

            val batch = firestore.batch()
            messages.documents.forEach { batch.delete(it.reference) }
            batch.commit().await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.error("Failed to cleanup signaling: ${e.message}", e)
        }
    }
}
