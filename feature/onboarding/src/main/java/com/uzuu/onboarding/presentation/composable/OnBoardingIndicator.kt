package com.uzuu.onboarding.presentation.composable

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.uzuu.diuchat.feature.base.common.res.AppTheme

/**
 * Bộ chấm chỉ số trang kiểu "pill indicator":
 * - Chấm active: hình thuốc rộng 26dp, màu ink đậm.
 * - Chấm bình thường: tròn 8dp, màu line mờ.
 * - Bấm vào từng chấm để nhảy tới trang tương ứng.
 */
@Composable
fun OnBoardingIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
    onDotClick: (Int) -> Unit = {},
) {
    val colors = AppTheme.colors

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { index ->
            val isActive = index == currentPage
            val dotWidth by animateDpAsState(
                targetValue = if (isActive) 26.dp else 8.dp,
                animationSpec = tween(durationMillis = 400),
                label = "dotWidth_$index",
            )
            val dotColor = if (isActive) colors.ink else colors.line

            Box(
                modifier = Modifier
                    .width(dotWidth)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(dotColor)
                    .clickable { onDotClick(index) },
            )
        }
    }
}
