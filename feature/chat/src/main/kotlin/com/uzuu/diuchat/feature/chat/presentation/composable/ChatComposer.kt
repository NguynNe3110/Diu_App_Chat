package com.uzuu.diuchat.feature.chat.presentation.composable

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.uzuu.diuchat.feature.base.common.res.AppTheme
import com.uzuu.diuchat.feature.chat.domain.model.MoodType

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
) {
    val selectedColor = Color(selectedMood.colorHex)

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = AppTheme.colors.surfaceElevated,
        tonalElevation = 4.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.line),
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            // Photo Preview Bar if a photo is attached
            AnimatedVisibility(
                visible = pendingPhotoUri != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                if (pendingPhotoUri != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.5.dp, selectedColor, RoundedCornerShape(10.dp)),
                        ) {
                            AsyncImage(
                                model = pendingPhotoUri,
                                contentDescription = "Xem trước ảnh",
                                modifier = Modifier.size(68.dp),
                                contentScale = ContentScale.Crop,
                            )
                        }

                        Text(
                            text = "Ảnh đính kèm sẵn sàng gửi (thêm lời nhắn bên dưới)",
                            style = AppTheme.typography.bodySmall,
                            color = AppTheme.colors.inkSecondary,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp),
                        )

                        IconButton(
                            onClick = onClearPhotoClick,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(AppTheme.colors.surface),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Bỏ ảnh",
                                tint = AppTheme.colors.inkSecondary,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }
            }

            // Expandable Mood Tray with 6 mood states
            AnimatedVisibility(
                visible = isMoodTrayOpen,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                Column(modifier = Modifier.padding(bottom = 8.dp)) {
                    Text(
                        text = "Chọn màu cảm xúc cho tin nhắn:",
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colors.inkSecondary,
                        modifier = Modifier.padding(start = 4.dp, bottom = 6.dp),
                    )

                    Row(
                        modifier =
                        Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        MoodType.entries.forEach { mood ->
                            val moodColor = Color(mood.colorHex)
                            val isSelected = mood == selectedMood
                            val chipBg = if (isSelected) moodColor.copy(alpha = 0.25f) else AppTheme.colors.surface
                            val chipBorder = if (isSelected) moodColor else AppTheme.colors.line

                            Box(
                                modifier =
                                Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(chipBg)
                                    .border(1.dp, chipBorder, RoundedCornerShape(16.dp))
                                    .clickable(onClickLabel = "Chọn cảm xúc ${mood.displayName}") { onSelectMood(mood) }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Box(
                                        modifier =
                                        Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(moodColor),
                                    )
                                    Text(
                                        text = mood.displayName,
                                        style =
                                        AppTheme.typography.labelMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                        ),
                                        color = if (isSelected) AppTheme.colors.ink else AppTheme.colors.inkSecondary,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Composer input row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Mood Tray toggle button
                IconButton(
                    onClick = onToggleMoodTray,
                    modifier = Modifier.size(36.dp),
                ) {
                    Box(
                        modifier =
                        Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(selectedColor.copy(alpha = 0.2f))
                            .border(1.5.dp, selectedColor, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mood,
                            contentDescription = "Chọn màu cảm xúc",
                            tint = selectedColor,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }

                // Photo attachment button
                IconButton(
                    onClick = onAttachPhotoClick,
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "Đính kèm ảnh",
                        tint = if (pendingPhotoUri != null) selectedColor else AppTheme.colors.inkSecondary,
                        modifier = Modifier.size(20.dp),
                    )
                }

                Spacer(modifier = Modifier.width(2.dp))

                // Input Text Field
                OutlinedTextField(
                    value = inputContent,
                    onValueChange = onInputChange,
                    placeholder = {
                        Text(
                            text = if (pendingPhotoUri != null) "Thêm lời nhắn cho ảnh..." else "Gửi lời dịu êm...",
                            color = AppTheme.colors.inkTertiary,
                            fontSize = 14.sp,
                        )
                    },
                    modifier =
                    Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors =
                    OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = AppTheme.colors.surface,
                        unfocusedContainerColor = AppTheme.colors.surface,
                        focusedBorderColor = selectedColor,
                        unfocusedBorderColor = AppTheme.colors.line,
                        focusedTextColor = AppTheme.colors.ink,
                        unfocusedTextColor = AppTheme.colors.ink,
                    ),
                    maxLines = 4,
                )

                Spacer(modifier = Modifier.width(4.dp))

                // Voice Recording or Send Button
                val canSend = (inputContent.trim().isNotEmpty() || pendingPhotoUri != null) && !isSending
                if (canSend) {
                    IconButton(
                        onClick = onSendClick,
                        modifier =
                        Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(selectedColor),
                    ) {
                        if (isSending) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Gửi tin nhắn",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                } else {
                    // Mic button for voice recording
                    IconButton(
                        onClick = onStartVoiceRecording,
                        modifier =
                        Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(AppTheme.colors.surface)
                            .border(1.dp, AppTheme.colors.line, CircleShape),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Ghi âm thoại",
                            tint = selectedColor,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }
    }
}
