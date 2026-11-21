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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.uzuu.diuchat.feature.base.common.res.AppTheme
import com.uzuu.diuchat.feature.chat.domain.model.MessageReaction
import com.uzuu.diuchat.feature.chat.domain.model.MoodType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReactDetailDialog(
    reactions: List<MessageReaction>,
    onDismiss: () -> Unit,
) {
    var selectedFilterMood by remember { mutableStateOf<MoodType?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = AppTheme.colors.surfaceElevated,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Biểu cảm trên tin nhắn (${reactions.size})",
                    style = AppTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = AppTheme.colors.ink,
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Đóng",
                        tint = AppTheme.colors.inkSecondary,
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Mood tabs
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                // "Tất cả" chip
                val isAllSelected = selectedFilterMood == null
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isAllSelected) AppTheme.colors.accent.copy(alpha = 0.2f) else AppTheme.colors.surface,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isAllSelected) AppTheme.colors.accent else AppTheme.colors.line,
                    ),
                    modifier = Modifier.clickable { selectedFilterMood = null },
                ) {
                    Text(
                        text = "Tất cả ${reactions.size}",
                        style = AppTheme.typography.labelMedium,
                        color = AppTheme.colors.ink,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    )
                }

                // Mood-specific counts
                val moodCounts = reactions.groupBy { it.mood }
                moodCounts.forEach { (mood, list) ->
                    val isSelected = selectedFilterMood == mood
                    val moodColor = Color(mood.colorHex)
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) moodColor.copy(alpha = 0.25f) else AppTheme.colors.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) moodColor else AppTheme.colors.line,
                        ),
                        modifier = Modifier.clickable { selectedFilterMood = mood },
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(moodColor),
                            )
                            Text(
                                text = "${mood.displayName} ${list.size}",
                                style = AppTheme.typography.labelMedium,
                                color = AppTheme.colors.ink,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Filtered list
            val filteredReactions = if (selectedFilterMood == null) {
                reactions
            } else {
                reactions.filter { it.mood == selectedFilterMood }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(filteredReactions) { reaction ->
                    val moodColor = Color(reaction.mood.colorHex)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(moodColor.copy(alpha = 0.25f))
                                    .border(1.dp, moodColor, CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = reaction.userName.take(1),
                                    style = AppTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = moodColor,
                                )
                            }
                            Column {
                                Text(
                                    text = if (reaction.isFromMe) "${reaction.userName} (Bạn)" else reaction.userName,
                                    style = AppTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    color = AppTheme.colors.ink,
                                )
                                Text(
                                    text = reaction.mood.displayName,
                                    style = AppTheme.typography.bodySmall,
                                    color = moodColor,
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(moodColor),
                        )
                    }
                }
            }
        }
    }
}
