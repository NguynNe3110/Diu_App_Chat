package com.uzuu.diuchat.feature.base.presentation.compose.composable

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uzuu.diuchat.feature.base.common.res.AppTheme

@Composable
fun DiuFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = AppTheme.colors
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text, fontSize = 14.sp) },
        modifier = modifier,
        enabled = enabled,
        shape = RoundedCornerShape(18.dp),
        colors =
        FilterChipDefaults.filterChipColors(
            containerColor = Color.Transparent,
            labelColor = colors.inkSecondary,
            selectedContainerColor = colors.ink,
            selectedLabelColor = colors.background,
            disabledContainerColor = Color.Transparent,
            disabledLabelColor = colors.inkTertiary,
        ),
        border =
        FilterChipDefaults.filterChipBorder(
            enabled = enabled,
            selected = selected,
            borderColor = colors.line,
            selectedBorderColor = Color.Transparent,
            disabledBorderColor = colors.line.copy(alpha = 0.35f),
            borderWidth = 1.dp,
            selectedBorderWidth = 0.dp,
        ),
    )
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DiuFilterChipPreview() {
    AppTheme {
        var selected by remember { mutableStateOf(true) }
        Surface(color = AppTheme.colors.background) {
            DiuFilterChip("Tất cả", selected, { selected = !selected })
        }
    }
}
