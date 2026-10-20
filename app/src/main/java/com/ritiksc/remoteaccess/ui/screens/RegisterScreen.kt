package com.ritiksc.remoteaccess.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
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
fun RegisterScreen(
    authViewModel: AuthViewModel,
    onNavigateLogin: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    var displayName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val registerState by authViewModel.registerState.collectAsState()

    LaunchedEffect(registerState) {
        if (registerState.data != null) {
            onRegisterSuccess()
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
                text = "Create Account",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = RitikTextPrimary
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Register to manage and access remote devices",
                fontSize = 14.sp,
                color = RitikTextSecondary
            )

            Spacer(Modifier.height(28.dp))

            if (registerState.error != null) {
                ErrorBanner(
                    message = registerState.error ?: "",
                    onDismiss = { authViewModel.clearRegisterError() }
                )
                Spacer(Modifier.height(16.dp))
            }

            RitikTextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = "Full Name",
                leadingIcon = Icons.Default.Person
            )

            Spacer(Modifier.height(14.dp))

            RitikTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email Address",
                leadingIcon = Icons.Default.Email
            )

            Spacer(Modifier.height(14.dp))

            RitikTextField(
                value = password,
                onValueChange = { password = it },
                label = "Password (min 6 characters)",
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

            Spacer(Modifier.height(14.dp))

            RitikTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = "Confirm Password",
                leadingIcon = Icons.Default.Lock,
                isPassword = !passwordVisible
            )

            Spacer(Modifier.height(28.dp))

            RitikGradientButton(
                text = "Register",
                isLoading = registerState.isLoading,
                onClick = {
                    authViewModel.register(email, password, confirmPassword, displayName)
                }
            )

            Spacer(Modifier.height(20.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Already have an account? ",
                    fontSize = 14.sp,
                    color = RitikTextSecondary
                )
                Text(
                    text = "Log In",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = RitikBlueLight,
                    modifier = Modifier.clickable { onNavigateLogin() }
                )
            }
        }
    }
}
