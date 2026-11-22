package com.uzuu.diuchat.feature.chat.presentation.composable

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.uzuu.diuchat.feature.base.common.res.AppIcons
import com.uzuu.diuchat.feature.base.common.res.AppPalette
import com.uzuu.diuchat.feature.base.common.res.AppTheme
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuMoodDot
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuOrbAvatar
import com.uzuu.diuchat.feature.chat.domain.model.MoodType
import com.uzuu.diuchat.feature.chat.domain.model.VoiceRecordingState

private val CORE_TRAY_MOODS = listOf(
    MoodType.THUONG,
    MoodType.VUI,
    MoodType.BINHYEN,
    MoodType.NHO,
    MoodType.TRAM,
)

private val DEFAULT_22_WAVE_BARS = listOf(
    0.25f, 0.55f, 0.85f, 0.45f, 0.92f, 0.60f, 0.75f, 0.40f, 0.80f, 0.50f, 0.35f,
    0.65f, 0.90f, 0.48f, 0.72f, 0.58f, 0.84f, 0.38f, 0.68f, 0.52f, 0.78f, 0.42f,
)

@Composable
fun ChatComposer(
    inputContent: String,
    selectedMood: MoodType,
    isMoodTrayOpen: Boolean,
    isSending: Boolean,
    pendingPhotoUri: String?,
    onInputChange: (String) -> Unit,
    onSelectMood: (MoodType) -> Unit,
    onToggleMoodTray: () -> Unit,
    onAttachPhotoClick: () -> Unit,
    onClearPhotoClick: () -> Unit,
    onStartVoiceRecording: () -> Unit,
    onSendClick: () -> Unit,
    modifier: Modifier = Modifier,
    partnerName: String = "",
    isAttachmentOpen: Boolean = false,
    onSlowSendClick: () -> Unit = {},
    voiceRecordingState: VoiceRecordingState = VoiceRecordingState.Idle,
    onCancelVoiceRecording: () -> Unit = {},
    onSendVoiceMessage: () -> Unit = {},
) {
    val colors = AppTheme.colors
    val selectedColor = Color(selectedMood.colorHex)
    val plusRotation by animateFloatAsState(
        targetValue = if (isAttachmentOpen) 45f else 0f,
        label = "plus_rotation",
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        // Photo Preview Bar if a photo is attached
        AnimatedVisibility(
            visible = pendingPhotoUri != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            if (pendingPhotoUri != null) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = colors.surfaceElevated,
                    border = BorderStroke(1.dp, colors.line),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.5.dp, selectedColor, RoundedCornerShape(12.dp)),
                        ) {
                            AsyncImage(
                                model = pendingPhotoUri,
                                contentDescription = "Xem trước ảnh",
                                modifier = Modifier.size(56.dp),
                                contentScale = ContentScale.Crop,
                            )
                        }

                        Text(
                            text = "Ảnh đính kèm sẵn sàng gửi",
                            style = AppTheme.typography.bodySmall,
                            color = colors.inkSecondary,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 10.dp),
                        )

                        IconButton(
                            onClick = onClearPhotoClick,
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(colors.surface),
                        ) {
                            Icon(
                                painter = painterResource(AppIcons.Close),
                                contentDescription = "Bỏ ảnh",
                                tint = colors.inkSecondary,
                                modifier = Modifier.size(14.dp),
                            )
                        }
                    }
                }
            }
        }

        // Expandable 5-orb Mood Tray (.tray)
        AnimatedVisibility(
            visible = isMoodTrayOpen,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = colors.surfaceElevated,
                border = BorderStroke(1.dp, colors.line),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Lời nhắn này mang màu",
                        style = AppTheme.typography.labelMedium,
                        color = colors.inkSecondary,
                        modifier = Modifier.padding(bottom = 10.dp),
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CORE_TRAY_MOODS.forEach { mood ->
                            val moodColor = Color(mood.colorHex)
                            val isSelected = mood == selectedMood

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable(onClickLabel = "Chọn cảm xúc ${mood.displayName}") {
                                        onSelectMood(mood)
                                    }
                                    .padding(horizontal = 6.dp, vertical = 4.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .then(
                                            if (isSelected) {
                                                Modifier.border(2.dp, colors.ink, CircleShape)
                                            } else {
                                                Modifier
                                            },
                                        ),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    DiuOrbAvatar(
                                        initials = "",
                                        moodColor = moodColor,
                                        size = 34.dp,
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = mood.displayName,
                                    style = AppTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    ),
                                    color = if (isSelected) colors.ink else colors.inkSecondary,
                                )
                            }
                        }
                    }
                }
            }
        }

        // 58.dp Floating Pill Composer (.composer / .composer.rec)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            shape = RoundedCornerShape(29.dp),
            color = colors.surfaceElevated,
            border = BorderStroke(1.dp, colors.line),
        ) {
            if (voiceRecordingState !is VoiceRecordingState.Idle) {
                InlineVoiceRecordingPill(
                    recordingState = voiceRecordingState,
                    moodColor = selectedColor,
                    onCancel = onCancelVoiceRecording,
                    onSend = onSendVoiceMessage,
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // 26.dp glowing mood dot toggle
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onToggleMoodTray),
                        contentAlignment = Alignment.Center,
                    ) {
                        DiuMoodDot(
                            color = selectedColor,
                            size = 26.dp,
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Borderless BasicTextField
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        if (inputContent.isEmpty()) {
                            Text(
                                text = "Nhắn cho $partnerName…",
                                style = AppTheme.typography.bodyMedium,
                                color = colors.inkTertiary,
                                maxLines = 1,
                            )
                        }
                        BasicTextField(
                            value = inputContent,
                            onValueChange = onInputChange,
                            textStyle = AppTheme.typography.bodyMedium.copy(color = colors.ink),
                            cursorBrush = SolidColor(colors.ink),
                            maxLines = 3,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    // + Attachment button (rotates 45° when open)
                    IconButton(
                        onClick = onAttachPhotoClick,
                        modifier = Modifier.size(36.dp),
                    ) {
                        Icon(
                            painter = painterResource(AppIcons.Plus),
                            contentDescription = "Đính kèm",
                            tint = colors.inkSecondary,
                            modifier = Modifier
                                .size(20.dp)
                                .rotate(plusRotation),
                        )
                    }

                    // Slow-send Clock button
                    IconButton(
                        onClick = onSlowSendClick,
                        modifier = Modifier.size(36.dp),
                    ) {
                        Icon(
                            painter = painterResource(AppIcons.Clock),
                            contentDescription = "Gửi chậm",
                            tint = colors.inkSecondary,
                            modifier = Modifier.size(20.dp),
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // 46.dp Circular Mic / Send End Button
                    val hasContent = inputContent.trim().isNotEmpty() || pendingPhotoUri != null
                    if (hasContent) {
                        IconButton(
                            onClick = { if (!isSending) onSendClick() },
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(colors.ink),
                        ) {
                            if (isSending) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = colors.background,
                                    strokeWidth = 2.dp,
                                )
                            } else {
                                Icon(
                                    painter = painterResource(AppIcons.Send),
                                    contentDescription = "Gửi tin nhắn",
                                    tint = colors.background,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                    } else {
                        IconButton(
                            onClick = onStartVoiceRecording,
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(selectedColor.copy(alpha = 0.55f)),
                        ) {
                            Icon(
                                painter = painterResource(AppIcons.Mic),
                                contentDescription = "Ghi âm thoại",
                                tint = colors.ink,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InlineVoiceRecordingPill(
    recordingState: VoiceRecordingState,
    moodColor: Color,
    onCancel: () -> Unit,
    onSend: () -> Unit,
) {
    val colors = AppTheme.colors
    val elapsedMs = when (recordingState) {
        is VoiceRecordingState.Recording -> recordingState.elapsedMs
        is VoiceRecordingState.Preview -> recordingState.durationMs
        else -> 9000L
    }
    val totalSec = (elapsedMs / 1000) % 60
    val totalMin = (elapsedMs / 1000) / 60
    val timerText = remember(totalSec, totalMin) {
        String.format(java.util.Locale.US, "%d:%02d", totalMin, totalSec)
    }

    val pulseTransition = rememberInfiniteTransition(label = "rec_dot_pulse")
    val dotAlpha by pulseTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(650),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "rec_dot_alpha",
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        IconButton(
            onClick = onCancel,
            modifier = Modifier.size(36.dp),
        ) {
            Icon(
                painter = painterResource(AppIcons.Close),
                contentDescription = "Huỷ ghi âm",
                tint = colors.inkSecondary,
                modifier = Modifier.size(18.dp),
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(9.dp)
                    .alpha(dotAlpha)
                    .clip(CircleShape)
                    .background(AppPalette.RecDot),
            )
            Text(
                text = timerText,
                style = AppTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = colors.ink,
            )
        }

        // 22-bar live waveform
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 10.dp)
                .height(26.dp),
            horizontalArrangement = Arrangement.spacedBy(2.5.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DEFAULT_22_WAVE_BARS.forEach { amp ->
                val barHeight = (amp.coerceIn(0.2f, 1.0f) * 22).dp
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(barHeight)
                        .clip(RoundedCornerShape(1.5.dp))
                        .background(moodColor),
                )
            }
        }

        IconButton(
            onClick = onSend,
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(colors.ink),
        ) {
            Icon(
                painter = painterResource(AppIcons.Send),
                contentDescription = "Gửi ghi âm",
                tint = colors.background,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
