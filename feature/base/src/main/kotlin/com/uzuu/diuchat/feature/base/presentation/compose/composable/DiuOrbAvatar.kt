package com.uzuu.diuchat.feature.base.presentation.compose.composable

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.uzuu.diuchat.feature.base.R
import com.uzuu.diuchat.feature.base.common.res.AppPalette
import com.uzuu.diuchat.feature.base.common.res.AppTheme
import com.uzuu.diuchat.feature.base.common.res.DiuSerif

private const val HEX_RGB_LENGTH = 6
private const val HEX_ARGB_LENGTH = 8
private const val HEX_RADIX = 16
private const val OPAQUE_ALPHA_MASK = 0xFF000000L

fun validateOrbSize(sizeDp: Float) {
    require(sizeDp.isFinite() && sizeDp > 0f) { "Avatar size must be finite and positive" }
}

fun parseMoodColorHex(
    hex: String?,
    fallback: Color = AppPalette.MoodThuong,
): Color {
    val cleaned = hex?.trim()?.removePrefix("#").orEmpty()
    val parsedColor = when (cleaned.length) {
        HEX_RGB_LENGTH -> {
            cleaned.toLongOrNull(HEX_RADIX)?.let { rgb ->
                Color(OPAQUE_ALPHA_MASK or rgb)
            }
        }

        HEX_ARGB_LENGTH -> {
            cleaned.toLongOrNull(HEX_RADIX)?.let { argb ->
                Color(argb)
            }
        }

        else -> {
            null
        }
    }
    return parsedColor ?: fallback
}

@Composable
fun DiuOrbAvatar(
    initials: String,
    moodColor: Color,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    floatOffsetY: Dp = 0.dp,
    contentDescription: String? = null,
    avatarUrl: String? = null,
    badgeText: String? = null,
    isPulsing: Boolean = false,
    content: (@Composable () -> Unit)? = null,
) {
    validateOrbSize(size.value)

    val pulseScale = if (isPulsing) {
        val transition = rememberInfiniteTransition(label = "orbPulse")
        val scale by transition.animateFloat(
            initialValue = 1.0f,
            targetValue = 1.04f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2500),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "orbPulseScale",
        )
        scale
    } else {
        1.0f
    }

    Box(
        modifier =
        modifier
            .size(size)
            .offset(y = floatOffsetY)
            .graphicsLayer {
                scaleX = pulseScale
                scaleY = pulseScale
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
            Modifier
                .fillMaxSize()
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
            } else if (!avatarUrl.isNullOrBlank()) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = contentDescription,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Text(
                    text = initials,
                    color = AppTheme.colors.orbText,
                    style = AppTheme.typography.titleMedium,
                    fontFamily = DiuSerif,
                    fontSize = (size.value * 0.4f).sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        if (!badgeText.isNullOrBlank()) {
            Surface(
                modifier =
                Modifier
                    .align(Alignment.BottomEnd)
                    .defaultMinSize(minWidth = 22.dp, minHeight = 22.dp),
                shape = CircleShape,
                color = AppTheme.colors.ink,
                border = BorderStroke(2.dp, AppTheme.colors.background),
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = badgeText,
                        color = AppTheme.colors.background,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                    )
                }
            }
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
                DiuOrbAvatar("N", AppTheme.colors.moods.vui, size = 72.dp, badgeText = "4", isPulsing = true)
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
