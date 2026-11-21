package com.uzuu.diuchat.feature.base.presentation.compose.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uzuu.diuchat.feature.base.R
import com.uzuu.diuchat.feature.base.common.res.AppTheme

@Composable
fun DiuOrbAvatar(
    initials: String,
    moodColor: Color,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    floatOffsetY: Dp = 0.dp,
    contentDescription: String? = null,
    content: (@Composable () -> Unit)? = null,
) {
    require(size.value.isFinite() && size > 0.dp) { "Avatar size must be finite and positive" }

    Box(
        modifier =
        modifier
            .size(size)
            .offset(y = floatOffsetY)
            .drawWithCache {
                val center = Offset(this.size.width / 2f, this.size.height / 2f)
                val radius = this.size.minDimension * 0.9f
                val glow =
                    Brush.radialGradient(
                        0f to moodColor.copy(alpha = 0.35f),
                        0.5f to moodColor.copy(alpha = 0.24f),
                        1f to moodColor.copy(alpha = 0f),
                        center = center,
                        radius = radius,
                    )
                onDrawBehind {
                    drawCircle(brush = glow, radius = radius, center = center)
                }
            }.clip(CircleShape)
            .drawWithCache {
                val brush =
                    Brush.radialGradient(
                        0f to lerp(moodColor, Color.White, 0.7f),
                        0.48f to moodColor,
                        1f to lerp(moodColor, Color.Black, 0.12f),
                        center = Offset(this.size.width * 0.34f, this.size.height * 0.28f),
                        radius = this.size.minDimension * 0.8f,
                    )
                onDrawBehind { drawRect(brush) }
            }.clearAndSetSemantics {
                contentDescription?.let { this.contentDescription = it }
            },
        contentAlignment = Alignment.Center,
    ) {
        if (content != null) {
            content()
        } else {
            Text(
                text = initials,
                color = AppTheme.colors.orbText,
                style = AppTheme.typography.titleMedium,
                fontFamily = FontFamily.Serif,
                fontSize = (size.value * 0.4f).sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DiuOrbAvatarPreview() {
    AppTheme {
        Surface(color = AppTheme.colors.background) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                DiuOrbAvatar("L", AppTheme.colors.moods.thuong, contentDescription = "Lan")
                DiuOrbAvatar("", AppTheme.colors.moods.binhyen) {
                    Icon(
                        painter = painterResource(R.drawable.ic_moon),
                        contentDescription = null,
                        tint = AppTheme.colors.orbText,
                    )
                }
            }
        }
    }
}
