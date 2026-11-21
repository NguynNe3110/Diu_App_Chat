package com.igorwojda.showcase.feature.base.presentation.compose.composable

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Backward compatibility stubs for the legacy package
 * [com.igorwojda.showcase.feature.base.presentation.compose.composable].
 * All canonical implementations now reside in
 * [com.uzuu.diuchat.feature.base.presentation.compose.composable].
 */

@Composable
fun DiuMoodDot(
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 9.dp,
) = com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuMoodDot(color, modifier, size)

@Composable
fun DiuCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit,
) = com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuCard(modifier, contentPadding, content)

@Composable
fun DiuCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) = com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuCheckbox(checked, onCheckedChange, contentDescription, modifier, enabled)

@Composable
fun DiuFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) = com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuFilterChip(text, selected, onClick, modifier, enabled)

@Composable
fun DiuIconButton(
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    soft: Boolean = false,
    danger: Boolean = false,
    content: @Composable () -> Unit,
) = com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuIconButton(onClick, contentDescription, modifier, enabled, soft, danger, content)

@Composable
fun DiuInlineNotice(
    message: String,
    modifier: Modifier = Modifier,
    leading: (@Composable () -> Unit)? = null,
) = com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuInlineNotice(message, modifier, leading)

@Composable
fun DiuRadioButton(
    selected: Boolean,
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) = com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuRadioButton(selected, onClick, contentDescription, modifier, enabled)

@Composable
fun DiuSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) = com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuSectionHeader(title, modifier, action)

@Composable
fun DiuSettingsRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    destructive: Boolean = false,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) = com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuSettingsRow(title, modifier, subtitle, onClick, destructive, leading, trailing)

@Composable
fun DiuSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) = com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuSwitch(checked, onCheckedChange, contentDescription, modifier, enabled)

@Composable
fun DiuTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) = com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuTextButton(text, onClick, modifier, enabled)

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
) = com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuTextField(value, onValueChange, label, modifier, enabled, singleLine, isError, placeholder, supportingText)
