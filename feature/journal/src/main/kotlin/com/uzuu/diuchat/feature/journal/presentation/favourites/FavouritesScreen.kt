@file:Suppress("ktlint:compose:vm-forwarding-check")

package com.uzuu.diuchat.feature.journal.presentation.favourites

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.uzuu.diuchat.feature.base.common.res.AppIcons
import com.uzuu.diuchat.feature.base.common.res.AppTheme
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuCard
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuEmptyState
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuIconButton
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuOrbAvatar
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuTopBar
import com.uzuu.diuchat.feature.journal.domain.model.FavouriteMemory
import com.uzuu.diuchat.feature.journal.domain.model.JournalMoodType
import org.koin.androidx.compose.koinViewModel

@Composable
fun FavouritesScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    onMemoryClick: (FavouriteMemory) -> Unit = {},
    viewModel: FavouritesViewModel = koinViewModel(),
) {
    val state by viewModel.uiStateFlow.collectAsStateWithLifecycle()

    FavouritesContent(
        state = state,
        onBackClick = onBackClick,
        onMemoryClick = onMemoryClick,
        onToggleFavourite = viewModel::onToggleFavourite,
        modifier = modifier,
    )
}

@Composable
fun FavouritesContent(
    state: FavouritesUiState,
    onBackClick: () -> Unit,
    onMemoryClick: (FavouriteMemory) -> Unit,
    onToggleFavourite: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors

    Scaffold(
        topBar = {
            DiuTopBar(
                title = "Kỷ niệm đã lưu",
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
        containerColor = Color.Transparent,
        modifier = modifier
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) { paddingValues ->
        if (state.memories.isEmpty() && !state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                DiuEmptyState(
                    title = "Chưa có kỷ niệm nào",
                    description = "Những tin nhắn bạn lưu lại sẽ xuất hiện ở đây để dễ tìm lại khi cần một chút ấm áp.",
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(state.memories, key = { it.id }) { memory ->
                    FavouriteMemoryCard(
                        memory = memory,
                        onClick = { onMemoryClick(memory) },
                        onToggleFavourite = { onToggleFavourite(memory.id) },
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Những tin nhắn bạn lưu lại sẽ xuất hiện ở đây để dễ tìm lại khi cần một chút ấm áp.",
                        style = AppTheme.typography.bodySmall,
                        color = colors.inkSecondary,
                        modifier = Modifier.padding(horizontal = 4.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun FavouriteMemoryCard(
    memory: FavouriteMemory,
    onClick: () -> Unit,
    onToggleFavourite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors

    DiuCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                DiuOrbAvatar(
                    initials = memory.senderName.take(1).uppercase(),
                    moodColor = memory.mood.color,
                    size = 32.dp,
                )
                Text(
                    text = memory.senderName,
                    style = AppTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.ink,
                )
                Text(
                    text = memory.savedDate,
                    style = AppTheme.typography.labelSmall,
                    color = colors.inkSecondary,
                    modifier = Modifier.weight(1f),
                )
                DiuIconButton(
                    onClick = onToggleFavourite,
                    contentDescription = "Bỏ lưu kỷ niệm",
                ) {
                    Icon(
                        painter = painterResource(AppIcons.Heart),
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            Text(
                text = memory.content,
                style = AppTheme.typography.bodyMedium,
                color = colors.ink,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FavouritesScreenPreview() {
    AppTheme {
        FavouritesContent(
            state = FavouritesUiState(
                memories = listOf(
                    FavouriteMemory(
                        id = "fav_1",
                        senderName = "Minh",
                        mood = JournalMoodType.THUONG,
                        content = "Cảm ơn vì đã luôn ở bên mình.",
                        savedDate = "14/02/2024",
                    ),
                    FavouriteMemory(
                        id = "fav_2",
                        senderName = "Bà nội",
                        mood = JournalMoodType.BINH_YEN,
                        content = "Con gái nhớ ăn uống đúng giờ nhé.",
                        savedDate = "08/03/2024",
                    ),
                ),
            ),
            onBackClick = {},
            onMemoryClick = {},
            onToggleFavourite = {},
        )
    }
}
