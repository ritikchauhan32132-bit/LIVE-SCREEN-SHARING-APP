package com.ritiksc.remoteaccess

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.ritiksc.remoteaccess.auth.AuthViewModel
import com.ritiksc.remoteaccess.connection.ConnectionViewModel
import com.ritiksc.remoteaccess.devices.DeviceViewModel
import com.ritiksc.remoteaccess.pairing.PairingViewModel
import com.ritiksc.remoteaccess.permissions.PermissionViewModel
import com.ritiksc.remoteaccess.ui.navigation.RitikNavGraph
import com.ritiksc.remoteaccess.ui.theme.RitikBackground
import com.ritiksc.remoteaccess.ui.theme.RitikScTheme

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val deviceViewModel: DeviceViewModel by viewModels()
    private val pairingViewModel: PairingViewModel by viewModels()
    private val connectionViewModel: ConnectionViewModel by viewModels()
    private val permissionViewModel: PermissionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            RitikScTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = RitikBackground
                ) {
                    val navController = rememberNavController()
                    RitikNavGraph(
                        navController = navController,
                        authViewModel = authViewModel,
                        deviceViewModel = deviceViewModel,
                        pairingViewModel = pairingViewModel,
                        connectionViewModel = connectionViewModel,
                        permissionViewModel = permissionViewModel
                    )
                }
            }
        }
    }
}
