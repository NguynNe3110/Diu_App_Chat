package com.uzuu.diuchat.feature.chat.presentation.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.uzuu.diuchat.feature.base.common.res.AppTheme
import com.uzuu.diuchat.feature.chat.domain.model.ChatMessage

@Composable
fun ReplyQuoteBar(
    replyingTo: ChatMessage,
    onCancelReply: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val moodColor = Color(replyingTo.mood.colorHex)

    Box(
        modifier =
        modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(AppTheme.colors.surfaceElevated)
            .border(1.dp, AppTheme.colors.line, RoundedCornerShape(12.dp))
            .padding(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                Modifier
                    .size(width = 3.dp, height = 32.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(moodColor),
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Trả lời ${replyingTo.senderName}",
                    style = AppTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = moodColor,
                )
                Text(
                    text = replyingTo.content,
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.inkSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            IconButton(
                onClick = onCancelReply,
                modifier = Modifier.size(24.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Hủy trả lời",
                    tint = AppTheme.colors.inkTertiary,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}
