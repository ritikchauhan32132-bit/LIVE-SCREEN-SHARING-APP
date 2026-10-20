package com.ritiksc.remoteaccess.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ritiksc.remoteaccess.common.TimeUtils
import com.ritiksc.remoteaccess.devices.DeviceViewModel
import com.ritiksc.remoteaccess.ui.components.EmptyState
import com.ritiksc.remoteaccess.ui.components.RitikCard
import com.ritiksc.remoteaccess.ui.components.StatusBadge
import com.ritiksc.remoteaccess.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    deviceViewModel: DeviceViewModel,
    onNavigateBack: () -> Unit
) {
    val historyState by deviceViewModel.connectionHistory.collectAsState()

    LaunchedEffect(Unit) {
        deviceViewModel.loadConnectionHistory()
    }

    Scaffold(
        containerColor = RitikBackground,
        topBar = {
            TopAppBar(
                title = { Text("Connection History", fontWeight = FontWeight.SemiBold, fontSize = 18.sp) },
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
                .padding(horizontal = 16.dp)
        ) {
            when {
                historyState.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = RitikBlue)
                    }
                }
                historyState.isEmpty -> {
                    EmptyState(
                        icon = Icons.Default.History,
                        title = "No Connection History",
                        subtitle = "When you complete remote screen sharing sessions, the audit records will appear here."
                    )
                }
                historyState.data != null -> {
                    val list = historyState.data!!
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize().padding(top = 12.dp)
                    ) {
                        items(list) { session ->
                            RitikCard {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${session.controllerDeviceName} → ${session.remoteDeviceName}",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = RitikTextPrimary
                                    )
                                    StatusBadge(
                                        text = TimeUtils.formatDuration(session.duration),
                                        color = RitikBlueLight
                                    )
                                }

                                Spacer(Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = TimeUtils.formatFull(session.startedAt),
                                        fontSize = 12.sp,
                                        color = RitikTextTertiary
                                    )
                                    if (session.endReason.isNotEmpty()) {
                                        Text(
                                            text = session.endReason,
                                            fontSize = 12.sp,
                                            color = RitikTextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
