package com.uzuu.diuchat.feature.base.presentation.compose.composable

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.uzuu.diuchat.feature.base.common.res.AppTheme

@Composable
fun DiuSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = AppTheme.colors
    val trackColor by animateColorAsState(
        if (checked) colors.accent else colors.ink.copy(alpha = 0.14f),
        label = "Switch track",
    )
    val thumbColor by animateColorAsState(
        if (checked) colors.onAccent else colors.background,
        label = "Switch thumb",
    )
    val thumbOffset by animateDpAsState(
        if (checked) 20.dp else 0.dp,
        label = "Switch thumb offset",
    )

    Box(
        modifier =
        modifier
            .sizeIn(minWidth = 50.dp, minHeight = 48.dp)
            .semantics { this.contentDescription = contentDescription }
            .toggleable(
                value = checked,
                enabled = enabled,
                role = Role.Switch,
                onValueChange = onCheckedChange,
                indication = null,
                interactionSource = null,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
            Modifier
                .alpha(if (enabled) 1f else 0.35f)
                .size(width = 50.dp, height = 30.dp)
                .background(trackColor, CircleShape)
                .padding(3.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Box(
                modifier =
                Modifier
                    .offset(x = thumbOffset)
                    .size(24.dp)
                    .shadow(2.dp, CircleShape)
                    .background(thumbColor, CircleShape),
            )
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DiuSwitchPreview() {
    AppTheme {
        var checked by remember { mutableStateOf(false) }
        Surface(color = AppTheme.colors.background) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    DiuSwitch(false, {}, contentDescription = "Tắt thông báo")
                    DiuSwitch(true, {}, contentDescription = "Bật thông báo")
                }
                DiuSwitch(checked, { checked = it }, contentDescription = "Tạm ngừng thông báo")
                DiuSwitch(false, {}, contentDescription = "Tạm ngừng thông báo", enabled = false)
            }
        }
    }
}
