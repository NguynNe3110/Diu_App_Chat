package com.uzuu.diuchat.feature.calls.presentation.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uzuu.diuchat.feature.base.common.res.AppIcons
import com.uzuu.diuchat.feature.base.common.res.AppTheme
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuOrbAvatar
import com.uzuu.diuchat.feature.calls.domain.model.CallSession
import com.uzuu.diuchat.feature.calls.domain.model.CallType

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
                    initials = session.callerName.take(1),
                    moodColor = colors.accent,
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
                // Decline
                IconButton(
                    onClick = onDecline,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE53935)),
                ) {
                    Icon(
                        painter = painterResource(AppIcons.Close),
                        contentDescription = "Từ chối",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp),
                    )
                }

                // Accept
                IconButton(
                    onClick = onAccept,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF43A047)),
                ) {
                    Icon(
                        painter = painterResource(AppIcons.Phone),
                        contentDescription = "Nghe máy",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}
