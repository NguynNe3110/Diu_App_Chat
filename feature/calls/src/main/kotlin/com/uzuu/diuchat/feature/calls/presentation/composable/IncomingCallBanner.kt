package com.uzuu.diuchat.feature.calls.presentation.composable

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uzuu.diuchat.feature.base.common.res.AppIcons
import com.uzuu.diuchat.feature.base.common.res.AppTheme
import com.uzuu.diuchat.feature.base.common.res.DiuSerif
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuOrbAvatar
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuRippleOrb
import com.uzuu.diuchat.feature.calls.domain.model.CallSession
import com.uzuu.diuchat.feature.calls.domain.model.CallType

val INCOMING_QUICK_REPLY_OPTIONS = listOf(
    "Mình gọi lại sau ít phút nha",
    "Đang họp, nhắn nhé",
    "Thương, tí gọi lại",
)

@Composable
fun IncomingCallFullScreen(
    session: CallSession,
    peerMoodColor: Color,
    isQuickReplyExpanded: Boolean,
    onToggleQuickReply: () -> Unit,
    onSelectQuickReply: (String) -> Unit,
    onRemindMe: () -> Unit,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    modifier: Modifier = Modifier,
    peerMoodLabel: String = "thương",
) {
    val colors = AppTheme.colors
    val callerName = if (session.isIncoming) session.callerName else session.calleeName

    val pulseTransition = rememberInfiniteTransition(label = "incomingAcceptPulse")
    val acceptScale by pulseTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "acceptBtnScale",
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 28.dp),
        ) {
            Text(
                text = if (session.type == CallType.VIDEO) "Gọi video dịu dàng" else "Đang gọi cho bạn",
                fontSize = 14.sp,
                color = colors.inkSecondary,
            )

            Spacer(modifier = Modifier.height(32.dp))

            DiuRippleOrb(
                initials = callerName.ifEmpty { "D" }.take(1).uppercase(),
                moodColor = peerMoodColor,
                size = 148.dp,
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = callerName,
                fontFamily = DiuSerif,
                fontSize = 40.sp,
                fontWeight = FontWeight.Normal,
                color = colors.ink,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = "●",
                    fontSize = 12.sp,
                    color = peerMoodColor,
                )
                Text(
                    text = "đang thấy $peerMoodLabel",
                    fontSize = 14.sp,
                    color = colors.inkSecondary,
                )
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AnimatedVisibility(visible = isQuickReplyExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    INCOMING_QUICK_REPLY_OPTIONS.forEach { replyText ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(colors.surfaceElevated)
                                .border(1.dp, colors.line, RoundedCornerShape(20.dp))
                                .clickable { onSelectQuickReply(replyText) }
                                .padding(horizontal = 18.dp, vertical = 10.dp),
                        ) {
                            Text(
                                text = replyText,
                                fontSize = 13.5.sp,
                                color = colors.ink,
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IncomingActionButton(
                    iconRes = AppIcons.Chat,
                    label = "Nhắn lại",
                    containerColor = if (isQuickReplyExpanded) colors.ink else colors.surface,
                    contentColor = if (isQuickReplyExpanded) colors.background else colors.ink,
                    borderColor = colors.line,
                    onClick = onToggleQuickReply,
                )

                IncomingActionButton(
                    iconRes = AppIcons.Close,
                    label = "Từ chối",
                    containerColor = colors.callEnd,
                    contentColor = colors.onAccent,
                    borderColor = null,
                    onClick = onDecline,
                )

                IncomingActionButton(
                    iconRes = if (session.type == CallType.VIDEO) AppIcons.Video else AppIcons.Phone,
                    label = "Nghe",
                    containerColor = colors.callAccept,
                    contentColor = colors.onAccent,
                    borderColor = null,
                    scale = acceptScale,
                    onClick = onAccept,
                )

                IncomingActionButton(
                    iconRes = AppIcons.Clock,
                    label = "Nhắc tôi",
                    containerColor = colors.surface,
                    contentColor = colors.ink,
                    borderColor = colors.line,
                    onClick = onRemindMe,
                )
            }
        }
    }
}

@Composable
private fun IncomingActionButton(
    iconRes: Int,
    label: String,
    containerColor: Color,
    contentColor: Color,
    borderColor: Color?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    scale: Float = 1f,
) {
    val colors = AppTheme.colors

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clip(CircleShape)
                .background(containerColor)
                .then(
                    if (borderColor != null) {
                        Modifier.border(1.dp, borderColor, CircleShape)
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

@Composable
fun IncomingCallBanner(
    session: CallSession,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceElevated),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                DiuOrbAvatar(
                    initials = session.callerName.ifEmpty { "D" }.take(1).uppercase(),
                    moodColor = colors.peach,
                    size = 48.dp,
                )
                Column {
                    Text(
                        text = session.callerName,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = colors.ink,
                    )
                    Text(
                        text = if (session.type == CallType.VIDEO) "Cuộc gọi video đến..." else "Cuộc gọi thoại đến...",
                        fontSize = 13.sp,
                        color = colors.inkSecondary,
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IconButton(
                    onClick = onDecline,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(colors.callEnd),
                ) {
                    Icon(
                        painter = painterResource(AppIcons.Close),
                        contentDescription = "Từ chối",
                        tint = colors.onAccent,
                        modifier = Modifier.size(20.dp),
                    )
                }

                IconButton(
                    onClick = onAccept,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(colors.callAccept),
                ) {
                    Icon(
                        painter = painterResource(AppIcons.Phone),
                        contentDescription = "Nghe máy",
                        tint = colors.onAccent,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}
