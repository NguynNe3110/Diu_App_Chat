package com.uzuu.diuchat.feature.base.presentation.compose.composable

import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.uzuu.diuchat.feature.base.common.res.AppTheme

@Composable
fun DiuRadioButton(
    selected: Boolean,
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = AppTheme.colors
    RadioButton(
        selected = selected,
        onClick = onClick,
        modifier = modifier.semantics { this.contentDescription = contentDescription },
        enabled = enabled,
        colors = RadioButtonDefaults.colors(
            selectedColor = colors.accent,
            unselectedColor = colors.line,
            disabledSelectedColor = colors.accent.copy(alpha = 0.35f),
            disabledUnselectedColor = colors.line.copy(alpha = 0.35f),
        ),
    )
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DiuRadioButtonPreview() {
    AppTheme {
        Surface(color = AppTheme.colors.background) {
            DiuRadioButton(true, {}, "Âm thanh mặc định")
        }
    }
}
