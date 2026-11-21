package com.uzuu.diuchat.feature.calls.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uzuu.diuchat.feature.base.common.res.AppTheme
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuOrbAvatar
import com.uzuu.diuchat.feature.calls.domain.model.CallSession
import com.uzuu.diuchat.feature.calls.domain.model.CallStatus
import com.uzuu.diuchat.feature.calls.domain.model.CallType
import com.uzuu.diuchat.feature.calls.presentation.composable.CallControls
import com.uzuu.diuchat.feature.calls.presentation.composable.IncomingCallBanner
import com.uzuu.diuchat.feature.calls.presentation.composable.VideoOverlay

@Composable
fun CallScreen(
    viewModel: CallViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiStateFlow.collectAsState()

    CallContent(
        uiState = uiState,
        onAcceptCall = { viewModel.acceptIncomingCall() },
        onRejectCall = { viewModel.rejectIncomingCall() },
        onToggleMic = { viewModel.toggleMicrophone() },
        onToggleSpeaker = { viewModel.toggleSpeakerphone() },
        onToggleVideo = { viewModel.toggleCameraVideo() },
        onFlipCamera = { viewModel.flipCamera() },
        onToggleMinimize = { viewModel.toggleOverlayMinimized() },
        onHangUp = {
            viewModel.hangUpCall()
            onNavigateBack()
        },
        modifier = modifier,
    )
}

@Composable
fun CallContent(
    uiState: CallUiState,
    onAcceptCall: () -> Unit,
    onRejectCall: () -> Unit,
    onToggleMic: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onToggleVideo: () -> Unit,
    onFlipCamera: () -> Unit,
    onToggleMinimize: () -> Unit,
    onHangUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val session = uiState.callSession
    val colors = AppTheme.colors

    Surface(
        modifier = modifier.fillMaxSize(),
        color = colors.background,
    ) {
        if (session == null || session.status == CallStatus.IDLE || session.status == CallStatus.ENDED) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Không có cuộc gọi đang diễn ra",
                        color = colors.inkSecondary,
                        fontSize = 16.sp,
                    )
                }
            }
        } else if (session.isIncoming && session.status == CallStatus.RINGING) {
            Box(modifier = Modifier.fillMaxSize()) {
                IncomingCallBanner(
                    session = session,
                    onAccept = onAcceptCall,
                    onDecline = onRejectCall,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        } else {
            // Outgoing / Active Call Screen
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                colors.background,
                                colors.surfaceElevated,
                            ),
                        ),
                    ),
            ) {
                // Header & Avatar
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 72.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    val peerName = if (session.isIncoming) session.callerName else session.calleeName

                    DiuOrbAvatar(
                        initials = peerName.take(1),
                        moodColor = colors.accent,
                        size = 110.dp,
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = peerName,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.ink,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val statusText = when (session.status) {
                        CallStatus.INVITING -> "Đang gọi..."
                        CallStatus.CONNECTING -> "Đang kết nối..."
                        CallStatus.ACTIVE -> uiState.formattedDuration
                        else -> ""
                    }

                    Text(
                        text = statusText,
                        fontSize = 16.sp,
                        color = colors.inkSecondary,
                    )

                    if (session.status == CallStatus.ACTIVE) {
                        Box(
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFE8F5E9))
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                        ) {
                            Text(
                                text = "${uiState.connectionQuality} • Mã hóa đầu cuối",
                                fontSize = 11.sp,
                                color = Color(0xFF2E7D32),
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }

                // Video Overlay (for video call)
                if (session.type == CallType.VIDEO && uiState.isVideoEnabled) {
                    VideoOverlay(
                        isMinimized = uiState.isOverlayMinimized,
                        isFrontCamera = uiState.isFrontCamera,
                        onToggleMinimize = onToggleMinimize,
                        onFlipCamera = onFlipCamera,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 48.dp, end = 20.dp),
                    )
                }

                // Controls at bottom
                CallControls(
                    isMuted = uiState.isMuted,
                    isSpeakerOn = uiState.isSpeakerOn,
                    isVideoEnabled = uiState.isVideoEnabled,
                    onToggleMic = onToggleMic,
                    onToggleSpeaker = onToggleSpeaker,
                    onToggleVideo = onToggleVideo,
                    onFlipCamera = onFlipCamera,
                    onHangUp = onHangUp,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CallScreenPreview() {
    AppTheme {
        CallContent(
            uiState = CallUiState(
                callSession = CallSession(
                    callId = "call_preview_1",
                    callerId = "u1",
                    callerName = "Minh",
                    calleeId = "u2",
                    calleeName = "Linh Dịu",
                    status = CallStatus.ACTIVE,
                    type = CallType.AUDIO,
                    isIncoming = false,
                    durationSeconds = 125,
                ),
                formattedDuration = "02:05",
                connectionQuality = "HD",
            ),
            onAcceptCall = {},
            onRejectCall = {},
            onToggleMic = {},
            onToggleSpeaker = {},
            onToggleVideo = {},
            onFlipCamera = {},
            onToggleMinimize = {},
            onHangUp = {},
        )
    }
}
