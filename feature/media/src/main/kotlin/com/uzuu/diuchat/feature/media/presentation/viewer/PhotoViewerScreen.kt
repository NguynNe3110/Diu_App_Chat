package com.uzuu.diuchat.feature.media.presentation.viewer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.uzuu.diuchat.feature.base.common.res.AppIcons
import com.uzuu.diuchat.feature.base.common.res.AppPalette
import com.uzuu.diuchat.feature.base.common.res.AppTheme
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuConfirmationDialog
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuIconButton
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuMoodDot
import kotlinx.coroutines.delay

private val viewerMoodReactions = listOf(
    "Thương" to AppPalette.MoodThuong,
    "Vui" to AppPalette.MoodVui,
    "Bình yên" to AppPalette.MoodBinhYen,
    "Nhớ" to AppPalette.MoodNho,
    "Trầm" to AppPalette.MoodTram,
)

@Composable
fun PhotoViewerScreen(
    caption: String,
    moodName: String,
    senderName: String,
    timestamp: String,
    onBackClick: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier,
    imageUrl: String = "",
    selectedReactionMood: String? = null,
    isMemorySaved: Boolean = false,
    onReactionClick: (String) -> Unit = {},
    onReplyClick: () -> Unit = {},
    onSaveMemoryClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
) {
    var showControls by remember { mutableStateOf(true) }
    var hasLoadError by remember(imageUrl) { mutableStateOf(false) }
    var localReactionMood by remember(selectedReactionMood) { mutableStateOf(selectedReactionMood) }
    var localMemorySaved by remember(isMemorySaved) { mutableStateOf(isMemorySaved) }
    var feedbackMessage by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(feedbackMessage) {
        if (feedbackMessage != null) {
            delay(2_000L)
            feedbackMessage = null
        }
    }

    val moodColor = when (moodName) {
        "Thương" -> AppPalette.MoodThuong
        "Vui" -> AppPalette.MoodVui
        "Nhớ" -> AppPalette.MoodNho
        "Trầm" -> AppPalette.MoodTram
        else -> AppPalette.MoodBinhYen
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { showControls = !showControls },
            ),
    ) {
        // Center photo presentation
        if (imageUrl.isNotBlank() && !hasLoadError) {
            AsyncImage(
                model = imageUrl,
                contentDescription = caption.ifBlank { "Hình ảnh kỷ niệm" },
                contentScale = ContentScale.Fit,
                onError = { hasLoadError = true },
                onSuccess = { hasLoadError = false },
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center),
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .aspectRatio(0.85f)
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                moodColor.copy(alpha = 0.5f),
                                moodColor.copy(alpha = 0.25f),
                                Color(0xFF1B1B1B),
                            ),
                        ),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        painter = painterResource(AppIcons.Image),
                        contentDescription = null,
                        tint = moodColor.copy(alpha = 0.8f),
                        modifier = Modifier.size(56.dp),
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = caption.ifBlank { "Hình ảnh kỷ niệm" },
                        color = Color.White.copy(alpha = 0.9f),
                        style = AppTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                    )
                }
            }
        }

        // Transient feedback pill
        AnimatedVisibility(
            visible = feedbackMessage != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 88.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.Black.copy(alpha = 0.78f),
            ) {
                Text(
                    text = feedbackMessage.orEmpty(),
                    color = Color.White,
                    style = AppTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
        }

        // Top Controls
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
            modifier = Modifier.align(Alignment.TopCenter),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.8f), Color.Transparent),
                        ),
                    )
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                DiuIconButton(
                    onClick = onBackClick,
                    contentDescription = "Quay lại",
                ) {
                    Icon(
                        painter = painterResource(AppIcons.Back),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp),
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = senderName.ifBlank { "Hình ảnh" },
                        color = Color.White,
                        style = AppTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    )
                    if (timestamp.isNotBlank()) {
                        Text(
                            text = timestamp,
                            color = Color.White.copy(alpha = 0.7f),
                            style = AppTheme.typography.bodySmall,
                        )
                    }
                }

                DiuIconButton(
                    onClick = onShareClick,
                    contentDescription = "Chia sẻ",
                ) {
                    Icon(
                        painter = painterResource(AppIcons.Share),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }

        // Bottom Controls
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)),
                        ),
                    )
                    .padding(horizontal = 24.dp, vertical = 24.dp),
            ) {
                // Mood Tag
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    DiuMoodDot(color = moodColor, size = 10.dp)
                    Text(
                        text = moodName.ifBlank { "Bình yên" },
                        color = moodColor,
                        style = AppTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                if (caption.isNotBlank()) {
                    Text(
                        text = caption,
                        color = Color.White,
                        style = AppTheme.typography.bodyLarge.copy(fontSize = 17.sp),
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 5 mood reaction dots
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    viewerMoodReactions.forEach { (reactionName, reactionColor) ->
                        val isSelected = localReactionMood == reactionName
                        Box(
                            modifier = Modifier
                                .size(if (isSelected) 34.dp else 28.dp)
                                .clip(CircleShape)
                                .background(reactionColor.copy(alpha = if (isSelected) 0.95f else 0.75f))
                                .then(
                                    if (isSelected) {
                                        Modifier.border(2.dp, Color.White, CircleShape)
                                    } else {
                                        Modifier
                                    },
                                )
                                .clickable {
                                    localReactionMood = if (isSelected) null else reactionName
                                    onReactionClick(reactionName)
                                },
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom actions: Trả lời, Lưu kỷ niệm, Xoá
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    PhotoViewerBottomAction(
                        icon = Icons.AutoMirrored.Filled.Reply,
                        label = "Trả lời",
                        onClick = onReplyClick,
                    )
                    PhotoViewerBottomAction(
                        icon = if (localMemorySaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        label = if (localMemorySaved) "Đã lưu" else "Lưu kỷ niệm",
                        tint = if (localMemorySaved) AppPalette.MoodVui else Color.White,
                        onClick = {
                            localMemorySaved = !localMemorySaved
                            feedbackMessage = if (localMemorySaved) "Đã lưu vào kỷ niệm" else "Đã bỏ lưu kỷ niệm"
                            onSaveMemoryClick()
                        },
                    )
                    PhotoViewerBottomAction(
                        icon = Icons.Outlined.DeleteOutline,
                        label = "Xoá",
                        tint = AppPalette.MoodThuong,
                        onClick = { showDeleteConfirm = true },
                    )
                }
            }
        }
    }

    if (showDeleteConfirm) {
        DiuConfirmationDialog(
            title = "Xoá ảnh này",
            message = "Ảnh sẽ được xoá khỏi cuộc trò chuyện này.",
            confirmLabel = "Xoá",
            dismissLabel = "Huỷ",
            destructive = true,
            onConfirm = {
                showDeleteConfirm = false
                onDeleteClick()
            },
            onDismiss = { showDeleteConfirm = false },
        )
    }
}

@Composable
private fun PhotoViewerBottomAction(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    tint: Color = Color.White,
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.White.copy(alpha = 0.12f),
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = label,
                color = tint,
                style = AppTheme.typography.labelSmall,
            )
        }
    }
}

@Preview(name = "Photo Viewer — Light", showBackground = true)
@Composable
private fun PhotoViewerScreenPreview() {
    AppTheme {
        PhotoViewerScreen(
            caption = "Chiều hoàng hôn bên bờ sông, bình yên lạ kỳ.",
            moodName = "Bình yên",
            senderName = "Minh",
            timestamp = "17:45",
            onBackClick = {},
            onShareClick = {},
        )
    }
}
