package com.uzuu.diuchat.feature.base.presentation.compose.composable

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.uzuu.diuchat.feature.base.common.res.AppTheme

@Composable
fun DiuPersonRow(
    name: String,
    initials: String,
    moodColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    timestamp: String? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val colors = AppTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        DiuOrbAvatar(initials, moodColor, floatOffsetY = 1.dp)
        Column(Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = name,
                    style = AppTheme.typography.titleMedium,
                    color = colors.ink,
                )
                timestamp?.let {
                    Text(
                        text = it,
                        style = AppTheme.typography.bodySmall,
                        color = colors.inkTertiary,
                    )
                }
            }
            subtitle?.let {
                Text(
                    text = it,
                    style = AppTheme.typography.bodyMedium,
                    color = colors.inkSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        trailing?.invoke()
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DiuPersonRowPreview() {
    AppTheme {
        Surface(color = AppTheme.colors.background) {
            DiuPersonRow(
                name = "Minh",
                initials = "M",
                moodColor = AppTheme.colors.moods.thuong,
                onClick = {},
                subtitle = "Thật hả! Em vui quá",
                timestamp = "21:14",
            )
        }
    }
}
