package com.ritiksc.remoteaccess.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.ritiksc.remoteaccess.common.Result
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    val currentUser: FirebaseUser? get() = auth.currentUser
    val isLoggedIn: Boolean get() = auth.currentUser != null

    val authStateFlow: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun login(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user?.let {
                Result.success(it)
            } ?: Result.error("Login failed: No user returned")
        } catch (e: Exception) {
            Result.error(mapAuthError(e), e)
        }
    }

    suspend fun register(email: String, password: String, displayName: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let { user ->
                val profileUpdate = UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .build()
                user.updateProfile(profileUpdate).await()
                Result.success(user)
            } ?: Result.error("Registration failed: No user returned")
        } catch (e: Exception) {
            Result.error(mapAuthError(e), e)
        }
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.error(mapAuthError(e), e)
        }
    }

    fun logout() {
        auth.signOut()
    }

    suspend fun getIdToken(forceRefresh: Boolean = false): String? {
        return try {
            currentUser?.getIdToken(forceRefresh)?.await()?.token
        } catch (e: Exception) {
            null
        }
    }

    private fun mapAuthError(e: Exception): String {
        val message = e.message ?: return "An unknown error occurred"
        return when {
            "badly formatted" in message -> "Please enter a valid email address"
            "no user record" in message || "USER_NOT_FOUND" in message ->
                "No account found with this email"
            "password is invalid" in message || "INVALID_PASSWORD" in message ->
                "Incorrect password"
            "email address is already" in message || "EMAIL_EXISTS" in message ->
                "An account with this email already exists"
            "blocked all requests" in message || "TOO_MANY_REQUESTS" in message ->
                "Too many attempts. Please try again later"
            "network error" in message || "NETWORK" in message ->
                "Network error. Check your internet connection"
            "weak password" in message ->
                "Password must be at least 6 characters"
            else -> message
        }
    }
}
