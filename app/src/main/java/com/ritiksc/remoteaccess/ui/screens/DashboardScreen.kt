package com.ritiksc.remoteaccess.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ritiksc.remoteaccess.common.TimeUtils
import com.ritiksc.remoteaccess.devices.DeviceViewModel
import com.ritiksc.remoteaccess.ui.components.*
import com.ritiksc.remoteaccess.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    deviceViewModel: DeviceViewModel,
    userName: String?,
    onNavigateAddDevice: () -> Unit,
    onNavigateAuthorizedDevices: () -> Unit,
    onNavigateDeviceDetails: (deviceId: String) -> Unit,
    onNavigateHistory: () -> Unit,
    onNavigatePermissions: () -> Unit,
    onNavigateSettings: () -> Unit,
    onNavigateHelp: () -> Unit,
    onStartDirectSharing: () -> Unit
) {
    val pairedDevicesState by deviceViewModel.pairedDevices.collectAsState()

    Scaffold(
        containerColor = RitikBackground,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(RitikBlueGlow),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Security,
                                contentDescription = null,
                                tint = RitikBlueLight,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "RITIK SC",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = RitikTextPrimary
                            )
                            Text(
                                text = "Hello, ${userName ?: "User"}",
                                fontSize = 11.sp,
                                color = RitikTextTertiary
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateHelp) {
                        Icon(Icons.Default.HelpOutline, contentDescription = "Help", tint = RitikTextSecondary)
                    }
                    IconButton(onClick = onNavigateSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = RitikTextSecondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = RitikBackground
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Quick Status Card
            item {
                RitikCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PulsingDot(color = RitikSuccess)
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "System Ready",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = RitikTextPrimary
                            )
                            Text(
                                text = "WebRTC & Firestore signaling active",
                                fontSize = 12.sp,
                                color = RitikTextTertiary
                            )
                        }
                        StatusBadge(text = "Online", color = RitikSuccess)
                    }
                }
            }

            // Quick Actions Grid
            item {
                SectionHeader(title = "QUICK ACTIONS")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionCard(
                        icon = Icons.Default.AddLink,
                        title = "Pair Device",
                        subtitle = "Scan QR / Code",
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateAddDevice
                    )
                    QuickActionCard(
                        icon = Icons.Default.ScreenShare,
                        title = "Share Screen",
                        subtitle = "Grant Access",
                        modifier = Modifier.weight(1f),
                        onClick = onStartDirectSharing
                    )
                }
            }

            // Secondary Quick Actions
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionCard(
                        icon = Icons.Default.History,
                        title = "History",
                        subtitle = "Past sessions",
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateHistory
                    )
                    QuickActionCard(
                        icon = Icons.Default.AdminPanelSettings,
                        title = "Permissions",
                        subtitle = "Audit access",
                        modifier = Modifier.weight(1f),
                        onClick = onNavigatePermissions
                    )
                }
            }

            // Paired Devices Section
            item {
                SectionHeader(
                    title = "AUTHORIZED DEVICES",
                    action = "View All",
                    onAction = onNavigateAuthorizedDevices
                )
            }

            when {
                pairedDevicesState.isLoading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = RitikBlue, modifier = Modifier.size(32.dp))
                        }
                    }
                }
                pairedDevicesState.isEmpty -> {
                    item {
                        EmptyState(
                            icon = Icons.Default.DevicesOther,
                            title = "No Devices Paired",
                            subtitle = "Add a controller or remote device to begin screen sharing and remote support.",
                            actionText = "Add Device",
                            onAction = onNavigateAddDevice
                        )
                    }
                }
                pairedDevicesState.data != null -> {
                    val devices = pairedDevicesState.data!!
                    items(devices.take(4)) { device ->
                        DeviceListItem(
                            name = device.remoteDeviceName.ifEmpty { "Android Device" },
                            status = "Authorized",
                            statusColor = RitikBlueLight,
                            lastSeen = "Paired ${TimeUtils.formatRelativeTime(device.pairedAt)}",
                            onClick = { onNavigateDeviceDetails(device.remoteDeviceId) }
                        )
                    }
                }
            }

            item {
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    RitikCard(
        modifier = modifier,
        onClick = onClick
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(RitikBlueGlow),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = RitikBlueLight,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = RitikTextPrimary
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = subtitle,
            fontSize = 11.sp,
            color = RitikTextTertiary
        )
    }
}
