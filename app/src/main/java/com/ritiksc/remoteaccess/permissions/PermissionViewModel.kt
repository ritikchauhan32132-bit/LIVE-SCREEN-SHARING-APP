package com.ritiksc.remoteaccess.permissions

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PermissionViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val permissionManager = PermissionManager(application)

    private val _permissions = MutableStateFlow<List<PermissionManager.PermissionInfo>>(emptyList())
    val permissions: StateFlow<List<PermissionManager.PermissionInfo>> = _permissions.asStateFlow()

    init {
        refreshPermissions()
    }

    fun refreshPermissions() {
        _permissions.value = permissionManager.getAllPermissions()
    }

    fun hasRequiredPermissions(): Boolean {
        return permissionManager.hasCameraPermission() && permissionManager.hasNotificationPermission()
    }

    fun getAppSettingsIntent() = permissionManager.getAppSettingsIntent()
}
