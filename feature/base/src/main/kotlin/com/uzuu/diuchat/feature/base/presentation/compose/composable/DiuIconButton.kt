package com.uzuu.diuchat.feature.base.presentation.compose.composable

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.uzuu.diuchat.feature.base.common.res.AppTheme

@Composable
fun DiuIconButton(
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    soft: Boolean = false,
    danger: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colors = AppTheme.colors
    val contentColor = when {
        danger -> colors.danger
        else -> colors.inkSecondary
    }

    IconButton(
        onClick = onClick,
        modifier = modifier
            .sizeIn(minWidth = 44.dp, minHeight = 44.dp)
            .semantics { this.contentDescription = contentDescription },
        enabled = enabled,
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = if (soft) colors.surface else Color.Transparent,
            contentColor = contentColor,
            disabledContentColor = colors.inkTertiary,
        ),
    ) {
        content()
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DiuIconButtonPreview() {
    AppTheme {
        Surface(color = AppTheme.colors.background) {
            Row {
                for (enabled in listOf(true, false)) {
                    DiuIconButton({}, "Thêm", enabled = enabled, soft = true) {
                        Text("+")
                    }
                }
            }
        }
    }
}
