package com.ritiksc.remoteaccess.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ritiksc.remoteaccess.auth.AuthViewModel
import com.ritiksc.remoteaccess.ui.components.RitikCard
import com.ritiksc.remoteaccess.ui.components.RitikOutlinedButton
import com.ritiksc.remoteaccess.ui.components.SectionHeader
import com.ritiksc.remoteaccess.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    authViewModel: AuthViewModel,
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit
) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var highQualityVideo by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = RitikBackground,
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.SemiBold, fontSize = 18.sp) },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SectionHeader(title = "USER ACCOUNT")

            RitikCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = RitikBlueLight,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            text = authViewModel.displayName ?: "Authorized User",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = RitikTextPrimary
                        )
                        Text(
                            text = authViewModel.userEmail ?: "No email",
                            fontSize = 13.sp,
                            color = RitikTextSecondary
                        )
                    }
                }
            }

            SectionHeader(title = "PREFERENCES")

            RitikCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Session Notifications", fontSize = 15.sp, color = RitikTextPrimary, fontWeight = FontWeight.Medium)
                        Text("Receive alerts for incoming connection requests", fontSize = 12.sp, color = RitikTextSecondary)
                    }
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { notificationsEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = RitikBlue,
                            checkedTrackColor = RitikBlueDark
                        )
                    )
                }

                Divider(color = RitikBorderSubtle, modifier = Modifier.padding(vertical = 12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("High Bitrate Streaming", fontSize = 15.sp, color = RitikTextPrimary, fontWeight = FontWeight.Medium)
                        Text("Stream up to 1080p (uses more mobile data)", fontSize = 12.sp, color = RitikTextSecondary)
                    }
                    Switch(
                        checked = highQualityVideo,
                        onCheckedChange = { highQualityVideo = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = RitikBlue,
                            checkedTrackColor = RitikBlueDark
                        )
                    )
                }
            }

            SectionHeader(title = "APP INFORMATION")

            RitikCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Application Name", fontSize = 13.sp, color = RitikTextSecondary)
                    Text("RITIK SC", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = RitikTextPrimary)
                }
                Divider(color = RitikBorderSubtle, modifier = Modifier.padding(vertical = 8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Version", fontSize = 13.sp, color = RitikTextSecondary)
                    Text("1.0.0 (Production Build)", fontSize = 13.sp, color = RitikTextPrimary)
                }
                Divider(color = RitikBorderSubtle, modifier = Modifier.padding(vertical = 8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Application ID", fontSize = 13.sp, color = RitikTextSecondary)
                    Text("com.ritiksc.remoteaccess", fontSize = 13.sp, color = RitikTextPrimary)
                }
            }

            Spacer(Modifier.height(16.dp))

            RitikOutlinedButton(
                text = "Log Out",
                icon = Icons.Default.ExitToApp,
                onClick = { showLogoutDialog = true }
            )
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Log Out from RITIK SC?", color = RitikTextPrimary) },
            text = { Text("You will need to sign in again to manage remote connections.", color = RitikTextSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        authViewModel.logout()
                        onLogout()
                    }
                ) {
                    Text("Log Out", color = RitikError, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = RitikTextSecondary)
                }
            },
            containerColor = RitikSurfaceVariant
        )
    }
}
