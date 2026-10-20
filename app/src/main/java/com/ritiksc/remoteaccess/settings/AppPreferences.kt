package com.ritiksc.remoteaccess.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ritik_sc_prefs")

class AppPreferences(private val context: Context) {

    companion object {
        val KEY_DEVICE_ID = stringPreferencesKey("device_id")
        val KEY_DEVICE_REGISTERED = booleanPreferencesKey("device_registered")
        val KEY_ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
        val KEY_DEVICE_ROLE = stringPreferencesKey("device_role") // "controller" or "remote"
        val KEY_AUTO_ACCEPT_CONNECTIONS = booleanPreferencesKey("auto_accept_connections")
        val KEY_SHOW_NOTIFICATIONS = booleanPreferencesKey("show_notifications")
        val KEY_VIDEO_QUALITY = stringPreferencesKey("video_quality") // "low", "medium", "high"
        val KEY_LAST_CONNECTED_DEVICE = stringPreferencesKey("last_connected_device")
    }

    val deviceId: Flow<String?> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[KEY_DEVICE_ID] }

    val isDeviceRegistered: Flow<Boolean> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[KEY_DEVICE_REGISTERED] ?: false }

    val isOnboardingComplete: Flow<Boolean> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[KEY_ONBOARDING_COMPLETE] ?: false }

    val deviceRole: Flow<String?> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[KEY_DEVICE_ROLE] }

    val autoAcceptConnections: Flow<Boolean> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[KEY_AUTO_ACCEPT_CONNECTIONS] ?: false }

    val showNotifications: Flow<Boolean> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[KEY_SHOW_NOTIFICATIONS] ?: true }

    val videoQuality: Flow<String> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[KEY_VIDEO_QUALITY] ?: "medium" }

    suspend fun setDeviceId(id: String) {
        context.dataStore.edit { it[KEY_DEVICE_ID] = id }
    }

    suspend fun setDeviceRegistered(registered: Boolean) {
        context.dataStore.edit { it[KEY_DEVICE_REGISTERED] = registered }
    }

    suspend fun setOnboardingComplete(complete: Boolean) {
        context.dataStore.edit { it[KEY_ONBOARDING_COMPLETE] = complete }
    }

    suspend fun setDeviceRole(role: String) {
        context.dataStore.edit { it[KEY_DEVICE_ROLE] = role }
    }

    suspend fun setAutoAcceptConnections(autoAccept: Boolean) {
        context.dataStore.edit { it[KEY_AUTO_ACCEPT_CONNECTIONS] = autoAccept }
    }

    suspend fun setShowNotifications(show: Boolean) {
        context.dataStore.edit { it[KEY_SHOW_NOTIFICATIONS] = show }
    }

    suspend fun setVideoQuality(quality: String) {
        context.dataStore.edit { it[KEY_VIDEO_QUALITY] = quality }
    }

    suspend fun setLastConnectedDevice(deviceId: String) {
        context.dataStore.edit { it[KEY_LAST_CONNECTED_DEVICE] = deviceId }
    }

    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }
}
