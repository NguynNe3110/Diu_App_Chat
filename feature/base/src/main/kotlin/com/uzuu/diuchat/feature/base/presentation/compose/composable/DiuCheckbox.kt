package com.uzuu.diuchat.feature.base.presentation.compose.composable

import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.uzuu.diuchat.feature.base.common.res.AppTheme

@Composable
fun DiuCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = AppTheme.colors
    Checkbox(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier.semantics { this.contentDescription = contentDescription },
        enabled = enabled,
        colors = CheckboxDefaults.colors(
            checkedColor = colors.ink,
            uncheckedColor = colors.line,
            checkmarkColor = colors.background,
            disabledCheckedColor = colors.ink.copy(alpha = 0.35f),
            disabledUncheckedColor = colors.line.copy(alpha = 0.35f),
        ),
    )
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DiuCheckboxPreview() {
    AppTheme {
        var checked by remember { mutableStateOf(false) }
        Surface(color = AppTheme.colors.background) {
            DiuCheckbox(checked, { checked = it }, "Chọn người thương")
        }
    }
}
