package com.ritiksc.remoteaccess.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ritiksc.remoteaccess.ui.components.RitikCard
import com.ritiksc.remoteaccess.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDeviceScreen(
    onNavigateGenerateCode: () -> Unit,
    onNavigateEnterCode: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        containerColor = RitikBackground,
        topBar = {
            TopAppBar(
                title = { Text("Add Device", fontWeight = FontWeight.SemiBold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = RitikTextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = RitikBackground)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Choose Pairing Method",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = RitikTextPrimary
            )

            Text(
                text = "Pair this device securely with another phone to enable remote support sessions.",
                fontSize = 14.sp,
                color = RitikTextSecondary
            )

            Spacer(Modifier.height(8.dp))

            PairingOptionCard(
                icon = Icons.Default.QrCode2,
                title = "Display Pairing Code & QR",
                description = "Choose this if THIS device will be viewed or supported remotely. Generates a secure 5-minute one-time code.",
                badgeText = "Remote Device",
                onClick = onNavigateGenerateCode
            )

            PairingOptionCard(
                icon = Icons.Default.QrCodeScanner,
                title = "Enter Code or Scan QR",
                description = "Choose this if THIS device will be the CONTROLLER. Scans the other phone's QR code or accepts the 8-character code.",
                badgeText = "Controller Device",
                onClick = onNavigateEnterCode
            )

            Spacer(Modifier.weight(1f))

            RitikCard {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Shield,
                        contentDescription = null,
                        tint = RitikBlueLight,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "Pairing requires explicit verification and both devices must belong to your authorized account.",
                        fontSize = 12.sp,
                        color = RitikTextTertiary,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun PairingOptionCard(
    icon: ImageVector,
    title: String,
    description: String,
    badgeText: String,
    onClick: () -> Unit
) {
    RitikCard(onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(RitikBlueGlow),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = RitikBlueLight, modifier = Modifier.size(24.dp))
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = RitikTextPrimary
                    )
                }

                Spacer(Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(RitikSurfaceElevated)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badgeText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = RitikBlueLight
                    )
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = description,
                    fontSize = 13.sp,
                    color = RitikTextSecondary,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
