package com.ritiksc.remoteaccess.streaming

import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjection
import android.util.Log
import com.ritiksc.remoteaccess.common.Constants
import com.ritiksc.remoteaccess.common.model.SignalingMessage
import com.ritiksc.remoteaccess.common.model.SignalingType
import com.ritiksc.remoteaccess.connection.SignalingRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.webrtc.*

/**
 * Manages WebRTC peer connections for screen sharing.
 *
 * This handles both the sender (remote device sharing its screen) and
 * receiver (controller device viewing the stream) roles.
 */
class WebRtcManager(
    private val context: Context,
    private val signalingRepository: SignalingRepository = SignalingRepository()
) {
    companion object {
        private const val TAG = "WebRtcManager"
    }

    private var peerConnectionFactory: PeerConnectionFactory? = null
    private var peerConnection: PeerConnection? = null
    private var videoCapturer: ScreenCapturerAndroid? = null
    private var localVideoSource: VideoSource? = null
    private var localVideoTrack: VideoTrack? = null
    private var surfaceTextureHelper: SurfaceTextureHelper? = null

    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var signalingJob: Job? = null

    private val _connectionState = MutableStateFlow(WebRtcConnectionState.IDLE)
    val connectionState: StateFlow<WebRtcConnectionState> = _connectionState.asStateFlow()

    private val _remoteVideoTrack = MutableStateFlow<VideoTrack?>(null)
    val remoteVideoTrack: StateFlow<VideoTrack?> = _remoteVideoTrack.asStateFlow()

    private var sessionId: String = ""
    private var localDeviceId: String = ""
    private var remoteDeviceId: String = ""
    private var isInitiator = false
    private var pendingIceCandidates = mutableListOf<IceCandidate>()

    enum class WebRtcConnectionState {
        IDLE,
        INITIALIZING,
        CONNECTING,
        CONNECTED,
        RECONNECTING,
        FAILED,
        CLOSED
    }

    fun initialize() {
        val options = PeerConnectionFactory.InitializationOptions.builder(context)
            .setEnableInternalTracer(false)
            .createInitializationOptions()
        PeerConnectionFactory.initialize(options)

        val encoderFactory = DefaultVideoEncoderFactory(
            EglBase.create().eglBaseContext,
            true,
            true
        )
        val decoderFactory = DefaultVideoDecoderFactory(
            EglBase.create().eglBaseContext
        )

        peerConnectionFactory = PeerConnectionFactory.builder()
            .setVideoEncoderFactory(encoderFactory)
            .setVideoDecoderFactory(decoderFactory)
            .createPeerConnectionFactory()

        Log.d(TAG, "WebRTC initialized")
    }

    /**
     * Start as sender (remote device sharing screen).
     * Requires a MediaProjection result intent from the consent flow.
     */
    fun startAsSender(
        sessionId: String,
        localDeviceId: String,
        remoteDeviceId: String,
        mediaProjectionIntent: Intent
    ) {
        this.sessionId = sessionId
        this.localDeviceId = localDeviceId
        this.remoteDeviceId = remoteDeviceId
        this.isInitiator = false

        _connectionState.value = WebRtcConnectionState.INITIALIZING

        createPeerConnection()
        startScreenCapture(mediaProjectionIntent)
        listenForSignaling()
    }

    /**
     * Start as receiver (controller device viewing the stream).
     */
    fun startAsReceiver(
        sessionId: String,
        localDeviceId: String,
        remoteDeviceId: String
    ) {
        this.sessionId = sessionId
        this.localDeviceId = localDeviceId
        this.remoteDeviceId = remoteDeviceId
        this.isInitiator = true

        _connectionState.value = WebRtcConnectionState.INITIALIZING

        createPeerConnection()
        listenForSignaling()
        createOffer()
    }

    private fun createPeerConnection() {
        val iceServers = Constants.DEFAULT_ICE_SERVERS.map { url ->
            PeerConnection.IceServer.builder(url).createIceServer()
        }

        // NOTE: For production, add TURN servers here with authenticated credentials.
        // TURN servers are required for reliable connectivity across NATs and firewalls.
        // Example:
        // PeerConnection.IceServer.builder("turn:your-turn-server.com:3478")
        //     .setUsername("short-lived-username")
        //     .setPassword("short-lived-credential")
        //     .createIceServer()

        val rtcConfig = PeerConnection.RTCConfiguration(iceServers).apply {
            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
            continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
            iceTransportsType = PeerConnection.IceTransportsType.ALL
        }

        peerConnection = peerConnectionFactory?.createPeerConnection(
            rtcConfig,
            object : PeerConnection.Observer {
                override fun onIceCandidate(candidate: IceCandidate) {
                    Log.d(TAG, "ICE candidate: ${candidate.sdpMid}")
                    sendIceCandidate(candidate)
                }

                override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>?) {
                    Log.d(TAG, "ICE candidates removed")
                }

                override fun onAddStream(stream: MediaStream?) {
                    Log.d(TAG, "Stream added: ${stream?.videoTracks?.size} video tracks")
                }

                override fun onTrack(transceiver: RtpTransceiver?) {
                    val track = transceiver?.receiver?.track()
                    if (track is VideoTrack) {
                        Log.d(TAG, "Remote video track received")
                        _remoteVideoTrack.value = track
                    }
                }

                override fun onSignalingChange(state: PeerConnection.SignalingState?) {
                    Log.d(TAG, "Signaling state: $state")
                }

                override fun onIceConnectionChange(state: PeerConnection.IceConnectionState?) {
                    Log.d(TAG, "ICE connection state: $state")
                    when (state) {
                        PeerConnection.IceConnectionState.CONNECTED,
                        PeerConnection.IceConnectionState.COMPLETED -> {
                            _connectionState.value = WebRtcConnectionState.CONNECTED
                        }
                        PeerConnection.IceConnectionState.DISCONNECTED -> {
                            _connectionState.value = WebRtcConnectionState.RECONNECTING
                        }
                        PeerConnection.IceConnectionState.FAILED -> {
                            _connectionState.value = WebRtcConnectionState.FAILED
                        }
                        PeerConnection.IceConnectionState.CLOSED -> {
                            _connectionState.value = WebRtcConnectionState.CLOSED
                        }
                        else -> {}
                    }
                }

                override fun onIceConnectionReceivingChange(receiving: Boolean) {}
                override fun onIceGatheringChange(state: PeerConnection.IceGatheringState?) {
                    Log.d(TAG, "ICE gathering state: $state")
                }
                override fun onRemoveStream(stream: MediaStream?) {}
                override fun onDataChannel(channel: DataChannel?) {}
                override fun onRenegotiationNeeded() {
                    Log.d(TAG, "Renegotiation needed")
                }
                override fun onAddTrack(receiver: RtpReceiver?, streams: Array<out MediaStream>?) {}
            }
        )

        _connectionState.value = WebRtcConnectionState.CONNECTING
    }

    private fun startScreenCapture(mediaProjectionIntent: Intent) {
        val eglBase = EglBase.create()
        surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", eglBase.eglBaseContext)

        localVideoSource = peerConnectionFactory?.createVideoSource(true)

        videoCapturer = ScreenCapturerAndroid(
            mediaProjectionIntent,
            object : MediaProjection.Callback() {
                override fun onStop() {
                    Log.d(TAG, "MediaProjection stopped")
                    stopCapture()
                }
            }
        )

        videoCapturer?.initialize(surfaceTextureHelper, context, localVideoSource?.capturerObserver)
        videoCapturer?.startCapture(Constants.VIDEO_WIDTH, Constants.VIDEO_HEIGHT, Constants.VIDEO_FPS)

        localVideoTrack = peerConnectionFactory?.createVideoTrack("screen_track", localVideoSource)
        localVideoTrack?.setEnabled(true)

        peerConnection?.addTrack(localVideoTrack, listOf("screen_stream"))
        Log.d(TAG, "Screen capture started")
    }

    private fun createOffer() {
        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "false"))
        }

        peerConnection?.createOffer(object : SdpObserver {
            override fun onCreateSuccess(sdp: SessionDescription) {
                peerConnection?.setLocalDescription(object : SdpObserver {
                    override fun onSetSuccess() {
                        sendSdp(sdp, SignalingType.OFFER)
                    }
                    override fun onSetFailure(error: String?) {
                        Log.e(TAG, "Set local SDP failed: $error")
                    }
                    override fun onCreateSuccess(sdp: SessionDescription?) {}
                    override fun onCreateFailure(error: String?) {}
                }, sdp)
            }
            override fun onCreateFailure(error: String?) {
                Log.e(TAG, "Create offer failed: $error")
                _connectionState.value = WebRtcConnectionState.FAILED
            }
            override fun onSetSuccess() {}
            override fun onSetFailure(error: String?) {}
        }, constraints)
    }

    private fun handleOffer(sdpString: String) {
        val sdp = SessionDescription(SessionDescription.Type.OFFER, sdpString)
        peerConnection?.setRemoteDescription(object : SdpObserver {
            override fun onSetSuccess() {
                // Add any pending ICE candidates
                pendingIceCandidates.forEach { peerConnection?.addIceCandidate(it) }
                pendingIceCandidates.clear()
                createAnswer()
            }
            override fun onSetFailure(error: String?) {
                Log.e(TAG, "Set remote SDP failed: $error")
            }
            override fun onCreateSuccess(sdp: SessionDescription?) {}
            override fun onCreateFailure(error: String?) {}
        }, sdp)
    }

    private fun createAnswer() {
        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "false"))
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "false"))
        }

        peerConnection?.createAnswer(object : SdpObserver {
            override fun onCreateSuccess(sdp: SessionDescription) {
                peerConnection?.setLocalDescription(object : SdpObserver {
                    override fun onSetSuccess() {
                        sendSdp(sdp, SignalingType.ANSWER)
                    }
                    override fun onSetFailure(error: String?) {
                        Log.e(TAG, "Set local SDP failed: $error")
                    }
                    override fun onCreateSuccess(sdp: SessionDescription?) {}
                    override fun onCreateFailure(error: String?) {}
                }, sdp)
            }
            override fun onCreateFailure(error: String?) {
                Log.e(TAG, "Create answer failed: $error")
            }
            override fun onSetSuccess() {}
            override fun onSetFailure(error: String?) {}
        }, constraints)
    }

    private fun handleAnswer(sdpString: String) {
        val sdp = SessionDescription(SessionDescription.Type.ANSWER, sdpString)
        peerConnection?.setRemoteDescription(object : SdpObserver {
            override fun onSetSuccess() {
                // Add any pending ICE candidates
                pendingIceCandidates.forEach { peerConnection?.addIceCandidate(it) }
                pendingIceCandidates.clear()
                Log.d(TAG, "Remote description set successfully")
            }
            override fun onSetFailure(error: String?) {
                Log.e(TAG, "Set remote SDP failed: $error")
            }
            override fun onCreateSuccess(sdp: SessionDescription?) {}
            override fun onCreateFailure(error: String?) {}
        }, sdp)
    }

    private fun handleIceCandidate(candidateJson: String) {
        try {
            // Parse: sdpMid|sdpMLineIndex|sdp
            val parts = candidateJson.split("|")
            if (parts.size == 3) {
                val candidate = IceCandidate(parts[0], parts[1].toInt(), parts[2])
                if (peerConnection?.remoteDescription != null) {
                    peerConnection?.addIceCandidate(candidate)
                } else {
                    pendingIceCandidates.add(candidate)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to handle ICE candidate: ${e.message}")
        }
    }

    private fun sendSdp(sdp: SessionDescription, type: SignalingType) {
        coroutineScope.launch {
            val message = SignalingMessage(
                type = type,
                sessionId = sessionId,
                senderDeviceId = localDeviceId,
                targetDeviceId = remoteDeviceId,
                payload = sdp.description,
                timestamp = System.currentTimeMillis()
            )
            signalingRepository.sendSignalingMessage(message)
        }
    }

    private fun sendIceCandidate(candidate: IceCandidate) {
        coroutineScope.launch {
            val payload = "${candidate.sdpMid}|${candidate.sdpMLineIndex}|${candidate.sdp}"
            val message = SignalingMessage(
                type = SignalingType.ICE_CANDIDATE,
                sessionId = sessionId,
                senderDeviceId = localDeviceId,
                targetDeviceId = remoteDeviceId,
                payload = payload,
                timestamp = System.currentTimeMillis()
            )
            signalingRepository.sendSignalingMessage(message)
        }
    }

    private fun listenForSignaling() {
        signalingJob = coroutineScope.launch {
            signalingRepository.observeSignalingMessages(sessionId, localDeviceId).collect { message ->
                when (message.type) {
                    SignalingType.OFFER -> handleOffer(message.payload)
                    SignalingType.ANSWER -> handleAnswer(message.payload)
                    SignalingType.ICE_CANDIDATE -> handleIceCandidate(message.payload)
                    SignalingType.SESSION_END -> {
                        Log.d(TAG, "Session ended by remote")
                        cleanup()
                    }
                    else -> {}
                }
            }
        }
    }

    fun stopCapture() {
        try {
            videoCapturer?.stopCapture()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping capture: ${e.message}")
        }
    }

    fun cleanup() {
        Log.d(TAG, "Cleaning up WebRTC resources")

        signalingJob?.cancel()
        signalingJob = null

        try {
            videoCapturer?.stopCapture()
            videoCapturer?.dispose()
        } catch (e: Exception) {
            Log.e(TAG, "Error disposing capturer: ${e.message}")
        }
        videoCapturer = null

        localVideoTrack?.dispose()
        localVideoTrack = null
        localVideoSource?.dispose()
        localVideoSource = null
        surfaceTextureHelper?.dispose()
        surfaceTextureHelper = null

        _remoteVideoTrack.value = null

        peerConnection?.close()
        peerConnection?.dispose()
        peerConnection = null

        _connectionState.value = WebRtcConnectionState.CLOSED

        // Cleanup signaling messages
        if (sessionId.isNotEmpty()) {
            coroutineScope.launch {
                signalingRepository.cleanupSignaling(sessionId)
            }
        }
    }

    fun release() {
        cleanup()
        peerConnectionFactory?.dispose()
        peerConnectionFactory = null
        coroutineScope.cancel()
    }
}
