package com.uzuu.diuchat.feature.base.presentation.compose.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.uzuu.diuchat.feature.base.common.res.AppTheme

@Composable
fun DiuMoodDot(
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 9.dp,
) {
    require(size.value.isFinite() && size > 0.dp) { "Dot size must be finite and positive" }
    Box(
        modifier = modifier
            .size(size)
            .drawWithCache {
                val center = Offset(this.size.width / 2, this.size.height / 2)
                val radius = this.size.minDimension
                val glow = Brush.radialGradient(
                    listOf(color.copy(alpha = 0.6f), color.copy(alpha = 0f)),
                    center,
                    radius,
                )
                onDrawBehind {
                    drawCircle(glow, radius, center)
                    drawCircle(color, radius / 2, center)
                }
            },
    )
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DiuMoodDotPreview() {
    AppTheme {
        Surface(color = AppTheme.colors.background) {
            val moods = AppTheme.colors.moods
            Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                listOf(moods.thuong, moods.vui, moods.binhyen, moods.nho, moods.tram).forEach {
                    DiuMoodDot(it, size = 16.dp)
                }
            }
        }
    }
}
