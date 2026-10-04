package com.uzuu.onboarding.presentation.composable

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uzuu.diuchat.feature.base.common.res.AppPalette
import com.uzuu.diuchat.feature.base.common.res.AppTheme

// ─── Màu 5 cảm xúc ───────────────────────────────────────────────────────────
private data class MoodItem(val name: String, val color: Color)

private val moods = listOf(
    MoodItem("Thương", AppPalette.MoodThuong),
    MoodItem("Vui", AppPalette.MoodVui),
    MoodItem("Bình yên", AppPalette.MoodBinhYen),
    MoodItem("Nhớ", AppPalette.MoodNho),
    MoodItem("Trầm", AppPalette.MoodTram),
)

// ─── Orb đơn giản (quả cầu phát sáng) ───────────────────────────────────────
@Composable
private fun MiniOrb(
    initial: String,
    color: Color,
    size: Dp,
    floatOffsetY: Dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(size)
            .offset(y = floatOffsetY)
            .shadow(elevation = 0.dp, shape = CircleShape)
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        color.copy(alpha = 0.5f),
                        color,
                        color.copy(alpha = 0.8f),
                    ),
                ),
            )
            .border(
                width = 0.5.dp,
                color = color.copy(alpha = 0.3f),
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initial,
            color = Color.White.copy(alpha = 0.9f),
            fontSize = (size.value * 0.3f).sp,
            textAlign = TextAlign.Center,
        )
    }
}

// ─── Slide 0: Chòm sao Orb bồng bềnh ─────────────────────────────────────────
@Composable
private fun Slide0Illustration() {
    val infiniteTransition = rememberInfiniteTransition(label = "orb_float")

    val float1 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = -14f,
        animationSpec = infiniteRepeatable(tween(3500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "f1",
    )
    val float2 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = -10f,
        animationSpec = infiniteRepeatable(tween(4200, 800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "f2",
    )
    val float3 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = -12f,
        animationSpec = infiniteRepeatable(tween(5000, 1600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "f3",
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp),
    ) {
        // Orb L — Vui — to nhất, góc trái
        MiniOrb(
            initial = "L",
            color = AppPalette.MoodVui,
            size = 110.dp,
            floatOffsetY = float1.dp,
            modifier = Modifier.align(Alignment.TopStart).offset(x = 40.dp, y = 60.dp),
        )
        // Orb M — Thương — cỡ vừa, giữa trên
        MiniOrb(
            initial = "M",
            color = AppPalette.MoodThuong,
            size = 82.dp,
            floatOffsetY = float2.dp,
            modifier = Modifier.align(Alignment.TopCenter).offset(x = 30.dp, y = 20.dp),
        )
        // Orb N — Bình yên — nhỏ, phải
        MiniOrb(
            initial = "N",
            color = AppPalette.MoodBinhYen,
            size = 62.dp,
            floatOffsetY = float3.dp,
            modifier = Modifier.align(Alignment.TopEnd).offset(x = (-24).dp, y = 140.dp),
        )
    }
}

// ─── Slide 1: 5 sắc cảm xúc xếp dọc so le ───────────────────────────────────
@Composable
private fun Slide1Illustration() {
    val infiniteTransition = rememberInfiniteTransition(label = "mood_glow")
    val glow by infiniteTransition.animateFloat(
        initialValue = 0.55f, targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glow",
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 36.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        moods.forEachIndexed { index, mood ->
            val isEven = index % 2 == 1
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (isEven) Modifier.padding(start = 48.dp) else Modifier),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                // Viên màu phát sáng
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(mood.color.copy(alpha = glow)),
                )
                Text(
                    text = mood.name,
                    fontSize = 22.sp,
                    fontStyle = FontStyle.Italic,
                    color = AppTheme.colors.ink,
                )
            }
        }
    }
}

// ─── Slide 2: Phong thư + Mặt trăng ─────────────────────────────────────────
@Composable
private fun Slide2Illustration() {
    val infiniteTransition = rememberInfiniteTransition(label = "quiet_float")
    val floatEnv by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = -12f,
        animationSpec = infiniteRepeatable(tween(4000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "env",
    )
    val floatMoon by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = -10f,
        animationSpec = infiniteRepeatable(tween(3200, 600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "moon",
    )
    val peach = AppPalette.Peach
    val quietPurple = AppPalette.QuietPurple

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp),
    ) {
        // Hình tròn phong thư ấm (lớn)
        Box(
            modifier = Modifier
                .size(170.dp)
                .offset(x = 44.dp, y = (52 + floatEnv).dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFF5E8D5),
                            peach,
                        ),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "✉", fontSize = 44.sp)
        }

        // Hình tròn mặt trăng (nhỏ, góc phải)
        Box(
            modifier = Modifier
                .size(86.dp)
                .align(Alignment.TopEnd)
                .offset(x = (-24).dp, y = (24 + floatMoon).dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            quietPurple.copy(alpha = 0.4f),
                            quietPurple.copy(alpha = 0.8f),
                        ),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "🌙", fontSize = 28.sp)
        }
    }
}

// ─── Public: Dispatcher theo page ─────────────────────────────────────────────
@Composable
fun OnBoardingIllustration(page: Int, modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        when (page) {
            0 -> Slide0Illustration()
            1 -> Slide1Illustration()
            2 -> Slide2Illustration()
        }
    }
}
