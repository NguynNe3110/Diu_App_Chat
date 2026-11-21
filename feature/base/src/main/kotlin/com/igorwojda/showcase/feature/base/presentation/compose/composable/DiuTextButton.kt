package com.uzuu.diuchat.feature.base.presentation.compose.composable

import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.uzuu.diuchat.feature.base.common.res.AppTheme

@Composable
fun DiuTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = AppTheme.colors
    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.textButtonColors(
            contentColor = colors.inkSecondary,
            disabledContentColor = colors.inkTertiary,
        ),
    ) {
        Text(text, style = AppTheme.typography.labelLarge)
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DiuTextButtonPreview() {
    AppTheme {
        Surface(color = AppTheme.colors.background) {
            DiuTextButton("Để sau cũng được", {})
        }
    }
}
