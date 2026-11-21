package com.uzuu.diuchat.feature.media.presentation.storage

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.uzuu.diuchat.feature.base.common.res.AppIcons
import com.uzuu.diuchat.feature.base.common.res.AppPalette
import com.uzuu.diuchat.feature.base.common.res.AppTheme
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuAmbientBackground
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuButton
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuButtonVariant
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuCard
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuConfirmationDialog
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuFilterChip
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuIconButton
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuInlineNotice
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuSectionHeader
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuSettingsRow
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuSwitch
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuTopBar
import com.uzuu.diuchat.feature.media.domain.model.AutoDownloadPolicy
import com.uzuu.diuchat.feature.media.domain.model.ConversationStorage
import com.uzuu.diuchat.feature.media.domain.model.StorageStats
import org.koin.androidx.compose.koinViewModel

@Composable
fun StorageScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: StorageViewModel = koinViewModel(),
) {
    val state by viewModel.uiStateFlow.collectAsStateWithLifecycle()

    StorageContent(
        state = state,
        onBackClick = onBackClick,
        onSetAutoDownloadPolicy = viewModel::onSetAutoDownloadPolicy,
        onToggleAutoDownloadVoice = viewModel::onToggleAutoDownloadVoice,
        onToggleKeepOriginalQuality = viewModel::onToggleKeepOriginalQuality,
        onShowClearCacheDialog = viewModel::onShowClearCacheDialog,
        onConfirmClearCache = viewModel::onConfirmClearCache,
        onSelectConversationToClear = viewModel::onSelectConversationToClear,
        onConfirmClearConversationMedia = viewModel::onConfirmClearConversationMedia,
        modifier = modifier,
    )
}

@Composable
fun StorageContent(
    state: StorageUiState,
    onBackClick: () -> Unit,
    onSetAutoDownloadPolicy: (AutoDownloadPolicy) -> Unit,
    onToggleAutoDownloadVoice: (Boolean) -> Unit,
    onToggleKeepOriginalQuality: (Boolean) -> Unit,
    onShowClearCacheDialog: (Boolean) -> Unit,
    onConfirmClearCache: () -> Unit,
    onSelectConversationToClear: (ConversationStorage?) -> Unit,
    onConfirmClearConversationMedia: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DiuAmbientBackground(modifier = modifier) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                DiuTopBar(
                    title = "Dung lượng & Dữ liệu",
                    navigationIcon = {
                        DiuIconButton(
                            onClick = onBackClick,
                            contentDescription = "Quay lại",
                        ) {
                            Icon(
                                painter = painterResource(AppIcons.Back),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    },
                )
            },
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Success message notice
                if (state.lastClearedMessage != null) {
                    item {
                        DiuInlineNotice(
                            message = state.lastClearedMessage,
                        )
                    }
                }

                // Storage Overview Card
                item {
                    StorageOverviewCard(stats = state.stats)
                }

                // Clear Cache Action Card
                item {
                    DiuSectionHeader(title = "DỌN DẸP BỘ NHỚ AN TOÀN")
                    Spacer(modifier = Modifier.height(4.dp))
                    DiuCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                        ) {
                            Text(
                                text = "Bộ nhớ đệm tạm thời: ${state.stats.formattedCache}",
                                style = AppTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = AppTheme.colors.ink,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Chỉ xóa các hình ảnh và tệp âm thanh xem trước. " +
                                    "Không xóa tin nhắn chưa gửi hoặc dữ liệu gốc trên máy chủ.",
                                style = AppTheme.typography.bodySmall,
                                color = AppTheme.colors.inkSecondary,
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            DiuButton(
                                onClick = { onShowClearCacheDialog(true) },
                                variant = DiuButtonVariant.Outline,
                                enabled = !state.isClearing && state.stats.cacheBytes > 0L,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(if (state.isClearing) "Đang dọn dẹp..." else "Dọn dẹp bộ nhớ đệm")
                            }
                        }
                    }
                }

                // Auto Download & Quality Options
                item {
                    DiuSectionHeader(title = "TỰ ĐỘNG TẢI & CHẤT LƯỢNG")
                    Spacer(modifier = Modifier.height(4.dp))
                    DiuCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            DiuSettingsRow(
                                title = "Chính sách tự động tải",
                                subtitle = state.autoDownloadPolicy.description,
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                AutoDownloadPolicy.entries.forEach { policy ->
                                    val isSelected = state.autoDownloadPolicy == policy
                                    DiuFilterChip(
                                        text = policy.label,
                                        selected = isSelected,
                                        onClick = { onSetAutoDownloadPolicy(policy) },
                                    )
                                }
                            }
                            DiuSettingsRow(
                                title = "Tự động tải lời nhắn thoại",
                                subtitle = "Luôn tải trước lời nhắn thoại để phát tức thì",
                                trailing = {
                                    DiuSwitch(
                                        checked = state.autoDownloadVoice,
                                        onCheckedChange = onToggleAutoDownloadVoice,
                                        contentDescription = "Tự động tải lời nhắn thoại",
                                    )
                                },
                            )
                            DiuSettingsRow(
                                title = "Giữ chất lượng ảnh gốc",
                                subtitle = "Không nén ảnh khi gửi (sử dụng nhiều dung lượng hơn)",
                                trailing = {
                                    DiuSwitch(
                                        checked = state.keepOriginalQuality,
                                        onCheckedChange = onToggleKeepOriginalQuality,
                                        contentDescription = "Giữ chất lượng ảnh gốc",
                                    )
                                },
                            )
                        }
                    }
                }

                // Breakdown by Conversation
                if (state.stats.conversations.isNotEmpty()) {
                    item {
                        DiuSectionHeader(title = "THEO CUỘC TRÒ CHUYỆN")
                    }

                    items(state.stats.conversations, key = { it.conversationId }) { conv ->
                        ConversationStorageCard(
                            item = conv,
                            onClearClick = { onSelectConversationToClear(conv) },
                        )
                    }
                }
            }
        }

        // Confirmation Dialog - All Cache
        if (state.showClearDialog) {
            DiuConfirmationDialog(
                title = "Dọn dẹp bộ nhớ đệm?",
                message = "Thao tác này sẽ xóa ${state.stats.formattedCache} dữ liệu tạm thời trên thiết bị. " +
                    "Bạn vẫn có thể tải lại ảnh và tin nhắn bất cứ lúc nào.",
                confirmLabel = "Dọn dẹp ngay",
                dismissLabel = "Hủy",
                onConfirm = onConfirmClearCache,
                onDismiss = { onShowClearCacheDialog(false) },
            )
        }

        // Confirmation Dialog - Per Conversation Media
        if (state.showConversationClearDialog && state.selectedConversationToClear != null) {
            val conv = state.selectedConversationToClear
            DiuConfirmationDialog(
                title = "Xóa tệp đệm của ${conv.contactName}?",
                message = "Thao tác này sẽ dọn dẹp ${conv.formattedSize} tệp ảnh và ghi âm tạm thời " +
                    "của ${conv.contactName}. Dữ liệu trên máy chủ hoặc tin nhắn chưa gửi không bị ảnh hưởng.",
                confirmLabel = "Xóa ngay",
                dismissLabel = "Hủy",
                destructive = true,
                onConfirm = onConfirmClearConversationMedia,
                onDismiss = { onSelectConversationToClear(null) },
            )
        }
    }
}

@Composable
private fun StorageOverviewCard(
    stats: StorageStats,
    modifier: Modifier = Modifier,
) {
    DiuCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
        ) {
            Text(
                text = "Tổng dung lượng sử dụng",
                style = AppTheme.typography.bodySmall,
                color = AppTheme.colors.inkSecondary,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stats.formattedTotal,
                style = AppTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                ),
                color = AppTheme.colors.ink,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Multi-segment progress bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(AppTheme.colors.surface),
            ) {
                if (stats.photosPercent > 0.01f) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(stats.photosPercent.coerceAtLeast(0.01f))
                            .background(AppPalette.MoodThuong),
                    )
                }
                if (stats.voicePercent > 0.01f) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(stats.voicePercent.coerceAtLeast(0.01f))
                            .background(AppPalette.MoodVui),
                    )
                }
                if (stats.cachePercent > 0.01f) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(stats.cachePercent.coerceAtLeast(0.01f))
                            .background(AppTheme.colors.line),
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                LegendItem(
                    color = AppPalette.MoodThuong,
                    label = "Hình ảnh",
                    sizeText = stats.formattedPhotos,
                )
                LegendItem(
                    color = AppPalette.MoodVui,
                    label = "Ghi âm",
                    sizeText = stats.formattedVoice,
                )
                LegendItem(
                    color = AppTheme.colors.line,
                    label = "Bộ nhớ đệm",
                    sizeText = stats.formattedCache,
                )
            }
        }
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String,
    sizeText: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color),
        )
        Column {
            Text(
                text = label,
                style = AppTheme.typography.labelSmall,
                color = AppTheme.colors.inkSecondary,
            )
            Text(
                text = sizeText,
                style = AppTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = AppTheme.colors.ink,
            )
        }
    }
}

@Composable
private fun ConversationStorageCard(
    item: ConversationStorage,
    onClearClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DiuCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = item.moodColor.copy(alpha = 0.2f),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = item.initials,
                        style = AppTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = item.moodColor,
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.contactName,
                    style = AppTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = AppTheme.colors.ink,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${item.photoCount} ảnh · ${item.voiceCount} ghi âm",
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.inkSecondary,
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = item.formattedSize,
                    style = AppTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = AppTheme.colors.ink,
                )
                if (item.totalBytes > 0L) {
                    TextButton(
                        onClick = onClearClick,
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                    ) {
                        Text(
                            text = "Dọn đệm",
                            style = AppTheme.typography.labelSmall,
                            color = AppTheme.colors.accent,
                        )
                    }
                }
            }
        }
    }
}
