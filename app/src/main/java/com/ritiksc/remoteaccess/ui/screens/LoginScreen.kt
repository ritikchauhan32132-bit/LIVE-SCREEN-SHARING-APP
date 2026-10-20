package com.ritiksc.remoteaccess.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ritiksc.remoteaccess.auth.AuthViewModel
import com.ritiksc.remoteaccess.ui.components.ErrorBanner
import com.ritiksc.remoteaccess.ui.components.RitikGradientButton
import com.ritiksc.remoteaccess.ui.components.RitikTextField
import com.ritiksc.remoteaccess.ui.theme.*

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onNavigateRegister: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val loginState by authViewModel.loginState.collectAsState()

    LaunchedEffect(loginState) {
        if (loginState.data != null) {
            onLoginSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RitikBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Welcome Back",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = RitikTextPrimary
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Sign in to access your remote devices",
                fontSize = 14.sp,
                color = RitikTextSecondary
            )

            Spacer(Modifier.height(32.dp))

            if (loginState.error != null) {
                ErrorBanner(
                    message = loginState.error ?: "",
                    onDismiss = { authViewModel.clearLoginError() }
                )
                Spacer(Modifier.height(16.dp))
            }

            RitikTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email Address",
                leadingIcon = Icons.Default.Email
            )

            Spacer(Modifier.height(16.dp))

            RitikTextField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                leadingIcon = Icons.Default.Lock,
                isPassword = !passwordVisible,
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle password visibility",
                            tint = RitikTextTertiary
                        )
                    }
                }
            )

            Spacer(Modifier.height(8.dp))

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                Text(
                    text = "Forgot Password?",
                    fontSize = 13.sp,
                    color = RitikBlueLight,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable {
                        if (email.isNotBlank()) {
                            authViewModel.sendPasswordReset(email)
                        }
                    }
                )
            }

            Spacer(Modifier.height(28.dp))

            RitikGradientButton(
                text = "Log In",
                isLoading = loginState.isLoading,
                onClick = {
                    authViewModel.login(email, password)
                }
            )

            Spacer(Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Don't have an account? ",
                    fontSize = 14.sp,
                    color = RitikTextSecondary
                )
                Text(
                    text = "Sign Up",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = RitikBlueLight,
                    modifier = Modifier.clickable { onNavigateRegister() }
                )
            }
        }
    }
}
