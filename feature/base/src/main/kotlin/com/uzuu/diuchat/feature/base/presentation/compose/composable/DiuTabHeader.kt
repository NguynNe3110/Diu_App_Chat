package com.uzuu.diuchat.feature.base.presentation.compose.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uzuu.diuchat.feature.base.common.res.AppTheme
import com.uzuu.diuchat.feature.base.common.res.DiuSerif

@Composable
fun DiuTabHeader(
    title: String,
    modifier: Modifier = Modifier,
    greeting: String? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier =
        modifier
            .fillMaxWidth()
            .padding(start = 22.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            if (!greeting.isNullOrBlank()) {
                Text(
                    text = greeting,
                    fontSize = 13.sp,
                    color = AppTheme.colors.inkSecondary,
                )
            }
            Text(
                text = title,
                fontSize = 34.sp,
                fontFamily = DiuSerif,
                fontWeight = FontWeight.Normal,
                color = AppTheme.colors.ink,
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = actions,
        )
    }
}
