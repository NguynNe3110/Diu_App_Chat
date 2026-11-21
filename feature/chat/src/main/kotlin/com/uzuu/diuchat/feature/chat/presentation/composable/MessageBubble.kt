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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Forward
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.uzuu.diuchat.feature.base.common.res.AppTheme
import com.uzuu.diuchat.feature.chat.domain.model.ChatMessage
import com.uzuu.diuchat.feature.chat.domain.model.MessageMediaType
import com.uzuu.diuchat.feature.chat.domain.model.MessageStatus
import com.uzuu.diuchat.feature.chat.domain.model.MoodType
import java.text.SimpleDateFormat
import java.util.Date

@Composable
fun MessageBubble(
    message: ChatMessage,
    onReplyClick: (ChatMessage) -> Unit,
    modifier: Modifier = Modifier,
    isPlayingAudio: Boolean = false,
    audioProgress: Float = 0f,
    onToggleAudioPlayback: (ChatMessage) -> Unit = {},
    onReactionClick: (ChatMessage, MoodType) -> Unit = { _, _ -> },
    onViewReactions: (ChatMessage) -> Unit = {},
    onPinClick: (ChatMessage) -> Unit = {},
    onForwardClick: (ChatMessage) -> Unit = {},
) {
    var showActionMenu by remember { mutableStateOf(false) }

    val moodColor = Color(message.mood.colorHex)
    val alignment = if (message.isFromMe) Alignment.End else Alignment.Start
    val bubbleShape =
        if (message.isFromMe) {
            RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp)
        } else {
            RoundedCornerShape(18.dp, 18.dp, 18.dp, 4.dp)
        }

    val bubbleBg =
        if (message.isFromMe) {
            moodColor.copy(alpha = 0.22f)
        } else {
            AppTheme.colors.surface
        }

    val locale = LocalLocale.current.platformLocale
    val timeStr =
        remember(message.createdAtEpochMs, locale) {
            SimpleDateFormat("HH:mm", locale).format(Date(message.createdAtEpochMs))
        }

    Column(
        modifier =
        modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalAlignment = alignment,
    ) {
        // Quick reaction popup tray when menu is shown
        AnimatedVisibility(
            visible = showActionMenu,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = AppTheme.colors.surfaceElevated,
                tonalElevation = 6.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.line),
                modifier = Modifier.padding(bottom = 4.dp),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // 6 Mood reactions
                    MoodType.entries.forEach { mood ->
                        val moodTint = Color(mood.colorHex)
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(moodTint.copy(alpha = 0.2f))
                                .border(1.dp, moodTint, CircleShape)
                                .clickable {
                                    onReactionClick(message, mood)
                                    showActionMenu = false
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(moodTint),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Reply action
                    IconButton(
                        onClick = {
                            onReplyClick(message)
                            showActionMenu = false
                        },
                        modifier = Modifier.size(28.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Reply,
                            contentDescription = "Trả lời",
                            tint = AppTheme.colors.inkSecondary,
                            modifier = Modifier.size(16.dp),
                        )
                    }

                    // Pin action
                    IconButton(
                        onClick = {
                            onPinClick(message)
                            showActionMenu = false
                        },
                        modifier = Modifier.size(28.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = if (message.isPinned) "Bỏ ghim" else "Ghim tin nhắn",
                            tint = if (message.isPinned) AppTheme.colors.accent else AppTheme.colors.inkSecondary,
                            modifier = Modifier.size(16.dp),
                        )
                    }

                    // Forward action
                    IconButton(
                        onClick = {
                            onForwardClick(message)
                            showActionMenu = false
                        },
                        modifier = Modifier.size(28.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Forward,
                            contentDescription = "Chuyển tiếp",
                            tint = AppTheme.colors.inkSecondary,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
        }

        Box(
            modifier =
            Modifier
                .widthIn(max = 280.dp)
                .clip(bubbleShape)
                .background(bubbleBg)
                .border(
                    width = 1.dp,
                    color = moodColor.copy(alpha = 0.5f),
                    shape = bubbleShape,
                ).clickable {
                    showActionMenu = !showActionMenu
                }
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            Column {
                // Pinned indicator badge
                if (message.isPinned) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(bottom = 4.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Đã ghim",
                            tint = moodColor,
                            modifier = Modifier.size(12.dp),
                        )
                        Text(
                            text = "Đã ghim",
                            style = AppTheme.typography.labelMedium.copy(fontSize = 10.sp),
                            color = moodColor,
                        )
                    }
                }

                // Reply quote banner if message replies to someone
                if (!message.replyQuote.isNullOrEmpty()) {
                    Box(
                        modifier =
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(AppTheme.colors.surfaceElevated.copy(alpha = 0.7f))
                            .border(width = 1.dp, color = moodColor.copy(alpha = 0.4f), shape = RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = message.replyQuote,
                            style = AppTheme.typography.bodySmall,
                            color = AppTheme.colors.inkSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // Sender Name if not from me
                if (!message.isFromMe) {
                    Text(
                        text = message.senderName,
                        style = AppTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = moodColor,
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }

                // Content depending on Media Type
                when (message.mediaType) {
                    MessageMediaType.PHOTO -> {
                        PhotoMessageContent(
                            message = message,
                            moodColor = moodColor,
                        )
                    }
                    MessageMediaType.VOICE -> {
                        VoiceMessageContent(
                            message = message,
                            moodColor = moodColor,
                            isPlaying = isPlayingAudio,
                            playbackProgress = audioProgress,
                            onTogglePlay = { onToggleAudioPlayback(message) },
                        )
                    }
                    MessageMediaType.TEXT -> {
                        Text(
                            text = message.content,
                            style = AppTheme.typography.bodyMedium,
                            color = AppTheme.colors.ink,
                        )
                    }
                }

                // Reactions row badge
                if (message.reactions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    val groupedReactions = message.reactions.groupBy { it.mood }
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(AppTheme.colors.surfaceElevated.copy(alpha = 0.9f))
                            .border(1.dp, AppTheme.colors.line, RoundedCornerShape(12.dp))
                            .clickable { onViewReactions(message) }
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        groupedReactions.forEach { (m, list) ->
                            val mColor = Color(m.colorHex)
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(mColor),
                            )
                            Text(
                                text = "${list.size}",
                                style = AppTheme.typography.labelMedium.copy(fontSize = 11.sp),
                                color = AppTheme.colors.ink,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Timestamp & Status Icon Row
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = timeStr,
                        fontSize = 11.sp,
                        color = AppTheme.colors.inkTertiary,
                    )

                    if (message.isFromMe) {
                        MessageStatusIcon(status = message.status, moodColor = moodColor)
                    }
                }
            }
        }
    }
}

@Composable
private fun PhotoMessageContent(
    message: ChatMessage,
    moodColor: Color,
) {
    Column {
        if (!message.mediaUri.isNullOrEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, moodColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
            ) {
                AsyncImage(
                    model = message.mediaUri,
                    contentDescription = "Hình ảnh đính kèm",
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    contentScale = ContentScale.Crop,
                )
            }
        }

        if (!message.caption.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = message.caption,
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colors.ink,
            )
        }
    }
}

@Composable
private fun VoiceMessageContent(
    message: ChatMessage,
    moodColor: Color,
    isPlaying: Boolean,
    playbackProgress: Float,
    onTogglePlay: () -> Unit,
) {
    val durationMs = message.audioDurationMs ?: 0L
    val totalSec = (durationMs / 1000) % 60
    val totalMin = (durationMs / 1000) / 60
    val durationStr = remember(totalSec, totalMin) {
        String.format("%02d:%02d", totalMin, totalSec)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
    ) {
        // Play/Pause button
        IconButton(
            onClick = onTogglePlay,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(moodColor),
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Tạm dừng" else "Phát âm thanh",
                tint = Color.White,
                modifier = Modifier.size(20.dp),
            )
        }

        // Waveform bars with playback scrub / progress tint
        val waveform = message.audioWaveform.ifEmpty {
            listOf(0.3f, 0.6f, 0.9f, 0.5f, 0.8f, 0.4f, 0.7f, 0.5f, 0.8f, 0.3f, 0.6f, 0.4f)
        }

        Row(
            modifier = Modifier
                .weight(1f)
                .height(26.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val totalBars = waveform.size
            waveform.forEachIndexed { index, amp ->
                val barProgress = index.toFloat() / totalBars.toFloat()
                val isPlayed = isPlaying && barProgress <= playbackProgress
                val barColor = if (isPlayed) moodColor else moodColor.copy(alpha = 0.35f)
                val barHeight = (amp.coerceIn(0.2f, 1.0f) * 22).dp

                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(barHeight)
                        .clip(RoundedCornerShape(1.5.dp))
                        .background(barColor),
                )
            }
        }

        Text(
            text = durationStr,
            fontSize = 12.sp,
            color = AppTheme.colors.inkSecondary,
        )
    }
}

@Composable
private fun MessageStatusIcon(
    status: MessageStatus,
    moodColor: Color,
) {
    val iconSize = 13.dp
    when (status) {
        MessageStatus.PENDING, MessageStatus.UPLOADING -> {
            Icon(
                imageVector = Icons.Default.AccessTime,
                contentDescription = "Đang gửi...",
                tint = AppTheme.colors.inkTertiary,
                modifier = Modifier.size(iconSize),
            )
        }
        MessageStatus.SENT -> {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Đã gửi",
                tint = AppTheme.colors.inkTertiary,
                modifier = Modifier.size(iconSize),
            )
        }
        MessageStatus.DELIVERED -> {
            Icon(
                imageVector = Icons.Default.DoneAll,
                contentDescription = "Đã nhận",
                tint = AppTheme.colors.inkTertiary,
                modifier = Modifier.size(iconSize),
            )
        }
        MessageStatus.READ -> {
            Icon(
                imageVector = Icons.Default.DoneAll,
                contentDescription = "Đã đọc",
                tint = moodColor,
                modifier = Modifier.size(iconSize),
            )
        }
        MessageStatus.FAILED -> {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = "Gửi thất bại",
                tint = Color(0xFFE53935),
                modifier = Modifier.size(iconSize),
            )
        }
    }
}
