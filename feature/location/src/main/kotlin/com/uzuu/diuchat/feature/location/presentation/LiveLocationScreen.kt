package com.uzuu.diuchat.feature.location.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.uzuu.diuchat.feature.base.common.res.AppIcons
import com.uzuu.diuchat.feature.base.common.res.AppTheme
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuButton
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuButtonVariant
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuCard
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuIconButton
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuInlineNotice
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuTopBar
import org.koin.androidx.compose.koinViewModel

@Composable
fun LiveLocationScreen(
    sessionId: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LiveLocationViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiStateFlow.collectAsState()

    LaunchedEffect(sessionId) {
        viewModel.observeSession(sessionId)
    }

    LiveLocationContent(
        uiState = uiState,
        sessionId = sessionId,
        onBackClick = onBackClick,
        onStopSharing = viewModel::stopSharing,
        modifier = modifier,
    )
}

@Composable
fun LiveLocationContent(
    uiState: LiveLocationUiState,
    sessionId: String,
    onBackClick: () -> Unit,
    onStopSharing: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background),
    ) {
        DiuTopBar(
            title = "Vị trí trực tiếp",
            subtitle = if (uiState.isSharingActive) "Đang hoạt động" else "Đã kết thúc",
            navigationIcon = {
                DiuIconButton(
                    onClick = onBackClick,
                    contentDescription = "Quay lại",
                ) {
                    Icon(
                        painter = painterResource(AppIcons.Back),
                        contentDescription = null,
                        tint = colors.ink,
                    )
                }
            },
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            DiuMapPreview(
                latitude = uiState.session?.lastKnownLocation?.latitude ?: 21.0285,
                longitude = uiState.session?.lastKnownLocation?.longitude ?: 105.8542,
                accuracyMeters = uiState.session?.lastKnownLocation?.accuracyMeters ?: 12f,
                isLive = uiState.isSharingActive,
            )

            DiuCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        painter = painterResource(AppIcons.Clock),
                        contentDescription = null,
                        tint = if (uiState.isSharingActive) colors.accent else colors.inkTertiary,
                        modifier = Modifier.size(24.dp),
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (uiState.isSharingActive) {
                                "Thời gian còn lại: ${uiState.remainingTimeFormatted}"
                            } else {
                                "Phiên chia sẻ đã kết thúc"
                            },
                            style = AppTheme.typography.titleMedium,
                            color = colors.ink,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        AccuracyRadiusIndicator(
                            accuracyMeters = uiState.session?.lastKnownLocation?.accuracyMeters ?: 12f,
                            label = uiState.session?.lastKnownLocation?.address ?: "Vị trí thời gian thực",
                        )
                    }
                }
            }

            if (uiState.isSharingActive) {
                DiuInlineNotice(
                    message = "Đang chia sẻ tọa độ qua tần suất thích ứng để tiết kiệm pin tối đa.",
                )
            } else {
                DiuInlineNotice(
                    message = "Phiên chia sẻ này đã dừng hoặc hết thời gian chỉ định.",
                )
            }
        }

        if (uiState.isSharingActive) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                DiuButton(
                    onClick = { onStopSharing(sessionId) },
                    variant = DiuButtonVariant.Danger,
                    enabled = !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Dừng chia sẻ vị trí")
                }
            }
        }
    }
}
