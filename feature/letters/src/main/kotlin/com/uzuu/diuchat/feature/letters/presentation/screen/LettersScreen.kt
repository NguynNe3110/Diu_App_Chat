package com.uzuu.diuchat.feature.letters.presentation.screen

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Drafts
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.outlined.LockClock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uzuu.diuchat.feature.base.common.res.AppTheme
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuButton
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuButtonVariant
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuCard
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuEmptyState
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuFilterChip
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuTopBar
import com.uzuu.diuchat.feature.letters.domain.model.LetterStatus
import com.uzuu.diuchat.feature.letters.domain.model.SealedLetter
import org.koin.androidx.compose.koinViewModel

@Composable
fun LettersScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    viewModel: LettersViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiStateFlow.collectAsState()

    if (uiState.isComposing) {
        ComposeLetterScreen(
            uiState = uiState,
            onBackClick = { viewModel.cancelComposing() },
            onRecipientChange = viewModel::updateDraftRecipient,
            onTitleChange = viewModel::updateDraftTitle,
            onMoodChange = viewModel::updateDraftMood,
            onShowScheduleSheet = viewModel::showScheduleSheet,
            onContentChange = viewModel::updateDraftContent,
            onSealAndSend = viewModel::sealAndSendLetter,
            onSetSchedule = viewModel::setSchedule,
        )
        return
    }

    LettersContent(
        uiState = uiState,
        onBackClick = onBackClick,
        onSelectTab = viewModel::selectTab,
        onOpenLetter = viewModel::openLetter,
        onEditLetter = viewModel::startEditing,
        onCancelLetter = viewModel::cancelLetter,
        onSendImmediately = viewModel::sendImmediately,
        onViewOpenedLetter = viewModel::viewOpenedLetter,
        onStartComposing = viewModel::startComposing,
        onDismissOpenedLetter = viewModel::dismissOpenedLetter,
        modifier = modifier,
    )
}

@Composable
fun LettersContent(
    uiState: LettersUiState,
    onBackClick: () -> Unit = {},
    onSelectTab: (LetterTab) -> Unit = {},
    onOpenLetter: (String) -> Unit = {},
    onEditLetter: (SealedLetter) -> Unit = {},
    onCancelLetter: (String) -> Unit = {},
    onSendImmediately: (String) -> Unit = {},
    onViewOpenedLetter: (SealedLetter) -> Unit = {},
    onStartComposing: () -> Unit = {},
    onDismissOpenedLetter: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors

    Surface(
        modifier = modifier.fillMaxSize(),
        color = AppTheme.colors.background,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding(),
            ) {
                DiuTopBar(
                    title = "Hộp thư gửi chậm",
                    subtitle = "Những lời nhắn gửi đến tương lai",
                    navigationIcon = if (onBackClick != {}) {
                        {
                            IconButton(onClick = onBackClick) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Quay lại",
                                    tint = colors.ink,
                                )
                            }
                        }
                    } else {
                        null
                    },
                )

                // Tab Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    LetterTab.entries.forEach { tab ->
                        DiuFilterChip(
                            text = tab.label,
                            selected = uiState.selectedTab == tab,
                            onClick = { onSelectTab(tab) },
                        )
                    }
                }

                // Danh sách thư
                if (uiState.filteredLetters.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        DiuEmptyState(
                            title = "Chưa có lá thư nào",
                            description = "Hãy viết một bức thư niêm phong và gửi đến người thương trong tương lai.",
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(uiState.filteredLetters, key = { it.id }) { letter ->
                            LetterCard(
                                letter = letter,
                                onOpenLetter = { onOpenLetter(letter.id) },
                                onEditLetter = { onEditLetter(letter) },
                                onCancelLetter = { onCancelLetter(letter.id) },
                                onSendImmediately = { onSendImmediately(letter.id) },
                                onViewOpenedLetter = { onViewOpenedLetter(letter) },
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(72.dp))
                        }
                    }
                }
            }

            // Nút viết thư mới (Floating Action Button)
            FloatingActionButton(
                onClick = onStartComposing,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
                    .navigationBarsPadding(),
                containerColor = colors.accent,
                contentColor = colors.onAccent,
                shape = CircleShape,
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Soạn thư mới",
                )
            }
        }
    }

    // Dialog đọc thư đã mở
    uiState.selectedOpenedLetter?.let { openedLetter ->
        OpenedLetterDialog(
            letter = openedLetter,
            onDismiss = onDismissOpenedLetter,
        )
    }
}

@Composable
private fun LetterCard(
    letter: SealedLetter,
    onOpenLetter: () -> Unit,
    onEditLetter: () -> Unit,
    onCancelLetter: () -> Unit,
    onSendImmediately: () -> Unit,
    onViewOpenedLetter: () -> Unit,
) {
    val colors = AppTheme.colors

    DiuCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = letter.status == LetterStatus.OPENED) { onViewOpenedLetter() },
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    val iconVector = when (letter.status) {
                        LetterStatus.SEALED_PENDING -> Icons.Default.Lock
                        LetterStatus.READY_TO_OPEN -> Icons.Default.Mail
                        LetterStatus.OPENED -> Icons.Default.Drafts
                        LetterStatus.CANCELLED -> Icons.Default.Lock
                    }
                    val iconTint = when (letter.status) {
                        LetterStatus.SEALED_PENDING -> colors.accent
                        LetterStatus.READY_TO_OPEN -> colors.danger
                        LetterStatus.OPENED -> colors.inkSecondary
                        LetterStatus.CANCELLED -> colors.inkSecondary
                    }
                    Icon(
                        imageVector = iconVector,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = if (letter.isSender) "Gửi đến: ${letter.recipientName}" else "Từ: ${letter.senderName}",
                        style = AppTheme.typography.labelSmall,
                        color = colors.inkSecondary,
                    )
                }

                // Mood tag
                Text(
                    text = "• ${letter.moodName}",
                    style = AppTheme.typography.labelSmall,
                    color = colors.accent,
                    fontWeight = FontWeight.Medium,
                )
            }

            Text(
                text = letter.title,
                style = AppTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = colors.ink,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
            )

            // Hiển thị trạng thái mở thư
            when (letter.status) {
                LetterStatus.SEALED_PENDING -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.LockClock,
                                contentDescription = null,
                                tint = colors.accent,
                                modifier = Modifier.size(16.dp),
                            )
                            Text(
                                text = "Mở vào: ${letter.scheduledUnlockFormatted}",
                                style = AppTheme.typography.bodySmall,
                                color = colors.inkSecondary,
                            )
                        }

                        // Thư của mình cho phép sửa, gửi ngay hoặc hủy trước giờ mở
                        if (letter.isSender) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                DiuButton(
                                    onClick = onEditLetter,
                                    variant = DiuButtonVariant.Outline,
                                    shape = RoundedCornerShape(16.dp),
                                ) {
                                    Text(
                                        text = "Sửa",
                                        style = AppTheme.typography.labelSmall,
                                        color = colors.accent,
                                    )
                                }
                                DiuButton(
                                    onClick = onSendImmediately,
                                    variant = DiuButtonVariant.Soft,
                                    shape = RoundedCornerShape(16.dp),
                                ) {
                                    Text(
                                        text = "Gửi ngay",
                                        style = AppTheme.typography.labelSmall,
                                        color = colors.ink,
                                    )
                                }
                                DiuButton(
                                    onClick = onCancelLetter,
                                    variant = DiuButtonVariant.Outline,
                                    shape = RoundedCornerShape(16.dp),
                                ) {
                                    Text(
                                        text = "Hủy",
                                        style = AppTheme.typography.labelSmall,
                                        color = colors.danger,
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = "Đang niêm phong 🔒",
                                style = AppTheme.typography.labelSmall,
                                color = colors.accent,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }

                LetterStatus.READY_TO_OPEN -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                    ) {
                        Text(
                            text = "Đã đến thời khắc hẹn ước! Bạn có thể mở niêm phong lá thư này ngay bây giờ.",
                            style = AppTheme.typography.bodySmall,
                            color = colors.inkSecondary,
                            modifier = Modifier.padding(bottom = 8.dp),
                        )
                        DiuButton(
                            onClick = onOpenLetter,
                            modifier = Modifier.fillMaxWidth(),
                            variant = DiuButtonVariant.Primary,
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = null)
                                Text(
                                    text = "Mở niêm phong lá thư",
                                    style = AppTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                    }
                }

                LetterStatus.OPENED -> {
                    Text(
                        text = letter.contentPayload,
                        style = AppTheme.typography.bodyMedium,
                        color = colors.ink,
                        maxLines = 2,
                        modifier = Modifier.padding(vertical = 4.dp),
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "Đã mở: ${letter.openedAtFormatted ?: "Trước đây"}",
                            style = AppTheme.typography.bodySmall,
                            color = colors.inkSecondary,
                        )
                        Text(
                            text = "Xem chi tiết →",
                            style = AppTheme.typography.labelSmall,
                            color = colors.accent,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }

                LetterStatus.CANCELLED -> {
                    Text(
                        text = "Lá thư đã bị hủy bởi người gửi.",
                        style = AppTheme.typography.bodySmall,
                        color = colors.inkSecondary,
                    )
                }
            }
        }
    }
}

@Composable
private fun OpenedLetterDialog(
    letter: SealedLetter,
    onDismiss: () -> Unit,
) {
    val colors = AppTheme.colors

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = colors.accent,
                )
                Text(
                    text = letter.title,
                    style = AppTheme.typography.titleMedium,
                    color = colors.ink,
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Gửi từ: ${letter.senderName} • Tâm trạng: ${letter.moodName}",
                    style = AppTheme.typography.bodySmall,
                    color = colors.inkSecondary,
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.surface)
                        .padding(14.dp),
                ) {
                    Text(
                        text = letter.contentPayload,
                        style = AppTheme.typography.bodyMedium,
                        color = colors.ink,
                        lineHeight = 22.sp,
                    )
                }
                Text(
                    text = "Thời điểm mở khóa: ${letter.openedAtFormatted ?: letter.scheduledUnlockFormatted}",
                    style = AppTheme.typography.labelSmall,
                    color = colors.inkSecondary,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng lại", color = colors.accent)
            }
        },
    )
}

@Preview(name = "Letters Screen — Light", showBackground = true)
@Composable
private fun LettersScreenPreview() {
    AppTheme {
        LettersContent(
            uiState = LettersUiState(
                letters = listOf(
                    SealedLetter(
                        id = "l1",
                        senderId = "me",
                        senderName = "Tôi",
                        recipientId = "linh",
                        recipientName = "Linh",
                        title = "Hẹn ước mùa hoa",
                        scheduledUnlockEpochMs = 1_770_000_000_000L,
                        scheduledUnlockFormatted = "08:00 Sáng mai",
                        status = LetterStatus.SEALED_PENDING,
                        isSender = true,
                        contentPayload = "Dịu dàng gửi em",
                        moodName = "Bình yên",
                    ),
                    SealedLetter(
                        id = "l2",
                        senderId = "linh",
                        senderName = "Linh",
                        recipientId = "me",
                        recipientName = "Tôi",
                        title = "Mở ra khi nhớ anh",
                        scheduledUnlockEpochMs = 1_760_000_000_000L,
                        scheduledUnlockFormatted = "Hôm nay",
                        status = LetterStatus.READY_TO_OPEN,
                        isSender = false,
                        contentPayload = "Em nhớ anh rất nhiều!",
                        moodName = "Thương",
                    ),
                ),
            ),
        )
    }
}
