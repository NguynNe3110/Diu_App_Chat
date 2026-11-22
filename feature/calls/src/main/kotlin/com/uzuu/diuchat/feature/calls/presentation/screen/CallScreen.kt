package com.uzuu.diuchat.feature.calls.presentation.screen

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.uzuu.diuchat.feature.base.common.res.AppIcons
import com.uzuu.diuchat.feature.base.common.res.AppTheme
import com.uzuu.diuchat.feature.base.common.res.DiuSerif
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuAmbientBackground
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuIconButton
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuRippleOrb
import com.uzuu.diuchat.feature.base.presentation.compose.composable.parseMoodColorHex
import com.uzuu.diuchat.feature.calls.data.CallMediaEngine
import com.uzuu.diuchat.feature.calls.domain.model.CallSession
import com.uzuu.diuchat.feature.calls.domain.model.CallStatus
import com.uzuu.diuchat.feature.calls.domain.model.CallType
import com.uzuu.diuchat.feature.calls.presentation.composable.CallControls
import com.uzuu.diuchat.feature.calls.presentation.composable.IncomingCallFullScreen
import com.uzuu.diuchat.feature.calls.presentation.composable.LiveKitVideoView
import com.uzuu.diuchat.feature.calls.presentation.composable.VideoOverlay
import io.livekit.android.renderer.SurfaceViewRenderer
import io.livekit.android.room.track.VideoTrack
import org.koin.compose.koinInject

@Composable
fun CallScreen(
    viewModel: CallViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    peerId: String = "",
    peerName: String = "",
    peerMoodHex: String = "#F7A3A9",
    isVideo: Boolean = false,
    isIncoming: Boolean = false,
) {
    val uiState by viewModel.uiStateFlow.collectAsState()
    val callMediaEngine: CallMediaEngine = koinInject()
    val localVideoTrack by callMediaEngine.localVideoTrack.collectAsState()
    val remoteVideoTrack by callMediaEngine.remoteVideoTrack.collectAsState()
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { _ -> }

    LaunchedEffect(isVideo) {
        val requiredPermissions = buildList {
            add(Manifest.permission.RECORD_AUDIO)
            if (isVideo) {
                add(Manifest.permission.CAMERA)
            }
        }
        val missingPermissions = requiredPermissions.filter { permission ->
            ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
        }
        if (missingPermissions.isNotEmpty()) {
            permissionLauncher.launch(missingPermissions.toTypedArray())
        }
    }

    LaunchedEffect(peerId, peerName, peerMoodHex, isVideo, isIncoming) {
        viewModel.initializeCallFromRoute(
            peerId = peerId,
            peerName = peerName,
            peerMoodHex = peerMoodHex,
            isVideo = isVideo,
            isIncoming = isIncoming,
        )
    }

    CallContent(
        uiState = uiState,
        localVideoTrack = localVideoTrack,
        remoteVideoTrack = remoteVideoTrack,
        onInitRenderer = callMediaEngine::initVideoRenderer,
        onNavigateBack = onNavigateBack,
        onAcceptCall = { viewModel.acceptIncomingCall() },
        onRejectCall = {
            viewModel.rejectIncomingCall()
            onNavigateBack()
        },
        onToggleQuickReply = { viewModel.toggleQuickReplyOptions() },
        onSelectQuickReply = { message ->
            viewModel.sendQuickReplyAndDecline(message)
            onNavigateBack()
        },
        onRemindMe = {
            viewModel.scheduleCallReminder()
            onNavigateBack()
        },
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
    onNavigateBack: () -> Unit = onHangUp,
    onToggleQuickReply: () -> Unit = {},
    onSelectQuickReply: (String) -> Unit = {},
    onRemindMe: () -> Unit = {},
    localVideoTrack: VideoTrack? = null,
    remoteVideoTrack: VideoTrack? = null,
    onInitRenderer: (SurfaceViewRenderer) -> Unit = {},
) {
    val session = uiState.callSession
    val colors = AppTheme.colors
    val peerMoodColor = parseMoodColorHex(uiState.peerMoodHex)
    val displayPeerName = when {
        session == null -> uiState.peerName
        session.isIncoming -> session.callerName
        else -> session.calleeName
    }

    DiuAmbientBackground(
        modifier = modifier.fillMaxSize(),
        primaryBlobColor = peerMoodColor,
        secondaryBlobColor = colors.peach,
    ) {
        if (session != null && session.isIncoming && session.status == CallStatus.RINGING) {
            IncomingCallFullScreen(
                session = session,
                peerMoodColor = peerMoodColor,
                isQuickReplyExpanded = uiState.isQuickReplyExpanded,
                onToggleQuickReply = onToggleQuickReply,
                onSelectQuickReply = onSelectQuickReply,
                onRemindMe = onRemindMe,
                onAccept = onAcceptCall,
                onDecline = onRejectCall,
            )
        } else {
            val isVideoMode = (session?.type == CallType.VIDEO) || uiState.isVideoEnabled
            val statusText = when (session?.status) {
                CallStatus.ACTIVE -> uiState.formattedDuration
                CallStatus.CONNECTING -> "Đang kết nối dịu dàng…"
                else -> "Đang gọi dịu dàng…"
            }

            Box(modifier = Modifier.fillMaxSize()) {
                if (isVideoMode) {
                    if (remoteVideoTrack != null) {
                        LiveKitVideoView(
                            videoTrack = remoteVideoTrack,
                            onInitRenderer = onInitRenderer,
                            modifier = Modifier.fillMaxSize(),
                        )
                    } else {
                        // Full-screen remote video feed backdrop
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            peerMoodColor.copy(alpha = 0.28f),
                                            colors.peach.copy(alpha = 0.22f),
                                            colors.surface,
                                        ),
                                    ),
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            DiuRippleOrb(
                                initials = displayPeerName.ifEmpty { "D" }.take(1).uppercase(),
                                moodColor = peerMoodColor,
                                size = 148.dp,
                            )
                        }
                    }
                }

                // Top bar: Back button + "Chỉ hai người · mã hoá" glass pill
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    DiuIconButton(
                        onClick = onNavigateBack,
                        contentDescription = "Quay lại",
                        soft = true,
                    ) {
                        Icon(
                            painter = painterResource(AppIcons.Back),
                            contentDescription = "Quay lại",
                            tint = colors.ink,
                            modifier = Modifier.size(20.dp),
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = colors.surface,
                        border = BorderStroke(1.dp, colors.line),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(
                                painter = painterResource(AppIcons.Lock),
                                contentDescription = null,
                                tint = colors.inkSecondary,
                                modifier = Modifier.size(12.dp),
                            )
                            Text(
                                text = "Chỉ hai người · mã hoá",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.inkSecondary,
                            )
                        }
                    }

                    Spacer(modifier = Modifier.size(44.dp))
                }

                if (isVideoMode) {
                    VideoOverlay(
                        isMinimized = uiState.isOverlayMinimized,
                        isFrontCamera = uiState.isFrontCamera,
                        onToggleMinimize = onToggleMinimize,
                        onFlipCamera = onFlipCamera,
                        localVideoTrack = localVideoTrack,
                        onInitRenderer = onInitRenderer,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 78.dp, end = 20.dp),
                    )

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(bottom = 28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = displayPeerName,
                            fontFamily = DiuSerif,
                            fontSize = 30.sp,
                            color = colors.ink,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = statusText,
                            fontSize = 14.sp,
                            color = colors.inkSecondary,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        CallControls(
                            isMuted = uiState.isMuted,
                            isSpeakerOn = uiState.isSpeakerOn,
                            isVideoEnabled = true,
                            onToggleMic = onToggleMic,
                            onToggleSpeaker = onToggleSpeaker,
                            onToggleVideo = onToggleVideo,
                            onFlipCamera = onFlipCamera,
                            onHangUp = onHangUp,
                        )
                    }
                } else {
                    // Audio call layout (Prototype #29)
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(bottom = 64.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        DiuRippleOrb(
                            initials = displayPeerName.ifEmpty { "D" }.take(1).uppercase(),
                            moodColor = peerMoodColor,
                            size = 148.dp,
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = displayPeerName,
                            fontFamily = DiuSerif,
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Normal,
                            color = colors.ink,
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = statusText,
                            fontSize = 15.sp,
                            color = colors.inkSecondary,
                        )
                    }

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
                            .padding(bottom = 28.dp),
                    )
                }
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
                    callerName = "Tôi",
                    calleeId = "minh",
                    calleeName = "Minh",
                    status = CallStatus.ACTIVE,
                    type = CallType.AUDIO,
                    isIncoming = false,
                    durationSeconds = 83,
                ),
                formattedDuration = "01:23",
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
