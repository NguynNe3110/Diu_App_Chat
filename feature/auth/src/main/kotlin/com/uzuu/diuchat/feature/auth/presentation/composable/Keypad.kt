package com.uzuu.diuchat.feature.auth.presentation.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.uzuu.diuchat.feature.base.common.res.DiuSerif
import androidx.compose.ui.unit.sp
import com.uzuu.diuchat.feature.base.common.res.AppTheme

@Composable
fun Keypad(
    onKeyPress: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val rows =
        listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("", "0", "del"),
        )

    Column(
        modifier =
        modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        rows.forEach { rowKeys ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                rowKeys.forEach { key ->
                    KeypadButton(
                        key = key,
                        modifier = Modifier.weight(1f),
                        enabled = enabled && key.isNotEmpty(),
                        onClick = { onKeyPress(key) },
                    )
                }
            }
        }
    }
}

@Composable
private fun KeypadButton(
    key: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val shape = RoundedCornerShape(20.dp)

    if (key.isEmpty()) {
        Spacer(modifier = modifier.height(54.dp))
        return
    }

    val description = if (key == "del") "Xoá ký tự" else "Số $key"

    Box(
        modifier =
        modifier
            .height(54.dp)
            .clip(shape)
            .clickable(
                enabled = enabled,
                onClick = onClick,
                role = Role.Button,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = AppTheme.colors.ink.copy(alpha = 0.12f)),
            ).semantics {
                this.contentDescription = description
                this.role = Role.Button
            },
        contentAlignment = Alignment.Center,
    ) {
        if (key == "del") {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.Backspace,
                contentDescription = null,
                tint = if (enabled) AppTheme.colors.ink else AppTheme.colors.inkTertiary,
            )
        } else {
            Text(
                text = key,
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = DiuSerif,
                color = if (enabled) AppTheme.colors.ink else AppTheme.colors.inkTertiary,
            )
        }
    }
}
