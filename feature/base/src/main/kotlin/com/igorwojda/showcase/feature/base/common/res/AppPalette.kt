package com.uzuu.diuchat.feature.base.common.res

import androidx.compose.ui.graphics.Color

/**
 * AppPalette — Bảng màu gốc (Primitive Color Tokens).
 *
 * Chứa toàn bộ giá trị màu thô được trích xuất từ thiết kế giao diện Dịu (Diu_Messenger_UI.html).
 * File này là "nguồn sự thật duy nhất" (Single Source of Truth) cho mọi mã màu trong app.
 *
 * ⚠️ Không sử dụng trực tiếp trong UI. Hãy dùng [AppColors] để truy cập màu theo ngữ cảnh
 * Light/Dark, hoặc [AppTheme] để truy cập nhanh trong Composable.
 *
 * Tương đương: app_palette.dart trong Flutter.
 */
object AppPalette {

    // ═══════════════════════════════════════════
    // ☀️  LIGHT MODE TOKENS
    // ═══════════════════════════════════════════

    /** Nền chính (be ấm nhẹ) */
    val LightBg = Color(0xFFFCF4EC)

    /** Mặt phẳng kính mờ — Surface glass 62% alpha (card, bottom nav, overlay) */
    val LightSurface = Color(0x9EFFFBF6)

    /** Mặt phẳng nổi — Surface elevated 85% alpha (composer, tray, notification bubble) */
    val LightSurface2 = Color(0xD9FEFBF7)

    /** Mực chính — Nâu trầm ấm (tiêu đề, nội dung chính) */
    val LightInk = Color(0xFF452F28)

    /** Mực phụ — (nội dung tin nhắn, phụ đề, icon phụ) */
    val LightInk2 = Color(0xFF876E64)

    /** Mực mờ — (placeholder, hint, icon vô hiệu) */
    val LightInk3 = Color(0xFFAF998E)

    /** Đường kẻ / viền — Divider, border 70% alpha */
    val LightLine = Color(0xB3E4D4C7)

    /** Điểm nhấn chính — Cam đào (CTA button, toggle on, active indicator) */
    val LightAccent = Color(0xFFDD8364)

    /** Chữ/icon trên nền accent */
    val LightOnAccent = Color(0xFFFFFBF5)

    // ═══════════════════════════════════════════
    // 🌙  DARK MODE TOKENS
    // ═══════════════════════════════════════════

    /** Nền chính tối (cà phê đậm ấm, không phải đen OLED) */
    val DarkBg = Color(0xFF211511)

    /** Mặt phẳng kính mờ tối — 55% alpha */
    val DarkSurface = Color(0x8C392A24)

    /** Mặt phẳng nổi tối — 90% alpha */
    val DarkSurface2 = Color(0xE6362722)

    /** Mực chính tối — Kem sáng */
    val DarkInk = Color(0xFFF7EDE2)

    /** Mực phụ tối — Be xám */
    val DarkInk2 = Color(0xFFC0AD9E)

    /** Mực mờ tối — Nâu xám */
    val DarkInk3 = Color(0xFF8F7C6F)

    /** Đường kẻ / viền tối — 60% alpha */
    val DarkLine = Color(0x995C4840)

    /** Điểm nhấn chính tối — Cam đào sáng nổi bật trên nền tối */
    val DarkAccent = Color(0xFFF2AB83)

    /** Chữ/icon trên nền accent tối */
    val DarkOnAccent = Color(0xFF271610)

    // ═══════════════════════════════════════════
    // 🍑  PEACH — Màu đào đặc trưng của Dịu
    // ═══════════════════════════════════════════

    /** Đào sáng (light mode) — Glow ấm, badge, indicator, bottom nav active */
    val Peach = Color(0xFFFFC29E)

    /** Đào trầm (dark mode) */
    val DarkPeach = Color(0xFFA15D3E)

    // ═══════════════════════════════════════════
    // 🌸  5 MOOD COLORS — Hệ màu cảm xúc cốt lõi
    // ═══════════════════════════════════════════

    /** Thương — Hồng phấn ấm (Soft Rose) */
    val MoodThuong = Color(0xFFF7A3A9)

    /** Vui — Vàng nắng hổ phách (Warm Amber) */
    val MoodVui = Color(0xFFF4CA84)

    /** Bình yên — Xanh xô thơm (Sage Green) */
    val MoodBinhYen = Color(0xFFA2D7AC)

    /** Nhớ — Tím oải hương (Lavender) */
    val MoodNho = Color(0xFFC5B3EA)

    /** Trầm — Xanh thanh bình (Soft Blue) */
    val MoodTram = Color(0xFF95C4E7)

    // ═══════════════════════════════════════════
    // 💬  CHAT — Màu trong cuộc hội thoại
    // ═══════════════════════════════════════════

    /** Chữ bong bóng tin nhắn "mình" (trên nền mood gradient) */
    val BubbleTextOnMood = Color(0xFF3A221A)

    /** Nền trích dẫn reply (light) — 45% alpha */
    val QuoteBgLight = Color(0x73FFFBF5)

    /** Nền trích dẫn reply (dark) — 25% alpha */
    val QuoteBgDark = Color(0x401E130F)

    // ═══════════════════════════════════════════
    // 📞  CALL — Cuộc gọi
    // ═══════════════════════════════════════════

    /** Nút kết thúc cuộc gọi (đỏ san hô ấm) */
    val CallEnd = Color(0xFFE6857E)

    /** Nút chấp nhận cuộc gọi (xanh lá dịu) */
    val CallAccept = Color(0xFF69BA7C)

    // ═══════════════════════════════════════════
    // ⚠️  DANGER / WARNING
    // ═══════════════════════════════════════════

    /** Màu cảnh báo / Chữ nguy hiểm (xoá, chặn, cuộc gọi nhỡ) */
    val Danger = Color(0xFFC25D58)

    /** Chấm ghi âm nhấp nháy */
    val RecDot = Color(0xFFE66E68)

    // ═══════════════════════════════════════════
    // 🖼️  VIEWER — Xem ảnh toàn màn hình
    // ═══════════════════════════════════════════

    /** Nền viewer tối (gần đen ấm) */
    val ViewerBg = Color(0xFF130B08)

    /** Chữ viewer sáng */
    val ViewerText = Color(0xFFF6F1EB)

    // ═══════════════════════════════════════════
    // 🌙  QUIET MODE — Chế độ yên tĩnh (tím oải hương)
    // ═══════════════════════════════════════════

    /** Tím yên tĩnh (banner, glow, card) */
    val QuietPurple = Color(0xFFC5B3EA)

    /** Vòng xoay dial yên tĩnh */
    val QuietRing = Color(0xFFBBAEE6)

    // ═══════════════════════════════════════════
    // 🔲  OVERLAY / SHEET — Lớp phủ
    // ═══════════════════════════════════════════

    /** Backdrop mờ khi mở Bottom Sheet — 28% alpha */
    val SheetBackdrop = Color(0x473F271E)

    // ═══════════════════════════════════════════
    // 📸  PHOTO — Ảnh & Label placeholder
    // ═══════════════════════════════════════════

    /** Nền label trên ảnh placeholder — 70% alpha */
    val PhotoLabelBg = Color(0xB3FFFBF5)

    /** Chữ label trên ảnh placeholder */
    val PhotoLabelText = Color(0xFF4D332B)

    // ═══════════════════════════════════════════
    // 🌅  AMBIENT — Gradient trời, glow, shadow
    // ═══════════════════════════════════════════

    /** Chữ avatar Orb — Trắng ấm 95% alpha */
    val OrbText = Color(0xF2FFFBF5)

    /** Ánh sáng ấm (OTP filled, mood check-in glow) */
    val WarmGlow = Color(0xFFF7D19C)

    /** Vàng trời (Sky ambient gradient) */
    val SkyWarmGold = Color(0xFFF4D29B)

    /** Gradient ấm onboarding */
    val OnbGradientWarm = Color(0xFFFFEBD2)

    /** Nền bóng đổ */
    val ShadowDark = Color(0xFF43251A)

    /** Badge thông báo sáng */
    val NotifBadgeLight = Color(0xFFFFBDB7)

    /** Khung điện thoại */
    val PhoneFrame = Color(0xFF372B26)
}
