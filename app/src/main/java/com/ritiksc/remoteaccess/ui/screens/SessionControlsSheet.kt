package com.ritiksc.remoteaccess.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ritiksc.remoteaccess.ui.components.RitikCard
import com.ritiksc.remoteaccess.ui.components.SectionHeader
import com.ritiksc.remoteaccess.ui.components.StatusBadge
import com.ritiksc.remoteaccess.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionControlsSheet(
    sessionDuration: String,
    rtcState: String,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = RitikSurface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = RitikBorder) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Session Diagnostics",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = RitikTextPrimary
            )

            SectionHeader(title = "STREAM DETAILS")

            RitikCard {
                DiagnosticRow(label = "Connection State", value = rtcState)
                Divider(color = RitikBorderSubtle, modifier = Modifier.padding(vertical = 8.dp))
                DiagnosticRow(label = "Session Duration", value = sessionDuration)
                Divider(color = RitikBorderSubtle, modifier = Modifier.padding(vertical = 8.dp))
                DiagnosticRow(label = "Video Stream Resolution", value = "1280 × 720 (HD)")
                Divider(color = RitikBorderSubtle, modifier = Modifier.padding(vertical = 8.dp))
                DiagnosticRow(label = "Frame Rate Target", value = "30 FPS")
                Divider(color = RitikBorderSubtle, modifier = Modifier.padding(vertical = 8.dp))
                DiagnosticRow(label = "SDP Semantics", value = "Unified Plan")
                Divider(color = RitikBorderSubtle, modifier = Modifier.padding(vertical = 8.dp))
                DiagnosticRow(label = "Signaling Transport", value = "Cloud Firestore + TLS")
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RitikSurfaceVariant)
            ) {
                Text("Close Diagnostics", color = RitikTextPrimary)
            }
        }
    }
}

@Composable
private fun DiagnosticRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 13.sp, color = RitikTextSecondary)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = RitikTextPrimary)
    }
}
