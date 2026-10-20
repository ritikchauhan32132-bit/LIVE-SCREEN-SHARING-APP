package com.ritiksc.remoteaccess.devices

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import com.ritiksc.remoteaccess.common.CodeGenerator
import com.ritiksc.remoteaccess.common.Result
import com.ritiksc.remoteaccess.common.UiState
import com.ritiksc.remoteaccess.common.model.ConnectionHistory
import com.ritiksc.remoteaccess.common.model.DeviceInfo
import com.ritiksc.remoteaccess.common.model.DeviceStatus
import com.ritiksc.remoteaccess.common.model.PairedDevice
import com.ritiksc.remoteaccess.settings.AppPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class DeviceViewModel(
    application: Application,
    private val deviceRepository: DeviceRepository = DeviceRepository(),
    private val appPreferences: AppPreferences = AppPreferences(application)
) : AndroidViewModel(application) {

    private val _pairedDevices = MutableStateFlow<UiState<List<PairedDevice>>>(UiState.loading())
    val pairedDevices: StateFlow<UiState<List<PairedDevice>>> = _pairedDevices.asStateFlow()

    private val _connectionHistory = MutableStateFlow<UiState<List<ConnectionHistory>>>(UiState.loading())
    val connectionHistory: StateFlow<UiState<List<ConnectionHistory>>> = _connectionHistory.asStateFlow()

    private val _selectedDevice = MutableStateFlow<DeviceInfo?>(null)
    val selectedDevice: StateFlow<DeviceInfo?> = _selectedDevice.asStateFlow()

    private val _localDeviceId = MutableStateFlow<String>("")
    val localDeviceId: StateFlow<String> = _localDeviceId.asStateFlow()

    init {
        ensureLocalDeviceRegistered()
        loadPairedDevices()
        loadConnectionHistory()
    }

    private fun ensureLocalDeviceRegistered() {
        viewModelScope.launch {
            var currentId = appPreferences.deviceId.firstOrNull()
            if (currentId.isNullOrEmpty()) {
                currentId = CodeGenerator.generateDeviceId()
                appPreferences.setDeviceId(currentId)
            }
            _localDeviceId.value = currentId

            // Get FCM Token
            val fcmToken = try {
                FirebaseMessaging.getInstance().token.await()
            } catch (e: Exception) {
                ""
            }

            deviceRepository.registerDevice(currentId, fcmToken)
            appPreferences.setDeviceRegistered(true)
        }
    }

    fun loadPairedDevices() {
        viewModelScope.launch {
            deviceRepository.observePairedDevices().collect { devices ->
                if (devices.isEmpty()) {
                    _pairedDevices.value = UiState.empty()
                } else {
                    _pairedDevices.value = UiState.success(devices)
                }
            }
        }
    }

    fun loadConnectionHistory() {
        viewModelScope.launch {
            _connectionHistory.value = UiState.loading()
            when (val result = deviceRepository.getConnectionHistory()) {
                is Result.Success -> {
                    if (result.data.isEmpty()) {
                        _connectionHistory.value = UiState.empty()
                    } else {
                        _connectionHistory.value = UiState.success(result.data)
                    }
                }
                is Result.Error -> {
                    _connectionHistory.value = UiState.error(result.message)
                }
                is Result.Loading -> {}
            }
        }
    }

    fun loadDeviceInfo(deviceId: String) {
        viewModelScope.launch {
            when (val result = deviceRepository.getDeviceInfo(deviceId)) {
                is Result.Success -> {
                    _selectedDevice.value = result.data
                }
                is Result.Error -> {
                    _selectedDevice.value = null
                }
                is Result.Loading -> {}
            }
        }
    }

    fun removePairedDevice(pairingId: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = deviceRepository.removePairedDevice(pairingId)
            onComplete(result.isSuccess)
        }
    }

    fun updateMyStatus(status: DeviceStatus, isOnline: Boolean) {
        viewModelScope.launch {
            val id = _localDeviceId.value
            if (id.isNotEmpty()) {
                deviceRepository.updateDeviceStatus(id, status, isOnline)
            }
        }
    }
}
