package com.uzuu.diuchat.feature.journal.presentation.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.uzuu.diuchat.feature.base.common.res.AppIcons
import com.uzuu.diuchat.feature.base.common.res.AppTheme
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuCard
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuCardSkeleton
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuEmptyState
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuFilterChip
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuIconButton
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuMoodDot
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuSearchField
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuSectionHeader
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuShimmerContainer
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuTopBar
import com.uzuu.diuchat.feature.journal.domain.model.JournalMoodType
import com.uzuu.diuchat.feature.journal.domain.model.SearchResultItem
import org.koin.androidx.compose.koinViewModel

@Composable
fun SearchScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    conversationId: String? = null,
    conversationName: String? = null,
    onResultClick: (SearchResultItem) -> Unit = {},
    viewModel: SearchViewModel = koinViewModel(),
) {
    val state by viewModel.uiStateFlow.collectAsStateWithLifecycle()

    LaunchedEffect(conversationId, conversationName) {
        viewModel.initScope(conversationId, conversationName)
    }

    SearchContent(
        state = state,
        onBackClick = onBackClick,
        onQueryChange = viewModel::onQueryChange,
        onSelectMood = viewModel::onSelectMood,
        onResultClick = onResultClick,
        modifier = modifier,
    )
}

@Composable
fun SearchContent(
    state: SearchUiState,
    onBackClick: () -> Unit,
    onQueryChange: (String) -> Unit,
    onSelectMood: (JournalMoodType?) -> Unit,
    onResultClick: (SearchResultItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            DiuTopBar(
                title = if (state.conversationName != null) {
                    "Tìm trong trò chuyện"
                } else {
                    "Tìm kiếm"
                },
                subtitle = state.conversationName,
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
        modifier = modifier,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding(),
        ) {
            // Search Input Field
            DiuSearchField(
                value = state.query,
                onValueChange = onQueryChange,
                label = "Tìm kiếm",
                placeholder = if (state.conversationName != null) {
                    "Tìm trong trò chuyện với ${state.conversationName}…"
                } else {
                    "Tìm kiếm tin nhắn, bạn bè, ảnh…"
                },
                onClear = { onQueryChange("") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
            )

            // Mood Filter Chips Row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    DiuFilterChip(
                        text = "Mọi màu",
                        selected = state.selectedMood == null,
                        onClick = { onSelectMood(null) },
                    )
                }

                items(JournalMoodType.entries) { mood ->
                    DiuFilterChip(
                        text = mood.displayName,
                        selected = state.selectedMood == mood,
                        onClick = { onSelectMood(mood) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Results List, Loading Skeleton, or Empty State
            if (state.isLoading) {
                DiuShimmerContainer(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        repeat(4) {
                            DiuCardSkeleton(lineCount = 2)
                        }
                    }
                }
            } else if (state.results.isEmpty() && state.query.isNotBlank()) {
                DiuEmptyState(
                    title = "Không tìm thấy kết quả",
                    description = "Thử tìm bằng từ khóa hoặc bộ lọc màu cảm xúc khác nhé.",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp),
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                ) {
                    if (state.results.isNotEmpty()) {
                        item {
                            DiuSectionHeader(title = "Kết quả (${state.results.size})")
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        items(
                            items = state.results,
                            key = { it.id },
                        ) { item ->
                            SearchResultCard(
                                item = item,
                                searchQuery = state.query,
                                onClick = { onResultClick(item) },
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    } else if (state.query.isBlank()) {
                        item {
                            DiuSectionHeader(title = "Gợi ý tìm kiếm")
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Nhập từ khóa để tìm tin nhắn, ghi chú hoặc lọc theo sắc thái cảm xúc.",
                                style = AppTheme.typography.bodyMedium,
                                color = AppTheme.colors.inkSecondary,
                                modifier = Modifier.padding(vertical = 12.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultCard(
    item: SearchResultItem,
    searchQuery: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DiuCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (item.mood != null) {
                        DiuMoodDot(color = item.mood.color, size = 10.dp)
                    }
                    Text(
                        text = item.conversationName,
                        style = AppTheme.typography.labelMedium,
                        color = AppTheme.colors.ink,
                    )
                }

                Text(
                    text = item.timestamp,
                    style = AppTheme.typography.labelSmall,
                    color = AppTheme.colors.inkSecondary,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Highlighted text snippet
            val highlightedSnippet = buildHighlightedSnippet(
                text = item.snippet,
                query = searchQuery,
                highlightColor = Color(0xFFFFD180),
            )

            Text(
                text = highlightedSnippet,
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colors.ink,
            )
        }
    }
}

private fun buildHighlightedSnippet(
    text: String,
    query: String,
    highlightColor: Color,
): AnnotatedString {
    if (query.isBlank()) return AnnotatedString(text)

    val lowerText = text.lowercase()
    val lowerQuery = query.lowercase()
    val index = lowerText.indexOf(lowerQuery)

    if (index < 0) return AnnotatedString(text)

    return buildAnnotatedString {
        append(text.substring(0, index))
        val end = index + query.length
        pushStyle(
            SpanStyle(
                background = highlightColor,
                fontWeight = FontWeight.Bold,
            ),
        )
        append(text.substring(index, end))
        pop()
        append(text.substring(end))
    }
}

@Preview(showBackground = true)
@Composable
private fun SearchScreenPreview() {
    AppTheme {
        SearchContent(
            state = SearchUiState(
                query = "bình yên",
                selectedMood = JournalMoodType.BINH_YEN,
                results = listOf(
                    SearchResultItem(
                        id = "res_1",
                        conversationId = "conv_1",
                        conversationName = "Linh Dịu",
                        senderName = "Linh",
                        snippet = "Cảm ơn vì những phút giây thật bình yên hôm nay.",
                        timestamp = "14:20",
                        mood = JournalMoodType.BINH_YEN,
                    ),
                    SearchResultItem(
                        id = "res_2",
                        conversationId = "conv_2",
                        conversationName = "Nhật ký màu",
                        senderName = "Mình",
                        snippet = "Ghi chép một ngày bình yên bên tách trà.",
                        timestamp = "20/11",
                        mood = JournalMoodType.BINH_YEN,
                    ),
                ),
            ),
            onBackClick = {},
            onQueryChange = {},
            onSelectMood = {},
            onResultClick = {},
        )
    }
}
