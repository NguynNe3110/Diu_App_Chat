package com.uzuu.diuchat.feature.base.presentation.compose.composable

import android.content.res.Configuration
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.uzuu.diuchat.feature.base.common.res.AppTheme

@Composable
fun DiuConfirmationDialog(
    title: String,
    message: String,
    confirmLabel: String,
    dismissLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    destructive: Boolean = false,
) {
    val colors = AppTheme.colors
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        title = {
            Text(title, style = AppTheme.typography.titleLarge)
        },
        text = {
            Text(message, style = AppTheme.typography.bodyMedium)
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = if (destructive) colors.danger else colors.accent,
                ),
            ) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = colors.inkSecondary,
                ),
            ) {
                Text(dismissLabel)
            }
        },
        containerColor = colors.background,
        titleContentColor = colors.ink,
        textContentColor = colors.inkSecondary,
    )
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DiuConfirmationDialogPreview() {
    AppTheme {
        DiuConfirmationDialog(
            title = "Xóa trò chuyện?",
            message = "Lời nhắn sẽ được xóa khỏi thiết bị này.",
            confirmLabel = "Xóa",
            dismissLabel = "Giữ lại",
            onConfirm = {},
            onDismiss = {},
            destructive = true,
        )
    }
}
