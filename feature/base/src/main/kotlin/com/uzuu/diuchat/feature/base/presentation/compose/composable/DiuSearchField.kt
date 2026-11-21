package com.uzuu.diuchat.feature.base.presentation.compose.composable

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
fun DiuSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    placeholder: String? = null,
    onClear: (() -> Unit)? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
) {
    val colors = AppTheme.colors
    val effectiveLeadingIcon: (@Composable () -> Unit)? =
        leadingIcon ?: {
            Icon(Icons.Outlined.Search, contentDescription = null, tint = colors.inkSecondary)
        }
    val effectiveTrailingIcon: (@Composable () -> Unit)? =
        trailingIcon ?: if (value.isNotEmpty() && onClear != null) {
            {
                IconButton(onClick = onClear) {
                    Icon(Icons.Outlined.Close, contentDescription = "Xóa tìm kiếm", tint = colors.inkSecondary)
                }
            }
        } else {
            null
        }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        singleLine = true,
        placeholder = { Text(placeholder ?: label) },
        leadingIcon = effectiveLeadingIcon,
        trailingIcon = effectiveTrailingIcon,
        shape = RoundedCornerShape(26.dp),
        textStyle = AppTheme.typography.bodyLarge,
        colors =
        OutlinedTextFieldDefaults.colors(
            focusedTextColor = colors.ink,
            unfocusedTextColor = colors.ink,
            focusedContainerColor = colors.surface,
            unfocusedContainerColor = colors.surface,
            focusedBorderColor = colors.accent,
            unfocusedBorderColor = colors.line,
            focusedPlaceholderColor = colors.inkTertiary,
            unfocusedPlaceholderColor = colors.inkTertiary,
            cursorColor = colors.accent,
            focusedLeadingIconColor = colors.inkSecondary,
            unfocusedLeadingIconColor = colors.inkSecondary,
            focusedTrailingIconColor = colors.inkSecondary,
            unfocusedTrailingIconColor = colors.inkSecondary,
        ),
    )
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DiuSearchFieldPreview() {
    AppTheme {
        var query by remember { mutableStateOf("") }
        Surface(color = AppTheme.colors.background) {
            DiuSearchField(
                value = query,
                onValueChange = { query = it },
                label = "Tìm người thân",
                onClear = { query = "" },
                modifier =
                Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
            )
        }
    }
}
