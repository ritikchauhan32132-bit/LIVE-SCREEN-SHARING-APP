package com.ritiksc.remoteaccess.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val RitikDarkColorScheme = darkColorScheme(
    primary = RitikBlue,
    onPrimary = RitikTextPrimary,
    primaryContainer = RitikBlueDark,
    onPrimaryContainer = RitikTextPrimary,
    secondary = RitikBlueLight,
    onSecondary = RitikBackground,
    secondaryContainer = RitikSurfaceElevated,
    onSecondaryContainer = RitikTextPrimary,
    tertiary = RitikGradientEnd,
    onTertiary = RitikBackground,
    error = RitikError,
    onError = RitikTextPrimary,
    errorContainer = RitikErrorBg,
    onErrorContainer = RitikError,
    background = RitikBackground,
    onBackground = RitikTextPrimary,
    surface = RitikSurface,
    onSurface = RitikTextPrimary,
    surfaceVariant = RitikSurfaceVariant,
    onSurfaceVariant = RitikTextSecondary,
    outline = RitikBorder,
    outlineVariant = RitikBorderSubtle,
    inverseSurface = RitikTextPrimary,
    inverseOnSurface = RitikBackground,
    inversePrimary = RitikBlueDark,
    surfaceTint = RitikBlue,
)

@Composable
fun RitikScTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = RitikDarkColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = RitikBackground.toArgb()
            window.navigationBarColor = RitikBackground.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = RitikTypography,
        content = content
    )
}
