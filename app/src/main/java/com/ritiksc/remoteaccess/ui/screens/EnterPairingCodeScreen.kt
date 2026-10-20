package com.ritiksc.remoteaccess.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ritiksc.remoteaccess.pairing.PairingViewModel
import com.ritiksc.remoteaccess.ui.components.ErrorBanner
import com.ritiksc.remoteaccess.ui.components.RitikGradientButton
import com.ritiksc.remoteaccess.ui.components.RitikOutlinedButton
import com.ritiksc.remoteaccess.ui.components.RitikTextField
import com.ritiksc.remoteaccess.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnterPairingCodeScreen(
    pairingViewModel: PairingViewModel,
    onNavigateBack: () -> Unit,
    onPairingSuccess: () -> Unit,
    onOpenQrScanner: () -> Unit
) {
    var codeInput by remember { mutableStateOf("") }
    val pairResult by pairingViewModel.pairResult.collectAsState()

    LaunchedEffect(pairResult) {
        if (pairResult.data != null) {
            pairingViewModel.clearPairResult()
            onPairingSuccess()
        }
    }

    Scaffold(
        containerColor = RitikBackground,
        topBar = {
            TopAppBar(
                title = { Text("Pair Remote Device", fontWeight = FontWeight.SemiBold, fontSize = 18.sp) },
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
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Enter Pairing Code",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = RitikTextPrimary
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Enter the 8-character one-time pairing code displayed on the remote device.",
                fontSize = 14.sp,
                color = RitikTextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))

            if (pairResult.error != null) {
                ErrorBanner(
                    message = pairResult.error ?: "",
                    onDismiss = { pairingViewModel.clearPairResult() }
                )
                Spacer(Modifier.height(16.dp))
            }

            RitikTextField(
                value = codeInput,
                onValueChange = {
                    if (it.length <= 8) {
                        codeInput = it.uppercase()
                    }
                },
                label = "8-Character Code",
                singleLine = true
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "${codeInput.length}/8 characters",
                fontSize = 12.sp,
                color = RitikTextTertiary,
                modifier = Modifier.align(Alignment.End)
            )

            Spacer(Modifier.height(28.dp))

            RitikGradientButton(
                text = "Verify & Pair Device",
                enabled = codeInput.length == 8,
                isLoading = pairResult.isLoading,
                onClick = {
                    pairingViewModel.pairWithCode(codeInput)
                }
            )

            Spacer(Modifier.height(16.dp))

            RitikOutlinedButton(
                text = "Scan QR Code",
                icon = Icons.Default.QrCodeScanner,
                onClick = onOpenQrScanner
            )
        }
    }
}
