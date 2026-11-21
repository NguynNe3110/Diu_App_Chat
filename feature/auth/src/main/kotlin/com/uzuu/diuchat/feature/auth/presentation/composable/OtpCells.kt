package com.uzuu.diuchat.feature.auth.presentation.composable

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uzuu.diuchat.feature.base.common.res.AppTheme
import com.uzuu.diuchat.feature.base.common.res.DiuSerif

@Composable
fun OtpCells(
    code: String,
    modifier: Modifier = Modifier,
    cellCount: Int = 6,
) {
    val colors = AppTheme.colors
    val moodList =
        listOf(
            colors.moods.thuong,
            colors.moods.vui,
            colors.moods.binhyen,
            colors.moods.nho,
            colors.moods.tram,
            colors.moods.thuong,
        )

    val shape = RoundedCornerShape(20.dp)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        for (i in 0 until cellCount) {
            val isFilled = i < code.length
            val isCurrent = i == code.length
            val digit = if (isFilled) code[i].toString() else ""
            val moodColor = moodList[i % moodList.size]

            val targetBg = if (isFilled) moodColor.copy(alpha = 0.40f) else colors.surface
            val bgColor by animateColorAsState(
                targetValue = targetBg,
                animationSpec = tween(durationMillis = 250),
                label = "otpBg_$i",
            )

            val borderStroke =
                when {
                    isFilled -> BorderStroke(1.5.dp, moodColor.copy(alpha = 0.25f))
                    isCurrent -> BorderStroke(1.5.dp, colors.accent)
                    else -> BorderStroke(1.5.dp, colors.line)
                }

            val shadowModifier =
                if (isFilled) {
                    Modifier.shadow(elevation = 6.dp, shape = shape, spotColor = moodColor, ambientColor = moodColor)
                } else {
                    Modifier
                }

            Box(
                modifier =
                Modifier
                    .weight(1f)
                    .height(62.dp)
                    .then(shadowModifier)
                    .clip(shape)
                    .background(bgColor)
                    .border(borderStroke, shape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = digit,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = DiuSerif,
                    color = colors.ink,
                )
            }
        }
    }
}
