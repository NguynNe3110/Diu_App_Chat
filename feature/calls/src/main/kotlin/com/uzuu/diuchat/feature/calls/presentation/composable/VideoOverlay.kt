package com.uzuu.diuchat.feature.calls.presentation.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uzuu.diuchat.feature.base.common.res.AppIcons
import com.uzuu.diuchat.feature.base.common.res.AppTheme

@Composable
fun VideoOverlay(
    isMinimized: Boolean,
    isFrontCamera: Boolean,
    onToggleMinimize: () -> Unit,
    onFlipCamera: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors

    if (isMinimized) {
        Box(
            modifier = modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(colors.ink.copy(alpha = 0.85f))
                .border(2.dp, colors.surface, CircleShape)
                .clickable { onToggleMinimize() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(AppIcons.Video),
                contentDescription = "Mở rộng camera",
                tint = colors.surface,
                modifier = Modifier.size(24.dp),
            )
        }
    } else {
        Box(
            modifier = modifier
                .width(120.dp)
                .height(160.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF2C3437))
                .border(2.dp, colors.line, RoundedCornerShape(16.dp))
                .clickable { onToggleMinimize() },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(colors.peach),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Tôi",
                        color = colors.ink,
                        fontSize = 12.sp,
                    )
                }

                Text(
                    text = if (isFrontCamera) "Trước" else "Sau",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 10.sp,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }

            IconButton(
                onClick = onFlipCamera,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f)),
            ) {
                Icon(
                    painter = painterResource(AppIcons.Flip),
                    contentDescription = "Đổi camera",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}
