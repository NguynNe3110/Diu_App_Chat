package com.igorwojda.showcase.feature.base.common.res

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * AppColors — Bộ màu ngữ nghĩa theo theme (Semantic Color Tokens).
 *
 * Lớp này ánh xạ các giá trị từ [AppPalette] vào tên gọi theo chức năng,
 * giúp UI code không cần biết đang ở Light hay Dark mode.
 *
 * Có 2 instance được tạo sẵn: [LightAppColors] và [DarkAppColors].
 * Truy cập qua [LocalAppColors] (CompositionLocal) hoặc [AppTheme.colors].
 *
 * Tương đương: app_colors.dart trong Flutter.
 */
@Immutable
data class AppColors(

    // ── Nền & Bề mặt ──────────────────────────

    /** Nền chính của toàn màn hình */
    val background: Color,

    /** Surface kính mờ (card, overlay, bottom nav) */
    val surface: Color,

    /** Surface nổi rõ hơn (composer, tray, notification bubble) */
    val surfaceElevated: Color,

    // ── Mực & Chữ ──────────────────────────────

    /** Chữ/icon chính — Tiêu đề, nội dung quan trọng */
    val ink: Color,

    /** Chữ/icon phụ — Phụ đề, nội dung tin nhắn, icon bình thường */
    val inkSecondary: Color,

    /** Chữ/icon mờ — Placeholder, hint, icon vô hiệu */
    val inkTertiary: Color,

    // ── Đường kẻ & Viền ────────────────────────

    /** Đường kẻ phân cách, border, divider */
    val line: Color,

    // ── Điểm nhấn ──────────────────────────────

    /** Peach — Sắc đào đặc trưng của Dịu (glow, badge, indicator) */
    val peach: Color,

    /** Accent — Điểm nhấn hành động (CTA, toggle on, active) */
    val accent: Color,

    /** Chữ/icon nằm trên nền accent */
    val onAccent: Color,

    // ── Mood — 5 cảm xúc ──────────────────────

    /** Bộ 5 màu cảm xúc */
    val moods: MoodColors,

    // ── Chat / Tin nhắn ────────────────────────

    /** Chữ bong bóng "mình" trên nền mood gradient */
    val bubbleTextOnMood: Color,

    /** Nền trích dẫn / reply quote */
    val quoteBg: Color,

    // ── Cuộc gọi ───────────────────────────────

    /** Nút kết thúc cuộc gọi */
    val callEnd: Color,

    /** Nút chấp nhận cuộc gọi */
    val callAccept: Color,

    // ── Cảnh báo / Nguy hiểm ───────────────────

    /** Chữ/icon nguy hiểm (xoá, chặn, cuộc gọi nhỡ) */
    val danger: Color,

    /** Chấm ghi âm nhấp nháy */
    val recDot: Color,

    // ── Viewer ─────────────────────────────────

    /** Nền xem ảnh toàn màn hình */
    val viewerBg: Color,

    /** Chữ trên viewer */
    val viewerText: Color,

    // ── Quiet Mode ─────────────────────────────

    /** Tím yên tĩnh (banner, glow, card) */
    val quietPurple: Color,

    /** Vòng dial yên tĩnh */
    val quietRing: Color,

    // ── Overlay / Sheet ────────────────────────

    /** Backdrop mờ khi mở Bottom Sheet */
    val sheetBackdrop: Color,

    // ── Photo placeholder ──────────────────────

    /** Nền label trên ảnh placeholder */
    val photoLabelBg: Color,

    /** Chữ label trên ảnh placeholder */
    val photoLabelText: Color,

    // ── Ambient ────────────────────────────────

    /** Chữ avatar Orb (trắng ấm) */
    val orbText: Color,

    /** Ánh sáng ấm (OTP glow, check-in glow) */
    val warmGlow: Color,

    /** Bóng đổ */
    val shadow: Color,
)

/**
 * MoodColors — Bộ 5 màu cảm xúc cốt lõi của Dịu.
 *
 * Không phân biệt light/dark — Mood colors giữ nguyên sắc giữa 2 theme
 * để đảm bảo tính nhất quán cảm xúc.
 */
@Immutable
data class MoodColors(
    /** Thương — Hồng phấn ấm */
    val thuong: Color = AppPalette.MoodThuong,
    /** Vui — Vàng nắng hổ phách */
    val vui: Color = AppPalette.MoodVui,
    /** Bình yên — Xanh xô thơm */
    val binhyen: Color = AppPalette.MoodBinhYen,
    /** Nhớ — Tím oải hương */
    val nho: Color = AppPalette.MoodNho,
    /** Trầm — Xanh thanh bình */
    val tram: Color = AppPalette.MoodTram,
)

// ═══════════════════════════════════════════════
// 2 Instance được cung cấp sẵn
// ═══════════════════════════════════════════════

val LightAppColors = AppColors(
    background = AppPalette.LightBg,
    surface = AppPalette.LightSurface,
    surfaceElevated = AppPalette.LightSurface2,
    ink = AppPalette.LightInk,
    inkSecondary = AppPalette.LightInk2,
    inkTertiary = AppPalette.LightInk3,
    line = AppPalette.LightLine,
    peach = AppPalette.Peach,
    accent = AppPalette.LightAccent,
    onAccent = AppPalette.LightOnAccent,
    moods = MoodColors(),
    bubbleTextOnMood = AppPalette.BubbleTextOnMood,
    quoteBg = AppPalette.QuoteBgLight,
    callEnd = AppPalette.CallEnd,
    callAccept = AppPalette.CallAccept,
    danger = AppPalette.Danger,
    recDot = AppPalette.RecDot,
    viewerBg = AppPalette.ViewerBg,
    viewerText = AppPalette.ViewerText,
    quietPurple = AppPalette.QuietPurple,
    quietRing = AppPalette.QuietRing,
    sheetBackdrop = AppPalette.SheetBackdrop,
    photoLabelBg = AppPalette.PhotoLabelBg,
    photoLabelText = AppPalette.PhotoLabelText,
    orbText = AppPalette.OrbText,
    warmGlow = AppPalette.WarmGlow,
    shadow = AppPalette.ShadowDark,
)

val DarkAppColors = AppColors(
    background = AppPalette.DarkBg,
    surface = AppPalette.DarkSurface,
    surfaceElevated = AppPalette.DarkSurface2,
    ink = AppPalette.DarkInk,
    inkSecondary = AppPalette.DarkInk2,
    inkTertiary = AppPalette.DarkInk3,
    line = AppPalette.DarkLine,
    peach = AppPalette.DarkPeach,
    accent = AppPalette.DarkAccent,
    onAccent = AppPalette.DarkOnAccent,
    moods = MoodColors(),
    bubbleTextOnMood = AppPalette.BubbleTextOnMood,
    quoteBg = AppPalette.QuoteBgDark,
    callEnd = AppPalette.CallEnd,
    callAccept = AppPalette.CallAccept,
    danger = AppPalette.Danger,
    recDot = AppPalette.RecDot,
    viewerBg = AppPalette.ViewerBg,
    viewerText = AppPalette.ViewerText,
    quietPurple = AppPalette.QuietPurple,
    quietRing = AppPalette.QuietRing,
    sheetBackdrop = AppPalette.SheetBackdrop,
    photoLabelBg = AppPalette.PhotoLabelBg,
    photoLabelText = AppPalette.PhotoLabelText,
    orbText = AppPalette.OrbText,
    warmGlow = AppPalette.WarmGlow,
    shadow = AppPalette.ShadowDark,
)

// ═══════════════════════════════════════════════
// CompositionLocal
// ═══════════════════════════════════════════════

/**
 * CompositionLocal cung cấp [AppColors] xuống toàn bộ cây Composable.
 * Được set bởi [AppTheme], truy cập qua [AppTheme.colors].
 */
val LocalAppColors = staticCompositionLocalOf { LightAppColors }
