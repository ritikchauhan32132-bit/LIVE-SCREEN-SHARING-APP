package com.ritiksc.remoteaccess.pairing

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ritiksc.remoteaccess.common.DeviceUtils
import com.ritiksc.remoteaccess.common.Result
import com.ritiksc.remoteaccess.common.UiState
import com.ritiksc.remoteaccess.common.model.PairedDevice
import com.ritiksc.remoteaccess.common.model.PairingCode
import com.ritiksc.remoteaccess.settings.AppPreferences
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class PairingViewModel(
    application: Application,
    private val pairingRepository: PairingRepository = PairingRepository(),
    private val appPreferences: AppPreferences = AppPreferences(application)
) : AndroidViewModel(application) {

    private val _generatedCode = MutableStateFlow<UiState<PairingCode>>(UiState())
    val generatedCode: StateFlow<UiState<PairingCode>> = _generatedCode.asStateFlow()

    private val _qrData = MutableStateFlow<String?>(null)
    val qrData: StateFlow<String?> = _qrData.asStateFlow()

    private val _secondsRemaining = MutableStateFlow<Long>(0L)
    val secondsRemaining: StateFlow<Long> = _secondsRemaining.asStateFlow()

    private val _pairResult = MutableStateFlow<UiState<PairedDevice>>(UiState())
    val pairResult: StateFlow<UiState<PairedDevice>> = _pairResult.asStateFlow()

    private val _incomingPairingRequests = MutableStateFlow<List<PairedDevice>>(emptyList())
    val incomingPairingRequests: StateFlow<List<PairedDevice>> = _incomingPairingRequests.asStateFlow()

    private var timerJob: Job? = null

    init {
        observeIncomingRequests()
    }

    private fun observeIncomingRequests() {
        viewModelScope.launch {
            val localId = appPreferences.deviceId.firstOrNull() ?: return@launch
            pairingRepository.observePairingRequests(localId).collect { requests ->
                _incomingPairingRequests.value = requests
            }
        }
    }

    fun generateNewPairingCode() {
        viewModelScope.launch {
            _generatedCode.value = UiState.loading()
            val localId = appPreferences.deviceId.firstOrNull() ?: ""
            when (val result = pairingRepository.generatePairingCode(localId)) {
                is Result.Success -> {
                    _generatedCode.value = UiState.success(result.data)
                    _qrData.value = "ritiksc://pair?code=${result.data.code}&uid=${result.data.ownerUid}"
                    startExpirationTimer(result.data.expiresAt)
                }
                is Result.Error -> {
                    _generatedCode.value = UiState.error(result.message)
                }
                is Result.Loading -> {}
            }
        }
    }

    private fun startExpirationTimer(expiresAt: Long) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                val diff = expiresAt - System.currentTimeMillis()
                if (diff <= 0) {
                    _secondsRemaining.value = 0L
                    _generatedCode.value = UiState.error("Pairing code expired. Please generate a new one.")
                    break
                }
                _secondsRemaining.value = diff / 1000
                delay(1000)
            }
        }
    }

    fun pairWithCode(inputCode: String) {
        val trimmed = inputCode.trim().uppercase()
        if (trimmed.length != 8) {
            _pairResult.value = UiState.error("Pairing code must be 8 characters")
            return
        }

        viewModelScope.launch {
            _pairResult.value = UiState.loading()
            val localId = appPreferences.deviceId.firstOrNull() ?: ""
            val localName = DeviceUtils.getDeviceName()

            when (val result = pairingRepository.validateAndUsePairingCode(trimmed, localId, localName)) {
                is Result.Success -> {
                    _pairResult.value = UiState.success(result.data)
                }
                is Result.Error -> {
                    _pairResult.value = UiState.error(result.message)
                }
                is Result.Loading -> {}
            }
        }
    }

    fun pairWithQrContent(qrRawContent: String) {
        val parsed = pairingRepository.parsePairingQrData(qrRawContent)
        if (parsed == null) {
            _pairResult.value = UiState.error("Invalid QR code format for RITIK SC")
            return
        }
        val (code, _) = parsed
        pairWithCode(code)
    }

    fun clearPairResult() {
        _pairResult.value = UiState()
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
