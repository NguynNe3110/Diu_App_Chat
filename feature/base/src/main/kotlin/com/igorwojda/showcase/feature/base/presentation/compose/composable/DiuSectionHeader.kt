package com.uzuu.diuchat.feature.base.presentation.compose.composable

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uzuu.diuchat.feature.base.common.res.AppTheme

@Composable
fun DiuSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            color = AppTheme.colors.inkTertiary,
            style = AppTheme.typography.labelMedium,
            letterSpacing = 1.sp,
        )
        action?.invoke()
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DiuSectionHeaderPreview() {
    AppTheme {
        Surface(color = AppTheme.colors.background) {
            DiuSectionHeader(
                title = "NGƯỜI THƯƠNG",
                action = { DiuTextButton("Xem tất cả", {}) },
            )
        }
    }
}
