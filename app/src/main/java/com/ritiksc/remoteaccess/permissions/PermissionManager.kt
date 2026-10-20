package com.ritiksc.remoteaccess.permissions

import android.Manifest
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat

/**
 * Centralized permission manager that determines what permissions are needed,
 * explains their purpose, and provides settings intents for special access.
 */
class PermissionManager(private val context: Context) {

    data class PermissionInfo(
        val permission: String,
        val displayName: String,
        val rationale: String,
        val isGranted: Boolean,
        val isSpecialAccess: Boolean = false,
        val settingsIntent: Intent? = null
    )

    fun getAllPermissions(): List<PermissionInfo> {
        return buildList {
            // Camera (for QR scanning)
            add(
                PermissionInfo(
                    permission = Manifest.permission.CAMERA,
                    displayName = "Camera",
                    rationale = "Camera access is needed to scan QR codes for device pairing.",
                    isGranted = isPermissionGranted(Manifest.permission.CAMERA)
                )
            )

            // Notifications (Android 13+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(
                    PermissionInfo(
                        permission = Manifest.permission.POST_NOTIFICATIONS,
                        displayName = "Notifications",
                        rationale = "Notifications let you know about connection requests and screen sharing status.",
                        isGranted = isPermissionGranted(Manifest.permission.POST_NOTIFICATIONS)
                    )
                )
            }

            // Usage stats (special access)
            add(
                PermissionInfo(
                    permission = Manifest.permission.PACKAGE_USAGE_STATS,
                    displayName = "Usage Statistics",
                    rationale = "Optional. Allows showing basic app usage information in the activity dashboard. You can enable this in Settings.",
                    isGranted = hasUsageStatsPermission(),
                    isSpecialAccess = true,
                    settingsIntent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                )
            )
        }
    }

    fun isPermissionGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    fun hasUsageStatsPermission(): Boolean {
        return try {
            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val mode = appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(),
                context.packageName
            )
            mode == AppOpsManager.MODE_ALLOWED
        } catch (e: Exception) {
            false
        }
    }

    fun getAppSettingsIntent(): Intent {
        return Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("scheme", context.packageName, null)
        }
    }

    fun getRuntimePermissions(): List<String> {
        val permissions = mutableListOf(Manifest.permission.CAMERA)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        return permissions
    }

    fun getUngrantedRuntimePermissions(): List<String> {
        return getRuntimePermissions().filter { !isPermissionGranted(it) }
    }

    fun hasCameraPermission(): Boolean = isPermissionGranted(Manifest.permission.CAMERA)

    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            isPermissionGranted(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            true // Not needed before Android 13
        }
    }
}
