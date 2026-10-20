package com.ritiksc.remoteaccess.ui.screens

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.ritiksc.remoteaccess.common.TimeUtils
import com.ritiksc.remoteaccess.connection.ConnectionViewModel
import com.ritiksc.remoteaccess.streaming.WebRtcManager
import com.ritiksc.remoteaccess.ui.components.PulsingDot
import com.ritiksc.remoteaccess.ui.components.StatusBadge
import com.ritiksc.remoteaccess.ui.theme.*
import kotlinx.coroutines.delay
import org.webrtc.EglBase
import org.webrtc.RendererCommon
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoTrack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveScreenViewerScreen(
    connectionViewModel: ConnectionViewModel,
    onEndSession: () -> Unit
) {
    val context = LocalContext.current
    val currentSession by connectionViewModel.currentSession.collectAsState()
    val rtcState by connectionViewModel.webRtcState.collectAsState()
    val remoteVideoTrack by connectionViewModel.remoteVideoTrack.collectAsState()

    var showControlsSheet by remember { mutableStateOf(false) }
    var sessionDurationSeconds by remember { mutableLongStateOf(0L) }

    LaunchedEffect(currentSession) {
        if (currentSession != null) {
            val startTime = System.currentTimeMillis()
            while (true) {
                sessionDurationSeconds = (System.currentTimeMillis() - startTime) / 1000
                delay(1000)
            }
        }
    }

    Scaffold(
        containerColor = Color.Black
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // WebRTC Video Surface Renderer
            if (remoteVideoTrack != null) {
                WebRtcVideoView(
                    videoTrack = remoteVideoTrack,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Connecting / Loading placeholder
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(RitikBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = RitikBlue, strokeWidth = 3.dp)
                        Spacer(Modifier.height(20.dp))
                        Text(
                            text = when (rtcState) {
                                WebRtcManager.WebRtcConnectionState.CONNECTING -> "Negotiating WebRTC stream…"
                                WebRtcManager.WebRtcConnectionState.RECONNECTING -> "Reconnecting remote stream…"
                                else -> "Waiting for remote screen frames…"
                            },
                            color = RitikTextSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Top Status Overlay Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(RitikSurface.copy(alpha = 0.85f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PulsingDot(
                            color = if (rtcState == WebRtcManager.WebRtcConnectionState.CONNECTED) RitikSuccess else RitikWarning
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = TimeUtils.formatDuration(sessionDurationSeconds * 1000),
                            color = RitikTextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                StatusBadge(
                    text = when (rtcState) {
                        WebRtcManager.WebRtcConnectionState.CONNECTED -> "Live Stream"
                        WebRtcManager.WebRtcConnectionState.CONNECTING -> "Connecting"
                        WebRtcManager.WebRtcConnectionState.RECONNECTING -> "Reconnecting"
                        else -> "Initializing"
                    },
                    color = when (rtcState) {
                        WebRtcManager.WebRtcConnectionState.CONNECTED -> RitikSuccess
                        WebRtcManager.WebRtcConnectionState.RECONNECTING -> RitikWarning
                        else -> RitikBlueLight
                    }
                )
            }

            // Bottom Floating Action Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .align(Alignment.BottomCenter),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Info / Diagnostics button
                IconButton(
                    onClick = { showControlsSheet = true },
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(RitikSurfaceVariant.copy(alpha = 0.9f))
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = "Diagnostics",
                        tint = RitikTextPrimary
                    )
                }

                Spacer(Modifier.width(20.dp))

                // End Session button
                Button(
                    onClick = {
                        connectionViewModel.disconnect("Ended by controller")
                        onEndSession()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RitikError),
                    shape = RoundedCornerShape(26.dp),
                    modifier = Modifier.height(52.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp)
                ) {
                    Icon(Icons.Default.CallEnd, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "End Session",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }

    if (showControlsSheet) {
        SessionControlsSheet(
            sessionDuration = TimeUtils.formatDuration(sessionDurationSeconds * 1000),
            rtcState = rtcState.name,
            onDismiss = { showControlsSheet = false }
        )
    }
}

@Composable
fun WebRtcVideoView(
    videoTrack: VideoTrack?,
    modifier: Modifier = Modifier
) {
    val eglBase = remember { EglBase.create() }

    AndroidView(
        factory = { ctx ->
            SurfaceViewRenderer(ctx).apply {
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                init(eglBase.eglBaseContext, null)
                setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT)
                setEnableHardwareScaler(true)
            }
        },
        update = { renderer ->
            videoTrack?.addSink(renderer)
        },
        onRelease = { renderer ->
            videoTrack?.removeSink(renderer)
            renderer.release()
            eglBase.release()
        },
        modifier = modifier
    )
}
