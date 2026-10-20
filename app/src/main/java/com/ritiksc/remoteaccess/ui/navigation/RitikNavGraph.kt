package com.ritiksc.remoteaccess.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.ritiksc.remoteaccess.auth.AuthViewModel
import com.ritiksc.remoteaccess.connection.ConnectionViewModel
import com.ritiksc.remoteaccess.devices.DeviceViewModel
import com.ritiksc.remoteaccess.pairing.PairingViewModel
import com.ritiksc.remoteaccess.permissions.PermissionViewModel
import com.ritiksc.remoteaccess.ui.screens.*

@Composable
fun RitikNavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    deviceViewModel: DeviceViewModel,
    pairingViewModel: PairingViewModel,
    connectionViewModel: ConnectionViewModel,
    permissionViewModel: PermissionViewModel
) {
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val incomingConnectionRequest by connectionViewModel.incomingRequest.collectAsState()

    // Automatically navigate to connection request screen if an incoming request arrives
    LaunchedEffect(incomingConnectionRequest) {
        if (incomingConnectionRequest != null) {
            navController.navigate("connection_request")
        }
    }

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen(
                isLoggedIn = isLoggedIn,
                onNavigateNext = { route ->
                    navController.navigate(route) {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        composable("welcome") {
            WelcomeScreen(
                onNavigateLogin = { navController.navigate("login") },
                onNavigateRegister = { navController.navigate("register") }
            )
        }

        composable("login") {
            LoginScreen(
                authViewModel = authViewModel,
                onNavigateRegister = { navController.navigate("register") },
                onLoginSuccess = {
                    navController.navigate("dashboard") {
                        popUpTo("welcome") { inclusive = true }
                    }
                }
            )
        }

        composable("register") {
            RegisterScreen(
                authViewModel = authViewModel,
                onNavigateLogin = { navController.navigate("login") },
                onRegisterSuccess = {
                    navController.navigate("dashboard") {
                        popUpTo("welcome") { inclusive = true }
                    }
                }
            )
        }

        composable("dashboard") {
            DashboardScreen(
                deviceViewModel = deviceViewModel,
                userName = authViewModel.displayName,
                onNavigateAddDevice = { navController.navigate("add_device") },
                onNavigateAuthorizedDevices = { navController.navigate("authorized_devices") },
                onNavigateDeviceDetails = { deviceId -> navController.navigate("device_details/$deviceId") },
                onNavigateHistory = { navController.navigate("history") },
                onNavigatePermissions = { navController.navigate("permissions") },
                onNavigateSettings = { navController.navigate("settings") },
                onNavigateHelp = { navController.navigate("help") },
                onStartDirectSharing = { navController.navigate("qr_pairing") }
            )
        }

        composable("add_device") {
            AddDeviceScreen(
                onNavigateGenerateCode = { navController.navigate("qr_pairing") },
                onNavigateEnterCode = { navController.navigate("enter_pairing_code") },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("qr_pairing") {
            QrPairingScreen(
                pairingViewModel = pairingViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("enter_pairing_code") {
            EnterPairingCodeScreen(
                pairingViewModel = pairingViewModel,
                onNavigateBack = { navController.popBackStack() },
                onPairingSuccess = {
                    navController.navigate("dashboard") {
                        popUpTo("dashboard") { inclusive = false }
                    }
                },
                onOpenQrScanner = {
                    navController.navigate("qr_scanner")
                }
            )
        }

        composable("qr_scanner") {
            QrScannerScreen(
                onQrScanned = { rawQr ->
                    pairingViewModel.pairWithQrContent(rawQr)
                    navController.popBackStack()
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("device_management") {
            DeviceManagementScreen(
                deviceViewModel = deviceViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateAddDevice = { navController.navigate("add_device") },
                onNavigateDeviceDetails = { deviceId -> navController.navigate("device_details/$deviceId") }
            )
        }

        composable(
            route = "device_details/{deviceId}",
            arguments = listOf(navArgument("deviceId") { type = NavType.StringType })
        ) { backStackEntry ->
            val deviceId = backStackEntry.arguments?.getString("deviceId") ?: ""
            DeviceDetailsScreen(
                deviceId = deviceId,
                deviceViewModel = deviceViewModel,
                onNavigateBack = { navController.popBackStack() },
                onStartConnection = { targetDeviceId ->
                    connectionViewModel.requestConnection(targetDeviceId, isViewOnly = true)
                    navController.navigate("live_viewer")
                }
            )
        }

        composable("authorized_devices") {
            AuthorizedDevicesScreen(
                deviceViewModel = deviceViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateAddDevice = { navController.navigate("add_device") },
                onNavigateDeviceDetails = { deviceId -> navController.navigate("device_details/$deviceId") },
                onNavigateDeviceManagement = { navController.navigate("device_management") }
            )
        }

        composable("connection_request") {
            ConnectionRequestScreen(
                connectionViewModel = connectionViewModel,
                onNavigateBack = { navController.popBackStack() },
                onSessionStarted = {
                    navController.navigate("dashboard") {
                        popUpTo("dashboard") { inclusive = false }
                    }
                }
            )
        }

        composable("live_viewer") {
            LiveScreenViewerScreen(
                connectionViewModel = connectionViewModel,
                onEndSession = { navController.popBackStack() }
            )
        }

        composable("permissions") {
            PermissionsScreen(
                permissionViewModel = permissionViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("history") {
            HistoryScreen(
                deviceViewModel = deviceViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("settings") {
            SettingsScreen(
                authViewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate("welcome") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable("help") {
            HelpScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("error_offline") {
            ErrorOfflineScreen(
                onRetry = { navController.popBackStack() }
            )
        }
    }
}
