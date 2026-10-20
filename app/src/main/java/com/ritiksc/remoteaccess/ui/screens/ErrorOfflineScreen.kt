package com.ritiksc.remoteaccess.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ritiksc.remoteaccess.ui.components.EmptyState
import com.ritiksc.remoteaccess.ui.components.RitikGradientButton
import com.ritiksc.remoteaccess.ui.theme.*

@Composable
fun ErrorOfflineScreen(
    errorMessage: String = "No Internet Connection",
    errorDetails: String = "Please check your Wi-Fi or cellular network settings and try again.",
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RitikBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            EmptyState(
                icon = Icons.Default.WifiOff,
                title = errorMessage,
                subtitle = errorDetails
            )

            Spacer(Modifier.height(24.dp))

            RitikGradientButton(
                text = "Retry Connection",
                onClick = onRetry
            )
        }
    }
}
