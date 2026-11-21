package com.uzuu.diuchat.feature.base.presentation.compose.composable

import android.content.res.Configuration
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.uzuu.diuchat.feature.base.common.res.AppTheme

@Composable
fun DiuInviteCodeCard(
    code: String,
    label: String,
    copyLabel: String,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    DiuCard(modifier) {
        Text(
            text = label,
            style = AppTheme.typography.bodySmall,
            color = colors.inkSecondary,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = code,
                modifier = Modifier.weight(1f),
                style = AppTheme.typography.titleMedium,
                fontFamily = FontFamily.Monospace,
            )
            DiuIconButton(
                onClick = onCopy,
                contentDescription = copyLabel,
            ) {
                Icon(Icons.Outlined.ContentCopy, contentDescription = null)
            }
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DiuInviteCodeCardPreview() {
    AppTheme {
        Surface(color = AppTheme.colors.background) {
            DiuInviteCodeCard("DIU-7K2M", "Mã mời của bạn", "Sao chép mã mời", {}, Modifier.padding(16.dp))
        }
    }
}
