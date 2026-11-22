package com.uzuu.diuchat.feature.base.presentation.compose.composable

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun calculateRippleRingProgress(
    basePhase: Float,
    ringIndex: Int,
    ringCount: Int = 3,
): Float {
    val raw = (basePhase + ringIndex.toFloat() / ringCount.coerceAtLeast(1)) % 1f
    return if (raw < 0f) raw + 1f else raw
}

@Composable
fun DiuRippleOrb(
    initials: String,
    moodColor: Color,
    modifier: Modifier = Modifier,
    size: Dp = 148.dp,
    avatarUrl: String? = null,
) {
    val transition = rememberInfiniteTransition(label = "rippleOrb")
    val basePhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "rippleBasePhase",
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val minRadius = this.size.minDimension * 0.5f * 0.72f
            val maxRadius = this.size.minDimension * 0.5f * 0.98f
            val strokePx = 1.5.dp.toPx()

            for (ringIndex in 0 until 3) {
                val progress = calculateRippleRingProgress(basePhase = basePhase, ringIndex = ringIndex, ringCount = 3)
                val radius = minRadius + (maxRadius - minRadius) * progress
                val alpha = ((1f - progress) * 0.55f).coerceIn(0f, 1f)
                drawCircle(
                    color = moodColor.copy(alpha = alpha),
                    radius = radius,
                    style = Stroke(width = strokePx),
                )
            }
        }

        DiuOrbAvatar(
            initials = initials,
            moodColor = moodColor,
            size = size * 0.72f,
            avatarUrl = avatarUrl,
            isPulsing = true,
        )
    }
}
