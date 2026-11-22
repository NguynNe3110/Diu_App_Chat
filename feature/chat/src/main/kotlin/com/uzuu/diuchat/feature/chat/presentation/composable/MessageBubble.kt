package com.uzuu.diuchat.feature.chat.presentation.composable

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PushPin
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.uzuu.diuchat.feature.base.common.res.AppIcons
import com.uzuu.diuchat.feature.base.common.res.AppTheme
import com.uzuu.diuchat.feature.base.common.res.DiuSerif
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuOrbAvatar
import com.uzuu.diuchat.feature.chat.domain.model.ChatMessage
import com.uzuu.diuchat.feature.chat.domain.model.MessageMediaType
import com.uzuu.diuchat.feature.chat.domain.model.MessageStatus
import com.uzuu.diuchat.feature.chat.domain.model.MoodType
import java.text.SimpleDateFormat
import java.util.Date

private val CORE_FIVE_MOODS = listOf(
    MoodType.THUONG,
    MoodType.VUI,
    MoodType.BINHYEN,
    MoodType.NHO,
    MoodType.TRAM,
)

@Composable
fun MessageBubble(
    message: ChatMessage,
    onReplyClick: (ChatMessage) -> Unit,
    modifier: Modifier = Modifier,
    isPlayingAudio: Boolean = false,
    audioProgress: Float = 0f,
    isGroupConversation: Boolean = false,
    onToggleAudioPlayback: (ChatMessage) -> Unit = {},
    onReactionClick: (ChatMessage, MoodType) -> Unit = { _, _ -> },
    onViewReactions: (ChatMessage) -> Unit = {},
    onPinClick: (ChatMessage) -> Unit = {},
    onForwardClick: (ChatMessage) -> Unit = {},
    onToggleSealedReveal: (String) -> Unit = {},
    onCancelScheduledLetter: (String) -> Unit = {},
    onPhotoClick: (ChatMessage) -> Unit = {},
) {
    var showActionMenu by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current
    val colors = AppTheme.colors

    val moodColor = Color(message.mood.colorHex)
    val alignment = if (message.isFromMe) Alignment.End else Alignment.Start
    val bubbleShape =
        if (message.isFromMe) {
            RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomEnd = 8.dp, bottomStart = 20.dp)
        } else {
            RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomEnd = 20.dp, bottomStart = 8.dp)
        }

    val textColor = if (message.isFromMe) colors.bubbleTextOnMood else colors.ink
    val secondaryTextColor = if (message.isFromMe) colors.bubbleTextOnMood.copy(alpha = 0.72f) else colors.inkTertiary

    val locale = LocalLocale.current.platformLocale
    val timeStr =
        remember(message.createdAtEpochMs, locale) {
            SimpleDateFormat("HH:mm", locale).format(Date(message.createdAtEpochMs))
        }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 5.dp),
        horizontalAlignment = alignment,
    ) {
        // 5-mood reaction picker above the bubble (.rpick)
        AnimatedVisibility(
            visible = showActionMenu,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = colors.surfaceElevated,
                border = BorderStroke(1.dp, colors.line),
                modifier = Modifier.padding(bottom = 6.dp),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CORE_FIVE_MOODS.forEach { mood ->
                        val moodTint = Color(mood.colorHex)
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(moodTint.copy(alpha = 0.25f))
                                .border(1.5.dp, moodTint, CircleShape)
                                .clickable {
                                    onReactionClick(message, mood)
                                    showActionMenu = false
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(moodTint),
                            )
                        }
                    }
                }
            }
        }

        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (!message.isFromMe && isGroupConversation) {
                DiuOrbAvatar(
                    initials = message.senderName.take(1).ifEmpty { "D" },
                    moodColor = moodColor,
                    size = 30.dp,
                )
            }

            Column(horizontalAlignment = alignment) {
                if (!message.isFromMe && isGroupConversation) {
                    Text(
                        text = message.senderName,
                        style = AppTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = colors.inkSecondary,
                        modifier = Modifier.padding(start = 4.dp, bottom = 3.dp),
                    )
                }

                when (message.mediaType) {
                    MessageMediaType.SEALED_LETTER -> {
                        SealedLetterBubbleContent(
                            message = message,
                            moodColor = moodColor,
                            timeStr = timeStr,
                            onToggleReveal = { onToggleSealedReveal(message.id) },
                            onCancelLetter = { onCancelScheduledLetter(message.id) },
                        )
                    }

                    MessageMediaType.LOCATION -> {
                        LocationBubbleContent(
                            message = message,
                            moodColor = moodColor,
                            timeStr = timeStr,
                            onClick = { showActionMenu = !showActionMenu },
                        )
                    }

                    else -> {
                        val bubbleModifier = if (message.isFromMe) {
                            Modifier
                                .widthIn(max = 280.dp)
                                .clip(bubbleShape)
                                .background(colors.background)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            moodColor.copy(alpha = 0.62f),
                                            moodColor.copy(alpha = 0.88f),
                                        ),
                                    ),
                                )
                                .border(1.dp, moodColor.copy(alpha = 0.55f), bubbleShape)
                        } else {
                            Modifier
                                .widthIn(max = 280.dp)
                                .clip(bubbleShape)
                                .background(colors.surfaceElevated)
                                .border(1.dp, moodColor.copy(alpha = 0.55f), bubbleShape)
                        }

                        Box(
                            modifier = bubbleModifier
                                .clickable {
                                    if (message.mediaType == MessageMediaType.PHOTO) {
                                        onPhotoClick(message)
                                    } else {
                                        showActionMenu = !showActionMenu
                                    }
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                        ) {
                            Column {
                                if (message.isPinned) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.padding(bottom = 4.dp),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PushPin,
                                            contentDescription = "Đã ghim",
                                            tint = textColor,
                                            modifier = Modifier.size(12.dp),
                                        )
                                        Text(
                                            text = "Đã ghim",
                                            style = AppTheme.typography.labelMedium.copy(fontSize = 10.sp),
                                            color = textColor,
                                        )
                                    }
                                }

                                if (!message.replyQuote.isNullOrEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(colors.quoteBg)
                                            .border(1.dp, moodColor.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                            .padding(horizontal = 8.dp, vertical = 5.dp),
                                    ) {
                                        Text(
                                            text = message.replyQuote,
                                            style = AppTheme.typography.bodySmall,
                                            color = textColor.copy(alpha = 0.85f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                }

                                when (message.mediaType) {
                                    MessageMediaType.PHOTO -> {
                                        PhotoMessageContent(
                                            message = message,
                                            moodColor = moodColor,
                                            textColor = textColor,
                                            onPhotoClick = { onPhotoClick(message) },
                                        )
                                    }

                                    MessageMediaType.VOICE -> {
                                        VoiceMessageContent(
                                            message = message,
                                            moodColor = moodColor,
                                            textColor = textColor,
                                            isPlaying = isPlayingAudio,
                                            playbackProgress = audioProgress,
                                            onTogglePlay = { onToggleAudioPlayback(message) },
                                        )
                                    }

                                    else -> {
                                        Text(
                                            text = message.content,
                                            style = AppTheme.typography.bodyMedium,
                                            color = textColor,
                                        )
                                    }
                                }

                                if (message.reactions.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    val groupedReactions = message.reactions.groupBy { it.mood }
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(colors.surfaceElevated.copy(alpha = 0.9f))
                                            .border(1.dp, colors.line, RoundedCornerShape(12.dp))
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
                                                color = colors.ink,
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    modifier = Modifier.align(Alignment.End),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Text(
                                        text = timeStr,
                                        fontSize = 11.sp,
                                        color = secondaryTextColor,
                                    )

                                    if (message.isFromMe) {
                                        MessageStatusIcon(
                                            status = message.status,
                                            moodColor = textColor,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Frosted glass action menu below the bubble (.bmenu)
        AnimatedVisibility(
            visible = showActionMenu,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = colors.surfaceElevated,
                border = BorderStroke(1.dp, colors.line),
                modifier = Modifier.padding(top = 6.dp),
            ) {
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    BubbleActionTextItem(
                        iconRes = AppIcons.Reply,
                        label = "Trả lời",
                        onClick = {
                            onReplyClick(message)
                            showActionMenu = false
                        },
                    )
                    BubbleActionTextItem(
                        iconRes = AppIcons.Copy,
                        label = "Sao chép",
                        onClick = {
                            clipboardManager.setText(AnnotatedString(message.content))
                            showActionMenu = false
                        },
                    )
                    BubbleActionTextItem(
                        iconRes = AppIcons.Pin,
                        label = if (message.isPinned) "Bỏ ghim" else "Ghim",
                        onClick = {
                            onPinClick(message)
                            showActionMenu = false
                        },
                    )
                    BubbleActionTextItem(
                        iconRes = AppIcons.Share,
                        label = "Chuyển tiếp",
                        onClick = {
                            onForwardClick(message)
                            showActionMenu = false
                        },
                    )
                    if (message.isFromMe) {
                        BubbleActionTextItem(
                            iconRes = AppIcons.Trash,
                            label = "Thu hồi",
                            isDanger = true,
                            onClick = {
                                onCancelScheduledLetter(message.id)
                                showActionMenu = false
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BubbleActionTextItem(
    iconRes: Int,
    label: String,
    isDanger: Boolean = false,
    onClick: () -> Unit,
) {
    val colors = AppTheme.colors
    val tint = if (isDanger) colors.danger else colors.inkSecondary
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(14.dp),
        )
        Text(
            text = label,
            style = AppTheme.typography.labelSmall,
            color = tint,
            maxLines = 1,
            softWrap = false,
        )
    }
}

@Composable
private fun SealedLetterBubbleContent(
    message: ChatMessage,
    moodColor: Color,
    timeStr: String,
    onToggleReveal: () -> Unit,
    onCancelLetter: () -> Unit,
) {
    val colors = AppTheme.colors
    val scheduleText = message.scheduledForLabel ?: "7:00 sáng mai"
    val unlockAt = message.unlockAtEpochMs
    val isUnlocked = unlockAt == null || System.currentTimeMillis() >= unlockAt
    val canReveal = isUnlocked || message.isFromMe

    Box(
        modifier = Modifier
            .width(250.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(colors.surfaceElevated)
            .drawBehind {
                // Soft diagonal stripes
                val stripeColor = moodColor.copy(alpha = 0.08f)
                val step = 16.dp.toPx()
                var x = -size.height
                while (x < size.width + size.height) {
                    drawLine(
                        color = stripeColor,
                        start = Offset(x, 0f),
                        end = Offset(x + size.height, size.height),
                        strokeWidth = 4.dp.toPx(),
                    )
                    x += step
                }
                // 1.5.dp dashed border
                drawRoundRect(
                    color = moodColor,
                    cornerRadius = CornerRadius(20.dp.toPx(), 20.dp.toPx()),
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f),
                    ),
                )
            }
            .clickable(enabled = canReveal) { onToggleReveal() }
            .padding(14.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    painter = painterResource(if (isUnlocked) AppIcons.Envelope else AppIcons.Lock),
                    contentDescription = "Thư hẹn",
                    tint = colors.inkSecondary,
                    modifier = Modifier.size(15.dp),
                )
                Text(
                    text = if (isUnlocked) {
                        "Thư hẹn · đã đến giờ mở"
                    } else {
                        "Đang niêm phong · mở vào $scheduleText"
                    },
                    style = AppTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.inkSecondary,
                )
            }

            if (message.isSealedRevealed && canReveal) {
                Text(
                    text = message.content,
                    style = AppTheme.typography.bodyMedium.copy(
                        fontFamily = DiuSerif,
                        fontStyle = FontStyle.Italic,
                    ),
                    color = colors.ink,
                )
            } else {
                Text(
                    text = when {
                        isUnlocked -> "Chạm để mở niêm phong lá thư…"
                        message.isFromMe -> "Lời nhắn đã niêm phong (chạm để xem lại)…"
                        else -> "Nội dung sẽ tự mở khóa khi đến giờ hẹn."
                    },
                    style = AppTheme.typography.bodySmall.copy(
                        fontFamily = DiuSerif,
                        fontStyle = FontStyle.Italic,
                    ),
                    color = colors.inkSecondary,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (message.isFromMe && !isUnlocked) {
                    Text(
                        text = "Huỷ thư hẹn",
                        style = AppTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.danger,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onCancelLetter() }
                            .padding(vertical = 2.dp, horizontal = 4.dp),
                    )
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }
                Text(
                    text = timeStr,
                    fontSize = 11.sp,
                    color = colors.inkTertiary,
                )
            }
        }
    }
}

@Composable
private fun LocationBubbleContent(
    message: ChatMessage,
    moodColor: Color,
    timeStr: String,
    onClick: () -> Unit,
) {
    val colors = AppTheme.colors
    val caption = message.locationDistanceLabel ?: "Mình đang ở đây · cách bạn 1,2 km"

    Surface(
        modifier = Modifier
            .width(230.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = colors.surfaceElevated,
        border = BorderStroke(1.dp, moodColor.copy(alpha = 0.55f)),
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.peach.copy(alpha = 0.22f)),
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val gridColor = moodColor.copy(alpha = 0.22f)
                    val cols = 5
                    val rows = 4
                    for (c in 1 until cols) {
                        val x = size.width * c / cols
                        drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1.dp.toPx())
                    }
                    for (r in 1 until rows) {
                        val y = size.height * r / rows
                        drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
                    }
                    // Stylized road path
                    drawLine(
                        color = moodColor.copy(alpha = 0.4f),
                        start = Offset(0f, size.height * 0.7f),
                        end = Offset(size.width, size.height * 0.35f),
                        strokeWidth = 6.dp.toPx(),
                    )
                    // Glowing center pin
                    val center = Offset(size.width / 2f, size.height / 2f)
                    drawCircle(
                        color = moodColor.copy(alpha = 0.32f),
                        radius = 18.dp.toPx(),
                        center = center,
                    )
                    drawCircle(
                        color = moodColor,
                        radius = 7.dp.toPx(),
                        center = center,
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = colors.surfaceElevated,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp),
                ) {
                    Text(
                        text = "bản đồ · vị trí",
                        style = AppTheme.typography.labelSmall,
                        color = colors.inkSecondary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = caption,
                style = AppTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = colors.ink,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = timeStr,
                fontSize = 11.sp,
                color = colors.inkTertiary,
                modifier = Modifier.align(Alignment.End),
            )
        }
    }
}

@Composable
private fun PhotoMessageContent(
    message: ChatMessage,
    moodColor: Color,
    textColor: Color,
    onPhotoClick: () -> Unit,
) {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(moodColor.copy(alpha = 0.25f))
                .border(1.dp, moodColor.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                .clickable { onPhotoClick() },
            contentAlignment = Alignment.Center,
        ) {
            if (!message.mediaUri.isNullOrEmpty()) {
                AsyncImage(
                    model = message.mediaUri,
                    contentDescription = "Hình ảnh đính kèm",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Icon(
                    painter = painterResource(AppIcons.Image),
                    contentDescription = "Hình ảnh",
                    tint = textColor,
                    modifier = Modifier.size(32.dp),
                )
            }
        }

        if (!message.caption.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = message.caption,
                style = AppTheme.typography.bodyMedium,
                color = textColor,
            )
        }
    }
}

@Composable
private fun VoiceMessageContent(
    message: ChatMessage,
    moodColor: Color,
    textColor: Color,
    isPlaying: Boolean,
    playbackProgress: Float,
    onTogglePlay: () -> Unit,
) {
    val durationMs = message.audioDurationMs ?: 0L
    val totalSec = (durationMs / 1000) % 60
    val totalMin = (durationMs / 1000) / 60
    val durationStr = remember(totalSec, totalMin) {
        String.format(java.util.Locale.US, "%02d:%02d", totalMin, totalSec)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
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
                val barColor = if (isPlayed) textColor else textColor.copy(alpha = 0.38f)
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
            color = textColor.copy(alpha = 0.8f),
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
                tint = moodColor.copy(alpha = 0.65f),
                modifier = Modifier.size(iconSize),
            )
        }

        MessageStatus.SENT -> {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Đã gửi",
                tint = moodColor.copy(alpha = 0.7f),
                modifier = Modifier.size(iconSize),
            )
        }

        MessageStatus.DELIVERED -> {
            Icon(
                imageVector = Icons.Default.DoneAll,
                contentDescription = "Đã nhận",
                tint = moodColor.copy(alpha = 0.75f),
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
