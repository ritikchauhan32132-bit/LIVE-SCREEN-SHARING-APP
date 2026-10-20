package com.ritiksc.remoteaccess.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ritiksc.remoteaccess.ui.components.RitikCard
import com.ritiksc.remoteaccess.ui.components.SectionHeader
import com.ritiksc.remoteaccess.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        containerColor = RitikBackground,
        topBar = {
            TopAppBar(
                title = { Text("Help & Troubleshooting", fontWeight = FontWeight.SemiBold, fontSize = 18.sp) },
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
            SectionHeader(title = "COMMONLY ASKED QUESTIONS")

            FaqItem(
                question = "How do I connect two phones across different networks?",
                answer = "RITIK SC uses genuine WebRTC with public STUN servers and TURN relay fallback. Both phones connect via authenticated internet signaling. They do NOT need to be on the same Wi-Fi network and can connect seamlessly over 4G/5G mobile data."
            )

            FaqItem(
                question = "Why does the remote phone ask for screen capture consent?",
                answer = "Android's MediaProjection framework requires explicit user consent before any screen capture can take place. This ensures no app can record or view the screen secretly."
            )

            FaqItem(
                question = "Why are pairing codes only valid for 5 minutes?",
                answer = "To prevent brute-force attacks and replay attempts, pairing codes are generated using cryptographically secure random values and automatically expire after 5 minutes."
            )

            FaqItem(
                question = "Is remote touch control supported?",
                answer = "In Android's standard security model, third-party apps cannot inject arbitrary touch events across other apps without Accessibility Services or device manufacturer MDM privileges. RITIK SC provides view-only remote screen streaming by default."
            )

            FaqItem(
                question = "How do I stop sharing my screen?",
                answer = "You can stop sharing at any moment by tapping 'Stop Sharing' in the persistent Android notification, or by pressing 'End Session' inside RITIK SC."
            )

            SectionHeader(title = "TROUBLESHOOTING")

            RitikCard {
                Text(
                    text = "If connection fails to establish:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = RitikTextPrimary
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "1. Ensure both devices have an active internet connection.\n2. Confirm both devices are logged into the same account.\n3. Make sure battery saver is not killing background services.\n4. If behind a symmetric corporate firewall, configure a TURN relay server.",
                    fontSize = 13.sp,
                    color = RitikTextSecondary,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun FaqItem(question: String, answer: String) {
    var expanded by remember { mutableStateOf(false) }

    RitikCard(onClick = { expanded = !expanded }) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = question,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = RitikTextPrimary,
                modifier = Modifier.weight(1f)
            )
            Icon(
                if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = RitikBlueLight
            )
        }
        if (expanded) {
            Spacer(Modifier.height(8.dp))
            Divider(color = RitikBorderSubtle)
            Spacer(Modifier.height(8.dp))
            Text(
                text = answer,
                fontSize = 13.sp,
                color = RitikTextSecondary,
                lineHeight = 18.sp
            )
        }
    }
}
