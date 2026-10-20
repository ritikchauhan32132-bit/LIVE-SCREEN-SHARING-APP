package com.ritiksc.remoteaccess.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DevicesOther
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ritiksc.remoteaccess.common.TimeUtils
import com.ritiksc.remoteaccess.devices.DeviceViewModel
import com.ritiksc.remoteaccess.ui.components.*
import com.ritiksc.remoteaccess.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthorizedDevicesScreen(
    deviceViewModel: DeviceViewModel,
    onNavigateBack: () -> Unit,
    onNavigateAddDevice: () -> Unit,
    onNavigateDeviceDetails: (deviceId: String) -> Unit,
    onNavigateDeviceManagement: () -> Unit = {}
) {
    val pairedDevicesState by deviceViewModel.pairedDevices.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        containerColor = RitikBackground,
        topBar = {
            TopAppBar(
                title = { Text("Authorized Devices", fontWeight = FontWeight.SemiBold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = RitikTextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateDeviceManagement) {
                        Icon(Icons.Default.Tune, contentDescription = "Manage Devices", tint = RitikTextSecondary)
                    }
                    IconButton(onClick = onNavigateAddDevice) {
                        Icon(Icons.Default.Add, contentDescription = "Add Device", tint = RitikBlueLight)
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
                .padding(horizontal = 16.dp)
        ) {
            RitikTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = "Search devices",
                leadingIcon = Icons.Default.Search,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            when {
                pairedDevicesState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = RitikBlue)
                    }
                }
                pairedDevicesState.isEmpty -> {
                    EmptyState(
                        icon = Icons.Default.DevicesOther,
                        title = "No Paired Devices",
                        subtitle = "Pair your phone with another device to enable remote support.",
                        actionText = "Pair Device",
                        onAction = onNavigateAddDevice
                    )
                }
                pairedDevicesState.data != null -> {
                    val filteredDevices = pairedDevicesState.data!!.filter {
                        it.remoteDeviceName.contains(searchQuery, ignoreCase = true)
                    }

                    if (filteredDevices.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No devices match '$searchQuery'", color = RitikTextSecondary)
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filteredDevices) { device ->
                                DeviceListItem(
                                    name = device.remoteDeviceName.ifEmpty { "Android Device" },
                                    status = "Authorized",
                                    statusColor = RitikSuccess,
                                    lastSeen = "Paired: ${TimeUtils.formatDate(device.pairedAt)}",
                                    onClick = { onNavigateDeviceDetails(device.remoteDeviceId) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
