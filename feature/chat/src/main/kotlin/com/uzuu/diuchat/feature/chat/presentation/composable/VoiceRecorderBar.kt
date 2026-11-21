package com.uzuu.diuchat.feature.chat.presentation.composable

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uzuu.diuchat.feature.base.common.res.AppTheme
import com.uzuu.diuchat.feature.chat.domain.model.MoodType
import com.uzuu.diuchat.feature.chat.domain.model.VoiceRecordingState

@Composable
fun VoiceRecorderBar(
    recordingState: VoiceRecordingState,
    selectedMood: MoodType,
    onCancelRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onSendVoiceMessage: () -> Unit,
    onTogglePreviewPlay: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val moodColor = Color(selectedMood.colorHex)

    AnimatedVisibility(
        visible = recordingState !is VoiceRecordingState.Idle,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier,
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = AppTheme.colors.surfaceElevated,
            tonalElevation = 6.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, moodColor.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        ) {
            when (recordingState) {
                is VoiceRecordingState.Recording -> {
                    val seconds = (recordingState.elapsedMs / 1000) % 60
                    val minutes = (recordingState.elapsedMs / 1000) / 60
                    val durationText = remember(seconds, minutes) {
                        String.format("%02d:%02d", minutes, seconds)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        // Trash / Cancel button
                        IconButton(
                            onClick = onCancelRecording,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(AppTheme.colors.surface),
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Hủy ghi âm",
                                tint = AppTheme.colors.inkSecondary,
                            )
                        }

                        // Recording indicator & metering waveform bars
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.FiberManualRecord,
                                contentDescription = "Đang ghi",
                                tint = Color(0xFFE53935),
                                modifier = Modifier.size(14.dp),
                            )

                            Text(
                                text = durationText,
                                style = AppTheme.typography.labelMedium,
                                color = AppTheme.colors.ink,
                            )

                            Spacer(modifier = Modifier.width(4.dp))

                            // Waveform bars
                            val bars = if (recordingState.amplitudes.isNotEmpty()) {
                                recordingState.amplitudes
                            } else {
                                listOf(0.3f, 0.6f, 0.9f, 0.4f, 0.8f, 0.5f, 0.7f, 0.3f, 0.6f, 0.8f, 0.5f, 0.4f)
                            }

                            Row(
                                modifier = Modifier.height(24.dp),
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                bars.take(18).forEach { amp ->
                                    val barHeight = (amp.coerceIn(0.15f, 1.0f) * 22).dp
                                    Box(
                                        modifier = Modifier
                                            .width(3.dp)
                                            .height(barHeight)
                                            .clip(RoundedCornerShape(1.5.dp))
                                            .background(moodColor),
                                    )
                                }
                            }
                        }

                        // Stop & Preview button
                        IconButton(
                            onClick = onStopRecording,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(moodColor.copy(alpha = 0.2f))
                                .border(1.dp, moodColor, CircleShape),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Dừng nghe lại",
                                tint = moodColor,
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Direct Send button
                        IconButton(
                            onClick = onSendVoiceMessage,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(moodColor),
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Gửi ghi âm",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }

                is VoiceRecordingState.Preview -> {
                    val totalSec = (recordingState.durationMs / 1000) % 60
                    val totalMin = (recordingState.durationMs / 1000) / 60
                    val totalDurationText = remember(totalSec, totalMin) {
                        String.format("%02d:%02d", totalMin, totalSec)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        // Discard
                        IconButton(
                            onClick = onCancelRecording,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(AppTheme.colors.surface),
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Hủy bỏ",
                                tint = AppTheme.colors.inkSecondary,
                            )
                        }

                        // Play/Pause Preview
                        IconButton(
                            onClick = onTogglePreviewPlay,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(moodColor.copy(alpha = 0.2f)),
                        ) {
                            Icon(
                                imageVector = if (recordingState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Nghe thử",
                                tint = moodColor,
                            )
                        }

                        // Preview Waveform
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                                .height(24.dp),
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            val bars = recordingState.waveform.ifEmpty {
                                listOf(0.4f, 0.7f, 0.5f, 0.8f, 0.3f, 0.6f, 0.9f, 0.4f, 0.5f, 0.7f, 0.3f)
                            }
                            bars.take(20).forEach { amp ->
                                val barHeight = (amp.coerceIn(0.2f, 1.0f) * 20).dp
                                Box(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .height(barHeight)
                                        .clip(RoundedCornerShape(1.5.dp))
                                        .background(moodColor),
                                )
                            }
                        }

                        Text(
                            text = totalDurationText,
                            fontSize = 12.sp,
                            color = AppTheme.colors.inkSecondary,
                            modifier = Modifier.padding(end = 8.dp),
                        )

                        // Send button
                        IconButton(
                            onClick = onSendVoiceMessage,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(moodColor),
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Gửi ghi âm",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }

                else -> {
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp))
                }
            }
        }
    }
}
