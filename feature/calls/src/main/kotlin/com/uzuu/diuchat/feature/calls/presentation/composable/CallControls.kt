package com.uzuu.diuchat.feature.calls.presentation.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.uzuu.diuchat.feature.base.common.res.AppIcons
import com.uzuu.diuchat.feature.base.common.res.AppTheme

@Composable
fun CallControls(
    isMuted: Boolean,
    isSpeakerOn: Boolean,
    isVideoEnabled: Boolean,
    onToggleMic: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onToggleVideo: () -> Unit,
    onFlipCamera: () -> Unit,
    onHangUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Mic button
        IconButton(
            onClick = onToggleMic,
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(if (isMuted) Color(0xFFFFCDD2) else colors.surfaceElevated),
        ) {
            Icon(
                painter = painterResource(if (isMuted) AppIcons.MicOff else AppIcons.Mic),
                contentDescription = if (isMuted) "Bật mic" else "Tắt mic",
                tint = if (isMuted) Color(0xFFD32F2F) else colors.ink,
                modifier = Modifier.size(24.dp),
            )
        }

        // Speaker button
        IconButton(
            onClick = onToggleSpeaker,
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(if (isSpeakerOn) Color(0xFFC8E6C9) else colors.surfaceElevated),
        ) {
            Icon(
                painter = painterResource(AppIcons.Speaker),
                contentDescription = if (isSpeakerOn) "Tắt loa ngoài" else "Bật loa ngoài",
                tint = if (isSpeakerOn) Color(0xFF2E7D32) else colors.ink,
                modifier = Modifier.size(24.dp),
            )
        }

        // Hangup button
        IconButton(
            onClick = onHangUp,
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color(0xFFE53935)),
        ) {
            Icon(
                painter = painterResource(AppIcons.Close),
                contentDescription = "Kết thúc cuộc gọi",
                tint = Color.White,
                modifier = Modifier.size(28.dp),
            )
        }

        // Video toggle button
        IconButton(
            onClick = onToggleVideo,
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(if (isVideoEnabled) Color(0xFFBBDEFB) else colors.surfaceElevated),
        ) {
            Icon(
                painter = painterResource(AppIcons.Video),
                contentDescription = if (isVideoEnabled) "Tắt camera" else "Bật camera",
                tint = if (isVideoEnabled) Color(0xFF1976D2) else colors.ink,
                modifier = Modifier.size(24.dp),
            )
        }

        // Flip camera button
        if (isVideoEnabled) {
            IconButton(
                onClick = onFlipCamera,
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(colors.surfaceElevated),
            ) {
                Icon(
                    painter = painterResource(AppIcons.Flip),
                    contentDescription = "Đổi camera",
                    tint = colors.ink,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    }
}
