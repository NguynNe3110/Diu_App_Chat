package com.uzuu.diuchat.feature.base.presentation.compose.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.uzuu.diuchat.feature.base.common.res.AppTheme

@Composable
fun DiuEmptyState(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    illustration: (@Composable () -> Unit)? = null,
    action: (@Composable () -> Unit)? = null,
) {
    val colors = AppTheme.colors
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        illustration?.invoke()
        Text(
            text = title,
            style = AppTheme.typography.headlineSmall,
            color = colors.ink,
            textAlign = TextAlign.Center,
        )
        Text(
            text = description,
            style = AppTheme.typography.bodyMedium,
            color = colors.inkSecondary,
            textAlign = TextAlign.Center,
        )
        action?.invoke()
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DiuEmptyStatePreview() {
    AppTheme {
        Surface(color = AppTheme.colors.background) {
            DiuEmptyState(
                title = "Chưa có lời nhắn nào",
                description = "Mời người thương để bắt đầu trò chuyện.",
                action = { DiuButton(onClick = {}) { Text("Mời người thương") } },
            )
        }
    }
}
