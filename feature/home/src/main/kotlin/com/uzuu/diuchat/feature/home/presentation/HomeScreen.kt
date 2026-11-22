package com.uzuu.diuchat.feature.home.presentation

import android.content.res.Configuration
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.uzuu.diuchat.feature.base.common.res.AppIcons
import com.uzuu.diuchat.feature.base.common.res.AppPalette
import com.uzuu.diuchat.feature.base.common.res.AppTheme
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuButton
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuConfirmationDialog
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuEmptyState
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuIconButton
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuInlineNotice
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuMoodDot
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuOrbAvatar
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuPersonListSkeleton
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuPersonRow
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuSectionHeader
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuTabHeader
import com.uzuu.diuchat.feature.home.domain.model.RecentChat
import com.uzuu.diuchat.feature.home.domain.model.StarPerson
import org.koin.androidx.compose.koinViewModel
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = koinViewModel(),
    onOpenChat: (String) -> Unit = {},
    onOpenContacts: () -> Unit = {},
    onOpenCheckin: () -> Unit = {},
    onOpenQuietHours: () -> Unit = {},
    onInvite: () -> Unit = {},
    onSearchClick: () -> Unit = onOpenContacts,
) {
    val state by viewModel.uiStateFlow.collectAsStateWithLifecycle()

    HomeScreenContent(
        state = state,
        modifier = modifier,
        onOpenChat = onOpenChat,
        onDeleteConversation = viewModel::onDeleteConversation,
        onOpenContacts = onOpenContacts,
        onOpenCheckin = onOpenCheckin,
        onOpenQuietHours = onOpenQuietHours,
        onInvite = onInvite,
        onSearchClick = onSearchClick,
    )
}

@Composable
fun HomeScreenContent(
    state: HomeUiState,
    modifier: Modifier = Modifier,
    onOpenChat: (String) -> Unit = {},
    onDeleteConversation: (String) -> Unit = {},
    onOpenContacts: () -> Unit = {},
    onOpenCheckin: () -> Unit = {},
    onOpenQuietHours: () -> Unit = {},
    onInvite: () -> Unit = {},
    onSearchClick: () -> Unit = onOpenContacts,
) {
    var pendingDeleteChat by remember { mutableStateOf<RecentChat?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 110.dp),
        ) {
            // ── Top Header ──────────────────────────────────────────────
            item {
                val greetingText = if (state.userName.isNotBlank()) {
                    "${state.greeting}, ${state.userName}"
                } else {
                    state.greeting
                }
                HomeHeader(
                    greeting = greetingText,
                    onSearchClick = onSearchClick,
                )
            }

            // ── Mood Check-in Prompt ────────────────────────────────────
            item {
                MoodCheckinPrompt(
                    onClick = onOpenCheckin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                )
            }

            // ── Quiet Mode Banner ───────────────────────────────────────
            if (state.isQuietModeActive) {
                item {
                    DiuInlineNotice(
                        message = state.quietBannerText,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 4.dp)
                            .clickable(onClick = onOpenQuietHours),
                        leading = {
                            Icon(
                                painter = painterResource(AppIcons.Moon),
                                contentDescription = null,
                                tint = AppTheme.colors.quietPurple,
                                modifier = Modifier.size(16.dp),
                            )
                        },
                    )
                }
            }

            // ── Constellation Layout ────────────────────────────────────
            if (state.stars.isNotEmpty()) {
                item {
                    ConstellationSection(
                        stars = state.stars,
                        onStarClick = onOpenChat,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .padding(top = 8.dp),
                    )
                }
            }

            // ── Recent Chats / Loading Skeleton / Empty State ───────────
            if (state.isLoading && state.isEmpty) {
                item {
                    DiuPersonListSkeleton(
                        itemCount = 4,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                }
            } else if (state.isEmpty) {
                item {
                    DiuEmptyState(
                        title = "Vòng thân còn trống",
                        description =
                        "Mời người thương, gia đình hay vài người bạn thân nhất. dịu dành cho số ít người thật sự quan trọng.",
                        illustration = {
                            DiuOrbAvatar(
                                initials = "♡",
                                moodColor = AppTheme.colors.peach,
                                size = 64.dp,
                            )
                        },
                        action = {
                            DiuButton(
                                onClick = onInvite,
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                            ) {
                                Icon(
                                    painter = painterResource(AppIcons.Plus),
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                                Spacer(modifier = Modifier.size(8.dp))
                                Text("Mời người đầu tiên")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                    )
                }
            } else if (state.recentChats.isNotEmpty()) {
                item {
                    DiuSectionHeader(
                        title = "Gần đây",
                        modifier = Modifier.padding(top = 12.dp),
                    )
                }

                items(state.recentChats, key = { it.id }) { chat ->
                    DiuPersonRow(
                        name = chat.contactName,
                        initials = chat.initials,
                        moodColor = chat.moodColor,
                        avatarUrl = chat.avatarUrl,
                        subtitle = chat.lastMessage,
                        timestamp = chat.timestamp,
                        onClick = { onOpenChat(chat.id) },
                        onLongClick = { pendingDeleteChat = chat },
                        trailing = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                if (chat.isPendingLetter) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = AppTheme.colors.surface,
                                        border = BorderStroke(1.dp, AppTheme.colors.line),
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        ) {
                                            Icon(
                                                painter = painterResource(AppIcons.Envelope),
                                                contentDescription = "Thư hẹn",
                                                tint = AppTheme.colors.inkSecondary,
                                                modifier = Modifier.size(12.dp),
                                            )
                                            Text(
                                                text = "7:00",
                                                style = AppTheme.typography.bodySmall,
                                                color = AppTheme.colors.inkSecondary,
                                                fontSize = 11.sp,
                                            )
                                        }
                                    }
                                }
                                if (chat.unreadCount > 0) {
                                    val unreadLabel = if (chat.unreadCount > 9) "9+" else "${chat.unreadCount}"
                                    Box(
                                        modifier = Modifier
                                            .defaultMinSize(minWidth = 20.dp, minHeight = 20.dp)
                                            .clip(CircleShape)
                                            .background(chat.moodColor)
                                            .padding(horizontal = 5.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = unreadLabel,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AppPalette.BubbleTextOnMood,
                                        )
                                    }
                                }
                            }
                        },
                    )
                }
            }
        }
    }

    pendingDeleteChat?.let { target ->
        DiuConfirmationDialog(
            title = "Xoá cuộc hội thoại",
            message = "Cuộc hội thoại với ${target.contactName} sẽ bị xoá khỏi danh sách của bạn.",
            confirmLabel = "Xoá",
            dismissLabel = "Huỷ",
            destructive = true,
            onConfirm = {
                pendingDeleteChat = null
                onDeleteConversation(target.id)
            },
            onDismiss = { pendingDeleteChat = null },
        )
    }
}

@Composable
private fun HomeHeader(
    greeting: String,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DiuTabHeader(
        title = "Vòng thân",
        greeting = greeting,
        modifier = modifier,
        actions = {
            DiuIconButton(
                onClick = onSearchClick,
                contentDescription = "Tìm kiếm danh bạ",
                soft = true,
            ) {
                Icon(
                    painter = painterResource(AppIcons.Search),
                    contentDescription = null,
                    tint = AppTheme.colors.inkSecondary,
                )
            }
        },
    )
}

@Composable
private fun MoodCheckinPrompt(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val moods = AppTheme.colors.moods
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = AppTheme.colors.surface,
        border = BorderStroke(1.dp, AppTheme.colors.line),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf(moods.thuong, moods.vui, moods.binhyen, moods.nho, moods.tram).forEach {
                    DiuMoodDot(color = it, size = 10.dp)
                }
            }
            Text(
                text = "Hôm nay bạn thấy thế nào?",
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colors.ink,
                modifier = Modifier.weight(1f),
            )
            Icon(
                painter = painterResource(AppIcons.Chev),
                contentDescription = null,
                tint = AppTheme.colors.inkTertiary,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
private fun ConstellationSection(
    stars: List<StarPerson>,
    onStarClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "constellationFloat")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "floatAnim",
    )

    BoxWithConstraints(
        modifier = modifier
            .semantics {
                contentDescription = "Chòm sao vòng thân: ${stars.joinToString { it.name }}"
            },
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()

        stars.forEachIndexed { index, star ->
            val phaseFactor = if (index % 2 == 0) 1f else -1f
            val currentOffset = (floatOffset * phaseFactor).roundToInt()
            var itemSize by remember { mutableStateOf(IntSize.Zero) }

            val maxX = (constraints.maxWidth - itemSize.width).coerceAtLeast(0)
            val maxY = (constraints.maxHeight - itemSize.height).coerceAtLeast(0)
            val targetX = ((widthPx * star.xPercent).roundToInt() - itemSize.width / 2).coerceIn(0, maxX)
            val targetY = ((heightPx * star.yPercent).roundToInt() - itemSize.height / 2 + currentOffset)
                .coerceIn(0, maxY)

            Box(
                modifier = Modifier
                    .offset { IntOffset(targetX, targetY) }
                    .onSizeChanged { itemSize = it }
                    .clickable(
                        role = Role.Button,
                        onClick = { onStarClick(star.id) },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        DiuOrbAvatar(
                            initials = star.initials,
                            moodColor = star.moodColor,
                            size = star.size,
                            contentDescription = star.name,
                            avatarUrl = star.avatarUrl,
                        )
                        if (star.memberCount > 1) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 2.dp, y = (-2).dp)
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(AppTheme.colors.surface)
                                    .border(1.dp, AppTheme.colors.line, CircleShape)
                                    .semantics {
                                        contentDescription = "${star.memberCount} thành viên"
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "${star.memberCount}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AppTheme.colors.ink,
                                )
                            }
                        }
                        if (star.isQuiet) {
                            Box(
                                modifier = Modifier
                                    .offset(x = 2.dp, y = 2.dp)
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(AppTheme.colors.background)
                                    .padding(2.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    painter = painterResource(AppIcons.Moon),
                                    contentDescription = "Đang trong giờ yên lặng",
                                    tint = AppTheme.colors.quietPurple,
                                    modifier = Modifier.size(12.dp),
                                )
                            }
                        }
                    }
                    Text(
                        text = star.name,
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colors.inkSecondary,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Preview(name = "Home Screen — Light", showBackground = true, widthDp = 412, heightDp = 840)
@Preview(name = "Home Screen — Dark", showBackground = true, widthDp = 412, heightDp = 840, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HomeScreenPreview() {
    AppTheme {
        HomeScreenContent(
            state = HomeUiState(
                greeting = "Chào buổi tối",
                userName = "Lan",
                isQuietModeActive = true,
                stars = listOf(
                    StarPerson("minh", "Minh", "M", AppPalette.MoodThuong, 88.dp, 0.46f, 0.15f, isQuiet = true),
                    StarPerson("nha", "Nhà mình", "N", AppPalette.MoodVui, 64.dp, 0.16f, 0.36f, memberCount = 3),
                    StarPerson("thao", "Thảo", "T", AppPalette.MoodNho, 58.dp, 0.80f, 0.38f),
                    StarPerson("ba", "Ba", "B", AppPalette.MoodBinhYen, 44.dp, 0.30f, 0.70f),
                    StarPerson("vy", "Bảo Vy", "V", AppPalette.MoodTram, 40.dp, 0.70f, 0.72f),
                ),
                recentChats = listOf(
                    RecentChat(
                        id = "minh",
                        contactName = "Minh",
                        initials = "M",
                        moodColor = AppPalette.MoodThuong,
                        lastMessage = "Thật hả!! Em vui quá",
                        timestamp = "21:14",
                        unreadCount = 0,
                        isPendingLetter = true,
                    ),
                    RecentChat("nha", "Nhà mình", "N", AppPalette.MoodVui, "Mẹ: Cuối tuần về ăn cơm nha con", "19:30", unreadCount = 2),
                    RecentChat("thao", "Thảo", "T", AppPalette.MoodNho, "Mai mình đi cà phê nha?", "Hôm qua", unreadCount = 0),
                ),
            ),
        )
    }
}

@Preview(name = "Home Screen Empty — Light", showBackground = true, widthDp = 412, heightDp = 840)
@Composable
private fun HomeScreenEmptyPreview() {
    val emptyState =
        HomeUiState(
            greeting = "Chào buổi sáng",
            userName = "Lan",
            stars = emptyList(),
            recentChats = emptyList(),
        )
    AppTheme {
        HomeScreenContent(state = emptyState)
    }
}
