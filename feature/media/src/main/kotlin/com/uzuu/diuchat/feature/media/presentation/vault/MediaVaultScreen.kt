package com.uzuu.diuchat.feature.media.presentation.vault

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.uzuu.diuchat.feature.base.common.res.AppIcons
import com.uzuu.diuchat.feature.base.common.res.AppTheme
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuAmbientBackground
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuCard
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuEmptyState
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuFilterChip
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuIconButton
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuMoodDot
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuSectionHeader
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuTopBar
import com.uzuu.diuchat.feature.media.domain.model.MediaType
import com.uzuu.diuchat.feature.media.domain.model.MediaVaultItem
import org.koin.androidx.compose.koinViewModel

@Composable
fun MediaVaultScreen(
    onBackClick: () -> Unit,
    onOpenPhoto: (MediaVaultItem) -> Unit,
    onOpenStorage: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MediaVaultViewModel = koinViewModel(),
) {
    val state by viewModel.uiStateFlow.collectAsStateWithLifecycle()

    MediaVaultContent(
        state = state,
        onBackClick = onBackClick,
        onOpenPhoto = onOpenPhoto,
        onOpenStorage = onOpenStorage,
        onSelectType = viewModel::onSelectType,
        onTogglePlayVoice = viewModel::onTogglePlayVoice,
        modifier = modifier,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MediaVaultContent(
    state: MediaVaultUiState,
    onBackClick: () -> Unit,
    onOpenPhoto: (MediaVaultItem) -> Unit,
    onOpenStorage: () -> Unit,
    onSelectType: (MediaType) -> Unit,
    onTogglePlayVoice: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    DiuAmbientBackground(modifier = modifier) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                DiuTopBar(
                    title = "Kho lưu trữ & Kỷ niệm",
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
                    actions = {
                        DiuIconButton(
                            onClick = onOpenStorage,
                            contentDescription = "Quản lý dung lượng",
                        ) {
                            Icon(
                                painter = painterResource(AppIcons.Trash),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    },
                )
            },
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            ) {
                // Filter chips
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(MediaType.entries.toTypedArray()) { type ->
                        DiuFilterChip(
                            text = type.label,
                            selected = state.selectedType == type,
                            onClick = { onSelectType(type) },
                        )
                    }
                }

                if (state.isEmpty) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        DiuEmptyState(
                            title = "Chưa có kỷ niệm nào",
                            description = "Các hình ảnh, tin nhắn thoại và thư đã mở sẽ được lưu giữ tự động tại đây.",
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        state.groupedByMonth.forEach { (month, items) ->
                            item(key = "header_$month") {
                                DiuSectionHeader(title = month)
                            }

                            val photos = items.filter { it.type == MediaType.PHOTO }
                            if (photos.isNotEmpty()) {
                                item(key = "photos_$month") {
                                    FlowRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        maxItemsInEachRow = 3,
                                    ) {
                                        photos.forEach { photo ->
                                            PhotoThumbnail(
                                                item = photo,
                                                onClick = { onOpenPhoto(photo) },
                                                modifier = Modifier.weight(1f),
                                            )
                                        }
                                    }
                                }
                            }

                            val nonPhotos = items.filter { it.type != MediaType.PHOTO }
                            if (nonPhotos.isNotEmpty()) {
                                items(nonPhotos, key = { it.id }) { nonPhoto ->
                                    when (nonPhoto.type) {
                                        MediaType.VOICE -> VoiceVaultCard(
                                            item = nonPhoto,
                                            isPlaying = state.currentlyPlayingVoiceId == nonPhoto.id,
                                            onTogglePlay = { onTogglePlayVoice(nonPhoto.id) },
                                        )

                                        MediaType.LETTER -> LetterVaultCard(item = nonPhoto)

                                        else -> Unit
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PhotoThumbnail(
    item: MediaVaultItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var hasLoadError by remember(item.url) { mutableStateOf(false) }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        item.moodColor.copy(alpha = 0.45f),
                        item.moodColor.copy(alpha = 0.15f),
                        AppTheme.colors.surface,
                    ),
                ),
            )
            .clickable(role = Role.Button, onClick = onClick),
    ) {
        if (item.url.isNotBlank() && !hasLoadError) {
            AsyncImage(
                model = item.url,
                contentDescription = item.caption.ifBlank { "Hình ảnh" },
                contentScale = ContentScale.Crop,
                onError = { hasLoadError = true },
                onSuccess = { hasLoadError = false },
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Icon(
                painter = painterResource(AppIcons.Image),
                contentDescription = null,
                tint = item.moodColor.copy(alpha = 0.8f),
                modifier = Modifier
                    .size(28.dp)
                    .align(Alignment.Center),
            )
        }

        // Mood dot top right
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp),
        ) {
            DiuMoodDot(color = item.moodColor, size = 10.dp)
        }

        // Caption at bottom
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.65f)),
                    ),
                )
                .padding(horizontal = 8.dp, vertical = 6.dp),
        ) {
            Text(
                text = item.caption.ifBlank { "Hình ảnh" },
                style = AppTheme.typography.bodySmall,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun VoiceVaultCard(
    item: MediaVaultItem,
    isPlaying: Boolean,
    onTogglePlay: () -> Unit,
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
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .clickable(role = Role.Button, onClick = onTogglePlay),
                color = item.moodColor.copy(alpha = 0.2f),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(if (isPlaying) AppIcons.Pause else AppIcons.Play),
                        contentDescription = if (isPlaying) "Tạm dừng" else "Phát",
                        tint = item.moodColor,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    DiuMoodDot(color = item.moodColor, size = 8.dp)
                    Text(
                        text = item.caption.ifBlank { "Lời nhắn thoại" },
                        style = AppTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = AppTheme.colors.ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${item.durationSeconds}s · ${item.senderName} · ${item.timestamp}",
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.inkSecondary,
                )
            }
        }
    }
}

@Composable
private fun LetterVaultCard(
    item: MediaVaultItem,
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
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape),
                color = item.moodColor.copy(alpha = 0.2f),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(AppIcons.Envelope),
                        contentDescription = "Thư đã mở",
                        tint = item.moodColor,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    DiuMoodDot(color = item.moodColor, size = 8.dp)
                    Text(
                        text = item.caption.ifBlank { "Thư đã mở" },
                        style = AppTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = AppTheme.colors.ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${item.senderName} · ${item.timestamp}",
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.inkSecondary,
                )
            }
        }
    }
}

@Preview(name = "Media Vault Screen — Light", showBackground = true)
@Composable
private fun MediaVaultScreenPreview() {
    AppTheme {
        val sampleItems = listOf(
            MediaVaultItem(
                id = "item_1",
                conversationId = "conv_1",
                type = MediaType.PHOTO,
                caption = "Chiều hoàng hôn bên hồ",
                timestamp = "17:30 20/11",
                senderName = "Linh",
                monthGroup = "Tháng 11, 2026",
            ),
            MediaVaultItem(
                id = "item_2",
                conversationId = "conv_1",
                type = MediaType.VOICE,
                caption = "Lời chúc ngủ ngon",
                durationSeconds = 42,
                timestamp = "22:15 19/11",
                senderName = "Tôi",
                monthGroup = "Tháng 11, 2026",
            ),
            MediaVaultItem(
                id = "item_3",
                conversationId = "conv_1",
                type = MediaType.LETTER,
                caption = "Bức thư đầu tiên đã mở",
                timestamp = "08:00 15/11",
                senderName = "Linh",
                monthGroup = "Tháng 11, 2026",
            ),
        )
        MediaVaultContent(
            state = MediaVaultUiState(
                allItems = sampleItems,
                filteredItems = sampleItems,
            ),
            onBackClick = {},
            onOpenPhoto = {},
            onOpenStorage = {},
            onSelectType = {},
            onTogglePlayVoice = {},
        )
    }
}
