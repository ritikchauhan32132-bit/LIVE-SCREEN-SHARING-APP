package com.ritiksc.remoteaccess.ui.screens

import android.app.Activity
import android.content.Context
import android.media.projection.MediaProjectionManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ScreenShare
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ritiksc.remoteaccess.connection.ConnectionViewModel
import com.ritiksc.remoteaccess.ui.components.RitikCard
import com.ritiksc.remoteaccess.ui.components.RitikGradientButton
import com.ritiksc.remoteaccess.ui.components.RitikOutlinedButton
import com.ritiksc.remoteaccess.ui.theme.*

@Composable
fun ConnectionRequestScreen(
    connectionViewModel: ConnectionViewModel,
    onNavigateBack: () -> Unit,
    onSessionStarted: () -> Unit
) {
    val incomingRequest by connectionViewModel.incomingRequest.collectAsState()
    val context = LocalContext.current

    val mediaProjectionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            connectionViewModel.approveConnection(result.data!!)
            onSessionStarted()
        } else {
            connectionViewModel.rejectConnection()
            onNavigateBack()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RitikBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(RitikBlueGlow),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.ScreenShare,
                    contentDescription = null,
                    tint = RitikBlueLight,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Screen Sharing Request",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = RitikTextPrimary
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "A controller device is requesting to view your screen in real time.",
                fontSize = 14.sp,
                color = RitikTextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            RitikCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Shield,
                        contentDescription = null,
                        tint = RitikWarning,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "Explicit Consent Required",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = RitikTextPrimary
                    )
                }

                Spacer(Modifier.height(10.dp))

                Text(
                    text = "If accepted, Android will ask for permission to capture your screen. All content displayed on your phone (notifications, apps, text) will be visible to the controller until you stop sharing.",
                    fontSize = 13.sp,
                    color = RitikTextSecondary,
                    lineHeight = 18.sp
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "• You can tap 'Stop Sharing' in the notification bar anytime.\n• Banking apps and protected screens are protected by Android.",
                    fontSize = 12.sp,
                    color = RitikTextTertiary,
                    lineHeight = 16.sp
                )
            }

            Spacer(Modifier.height(32.dp))

            RitikGradientButton(
                text = "Grant Consent & Start Sharing",
                onClick = {
                    val mediaProjectionManager =
                        context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                    mediaProjectionLauncher.launch(mediaProjectionManager.createScreenCaptureIntent())
                }
            )

            Spacer(Modifier.height(12.dp))

            RitikOutlinedButton(
                text = "Decline",
                icon = Icons.Default.Close,
                onClick = {
                    connectionViewModel.rejectConnection()
                    onNavigateBack()
                }
            )
        }
    }
}
