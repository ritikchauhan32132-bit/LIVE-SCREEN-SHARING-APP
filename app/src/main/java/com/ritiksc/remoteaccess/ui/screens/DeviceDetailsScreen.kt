package com.ritiksc.remoteaccess.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ritiksc.remoteaccess.common.TimeUtils
import com.ritiksc.remoteaccess.devices.DeviceViewModel
import com.ritiksc.remoteaccess.ui.components.*
import com.ritiksc.remoteaccess.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceDetailsScreen(
    deviceId: String,
    deviceViewModel: DeviceViewModel,
    onNavigateBack: () -> Unit,
    onStartConnection: (targetDeviceId: String) -> Unit
) {
    val selectedDevice by deviceViewModel.selectedDevice.collectAsState()
    var showRevokeDialog by remember { mutableStateOf(false) }

    LaunchedEffect(deviceId) {
        deviceViewModel.loadDeviceInfo(deviceId)
    }

    Scaffold(
        containerColor = RitikBackground,
        topBar = {
            TopAppBar(
                title = { Text("Device Details", fontWeight = FontWeight.SemiBold, fontSize = 18.sp) },
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
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(RitikBlueGlow),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.PhoneAndroid,
                            contentDescription = null,
                            tint = RitikBlueLight,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = selectedDevice?.deviceName ?: "Remote Device",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = RitikTextPrimary
                    )

                    Spacer(Modifier.height(4.dp))

                    StatusBadge(
                        text = if (selectedDevice?.isOnline == true) "Online" else "Offline",
                        color = if (selectedDevice?.isOnline == true) RitikSuccess else RitikTextTertiary
                    )
                }
            }

            SectionHeader(title = "DEVICE INFORMATION")

            RitikCard {
                DetailRow(label = "Model", value = selectedDevice?.model ?: "Unknown")
                Divider(color = RitikBorderSubtle, modifier = Modifier.padding(vertical = 8.dp))
                DetailRow(label = "Manufacturer", value = selectedDevice?.manufacturer ?: "Unknown")
                Divider(color = RitikBorderSubtle, modifier = Modifier.padding(vertical = 8.dp))
                DetailRow(label = "Operating System", value = selectedDevice?.androidVersion ?: "Android")
                Divider(color = RitikBorderSubtle, modifier = Modifier.padding(vertical = 8.dp))
                DetailRow(label = "Device ID", value = deviceId.take(16) + "…")
                Divider(color = RitikBorderSubtle, modifier = Modifier.padding(vertical = 8.dp))
                DetailRow(
                    label = "Last Seen",
                    value = selectedDevice?.lastSeenAt?.let { TimeUtils.formatRelativeTime(it) } ?: "Recently"
                )
            }

            SectionHeader(title = "ACTIONS")

            RitikGradientButton(
                text = "Connect & View Screen",
                onClick = { onStartConnection(deviceId) }
            )

            RitikOutlinedButton(
                text = "Revoke & Remove Device",
                icon = Icons.Default.Delete,
                onClick = { showRevokeDialog = true }
            )
        }
    }

    if (showRevokeDialog) {
        AlertDialog(
            onDismissRequest = { showRevokeDialog = false },
            title = { Text("Revoke Device Authorization?", color = RitikTextPrimary) },
            text = {
                Text(
                    "This will remove the device from your authorized list and terminate any active sessions. To reconnect later, you must pair again.",
                    color = RitikTextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRevokeDialog = false
                        // Use pairing id format or remove
                        deviceViewModel.removePairedDevice(deviceId) {
                            onNavigateBack()
                        }
                    }
                ) {
                    Text("Revoke", color = RitikError, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRevokeDialog = false }) {
                    Text("Cancel", color = RitikTextSecondary)
                }
            },
            containerColor = RitikSurfaceVariant
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 13.sp, color = RitikTextSecondary)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = RitikTextPrimary)
    }
}
