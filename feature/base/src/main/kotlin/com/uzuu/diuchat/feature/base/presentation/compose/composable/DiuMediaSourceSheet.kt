package com.uzuu.diuchat.feature.base.presentation.compose.composable

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.uzuu.diuchat.feature.base.common.res.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiuMediaSourceSheet(
    onPickFromGallery: () -> Unit,
    onTakePhoto: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Chọn ảnh",
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val colors = AppTheme.colors

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.background,
        modifier = modifier,
    ) {
        Column(
            modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = title,
                style = AppTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.ink,
            )

            MediaSourceOptionRow(
                icon = Icons.Outlined.PhotoLibrary,
                title = "Chọn ảnh từ thư viện",
                subtitle = "Mở bộ chọn ảnh hệ thống, không cần quyền lưu trữ",
                onClick = {
                    onDismiss()
                    onPickFromGallery()
                },
            )

            MediaSourceOptionRow(
                icon = Icons.Outlined.PhotoCamera,
                title = "Chụp ảnh mới",
                subtitle = "Mở máy ảnh để chụp trực tiếp",
                onClick = {
                    onDismiss()
                    onTakePhoto()
                },
            )

            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
private fun MediaSourceOptionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    val colors = AppTheme.colors
    Surface(
        modifier =
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        color = colors.surface,
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.accent,
                modifier = Modifier.size(24.dp),
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = AppTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = colors.ink,
                )
                Text(
                    text = subtitle,
                    style = AppTheme.typography.bodySmall,
                    color = colors.inkSecondary,
                )
            }
        }
    }
}
