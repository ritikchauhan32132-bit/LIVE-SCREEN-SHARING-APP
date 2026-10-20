package com.ritiksc.remoteaccess.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ritiksc.remoteaccess.R
import com.ritiksc.remoteaccess.ui.components.RitikGradientButton
import com.ritiksc.remoteaccess.ui.components.RitikOutlinedButton
import com.ritiksc.remoteaccess.ui.theme.*

@Composable
fun WelcomeScreen(
    onNavigateLogin: () -> Unit,
    onNavigateRegister: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RitikBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 40.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(RitikBlue.copy(alpha = 0.4f), Color.Transparent)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = "RITIK SC Logo",
                        modifier = Modifier.size(70.dp)
                    )
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "RITIK SC",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = RitikTextPrimary,
                    letterSpacing = 1.sp
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = "Professional Android Remote Support & Screen Sharing",
                    fontSize = 14.sp,
                    color = RitikTextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(36.dp))

                FeatureItem(
                    icon = Icons.Default.Visibility,
                    title = "Live Screen Streaming",
                    description = "Ultra-low latency screen sharing powered by genuine WebRTC technology."
                )

                Spacer(Modifier.height(16.dp))

                FeatureItem(
                    icon = Icons.Default.Lock,
                    title = "End-to-End Security",
                    description = "Short-lived pairing codes, cryptographic tokens, and explicit user consent."
                )

                Spacer(Modifier.height(16.dp))

                FeatureItem(
                    icon = Icons.Default.Speed,
                    title = "Cross-Network Connectivity",
                    description = "STUN & TURN relay support allows secure connection across mobile data and Wi-Fi."
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                RitikGradientButton(
                    text = "Log In",
                    onClick = onNavigateLogin
                )

                Spacer(Modifier.height(12.dp))

                RitikOutlinedButton(
                    text = "Create Account",
                    onClick = onNavigateRegister
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "By continuing, you agree to RITIK SC Terms of Service and Privacy Policy.",
                    fontSize = 11.sp,
                    color = RitikTextTertiary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun FeatureItem(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(RitikSurfaceVariant)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(RitikBlueGlow),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = RitikBlueLight,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = RitikTextPrimary
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = description,
                fontSize = 12.sp,
                color = RitikTextSecondary,
                lineHeight = 16.sp
            )
        }
    }
}
