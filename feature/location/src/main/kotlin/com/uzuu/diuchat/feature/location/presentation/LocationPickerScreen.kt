@file:Suppress("ktlint:compose:vm-forwarding-check")

package com.uzuu.diuchat.feature.location.presentation

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.uzuu.diuchat.feature.base.common.res.AppIcons
import com.uzuu.diuchat.feature.base.common.res.AppTheme
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuButton
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuCard
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuIconButton
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuInlineNotice
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuOrbAvatar
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuSkeletonLine
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuSwitch
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
    val accuracyMeters = (uiState.currentLocation?.accuracyMeters ?: 15f).toInt()

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        DiuTopBar(
            title = "Vị trí",
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
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Centered glowing user orb over map preview with overlaid address pill
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                contentAlignment = Alignment.Center,
            ) {
                DiuMapPreview(
                    latitude = uiState.currentLocation?.latitude ?: 21.0285,
                    longitude = uiState.currentLocation?.longitude ?: 105.8542,
                    accuracyMeters = uiState.currentLocation?.accuracyMeters ?: 12f,
                    isLive = uiState.isLiveMode,
                    modifier = Modifier.fillMaxSize(),
                )
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    colors.moods.vui.copy(alpha = 0.35f),
                                    colors.moods.vui.copy(alpha = 0.0f),
                                ),
                            ),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    DiuOrbAvatar(
                        initials = "L",
                        moodColor = colors.moods.vui,
                        size = 48.dp,
                    )
                }

                // Overlaid address pill at bottom edge of map Box
                Surface(
                    shape = RoundedCornerShape(50),
                    color = colors.surfaceElevated,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(12.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(
                            painter = painterResource(AppIcons.Location),
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(18.dp),
                        )
                        if (uiState.isLoading && uiState.currentLocation == null) {
                            DiuSkeletonLine(
                                modifier = Modifier.weight(1f),
                                height = 14.dp,
                            )
                        } else {
                            Text(
                                text = uiState.currentLocation?.address ?: "Đang xác định địa chỉ...",
                                style = AppTheme.typography.bodyMedium,
                                color = colors.ink,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }

            // Action card 1: Gửi vị trí hiện tại
            DiuCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = uiState.currentLocation != null && !uiState.isLoading) {
                        uiState.currentLocation?.let(onSendLocation)
                    },
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        painter = painterResource(AppIcons.Pin),
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(20.dp),
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Gửi vị trí hiện tại",
                            style = AppTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.ink,
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Chính xác trong phạm vi ${accuracyMeters}m",
                            style = AppTheme.typography.bodySmall,
                            color = colors.inkSecondary,
                        )
                    }
                }
            }

            // Action card 2: Chia sẻ vị trí trực tiếp switch
            DiuCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        painter = painterResource(AppIcons.Clock),
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(20.dp),
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Chia sẻ vị trí trực tiếp",
                            style = AppTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.ink,
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Cập nhật theo thời gian thực",
                            style = AppTheme.typography.bodySmall,
                            color = colors.inkSecondary,
                        )
                    }
                    DiuSwitch(
                        checked = uiState.isLiveMode,
                        onCheckedChange = onToggleMode,
                        contentDescription = "Bật chia sẻ vị trí trực tiếp",
                    )
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

                DiuButton(
                    onClick = { onStartLiveSharing(conversationId) },
                    enabled = !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Bắt đầu chia sẻ (${uiState.selectedDuration.label})")
                }
            }

            uiState.errorMessage?.let { error ->
                Text(
                    text = error,
                    style = AppTheme.typography.bodySmall,
                    color = colors.danger,
                )
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
                    accuracyMeters = 15f,
                    address = "Hồ Gươm, Hoàn Kiếm, Hà Nội",
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
