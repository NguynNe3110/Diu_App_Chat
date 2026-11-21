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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.uzuu.diuchat.feature.base.common.res.AppTheme
import com.uzuu.diuchat.feature.chat.domain.model.ChatMessage
import com.uzuu.diuchat.feature.chat.domain.model.Conversation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForwardDialog(
    messageToForward: ChatMessage,
    availableConversations: List<Conversation>,
    onDismiss: () -> Unit,
    onConfirmForward: (List<String>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedIds = remember { mutableStateListOf<String>() }

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = AppTheme.colors.surfaceElevated,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth().padding(16.dp),
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Chuyển tiếp tin nhắn",
                    style = AppTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = AppTheme.colors.ink,
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Snippet of the message to forward
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(AppTheme.colors.surface)
                        .border(1.dp, AppTheme.colors.line, RoundedCornerShape(10.dp))
                        .padding(10.dp),
                ) {
                    Text(
                        text = messageToForward.content,
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colors.inkSecondary,
                        maxLines = 2,
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Chọn người nhận:",
                    style = AppTheme.typography.labelMedium,
                    color = AppTheme.colors.inkSecondary,
                )

                Spacer(modifier = Modifier.height(8.dp))

                val sampleConversations = if (availableConversations.isNotEmpty()) {
                    availableConversations
                } else {
                    listOf(
                        Conversation(
                            id = "conv_linh_diu",
                            title = "Linh Dịu",
                            mood = com.uzuu.diuchat.feature.chat.domain.model.MoodType.BINHYEN,
                            lastMessage = "",
                            lastMessageTimeEpochMs = 0L,
                        ),
                        Conversation(
                            id = "conv_gia_dinh",
                            title = "Gia đình nhỏ",
                            mood = com.uzuu.diuchat.feature.chat.domain.model.MoodType.THUONG,
                            lastMessage = "",
                            lastMessageTimeEpochMs = 0L,
                            isGroup = true,
                        ),
                        Conversation(
                            id = "conv_minh",
                            title = "Minh Trí",
                            mood = com.uzuu.diuchat.feature.chat.domain.model.MoodType.VUI,
                            lastMessage = "",
                            lastMessageTimeEpochMs = 0L,
                        ),
                    )
                }

                LazyColumn(
                    modifier = Modifier.height(180.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    items(sampleConversations) { convo ->
                        val isChecked = selectedIds.contains(convo.id)
                        val moodColor = Color(convo.mood.colorHex)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    if (isChecked) selectedIds.remove(convo.id) else selectedIds.add(convo.id)
                                }
                                .padding(vertical = 4.dp, horizontal = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(moodColor.copy(alpha = 0.25f)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = convo.title.take(1),
                                    style = AppTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = moodColor,
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Text(
                                text = convo.title,
                                style = AppTheme.typography.bodyMedium,
                                color = AppTheme.colors.ink,
                                modifier = Modifier.weight(1f),
                            )

                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { checked ->
                                    if (checked) selectedIds.add(convo.id) else selectedIds.remove(convo.id)
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = AppTheme.colors.accent,
                                ),
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Hủy", color = AppTheme.colors.inkSecondary)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    TextButton(
                        onClick = {
                            if (selectedIds.isNotEmpty()) {
                                onConfirmForward(selectedIds.toList())
                            }
                        },
                        enabled = selectedIds.isNotEmpty(),
                    ) {
                        Text(
                            text = "Gửi (${selectedIds.size})",
                            fontWeight = FontWeight.SemiBold,
                            color = if (selectedIds.isNotEmpty()) AppTheme.colors.accent else AppTheme.colors.inkTertiary,
                        )
                    }
                }
            }
        }
    }
}
