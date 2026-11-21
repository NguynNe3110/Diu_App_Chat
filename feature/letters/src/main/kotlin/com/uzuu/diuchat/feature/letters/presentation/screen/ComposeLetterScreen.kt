package com.uzuu.diuchat.feature.letters.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Favorite
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uzuu.diuchat.feature.base.common.res.AppTheme
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuButton
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuCard
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuFilterChip
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuTopBar
import com.uzuu.diuchat.feature.letters.domain.model.LetterScheduleType
import com.uzuu.diuchat.feature.letters.presentation.composable.ScheduleSheet

@Composable
fun ComposeLetterScreen(
    uiState: LettersUiState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    onTitleChange: (String) -> Unit = {},
    onMoodChange: (String) -> Unit = {},
    onShowScheduleSheet: (Boolean) -> Unit = {},
    onContentChange: (String) -> Unit = {},
    onSealAndSend: () -> Unit = {},
    onSetSchedule: (LetterScheduleType, String) -> Unit = { _, _ -> },
) {
    val colors = AppTheme.colors
    val scrollState = rememberScrollState()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = AppTheme.colors.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            DiuTopBar(
                title = "Viết thư gửi chậm",
                subtitle = "Gửi đến: ${uiState.draftRecipientName}",
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = colors.ink,
                        )
                    }
                },
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp),
            ) {
                // Tiêu đề thư
                Text(
                    text = "Tiêu đề lá thư",
                    style = AppTheme.typography.labelSmall,
                    color = colors.inkSecondary,
                    modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                )
                OutlinedTextField(
                    value = uiState.draftTitle,
                    onValueChange = onTitleChange,
                    placeholder = {
                        Text(
                            text = "Ví dụ: Gửi em buổi sáng mai...",
                            color = colors.inkSecondary.copy(alpha = 0.6f),
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.accent,
                        unfocusedBorderColor = colors.line,
                        focusedTextColor = colors.ink,
                        unfocusedTextColor = colors.ink,
                    ),
                )

                // Cảm xúc gắn liền với thư
                Text(
                    text = "Cảm xúc gửi gắm",
                    style = AppTheme.typography.labelSmall,
                    color = colors.inkSecondary,
                    modifier = Modifier.padding(top = 16.dp, bottom = 6.dp),
                )
                val moods = listOf("Thương", "Vui", "Bình yên", "Nhớ", "Trầm", "Lo")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    moods.forEach { mood ->
                        DiuFilterChip(
                            text = mood,
                            selected = uiState.draftMood == mood,
                            onClick = { onMoodChange(mood) },
                        )
                    }
                }

                // Chọn thời điểm mở thư
                Text(
                    text = "Thời khắc mở thư",
                    style = AppTheme.typography.labelSmall,
                    color = colors.inkSecondary,
                    modifier = Modifier.padding(top = 16.dp, bottom = 6.dp),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surface)
                        .clickable { onShowScheduleSheet(true) }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(colors.accent.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AccessTime,
                                contentDescription = null,
                                tint = colors.accent,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        Column {
                            Text(
                                text = uiState.draftScheduledUnlockFormatted,
                                style = AppTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.ink,
                            )
                            Text(
                                text = uiState.draftScheduleType.label,
                                style = AppTheme.typography.bodySmall,
                                color = colors.inkSecondary,
                            )
                        }
                    }
                    Text(
                        text = "Thay đổi",
                        style = AppTheme.typography.labelSmall,
                        color = colors.accent,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                // Nội dung lá thư
                Text(
                    text = "Nội dung tâm sự",
                    style = AppTheme.typography.labelSmall,
                    color = colors.inkSecondary,
                    modifier = Modifier.padding(top = 16.dp, bottom = 6.dp),
                )
                OutlinedTextField(
                    value = uiState.draftContent,
                    onValueChange = onContentChange,
                    placeholder = {
                        Text(
                            "Viết những điều bạn muốn nói nhưng chưa vội gửi ngay...",
                            color = colors.inkSecondary.copy(alpha = 0.6f),
                            lineHeight = 22.sp,
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 180.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.accent,
                        unfocusedBorderColor = colors.line,
                        focusedTextColor = colors.ink,
                        unfocusedTextColor = colors.ink,
                    ),
                )

                // Lưu ý niêm phong
                Box(modifier = Modifier.padding(vertical = 14.dp)) {
                    DiuCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = colors.accent,
                                modifier = Modifier.size(20.dp),
                            )
                            Text(
                                text = "Lá thư được niêm phong an toàn. " +
                                    "Người nhận chỉ thấy thời điểm đếm ngược và không thể xem nội dung trước giờ hẹn.",
                                style = AppTheme.typography.bodySmall,
                                color = colors.inkSecondary,
                                lineHeight = 18.sp,
                            )
                        }
                    }
                }
            }

            // Nút niêm phong thư
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .padding(16.dp),
            ) {
                DiuButton(
                    onClick = onSealAndSend,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState.draftTitle.isNotBlank() && uiState.draftContent.isNotBlank(),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(imageVector = Icons.Outlined.Favorite, contentDescription = null)
                        Text(
                            text = "Niêm phong lá thư",
                            style = AppTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }

    if (uiState.showScheduleSheet) {
        ScheduleSheet(
            selectedType = uiState.draftScheduleType,
            onSelectSchedule = onSetSchedule,
            onDismiss = { onShowScheduleSheet(false) },
        )
    }
}

@Preview(name = "Compose Letter Screen — Light", showBackground = true)
@Composable
private fun ComposeLetterScreenPreview() {
    AppTheme {
        ComposeLetterScreen(
            uiState = LettersUiState(
                draftTitle = "Gửi ngày mai dịu êm",
                draftContent = "Mong ngày mai của em sẽ thật nhẹ nhàng và bình an...",
                draftMood = "Bình yên",
                draftScheduledUnlockFormatted = "08:00 Sáng mai",
            ),
            onBackClick = {},
        )
    }
}
