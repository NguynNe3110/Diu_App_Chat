package com.uzuu.diuchat.feature.location.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuFilterChip
import com.uzuu.diuchat.feature.location.domain.model.LiveDuration

@Composable
fun LiveDurationPicker(
    selectedDuration: LiveDuration,
    onSelectDuration: (LiveDuration) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        LiveDuration.entries.forEach { duration ->
            DiuFilterChip(
                text = duration.label,
                selected = duration == selectedDuration,
                onClick = { onSelectDuration(duration) },
            )
        }
    }
}
