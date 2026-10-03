package com.uzuu.diuchat.feature.base.common.res

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

/**
 * AppTheme — Theme wrapper chính của ứng dụng Dịu.
 *
 * Bọc UI trong [AppTheme] để tự động:
 * 1. Cung cấp [AppColors] (custom tokens của Dịu) qua [LocalAppColors].
 * 2. Ánh xạ vào Material 3 [ColorScheme] để các M3 component (Button, Card, Scaffold,...)
 *    tự động sử dụng đúng sắc thái.
 *
 * Usage:
 * ```kotlin
 * // Trong Activity:
 * setContent {
 *     AppTheme {
 *         MainShowcaseScreen()
 *     }
 * }
 *
 * // Trong Composable:
 * Text(color = AppTheme.colors.ink)               // Custom token
 * Text(color = AppTheme.colors.moods.thuong)       // Mood color
 * Text(color = MaterialTheme.colorScheme.primary)  // M3 standard
 * ```
 *
 * Tương đương: app_themes.dart trong Flutter.
 */

// ═══════════════════════════════════════════════
// Material 3 ColorScheme mapping
// ═══════════════════════════════════════════════

private val DiuLightColorScheme = lightColorScheme(
    primary = AppPalette.LightAccent,
    onPrimary = AppPalette.LightOnAccent,
    primaryContainer = AppPalette.Peach,
    onPrimaryContainer = AppPalette.LightInk,
    secondary = AppPalette.Peach,
    onSecondary = AppPalette.LightInk,
    background = AppPalette.LightBg,
    onBackground = AppPalette.LightInk,
    surface = AppPalette.LightBg,
    onSurface = AppPalette.LightInk,
    surfaceVariant = AppPalette.LightSurface2,
    onSurfaceVariant = AppPalette.LightInk2,
    outline = AppPalette.LightLine,
    outlineVariant = AppPalette.LightLine,
    error = AppPalette.Danger,
    onError = AppPalette.LightOnAccent,
)

private val DiuDarkColorScheme = darkColorScheme(
    primary = AppPalette.DarkAccent,
    onPrimary = AppPalette.DarkOnAccent,
    primaryContainer = AppPalette.DarkPeach,
    onPrimaryContainer = AppPalette.DarkInk,
    secondary = AppPalette.DarkPeach,
    onSecondary = AppPalette.DarkInk,
    background = AppPalette.DarkBg,
    onBackground = AppPalette.DarkInk,
    surface = AppPalette.DarkBg,
    onSurface = AppPalette.DarkInk,
    surfaceVariant = AppPalette.DarkSurface2,
    onSurfaceVariant = AppPalette.DarkInk2,
    outline = AppPalette.DarkLine,
    outlineVariant = AppPalette.DarkLine,
    error = AppPalette.Danger,
    onError = AppPalette.DarkOnAccent,
)

// ═══════════════════════════════════════════════
// Theme Composable
// ═══════════════════════════════════════════════

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val appColors = if (darkTheme) DarkAppColors else LightAppColors
    val materialColorScheme = if (darkTheme) DiuDarkColorScheme else DiuLightColorScheme

    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            content = content,
        )
    }
}

// ═══════════════════════════════════════════════
// Accessor object — Truy cập nhanh trong Composable
// ═══════════════════════════════════════════════

/**
 * Object tiện ích để truy cập [AppColors] trong bất kỳ Composable nào.
 *
 * ```kotlin
 * val bg = AppTheme.colors.background
 * val thuong = AppTheme.colors.moods.thuong
 * ```
 */
object AppTheme {
    /**
     * Bộ màu hiện tại theo theme (Light hoặc Dark).
     * Phải được gọi bên trong @Composable scope.
     */
    val colors: AppColors
        @Composable
        get() = LocalAppColors.current
}
