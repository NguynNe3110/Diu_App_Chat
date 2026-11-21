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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.uzuu.diuchat.feature.base.common.res.AppIcons
import com.uzuu.diuchat.feature.base.common.res.AppTheme
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuButton
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuCard
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuFilterChip
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuIconButton
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuInlineNotice
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuTopBar
import com.uzuu.diuchat.feature.location.domain.model.LiveDuration
import com.uzuu.diuchat.feature.location.domain.model.LiveLocationSession
import com.uzuu.diuchat.feature.location.domain.model.LocationSnapshot
import org.koin.androidx.compose.koinViewModel

@Composable
fun LocationPickerScreen(
    conversationId: String,
    onBackClick: () -> Unit,
    onSendLocation: (LocationSnapshot) -> Unit,
    onStartLiveSession: (LiveLocationSession) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LocationPickerViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiStateFlow.collectAsState()

    LocationPickerContent(
        uiState = uiState,
        conversationId = conversationId,
        onBackClick = onBackClick,
        onToggleMode = viewModel::toggleMode,
        onSelectDuration = viewModel::selectDuration,
        onStartLiveSharing = { convId ->
            viewModel.startLiveSharing(convId, onStartLiveSession)
        },
        onSendLocation = onSendLocation,
        modifier = modifier,
    )
}

@Composable
fun LocationPickerContent(
    uiState: LocationPickerUiState,
    conversationId: String,
    onBackClick: () -> Unit,
    onToggleMode: (Boolean) -> Unit,
    onSelectDuration: (LiveDuration) -> Unit,
    onStartLiveSharing: (String) -> Unit,
    onSendLocation: (LocationSnapshot) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background),
    ) {
        DiuTopBar(
            title = if (uiState.isLiveMode) "Chia sẻ vị trí trực tiếp" else "Gửi vị trí",
            subtitle = "Chia sẻ vị trí với cuộc trò chuyện",
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                DiuFilterChip(
                    text = "Vị trí hiện tại",
                    selected = !uiState.isLiveMode,
                    onClick = { onToggleMode(false) },
                )
                DiuFilterChip(
                    text = "Vị trí trực tiếp",
                    selected = uiState.isLiveMode,
                    onClick = { onToggleMode(true) },
                )
            }

            DiuMapPreview(
                latitude = uiState.currentLocation?.latitude ?: 21.0285,
                longitude = uiState.currentLocation?.longitude ?: 105.8542,
                accuracyMeters = uiState.currentLocation?.accuracyMeters ?: 12f,
                isLive = uiState.isLiveMode,
            )

            DiuCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(
                        painter = painterResource(AppIcons.Location),
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(20.dp),
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = uiState.currentLocation?.address ?: "Đang xác định địa chỉ...",
                            style = AppTheme.typography.titleMedium,
                            color = colors.ink,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        AccuracyRadiusIndicator(
                            accuracyMeters = uiState.currentLocation?.accuracyMeters ?: 12f,
                        )
                    }
                }
            }

            if (uiState.isLiveMode) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Thời gian chia sẻ trực tiếp",
                        style = AppTheme.typography.titleMedium,
                        color = colors.ink,
                    )
                    LiveDurationPicker(
                        selectedDuration = uiState.selectedDuration,
                        onSelectDuration = onSelectDuration,
                    )
                }

                DiuInlineNotice(
                    message = "Vị trí sẽ tự động ngừng chia sẻ sau khi hết giờ. Bạn có thể dừng bất kỳ lúc nào.",
                )
            }

            uiState.errorMessage?.let { error ->
                Text(
                    text = error,
                    style = AppTheme.typography.bodySmall,
                    color = colors.danger,
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            if (uiState.isLiveMode) {
                DiuButton(
                    onClick = { onStartLiveSharing(conversationId) },
                    enabled = !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Bắt đầu chia sẻ (${uiState.selectedDuration.label})")
                }
            } else {
                DiuButton(
                    onClick = {
                        uiState.currentLocation?.let { onSendLocation(it) }
                    },
                    enabled = uiState.currentLocation != null && !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Gửi vị trí này")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LocationPickerScreenPreview() {
    AppTheme {
        LocationPickerContent(
            uiState = LocationPickerUiState(
                currentLocation = LocationSnapshot(
                    latitude = 21.0285,
                    longitude = 105.8542,
                    accuracyMeters = 10f,
                    address = "Hoàn Kiếm, Hà Nội",
                ),
            ),
            conversationId = "conv_1",
            onBackClick = {},
            onToggleMode = {},
            onSelectDuration = {},
            onStartLiveSharing = {},
            onSendLocation = {},
        )
    }
}
