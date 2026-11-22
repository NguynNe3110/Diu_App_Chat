package com.uzuu.diuchat.feature.settings.presentation.composable

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uzuu.diuchat.feature.base.common.res.AppIcons
import com.uzuu.diuchat.feature.base.common.res.AppPalette
import com.uzuu.diuchat.feature.base.common.res.AppTheme
import com.uzuu.diuchat.feature.base.common.res.DiuSerif
import com.uzuu.diuchat.feature.settings.domain.model.QuietSchedule

@Composable
fun QuietHoursDial(
    schedule: QuietSchedule,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val startAngle = (schedule.startMinuteOfDay / 1440f) * 360f - 90f
    val sweepAngle = (schedule.durationMinutes / 1440f) * 360f

    Box(
        modifier = modifier.size(230.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = 18.dp.toPx()
            val inset = strokePx / 2f + 16.dp.toPx()
            val arcSize = Size(size.width - inset * 2f, size.height - inset * 2f)
            val topLeft = Offset(inset, inset)

            // Track circle
            drawArc(
                color = colors.line.copy(alpha = 0.45f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round),
            )

            // Active lavender arc
            if (schedule.enabled && sweepAngle > 0f) {
                drawArc(
                    color = AppPalette.QuietPurple,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round),
                )
            }
        }

        // 24-hour markers (0, 6, 12, 18)
        Text(
            text = "0",
            fontSize = 11.sp,
            color = colors.inkTertiary,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 0.dp),
        )
        Text(
            text = "6",
            fontSize = 11.sp,
            color = colors.inkTertiary,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = 0.dp),
        )
        Text(
            text = "12",
            fontSize = 11.sp,
            color = colors.inkTertiary,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = 0.dp),
        )
        Text(
            text = "18",
            fontSize = 11.sp,
            color = colors.inkTertiary,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = 0.dp),
        )

        // Inner circular well
        Box(
            modifier = Modifier
                .size(144.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            AppPalette.QuietPurple.copy(alpha = 0.28f),
                            colors.surfaceElevated,
                        ),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    painter = painterResource(AppIcons.Moon),
                    contentDescription = null,
                    tint = colors.ink,
                    modifier = Modifier.size(24.dp),
                )
                Text(
                    text = "${schedule.startTimeFormatted} – ${schedule.endTimeFormatted}",
                    fontFamily = DiuSerif,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Normal,
                    color = colors.ink,
                )
                Text(
                    text = schedule.durationHoursFormatted,
                    fontSize = 12.5.sp,
                    color = colors.inkSecondary,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun QuietHoursDialPreview() {
    AppTheme {
        Surface(color = AppTheme.colors.background) {
            QuietHoursDial(
                schedule = QuietSchedule(),
                modifier = Modifier.padding(24.dp),
            )
        }
    }
}
