package com.uzuu.diuchat.feature.calls.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uzuu.diuchat.feature.base.common.res.AppIcons
import com.uzuu.diuchat.feature.base.common.res.AppTheme
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuOrbAvatar
import com.uzuu.diuchat.feature.calls.domain.model.CallEndReason
import com.uzuu.diuchat.feature.calls.domain.model.CallLog
import com.uzuu.diuchat.feature.calls.domain.model.CallType

@Composable
fun CallHistoryScreen(
    viewModel: CallHistoryViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    onStartCall: () -> Unit = {},
) {
    val uiState by viewModel.uiStateFlow.collectAsState()

    CallHistoryContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onSetFilter = { viewModel.setFilter(it) },
        onRedial = { log ->
            viewModel.redial(log)
            onStartCall()
        },
        onStartCall = onStartCall,
        modifier = modifier,
    )
}

@Composable
fun CallHistoryContent(
    uiState: CallHistoryUiState,
    onNavigateBack: () -> Unit,
    onSetFilter: (CallHistoryFilter) -> Unit,
    onRedial: (CallLog) -> Unit,
    modifier: Modifier = Modifier,
    onStartCall: () -> Unit = {},
) {
    val colors = AppTheme.colors

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 12.dp, top = 16.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Cuộc gọi",
                style = AppTheme.typography.headlineMedium.copy(fontFamily = FontFamily.Serif),
                color = colors.ink,
            )
            IconButton(onClick = onNavigateBack) {
                Icon(
                    painter = painterResource(AppIcons.Back),
                    contentDescription = "Quay lại",
                    tint = colors.ink,
                )
            }
        }

        // Filter chips: Tất cả / Lỡ (matching prototype .chips)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
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
        val favoriteCallers = listOf(
            Triple("Minh", "M", colors.moods.thuong),
            Triple("Mẹ", "M", colors.moods.vui),
            Triple("Thảo", "T", colors.moods.nho),
            Triple("Ba", "B", colors.moods.binhyen),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            favoriteCallers.forEach { (name, initials, mood) ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onStartCall() },
                ) {
                    DiuOrbAvatar(
                        initials = initials,
                        moodColor = mood,
                        size = 56.dp,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = name,
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
            ) {
                items(uiState.filteredLogs, key = { it.id }) { log ->
                    CallHistoryItem(
                        log = log,
                        onRedial = { onRedial(log) },
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
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
    onRedial: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surfaceElevated)
            .clickable { onRedial() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DiuOrbAvatar(
                initials = log.peerName.take(1),
                moodColor = colors.accent,
                size = 44.dp,
            )

            Column {
                Text(
                    text = log.peerName,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = if (log.endReason == CallEndReason.MISSED) Color(0xFFE53935) else colors.ink,
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    val iconRes = if (log.isIncoming) AppIcons.CallIn else AppIcons.CallOut
                    val iconTint = if (log.endReason == CallEndReason.MISSED) Color(0xFFE53935) else colors.inkSecondary

                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(14.dp),
                    )

                    val typeLabel = if (log.type == CallType.VIDEO) "Video" else "Thoại"
                    val durationLabel = if (log.durationSeconds > 0) " (${log.durationSeconds}s)" else ""

                    Text(
                        text = "$typeLabel$durationLabel",
                        fontSize = 12.sp,
                        color = colors.inkSecondary,
                    )
                }
            }
        }

        // Action icon to redial
        IconButton(
            onClick = onRedial,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(colors.surface),
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
                        peerId = "peer_1",
                        peerName = "Linh Dịu",
                        type = CallType.AUDIO,
                        isIncoming = true,
                        durationSeconds = 180,
                        timestampEpochMs = 1_700_000_000_000L,
                        endReason = CallEndReason.COMPLETED,
                    ),
                    CallLog(
                        id = "log_2",
                        callId = "call_2",
                        peerId = "peer_2",
                        peerName = "Minh Trí",
                        type = CallType.VIDEO,
                        isIncoming = false,
                        durationSeconds = 0,
                        timestampEpochMs = 1_699_990_000_000L,
                        endReason = CallEndReason.MISSED,
                    ),
                ),
            ),
            onNavigateBack = {},
            onSetFilter = {},
            onRedial = {},
        )
    }
}
