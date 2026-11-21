package com.uzuu.diuchat.feature.settings.presentation.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.uzuu.diuchat.feature.base.common.res.AppTheme
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuButton
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuFilterChip
import com.uzuu.diuchat.feature.settings.domain.model.UserProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileSheet(
    currentProfile: UserProfile,
    onSave: (displayName: String, bio: String, birthday: String, primaryMood: String) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val colors = AppTheme.colors

    var displayName by remember { mutableStateOf(currentProfile.displayName) }
    var bio by remember { mutableStateOf(currentProfile.bio) }
    var birthday by remember { mutableStateOf(currentProfile.birthday) }
    var primaryMood by remember { mutableStateOf(currentProfile.primaryMood) }

    val moods = listOf("Thương", "Vui", "Bình yên", "Nhớ", "Trầm", "Lo")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Chỉnh sửa hồ sơ",
                style = AppTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colors.ink,
            )

            // Tên hiển thị
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Tên hiển thị",
                    style = AppTheme.typography.labelSmall,
                    color = colors.inkSecondary,
                )
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.accent,
                        unfocusedBorderColor = colors.line,
                        focusedTextColor = colors.ink,
                        unfocusedTextColor = colors.ink,
                    ),
                )
            }

            // Giới thiệu bản thân
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Lời giới thiệu",
                    style = AppTheme.typography.labelSmall,
                    color = colors.inkSecondary,
                )
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.accent,
                        unfocusedBorderColor = colors.line,
                        focusedTextColor = colors.ink,
                        unfocusedTextColor = colors.ink,
                    ),
                )
            }

            // Ngày sinh
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Ngày sinh",
                    style = AppTheme.typography.labelSmall,
                    color = colors.inkSecondary,
                )
                OutlinedTextField(
                    value = birthday,
                    onValueChange = { birthday = it },
                    singleLine = true,
                    placeholder = { Text("DD/MM/YYYY", color = colors.inkSecondary.copy(alpha = 0.5f)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.accent,
                        unfocusedBorderColor = colors.line,
                        focusedTextColor = colors.ink,
                        unfocusedTextColor = colors.ink,
                    ),
                )
            }

            // Tâm trạng đại diện
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Tâm trạng đại diện",
                    style = AppTheme.typography.labelSmall,
                    color = colors.inkSecondary,
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    moods.forEach { mood ->
                        DiuFilterChip(
                            text = mood,
                            selected = primaryMood == mood,
                            onClick = { primaryMood = mood },
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            DiuButton(
                onClick = {
                    onSave(displayName.trim(), bio.trim(), birthday.trim(), primaryMood)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = displayName.isNotBlank(),
            ) {
                Text(
                    text = "Lưu thay đổi",
                    style = AppTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}
