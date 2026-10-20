package com.ritiksc.remoteaccess.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ritiksc.remoteaccess.common.DeviceUtils
import com.ritiksc.remoteaccess.common.TimeUtils
import com.ritiksc.remoteaccess.devices.DeviceViewModel
import com.ritiksc.remoteaccess.ui.components.*
import com.ritiksc.remoteaccess.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceManagementScreen(
    deviceViewModel: DeviceViewModel,
    onNavigateBack: () -> Unit,
    onNavigateAddDevice: () -> Unit,
    onNavigateDeviceDetails: (deviceId: String) -> Unit
) {
    val pairedDevicesState by deviceViewModel.pairedDevices.collectAsState()
    val localDeviceId by deviceViewModel.localDeviceId.collectAsState()
    var deviceToRevoke by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = RitikBackground,
        topBar = {
            TopAppBar(
                title = { Text("Device Management", fontWeight = FontWeight.SemiBold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = RitikTextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateAddDevice) {
                        Icon(Icons.Default.Add, contentDescription = "Pair New Device", tint = RitikBlueLight)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = RitikBackground)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Local Device Status Card
            item {
                SectionHeader(title = "THIS DEVICE")
                RitikCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(RitikBlueGlow),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.PhoneAndroid,
                                contentDescription = null,
                                tint = RitikBlueLight,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = DeviceUtils.getDeviceName(),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = RitikTextPrimary
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "ID: ${localDeviceId.take(12)}…",
                                fontSize = 12.sp,
                                color = RitikTextTertiary
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = DeviceUtils.getAndroidVersion(),
                                fontSize = 11.sp,
                                color = RitikTextSecondary
                            )
                        }
                        StatusBadge(text = "Active", color = RitikSuccess)
                    }
                }
            }

            // Authorized Remote Devices
            item {
                SectionHeader(
                    title = "AUTHORIZED DEVICES",
                    action = "Pair New",
                    onAction = onNavigateAddDevice
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
                            title = "No Devices Connected",
                            subtitle = "Pair another Android phone using a QR code or an 8-character pairing code to manage it here.",
                            actionText = "Add Device",
                            onAction = onNavigateAddDevice
                        )
                    }
                }
                pairedDevicesState.data != null -> {
                    val devices = pairedDevicesState.data!!
                    items(devices) { device ->
                        RitikCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(RitikSurfaceElevated),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.PhoneAndroid,
                                        contentDescription = null,
                                        tint = RitikBlueLight,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = device.remoteDeviceName.ifEmpty { "Android Device" },
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = RitikTextPrimary
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = "Paired on ${TimeUtils.formatDate(device.pairedAt)}",
                                        fontSize = 12.sp,
                                        color = RitikTextTertiary
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { onNavigateDeviceDetails(device.remoteDeviceId) }
                                    ) {
                                        Icon(
                                            Icons.Default.Visibility,
                                            contentDescription = "Details & Stream",
                                            tint = RitikBlueLight
                                        )
                                    }
                                    IconButton(
                                        onClick = { deviceToRevoke = device.remoteDeviceId }
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Revoke Access",
                                            tint = RitikError
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(20.dp))
            }
        }
    }

    if (deviceToRevoke != null) {
        AlertDialog(
            onDismissRequest = { deviceToRevoke = null },
            title = { Text("Revoke Device Access?", color = RitikTextPrimary) },
            text = {
                Text(
                    "This device will be disconnected and removed from your authorized list. You will need to pair it again to reconnect.",
                    color = RitikTextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val id = deviceToRevoke!!
                        deviceToRevoke = null
                        deviceViewModel.removePairedDevice(id) {}
                    }
                ) {
                    Text("Revoke", color = RitikError, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deviceToRevoke = null }) {
                    Text("Cancel", color = RitikTextSecondary)
                }
            },
            containerColor = RitikSurfaceVariant
        )
    }
}
