package com.uzuu.diuchat.feature.calls.presentation.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uzuu.diuchat.feature.base.common.res.AppIcons
import com.uzuu.diuchat.feature.base.common.res.AppTheme
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuOrbAvatar
import io.livekit.android.renderer.TextureViewRenderer
import io.livekit.android.room.track.VideoTrack

@Composable
fun VideoOverlay(
    isMinimized: Boolean,
    isFrontCamera: Boolean,
    onToggleMinimize: () -> Unit,
    onFlipCamera: () -> Unit,
    modifier: Modifier = Modifier,
    localVideoTrack: VideoTrack? = null,
    onInitRenderer: ((TextureViewRenderer) -> Unit)? = null,
) {
    val colors = AppTheme.colors
    val pipShape = RoundedCornerShape(22.dp)

    if (isMinimized) {
        Box(
            modifier = modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(colors.surfaceElevated)
                .border(2.dp, colors.background, CircleShape)
                .clickable { onToggleMinimize() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(AppIcons.Video),
                contentDescription = "Mở rộng camera",
                tint = colors.ink,
                modifier = Modifier.size(22.dp),
            )
        }
    } else {
        Box(
            modifier = modifier
                .width(108.dp)
                .height(150.dp)
                .clip(pipShape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            colors.peach.copy(alpha = 0.65f),
                            colors.surfaceElevated,
                        ),
                    ),
                )
                .border(3.dp, colors.background, pipShape)
                .clickable { onToggleMinimize() },
        ) {
            if (localVideoTrack != null && onInitRenderer != null) {
                LiveKitVideoView(
                    videoTrack = localVideoTrack,
                    onInitRenderer = onInitRenderer,
                    isMirror = isFrontCamera,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    DiuOrbAvatar(
                        initials = "B",
                        moodColor = colors.peach,
                        size = 42.dp,
                    )

                    Text(
                        text = if (isFrontCamera) "Bạn" else "Cam sau",
                        color = colors.inkSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }

            IconButton(
                onClick = onFlipCamera,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(6.dp)
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(colors.surface),
            ) {
                Icon(
                    painter = painterResource(AppIcons.Flip),
                    contentDescription = "Đổi camera",
                    tint = colors.ink,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
    }
}
