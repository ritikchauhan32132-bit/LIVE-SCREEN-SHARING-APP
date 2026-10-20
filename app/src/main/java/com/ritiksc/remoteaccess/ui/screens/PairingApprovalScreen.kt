package com.ritiksc.remoteaccess.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ritiksc.remoteaccess.ui.components.RitikCard
import com.ritiksc.remoteaccess.ui.components.RitikGradientButton
import com.ritiksc.remoteaccess.ui.components.RitikOutlinedButton
import com.ritiksc.remoteaccess.ui.theme.*

@Composable
fun PairingApprovalScreen(
    controllerDeviceName: String,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
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
                    Icons.Default.PhoneAndroid,
                    contentDescription = null,
                    tint = RitikBlueLight,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Device Pairing Request",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = RitikTextPrimary
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "A controller device is requesting authorization to pair with this phone.",
                fontSize = 14.sp,
                color = RitikTextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))

            RitikCard {
                Text(
                    text = "CONTROLLER DEVICE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = RitikTextTertiary,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = controllerDeviceName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = RitikTextPrimary
                )
                Spacer(Modifier.height(12.dp))
                Divider(color = RitikBorderSubtle)
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Authorizing will allow this controller to send remote-support screen sharing requests. You will still need to explicitly approve each screen sharing session.",
                    fontSize = 12.sp,
                    color = RitikTextSecondary,
                    lineHeight = 16.sp
                )
            }

            Spacer(Modifier.height(32.dp))

            RitikGradientButton(
                text = "Approve & Pair",
                onClick = onApprove
            )

            Spacer(Modifier.height(12.dp))

            RitikOutlinedButton(
                text = "Reject Request",
                icon = Icons.Default.Close,
                onClick = onReject
            )
        }
    }
}
