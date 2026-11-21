package com.uzuu.diuchat.feature.location.presentation

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.uzuu.diuchat.feature.base.common.res.AppTheme

@Suppress("UnusedParameter")
@Composable
fun DiuMapPreview(
    modifier: Modifier = Modifier,
    latitude: Double = 21.0285,
    longitude: Double = 105.8542,
    accuracyMeters: Float = 12f,
    isLive: Boolean = false,
) {
    val colors = AppTheme.colors
    val pulseTransition = rememberInfiniteTransition(label = "RadarPulse")
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "PulseScale",
    )
    val pulseAlpha by pulseTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "PulseAlpha",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(240.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(colors.surface),
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val center = Offset(canvasWidth / 2f, canvasHeight / 2f)

            // Draw map background roads/grid lines
            val gridSpacing = 40.dp.toPx()
            var x = 0f
            while (x < canvasWidth) {
                drawLine(
                    color = colors.line.copy(alpha = 0.5f),
                    start = Offset(x, 0f),
                    end = Offset(x, canvasHeight),
                    strokeWidth = 1.dp.toPx(),
                )
                x += gridSpacing
            }
            var y = 0f
            while (y < canvasHeight) {
                drawLine(
                    color = colors.line.copy(alpha = 0.5f),
                    start = Offset(0f, y),
                    end = Offset(canvasWidth, y),
                    strokeWidth = 1.dp.toPx(),
                )
                y += gridSpacing
            }

            // Draw accent road diagonal
            drawLine(
                color = colors.line.copy(alpha = 0.9f),
                start = Offset(0f, canvasHeight * 0.7f),
                end = Offset(canvasWidth, canvasHeight * 0.3f),
                strokeWidth = 6.dp.toPx(),
            )
            drawLine(
                color = colors.background,
                start = Offset(0f, canvasHeight * 0.7f),
                end = Offset(canvasWidth, canvasHeight * 0.3f),
                strokeWidth = 4.dp.toPx(),
            )

            // Accuracy radius circle
            val baseRadius = (accuracyMeters.coerceIn(10f, 100f) * 1.5f).dp.toPx()
            drawCircle(
                color = colors.accent.copy(alpha = 0.15f),
                radius = baseRadius,
                center = center,
            )
            drawCircle(
                color = colors.accent.copy(alpha = 0.4f),
                radius = baseRadius,
                center = center,
                style = Stroke(
                    width = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f),
                ),
            )

            // Pulsing live radar wave
            if (isLive) {
                val waveRadius = baseRadius * (1f + pulseScale)
                drawCircle(
                    color = colors.accent.copy(alpha = pulseAlpha),
                    radius = waveRadius,
                    center = center,
                    style = Stroke(width = 2.dp.toPx()),
                )
            }

            // Marker Pin / Dot
            drawCircle(
                color = colors.background,
                radius = 12.dp.toPx(),
                center = center,
            )
            drawCircle(
                color = colors.accent,
                radius = 9.dp.toPx(),
                center = center,
            )
            drawCircle(
                color = Color.White,
                radius = 3.5.dp.toPx(),
                center = center,
            )
        }
    }
}
