package com.uzuu.diuchat.feature.location.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.uzuu.diuchat.feature.base.common.res.AppIcons
import com.uzuu.diuchat.feature.base.common.res.AppTheme

@Composable
fun AccuracyRadiusIndicator(
    accuracyMeters: Float,
    modifier: Modifier = Modifier,
    label: String? = null,
) {
    val colors = AppTheme.colors
    val displayAccuracy = accuracyMeters.toInt().coerceAtLeast(1)
    val text = label ?: "Chính xác trong bán kính ±${displayAccuracy}m"

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surface)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            painter = painterResource(AppIcons.Location),
            contentDescription = null,
            tint = colors.accent,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = text,
            style = AppTheme.typography.bodySmall,
            color = colors.inkSecondary,
        )
    }
}
