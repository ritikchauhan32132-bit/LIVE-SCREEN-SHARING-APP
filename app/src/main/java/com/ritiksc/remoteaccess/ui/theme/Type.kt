package com.ritiksc.remoteaccess.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Using system default sans-serif which maps well on Android
val RitikFontFamily = FontFamily.SansSerif

val RitikTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = RitikFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = (-0.5).sp,
        color = RitikTextPrimary
    ),
    displayMedium = TextStyle(
        fontFamily = RitikFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.25).sp,
        color = RitikTextPrimary
    ),
    headlineLarge = TextStyle(
        fontFamily = RitikFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        color = RitikTextPrimary
    ),
    headlineMedium = TextStyle(
        fontFamily = RitikFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        color = RitikTextPrimary
    ),
    titleLarge = TextStyle(
        fontFamily = RitikFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        color = RitikTextPrimary
    ),
    titleMedium = TextStyle(
        fontFamily = RitikFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        color = RitikTextPrimary
    ),
    titleSmall = TextStyle(
        fontFamily = RitikFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = RitikTextPrimary
    ),
    bodyLarge = TextStyle(
        fontFamily = RitikFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        color = RitikTextPrimary
    ),
    bodyMedium = TextStyle(
        fontFamily = RitikFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = RitikTextSecondary
    ),
    bodySmall = TextStyle(
        fontFamily = RitikFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        color = RitikTextTertiary
    ),
    labelLarge = TextStyle(
        fontFamily = RitikFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = RitikTextPrimary
    ),
    labelMedium = TextStyle(
        fontFamily = RitikFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        color = RitikTextSecondary
    ),
    labelSmall = TextStyle(
        fontFamily = RitikFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.5.sp,
        color = RitikTextTertiary
    )
)
