package com.ritiksc.remoteaccess.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.ritiksc.remoteaccess.pairing.PairingViewModel
import com.ritiksc.remoteaccess.ui.components.RitikGradientButton
import com.ritiksc.remoteaccess.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrPairingScreen(
    pairingViewModel: PairingViewModel,
    onNavigateBack: () -> Unit
) {
    val codeState by pairingViewModel.generatedCode.collectAsState()
    val secondsRemaining by pairingViewModel.secondsRemaining.collectAsState()
    val qrData by pairingViewModel.qrData.collectAsState()

    LaunchedEffect(Unit) {
        pairingViewModel.generateNewPairingCode()
    }

    val qrBitmap = remember(qrData) {
        qrData?.let { generateQrBitmap(it, 512) }
    }

    Scaffold(
        containerColor = RitikBackground,
        topBar = {
            TopAppBar(
                title = { Text("Device Pairing Code", fontWeight = FontWeight.SemiBold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = RitikTextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { pairingViewModel.generateNewPairingCode() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Regenerate", tint = RitikBlueLight)
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Scan or Enter Code",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = RitikTextPrimary
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "On your controller device, scan this QR code or type the 8-character code below.",
                fontSize = 14.sp,
                color = RitikTextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(28.dp))

            // QR Code Box
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (codeState.isLoading) {
                    CircularProgressIndicator(color = RitikBlue)
                } else if (qrBitmap != null) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "Pairing QR Code",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text("Generating QR…", color = Color.Black, fontSize = 14.sp)
                }
            }

            Spacer(Modifier.height(28.dp))

            // Code Display
            val code = codeState.data?.code ?: "••••••••"
            Card(
                colors = CardDefaults.cardColors(containerColor = RitikSurfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "ONE-TIME CODE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = RitikTextTertiary,
                        letterSpacing = 1.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = formatCode(code),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = RitikBlueLight,
                        letterSpacing = 4.sp
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Expiration Countdown
            Text(
                text = if (secondsRemaining > 0) "Code expires in ${secondsRemaining}s" else "Code expired",
                fontSize = 13.sp,
                color = if (secondsRemaining > 30) RitikTextTertiary else RitikError,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(28.dp))

            RitikGradientButton(
                text = "Generate New Code",
                onClick = { pairingViewModel.generateNewPairingCode() }
            )
        }
    }
}

private fun formatCode(code: String): String {
    return if (code.length == 8) {
        "${code.substring(0, 4)} ${code.substring(4, 8)}"
    } else {
        code
    }
}

private fun generateQrBitmap(content: String, size: Int): Bitmap? {
    return try {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size)
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        bitmap
    } catch (e: Exception) {
        null
    }
}
