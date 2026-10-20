package com.ritiksc.remoteaccess.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ritiksc.remoteaccess.permissions.PermissionManager
import com.ritiksc.remoteaccess.permissions.PermissionViewModel
import com.ritiksc.remoteaccess.ui.components.RitikCard
import com.ritiksc.remoteaccess.ui.components.RitikOutlinedButton
import com.ritiksc.remoteaccess.ui.components.SectionHeader
import com.ritiksc.remoteaccess.ui.components.StatusBadge
import com.ritiksc.remoteaccess.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionsScreen(
    permissionViewModel: PermissionViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val permissions by permissionViewModel.permissions.collectAsState()

    LaunchedEffect(Unit) {
        permissionViewModel.refreshPermissions()
    }

    Scaffold(
        containerColor = RitikBackground,
        topBar = {
            TopAppBar(
                title = { Text("Permissions & Privacy", fontWeight = FontWeight.SemiBold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = RitikTextPrimary)
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
            item {
                RitikCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(RitikBlueGlow),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = RitikBlueLight)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Zero Surveillance Guarantee",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = RitikTextPrimary
                            )
                            Text(
                                text = "RITIK SC never accesses files, contacts, keystrokes, or microphones covertly.",
                                fontSize = 12.sp,
                                color = RitikTextSecondary
                            )
                        }
                    }
                }
            }

            item {
                SectionHeader(title = "REQUIRED & OPTIONAL PERMISSIONS")
            }

            items(permissions) { perm ->
                PermissionItemCard(
                    perm = perm,
                    onOpenSettings = {
                        val intent = perm.settingsIntent ?: permissionViewModel.getAppSettingsIntent()
                        context.startActivity(intent)
                    }
                )
            }

            item {
                Spacer(Modifier.height(8.dp))
                RitikOutlinedButton(
                    text = "Open App Settings",
                    icon = Icons.Default.OpenInNew,
                    onClick = {
                        context.startActivity(permissionViewModel.getAppSettingsIntent())
                    }
                )
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun PermissionItemCard(
    perm: PermissionManager.PermissionInfo,
    onOpenSettings: () -> Unit
) {
    RitikCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = perm.displayName,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = RitikTextPrimary
            )
            StatusBadge(
                text = if (perm.isGranted) "Granted" else "Not Granted",
                color = if (perm.isGranted) RitikSuccess else RitikError
            )
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = perm.rationale,
            fontSize = 13.sp,
            color = RitikTextSecondary,
            lineHeight = 18.sp
        )

        if (!perm.isGranted) {
            Spacer(Modifier.height(12.dp))
            TextButton(
                onClick = onOpenSettings,
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = "Configure in Settings →",
                    color = RitikBlueLight,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
