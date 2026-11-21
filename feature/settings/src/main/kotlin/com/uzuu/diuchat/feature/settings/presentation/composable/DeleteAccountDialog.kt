package com.uzuu.diuchat.feature.settings.presentation.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uzuu.diuchat.feature.base.common.res.AppTheme

@Composable
fun DeleteAccountDialog(
    userPhone: String,
    onConfirmDelete: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = AppTheme.colors
    var enteredPhone by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Xoá tài khoản vĩnh viễn?",
                style = AppTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = colors.danger,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Hành động này không thể hoàn tác. Toàn bộ hồ sơ, dữ liệu cá nhân và thiết lập của bạn sẽ bị xoá vĩnh viễn.",
                    style = AppTheme.typography.bodySmall,
                    color = colors.ink,
                    lineHeight = 18.sp,
                )
                Text(
                    text = "Để bảo vệ kỷ niệm chung, các tin nhắn đã gửi tới người thân sẽ được ẩn danh hoá an toàn và đánh dấu đã xoá.",
                    style = AppTheme.typography.bodySmall,
                    color = colors.inkSecondary,
                    lineHeight = 18.sp,
                )
                Text(
                    text = "Nhập số điện thoại của bạn ($userPhone) để xác nhận:",
                    style = AppTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = colors.ink,
                )
                OutlinedTextField(
                    value = enteredPhone,
                    onValueChange = { enteredPhone = it },
                    singleLine = true,
                    placeholder = { Text(userPhone, color = colors.inkSecondary.copy(alpha = 0.5f)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.danger,
                        unfocusedBorderColor = colors.line,
                        focusedTextColor = colors.ink,
                        unfocusedTextColor = colors.ink,
                    ),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirmDelete(enteredPhone.trim()) },
                enabled = enteredPhone.trim() == userPhone || enteredPhone.isNotBlank(),
            ) {
                Text(
                    text = "Xác nhận xoá",
                    color = if (enteredPhone.isNotBlank()) colors.danger else colors.inkSecondary,
                    fontWeight = FontWeight.Bold,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy", color = colors.inkSecondary)
            }
        },
    )
}

@Preview(name = "Delete Account Dialog", showBackground = true)
@Composable
private fun DeleteAccountDialogPreview() {
    AppTheme {
        DeleteAccountDialog(
            userPhone = "0912345678",
            onConfirmDelete = {},
            onDismiss = {},
        )
    }
}
