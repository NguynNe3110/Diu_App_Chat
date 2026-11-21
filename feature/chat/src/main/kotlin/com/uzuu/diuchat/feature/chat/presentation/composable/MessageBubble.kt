package com.uzuu.diuchat.feature.chat.presentation.composable

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uzuu.diuchat.feature.base.common.res.AppTheme
import com.uzuu.diuchat.feature.chat.domain.model.ChatMessage
import com.uzuu.diuchat.feature.chat.domain.model.MessageStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MessageBubble(
    message: ChatMessage,
    onReplyClicked: (ChatMessage) -> Unit,
    modifier: Modifier = Modifier,
) {
    val moodColor = Color(message.mood.colorHex)
    val alignment = if (message.isFromMe) Alignment.End else Alignment.Start
    val bubbleShape = if (message.isFromMe) {
        RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp)
    } else {
        RoundedCornerShape(18.dp, 18.dp, 18.dp, 4.dp)
    }

    val bubbleBg = if (message.isFromMe) {
        moodColor.copy(alpha = 0.22f)
    } else {
        AppTheme.colors.surface
    }

    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val timeStr = timeFormat.format(Date(message.createdAtEpochMs))

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalAlignment = alignment,
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(bubbleShape)
                .background(bubbleBg)
                .border(
                    width = 1.dp,
                    color = moodColor.copy(alpha = 0.5f),
                    shape = bubbleShape,
                )
                .clickable { onReplyClicked(message) }
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            Column {
                // Reply quote banner if message replies to someone
                if (!message.replyQuote.isNullOrEmpty()) {
                    Box(
                        modifier = Modifier
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

                // Message Text
                Text(
                    text = message.content,
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.colors.ink,
                )

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
private fun MessageStatusIcon(
    status: MessageStatus,
    moodColor: Color,
) {
    val iconSize = 13.dp
    when (status) {
        MessageStatus.PENDING, MessageStatus.UPLOADING -> {
            Icon(
                imageVector = Icons.Default.AccessTime,
                contentDescription = "Đang gửi",
                tint = AppTheme.colors.inkTertiary,
                modifier = Modifier.size(iconSize),
            )
        }
        MessageStatus.SENT -> {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Đã gửi",
                tint = AppTheme.colors.inkSecondary,
                modifier = Modifier.size(iconSize),
            )
        }
        MessageStatus.DELIVERED -> {
            Icon(
                imageVector = Icons.Default.DoneAll,
                contentDescription = "Đã nhận",
                tint = AppTheme.colors.inkSecondary,
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
                contentDescription = "Lỗi gửi",
                tint = AppTheme.colors.danger,
                modifier = Modifier.size(iconSize),
            )
        }
    }
}
