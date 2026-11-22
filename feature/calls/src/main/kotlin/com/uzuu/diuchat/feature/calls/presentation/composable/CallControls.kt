package com.uzuu.diuchat.feature.calls.presentation.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 1. Mic / Đã tắt
        LabeledCallControlButton(
            iconRes = if (isMuted) AppIcons.MicOff else AppIcons.Mic,
            label = if (isMuted) "Đã tắt" else "Mic",
            containerColor = if (isMuted) colors.ink else colors.surface,
            contentColor = if (isMuted) colors.background else colors.ink,
            borderColor = if (isMuted) colors.ink else colors.line,
            onClick = onToggleMic,
        )

        // 2. Video
        LabeledCallControlButton(
            iconRes = AppIcons.Video,
            label = "Video",
            containerColor = if (isVideoEnabled) colors.ink else colors.surface,
            contentColor = if (isVideoEnabled) colors.background else colors.ink,
            borderColor = if (isVideoEnabled) colors.ink else colors.line,
            onClick = onToggleVideo,
        )

        // 3. Loa / Đổi cam
        if (isVideoEnabled) {
            LabeledCallControlButton(
                iconRes = AppIcons.Flip,
                label = "Đổi cam",
                containerColor = colors.surface,
                contentColor = colors.ink,
                borderColor = colors.line,
                onClick = onFlipCamera,
            )
        } else {
            LabeledCallControlButton(
                iconRes = AppIcons.Speaker,
                label = "Loa",
                containerColor = if (isSpeakerOn) colors.ink else colors.surface,
                contentColor = if (isSpeakerOn) colors.background else colors.ink,
                borderColor = if (isSpeakerOn) colors.ink else colors.line,
                onClick = onToggleSpeaker,
            )
        }

        // 4. Kết thúc
        LabeledCallControlButton(
            iconRes = AppIcons.Close,
            label = "Kết thúc",
            containerColor = colors.callEnd,
            contentColor = colors.onAccent,
            borderColor = null,
            onClick = onHangUp,
        )
    }
}

@Composable
private fun LabeledCallControlButton(
    iconRes: Int,
    label: String,
    containerColor: Color,
    contentColor: Color,
    borderColor: Color?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(containerColor)
                .then(
                    if (borderColor != null) {
                        Modifier.border(width = 1.dp, color = borderColor, shape = CircleShape)
                    } else {
                        Modifier
                    },
                )
                .clickable(role = Role.Button, onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(24.dp),
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = label,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Medium,
            color = colors.inkSecondary,
        )
    }
}
