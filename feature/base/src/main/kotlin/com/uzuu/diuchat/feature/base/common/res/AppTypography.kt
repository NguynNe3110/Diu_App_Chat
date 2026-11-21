package com.uzuu.diuchat.feature.base.common.res

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** Shared type scale. Colors are supplied by the component using AppTheme.colors. */
internal val DiuTypography = Typography(
    displayLarge = diuTextStyle(size = 36, lineHeight = 42),
    displayMedium = diuTextStyle(size = 34, lineHeight = 40),
    displaySmall = diuTextStyle(size = 28, lineHeight = 34),
    headlineLarge = diuTextStyle(size = 28, lineHeight = 34),
    headlineMedium = diuTextStyle(size = 24, lineHeight = 30),
    headlineSmall = diuTextStyle(size = 22, lineHeight = 28),
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

private fun diuTextStyle(
    size: Int,
    lineHeight: Int,
    weight: FontWeight = FontWeight.Normal,
) = TextStyle(
    fontFamily = BeVietnamPro,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = 0.sp,
)
