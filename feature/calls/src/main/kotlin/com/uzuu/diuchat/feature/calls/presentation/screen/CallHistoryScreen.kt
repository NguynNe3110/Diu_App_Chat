package com.uzuu.diuchat.feature.calls.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uzuu.diuchat.feature.base.common.res.AppIcons
import com.uzuu.diuchat.feature.base.common.res.AppTheme
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuIconButton
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuOrbAvatar
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuSectionHeader
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuTabHeader
import com.uzuu.diuchat.feature.base.presentation.compose.composable.parseMoodColorHex
import com.uzuu.diuchat.feature.calls.domain.model.CallEndReason
import com.uzuu.diuchat.feature.calls.domain.model.CallLog
import com.uzuu.diuchat.feature.calls.domain.model.CallType

@Composable
fun CallHistoryScreen(
    viewModel: CallHistoryViewModel,
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier,
    onStartCall: (peerId: String, peerName: String, peerMoodHex: String, isVideo: Boolean) -> Unit = { _, _, _, _ -> },
    onStartCallWithPeer: ((peerId: String, peerName: String, peerMoodHex: String, isVideo: Boolean) -> Unit)? = null,
) {
    val uiState by viewModel.uiStateFlow.collectAsState()
    val navigateToCall = onStartCallWithPeer ?: onStartCall

    CallHistoryContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onSetFilter = { viewModel.setFilter(it) },
        onFavoriteClick = { favorite ->
            viewModel.startCallToFavorite(favorite)
            navigateToCall(favorite.peerId, favorite.name, favorite.moodHex, false)
        },
        onRedial = { log ->
            viewModel.redial(log)
            navigateToCall(
                log.peerId,
                log.peerName,
                inferPeerMoodHex(log.peerName, uiState.favoriteCallers),
                log.type == CallType.VIDEO,
            )
        },
        modifier = modifier,
    )
}

@Composable
fun CallHistoryScreen(
    viewModel: CallHistoryViewModel,
    onNavigateBack: () -> Unit,
    onStartCall: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CallHistoryScreen(
        viewModel = viewModel,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
        onStartCall = { _, _, _, _ -> onStartCall() },
    )
}

@Composable
@Suppress("UnusedParameter")
fun CallHistoryContent(
    uiState: CallHistoryUiState,
    onNavigateBack: () -> Unit = {},
    onSetFilter: (CallHistoryFilter) -> Unit,
    onRedial: (CallLog) -> Unit,
    modifier: Modifier = Modifier,
    onFavoriteClick: (FavoriteCaller) -> Unit = {},
) {
    val colors = AppTheme.colors

    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        DiuTabHeader(title = "Cuộc gọi")

        // Filter chips: Tất cả / Lỡ (matching prototype .chips)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CallPillChip(
                text = "Tất cả",
                selected = uiState.filter == CallHistoryFilter.ALL,
                onClick = { onSetFilter(CallHistoryFilter.ALL) },
            )
            CallPillChip(
                text = "Lỡ",
                selected = uiState.filter == CallHistoryFilter.MISSED,
                onClick = { onSetFilter(CallHistoryFilter.MISSED) },
            )
        }

        // Favorites row (matching prototype .fav-row)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            uiState.favoriteCallers.forEach { favorite ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onFavoriteClick(favorite) }
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                ) {
                    DiuOrbAvatar(
                        initials = favorite.initials,
                        moodColor = parseMoodColorHex(favorite.moodHex),
                        size = 56.dp,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = favorite.name,
                        style = AppTheme.typography.bodySmall,
                        fontSize = 12.sp,
                        color = colors.inkSecondary,
                    )
                }
            }
        }

        if (uiState.filteredLogs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (uiState.filter == CallHistoryFilter.MISSED) {
                        "Không có cuộc gọi lỡ nào. Yên bình ghê."
                    } else {
                        "Chưa có cuộc gọi nào. Yên bình ghê."
                    },
                    color = colors.inkSecondary,
                    fontSize = 15.sp,
                )
            }
        } else {
            val groupedLogs = uiState.groupedLogsByDate
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 110.dp),
            ) {
                groupedLogs.forEach { (sectionTitle, sectionLogs) ->
                    item(key = "header_$sectionTitle") {
                        DiuSectionHeader(title = sectionTitle)
                    }
                    items(sectionLogs, key = { it.id }) { log ->
                        CallHistoryItem(
                            log = log,
                            moodHex = inferPeerMoodHex(log.peerName, uiState.favoriteCallers),
                            onRedial = { onRedial(log) },
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                        )
                    }
                }
            }
        }
    }
}

private fun inferPeerMoodHex(
    peerName: String,
    favorites: List<FavoriteCaller>,
): String {
    val matched = favorites.firstOrNull { fav ->
        peerName.contains(fav.name, ignoreCase = true)
    }
    return matched?.moodHex ?: "#F7A3A9"
}

@Composable
private fun CallPillChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = AppTheme.colors
    Box(
        modifier = Modifier
            .height(36.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected) colors.ink else colors.surface)
            .border(
                width = 1.dp,
                color = if (selected) colors.ink else colors.line,
                shape = RoundedCornerShape(18.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = if (selected) colors.background else colors.inkSecondary,
        )
    }
}

@Composable
private fun CallHistoryItem(
    log: CallLog,
    moodHex: String,
    onRedial: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val isMissed = log.endReason == CallEndReason.MISSED

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.surface)
            .border(1.dp, colors.line, RoundedCornerShape(20.dp))
            .clickable { onRedial() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DiuOrbAvatar(
                initials = log.peerName.take(1),
                moodColor = parseMoodColorHex(moodHex),
                size = 44.dp,
            )

            Column {
                Text(
                    text = log.peerName,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = if (isMissed) colors.danger else colors.ink,
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    val iconRes = if (log.isIncoming) AppIcons.CallIn else AppIcons.CallOut
                    val iconTint = if (isMissed) colors.danger else colors.inkSecondary

                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(14.dp),
                    )

                    val typeLabel = if (log.type == CallType.VIDEO) "Video" else "Thoại"
                    val durationLabel = when {
                        isMissed -> " · Cuộc gọi nhỡ"
                        log.durationSeconds >= 60 -> " · ${log.durationSeconds / 60} phút"
                        log.durationSeconds > 0 -> " · ${log.durationSeconds} giây"
                        else -> ""
                    }

                    Text(
                        text = "$typeLabel$durationLabel",
                        fontSize = 12.5.sp,
                        color = if (isMissed) colors.danger else colors.inkSecondary,
                    )
                }
            }
        }

        DiuIconButton(
            onClick = onRedial,
            contentDescription = "Gọi lại",
            soft = true,
        ) {
            Icon(
                painter = painterResource(if (log.type == CallType.VIDEO) AppIcons.Video else AppIcons.Phone),
                contentDescription = "Gọi lại",
                tint = colors.ink,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CallHistoryScreenPreview() {
    AppTheme {
        CallHistoryContent(
            uiState = CallHistoryUiState(
                callLogs = listOf(
                    CallLog(
                        id = "log_1",
                        callId = "call_1",
                        peerId = "minh",
                        peerName = "Minh",
                        type = CallType.AUDIO,
                        isIncoming = true,
                        durationSeconds = 840,
                        timestampEpochMs = 1_700_000_000_000L,
                        endReason = CallEndReason.COMPLETED,
                    ),
                    CallLog(
                        id = "log_2",
                        callId = "call_2",
                        peerId = "me_yeu",
                        peerName = "Mẹ",
                        type = CallType.VIDEO,
                        isIncoming = true,
                        durationSeconds = 0,
                        timestampEpochMs = 1_699_920_000_000L,
                        endReason = CallEndReason.MISSED,
                    ),
                ),
            ),
            onSetFilter = {},
            onRedial = {},
        )
    }
}
