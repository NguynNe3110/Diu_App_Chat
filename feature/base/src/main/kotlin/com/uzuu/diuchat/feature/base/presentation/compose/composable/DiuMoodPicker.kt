package com.uzuu.diuchat.feature.base.presentation.compose.composable

import android.content.res.Configuration
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.uzuu.diuchat.feature.base.common.res.AppTheme

@Composable
fun DiuMoodPicker(
    options: List<DiuMoodOption>,
    selectedId: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { option ->
            val isSelected = option.id == selectedId
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(option.id) },
                enabled = enabled,
                label = { Text(option.label) },
                leadingIcon = { DiuMoodDot(option.color) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = option.color.copy(alpha = 0.3f),
                    selectedLabelColor = AppTheme.colors.ink,
                    labelColor = AppTheme.colors.inkSecondary,
                ),
            )
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DiuMoodPickerPreview() {
    AppTheme {
        var selected by remember { mutableStateOf<String?>("thuong") }
        val moods = AppTheme.colors.moods
        Surface(color = AppTheme.colors.background) {
            DiuMoodPicker(
                options = listOf(
                    DiuMoodOption("thuong", "Thương", moods.thuong),
                    DiuMoodOption("vui", "Vui", moods.vui),
                    DiuMoodOption("binhyen", "Bình yên", moods.binhyen),
                    DiuMoodOption("nho", "Nhớ", moods.nho),
                    DiuMoodOption("tram", "Trầm", moods.tram),
                ),
                selectedId = selected,
                onSelect = { selected = it },
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}
