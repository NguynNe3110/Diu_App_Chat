package com.uzuu.diuchat.feature.base.common.res

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** Shared type scale. Display & headline slots use [DiuSerif]; body & labels use [BeVietnamPro]. */
internal val DiuTypography =
    Typography(
        displayLarge = diuTextStyle(size = 36, lineHeight = 42, fontFamily = DiuSerif),
        displayMedium = diuTextStyle(size = 34, lineHeight = 40, fontFamily = DiuSerif),
        displaySmall = diuTextStyle(size = 28, lineHeight = 34, fontFamily = DiuSerif),
        headlineLarge = diuTextStyle(size = 28, lineHeight = 34, fontFamily = DiuSerif),
        headlineMedium = diuTextStyle(size = 24, lineHeight = 30, fontFamily = DiuSerif),
        headlineSmall = diuTextStyle(size = 22, lineHeight = 28, fontFamily = DiuSerif),
        titleLarge = diuTextStyle(size = 22, lineHeight = 28, weight = FontWeight.Medium),
        titleMedium = diuTextStyle(size = 17, lineHeight = 24, weight = FontWeight.Medium),
        titleSmall = diuTextStyle(size = 16, lineHeight = 22, weight = FontWeight.Medium),
        bodyLarge = diuTextStyle(size = 16, lineHeight = 24),
        bodyMedium = diuTextStyle(size = 15, lineHeight = 22),
        bodySmall = diuTextStyle(size = 13, lineHeight = 18),
        labelLarge = diuTextStyle(size = 16, lineHeight = 22, weight = FontWeight.Medium),
        labelMedium = diuTextStyle(size = 12, lineHeight = 17, weight = FontWeight.Medium),
        labelSmall = diuTextStyle(size = 11, lineHeight = 16, weight = FontWeight.Medium),
    )

/** Dedicated Serif display/headline tokens matching prototype screen titles. */
object DiuSerifTypography {
    val displayLarge: TextStyle = diuTextStyle(size = 36, lineHeight = 42, fontFamily = DiuSerif)
    val displayMedium: TextStyle = diuTextStyle(size = 34, lineHeight = 40, fontFamily = DiuSerif)
    val displaySmall: TextStyle = diuTextStyle(size = 28, lineHeight = 34, fontFamily = DiuSerif)
    val headlineLarge: TextStyle = diuTextStyle(size = 28, lineHeight = 34, fontFamily = DiuSerif)
    val headlineMedium: TextStyle = diuTextStyle(size = 24, lineHeight = 30, fontFamily = DiuSerif)
    val headlineSmall: TextStyle = diuTextStyle(size = 22, lineHeight = 28, fontFamily = DiuSerif)
}

private fun diuTextStyle(
    size: Int,
    lineHeight: Int,
    weight: FontWeight = FontWeight.Normal,
    fontFamily: FontFamily = BeVietnamPro,
) = TextStyle(
    fontFamily = fontFamily,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = 0.sp,
)
