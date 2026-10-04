package com.uzuu.diuchat.feature.base.presentation.compose.composable

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.uzuu.diuchat.feature.base.common.res.AppPalette
import com.uzuu.diuchat.feature.base.common.res.AppTheme

// ─────────────────────────────────────────────────────────────────────────────
// DiuAmbientBackground
//
// Kỹ thuật: "Soft Ambient Blobs" — 3 lớp xếp chồng:
//
//  ┌──────────────────────────────────────┐
//  │  Lớp 3: Content (các màn hình con)  │  ← trong suốt, thấy xuyên qua
//  ├──────────────────────────────────────┤
//  │  Lớp 2: Frosted Glass Overlay        │  ← background.copy(alpha ≈ 0.52f)
//  ├──────────────────────────────────────┤
//  │  Lớp 1: Ambient Color Blobs (Canvas) │  ← Brush.radialGradient color→transparent
//  ├──────────────────────────────────────┤
//  │  Lớp 0: Nền solid background         │  ← màu cứng phía sau cùng
//  └──────────────────────────────────────┘
//
// Tại sao KHÔNG dùng Modifier.blur():
//   - Cần API 31+, không tương thích rộng.
//   - Brush.radialGradient (color → transparent) với bán kính lớn
//     cho hiệu ứng tương tự nhưng nhẹ hơn, chạy mọi API.
//
// Cách dùng:
//   DiuAmbientBackground {
//       // nội dung màn hình, background tự động trong suốt
//   }
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Background toàn màn hình của app Dịu.
 * Đặt 1 lần duy nhất trong MainShowcaseScreen / Activity, các màn hình con
 * chỉ cần để background = Color.Transparent để "thấy xuyên qua".
 */
@Composable
fun DiuAmbientBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {},
) {
    val colors = AppTheme.colors

    Box(modifier = modifier.fillMaxSize()) {

        // ── Lớp 0: Nền solid ────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background),
        )

        // ── Lớp 1: Ambient Color Blobs ──────────────────────────────────────
        // Kỹ thuật: radialGradient (màu → transparent) bán kính lớn
        // → tự nhiên mờ dần ra ngoài mà không cần blur thật
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Blob 1 — Peach ấm — góc phải trên
            // Giống ảnh chụp: blob cam đào góc phải
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        AppPalette.Peach,
                        AppPalette.Peach.copy(alpha = 0.48f),
                        Color.Transparent,
                    ),
                    radius = w * 0.65f,
                    center = Offset(x = w * 0.25f, y = h * 0.12f,),
                ),
                radius = w * 1.4f,
                center = Offset(w * 0.25f, h * 0.12f),
            )

            // Blob 2 — Thuong (Rose) — góc phải giữa
            // Giống ảnh chụp: blob hồng nhạt bên phải giữa màn hình
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        AppPalette.MoodThuong.copy(alpha = 0.8f),
                        AppPalette.MoodThuong.copy(alpha = 0.42f),
                        Color.Transparent,
                    ),
                    center = Offset(
                        x = w * 0.95f,
                        y = h * 0.56f,
                    ),
                    radius = w * 0.56f,
                ),
                radius = w * 0.8f,
                center = Offset(w * 0.95f, h * 0.56f),
            )

            // Blob 3 — BinhYen (Sage Green) — góc trái dưới — nhẹ nhất
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        AppPalette.MoodVui,
                        Color.Transparent,
                    ),
                    center = Offset(w * 0.15f, h * 0.88f),
                    radius = w * 0.6f,
                ),
                radius = w * 0.6f,
                center = Offset(w * 0.15f, h * 0.88f),
            )
        }

        // ── Lớp 2: Frosted Glass Overlay ────────────────────────────────────
        // Lớp kính mờ đục phủ lên blobs, làm dịu màu sắc xuống
        // → tạo cảm giác "nhìn xuyên kính sữa" như ảnh mẫu
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background.copy(alpha = 0.35f)),
        )

        // ── Lớp 3: Content ──────────────────────────────────────────────────
        content()
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Previews — Light & Dark
// ─────────────────────────────────────────────────────────────────────────────

@Preview(
    name = "Diu Ambient Background — Light",
    showBackground = true,
    widthDp = 392,
    heightDp = 836,
)
@Composable
private fun DiuAmbientBackgroundLightPreview() {
    AppTheme(darkTheme = false) {
        DiuAmbientBackground()
    }
}

@Preview(
    name = "Diu Ambient Background — Dark",
    showBackground = true,
    widthDp = 392,
    heightDp = 836,
    backgroundColor = 0xFF211511,
)
@Composable
private fun DiuAmbientBackgroundDarkPreview() {
    AppTheme(darkTheme = true) {
        DiuAmbientBackground()
    }
}
