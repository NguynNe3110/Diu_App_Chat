package com.uzuu.diuchat.feature.calls.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uzuu.diuchat.feature.base.common.res.AppIcons
import com.uzuu.diuchat.feature.base.common.res.AppTheme
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuOrbAvatar
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuTopBar
import com.uzuu.diuchat.feature.calls.domain.model.CallEndReason
import com.uzuu.diuchat.feature.calls.domain.model.CallLog
import com.uzuu.diuchat.feature.calls.domain.model.CallType

@Composable
fun CallHistoryScreen(
    viewModel: CallHistoryViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiStateFlow.collectAsState()
    val colors = AppTheme.colors

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background),
    ) {
        DiuTopBar(
            title = "Cuộc gọi",
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        painter = painterResource(AppIcons.Back),
                        contentDescription = "Quay lại",
                        tint = colors.ink,
                    )
                }
            },
        )

        // Filter tabs: Tất cả / Gọi nhỡ
        TabRow(
            selectedTabIndex = uiState.filter.ordinal,
            containerColor = colors.background,
            contentColor = colors.accent,
        ) {
            Tab(
                selected = uiState.filter == CallHistoryFilter.ALL,
                onClick = { viewModel.setFilter(CallHistoryFilter.ALL) },
                text = {
                    Text(
                        text = "Tất cả",
                        fontWeight = if (uiState.filter == CallHistoryFilter.ALL) FontWeight.Bold else FontWeight.Normal,
                    )
                },
            )
            Tab(
                selected = uiState.filter == CallHistoryFilter.MISSED,
                onClick = { viewModel.setFilter(CallHistoryFilter.MISSED) },
                text = {
                    Text(
                        text = "Cuộc gọi nhỡ",
                        fontWeight = if (uiState.filter == CallHistoryFilter.MISSED) FontWeight.Bold else FontWeight.Normal,
                    )
                },
            )
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
                        "Không có cuộc gọi nhỡ"
                    } else {
                        "Chưa có lịch sử cuộc gọi"
                    },
                    color = colors.inkSecondary,
                    fontSize = 15.sp,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                items(uiState.filteredLogs, key = { it.id }) { log ->
                    CallHistoryItem(
                        log = log,
                        onRedial = { viewModel.redial(log) },
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
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
