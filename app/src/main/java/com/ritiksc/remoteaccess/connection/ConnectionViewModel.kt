package com.ritiksc.remoteaccess.connection

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ritiksc.remoteaccess.common.DeviceUtils
import com.ritiksc.remoteaccess.common.Result
import com.ritiksc.remoteaccess.common.UiState
import com.ritiksc.remoteaccess.common.model.ConnectionSession
import com.ritiksc.remoteaccess.common.model.SessionStatus
import com.ritiksc.remoteaccess.settings.AppPreferences
import com.ritiksc.remoteaccess.streaming.ScreenCaptureService
import com.ritiksc.remoteaccess.streaming.WebRtcManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.webrtc.VideoTrack

class ConnectionViewModel(
    application: Application,
    private val signalingRepository: SignalingRepository = SignalingRepository(),
    private val appPreferences: AppPreferences = AppPreferences(application)
) : AndroidViewModel(application) {

    private val webRtcManager = WebRtcManager(application, signalingRepository)

    private val _currentSession = MutableStateFlow<ConnectionSession?>(null)
    val currentSession: StateFlow<ConnectionSession?> = _currentSession.asStateFlow()

    private val _incomingRequest = MutableStateFlow<ConnectionSession?>(null)
    val incomingRequest: StateFlow<ConnectionSession?> = _incomingRequest.asStateFlow()

    private val _sessionState = MutableStateFlow<UiState<SessionStatus>>(UiState())
    val sessionState: StateFlow<UiState<SessionStatus>> = _sessionState.asStateFlow()

    val webRtcState: StateFlow<WebRtcManager.WebRtcConnectionState> = webRtcManager.connectionState
    val remoteVideoTrack: StateFlow<VideoTrack?> = webRtcManager.remoteVideoTrack

    private var sessionObserverJob: Job? = null
    private var incomingRequestJob: Job? = null

    init {
        webRtcManager.initialize()
        startListeningForIncomingRequests()
    }

    private fun startListeningForIncomingRequests() {
        incomingRequestJob = viewModelScope.launch {
            val localId = appPreferences.deviceId.firstOrNull() ?: return@launch
            signalingRepository.observeSessionRequests(localId).collect { session ->
                _incomingRequest.value = session
            }
        }
    }

    /**
     * Called by Controller device to request connection with a remote device.
     */
    fun requestConnection(targetRemoteDeviceId: String, isViewOnly: Boolean = true) {
        viewModelScope.launch {
            _sessionState.value = UiState.loading()
            val localId = appPreferences.deviceId.firstOrNull() ?: ""

            when (val result = signalingRepository.createSession(localId, targetRemoteDeviceId, isViewOnly)) {
                is Result.Success -> {
                    val session = result.data
                    _currentSession.value = session
                    _sessionState.value = UiState.success(session.status)
                    observeSessionStatus(session.sessionId)
                }
                is Result.Error -> {
                    _sessionState.value = UiState.error(result.message)
                }
                is Result.Loading -> {}
            }
        }
    }

    private fun observeSessionStatus(sessionId: String) {
        sessionObserverJob?.cancel()
        sessionObserverJob = viewModelScope.launch {
            signalingRepository.observeSession(sessionId).collect { session ->
                _currentSession.value = session
                if (session != null) {
                    _sessionState.value = UiState.success(session.status)
                    if (session.status == SessionStatus.APPROVED) {
                        // Controller initiates WebRTC connection
                        val localId = appPreferences.deviceId.firstOrNull() ?: ""
                        webRtcManager.startAsReceiver(
                            sessionId = session.sessionId,
                            localDeviceId = localId,
                            remoteDeviceId = session.remoteDeviceId
                        )
                        signalingRepository.updateSessionStatus(sessionId, SessionStatus.CONNECTED)
                    } else if (session.status == SessionStatus.DISCONNECTED || session.status == SessionStatus.FAILED) {
                        disconnect(session.endReason)
                    }
                }
            }
        }
    }

    /**
     * Called by Remote device to accept connection and start screen sharing.
     */
    fun approveConnection(mediaProjectionIntent: Intent) {
        val session = _incomingRequest.value ?: return
        viewModelScope.launch {
            val localId = appPreferences.deviceId.firstOrNull() ?: ""

            // 1. Start foreground service for screen capture
            val serviceIntent = Intent(getApplication(), ScreenCaptureService::class.java).apply {
                action = ScreenCaptureService.ACTION_START
                putExtra(ScreenCaptureService.EXTRA_SESSION_ID, session.sessionId)
            }
            getApplication<Application>().startService(serviceIntent)

            // 2. Start WebRTC as sender
            webRtcManager.startAsSender(
                sessionId = session.sessionId,
                localDeviceId = localId,
                remoteDeviceId = session.controllerDeviceId,
                mediaProjectionIntent = mediaProjectionIntent
            )

            // 3. Mark session approved
            signalingRepository.updateSessionStatus(session.sessionId, SessionStatus.APPROVED)
            _currentSession.value = session
            _incomingRequest.value = null
            observeSessionStatus(session.sessionId)
        }
    }

    fun rejectConnection() {
        val session = _incomingRequest.value ?: return
        viewModelScope.launch {
            signalingRepository.updateSessionStatus(session.sessionId, SessionStatus.REJECTED, "User rejected request")
            _incomingRequest.value = null
        }
    }

    fun disconnect(reason: String = "Session closed by user") {
        val session = _currentSession.value
        viewModelScope.launch {
            if (session != null) {
                signalingRepository.updateSessionStatus(session.sessionId, SessionStatus.DISCONNECTED, reason)
                signalingRepository.saveConnectionHistory(
                    session = session.copy(endedAt = System.currentTimeMillis(), endReason = reason),
                    controllerName = "Controller",
                    remoteName = DeviceUtils.getDeviceName()
                )
            }

            // Stop screen capture service
            val stopIntent = Intent(getApplication(), ScreenCaptureService::class.java).apply {
                action = ScreenCaptureService.ACTION_STOP
            }
            getApplication<Application>().startService(stopIntent)

            // Cleanup WebRTC
            webRtcManager.cleanup()

            _currentSession.value = null
            _sessionState.value = UiState.success(SessionStatus.DISCONNECTED)
        }
    }

    override fun onCleared() {
        super.onCleared()
        sessionObserverJob?.cancel()
        incomingRequestJob?.cancel()
        webRtcManager.release()
    }
}
