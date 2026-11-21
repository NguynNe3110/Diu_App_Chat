package com.uzuu.diuchat.feature.base.presentation.compose.composable

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.uzuu.diuchat.feature.base.common.res.AppTheme

@Composable
fun DiuTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    isError: Boolean = false,
    placeholder: String? = null,
    supportingText: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    val colors = AppTheme.colors
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = placeholder?.let { text -> { Text(text) } },
        modifier = modifier,
        enabled = enabled,
        singleLine = singleLine,
        isError = isError,
        supportingText = supportingText?.let { text -> { Text(text) } },
        keyboardOptions = keyboardOptions,
        shape = RoundedCornerShape(16.dp),
        textStyle = AppTheme.typography.bodyLarge,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = colors.ink,
            unfocusedTextColor = colors.ink,
            disabledTextColor = colors.ink.copy(alpha = 0.38f),
            focusedContainerColor = colors.surface,
            unfocusedContainerColor = colors.surface,
            disabledContainerColor = colors.surface.copy(alpha = 0.5f),
            focusedBorderColor = colors.accent,
            unfocusedBorderColor = colors.line,
            disabledBorderColor = colors.line.copy(alpha = 0.35f),
            focusedLabelColor = colors.accent,
            unfocusedLabelColor = colors.inkSecondary,
            disabledLabelColor = colors.inkTertiary,
            cursorColor = colors.accent,
            errorBorderColor = colors.danger,
            errorLabelColor = colors.danger,
        ),
    )
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DiuTextFieldPreview() {
    AppTheme {
        var name by remember { mutableStateOf("Lan") }
        Surface(color = AppTheme.colors.background) {
            DiuTextField(name, { name = it }, "Tên của bạn", Modifier.padding(16.dp))
        }
    }
}
