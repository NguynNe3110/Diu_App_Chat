package com.uzuu.diuchat.feature.base.presentation.compose.preview

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.uzuu.diuchat.feature.base.common.res.AppTheme
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuButton
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuButtonVariant
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuCard
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuCheckbox
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuConfirmationDialog
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuEmptyState
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuFilterChip
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuIconButton
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuInlineNotice
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuInviteCodeCard
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuMoodDot
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuMoodOption
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuMoodPicker
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuOrbAvatar
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuPaginationDots
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuPersonRow
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuRadioButton
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuSearchField
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuSectionHeader
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuSelectedPersonChip
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuSettingsRow
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuSwitch
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuTextButton
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuTextField
import com.uzuu.diuchat.feature.base.presentation.compose.composable.DiuTopBar

@Preview(name = "Components — Light", showBackground = true, widthDp = 412, heightDp = 2200)
@Preview(name = "Components — Dark", showBackground = true, widthDp = 412, heightDp = 2200, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DiuComponentsPreview() {
    AppTheme {
        var checked by remember { mutableStateOf(false) }
        var radioSelected by remember { mutableStateOf(false) }
        var query by remember { mutableStateOf("") }
        var name by remember { mutableStateOf("Lan") }
        var chipSelected by remember { mutableStateOf(true) }
        var selectedMood by remember { mutableStateOf<String?>("thuong") }
        var selectedPage by remember { mutableIntStateOf(0) }
        var personVisible by remember { mutableStateOf(true) }
        var dialogVisible by remember { mutableStateOf(false) }
        var copied by remember { mutableStateOf(false) }
        val moods = AppTheme.colors.moods
        val moodOptions = listOf(
            DiuMoodOption("thuong", "Thương", moods.thuong),
            DiuMoodOption("vui", "Vui", moods.vui),
            DiuMoodOption("binhyen", "Bình yên", moods.binhyen),
            DiuMoodOption("nho", "Nhớ", moods.nho),
            DiuMoodOption("tram", "Trầm", moods.tram),
        )

        Surface(color = AppTheme.colors.background) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text("Thư viện component Dịu", style = AppTheme.typography.headlineSmall)

                Text("DiuOrbAvatar · DiuMoodDot", style = AppTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DiuOrbAvatar("L", moods.thuong, floatOffsetY = 1.dp)
                    DiuOrbAvatar("M", moods.vui, floatOffsetY = 1.dp)
                    DiuOrbAvatar("T", moods.nho, floatOffsetY = 1.dp)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    moodOptions.forEach { DiuMoodDot(it.color, size = 16.dp) }
                }

                Text("DiuButton · DiuTextButton · DiuIconButton", style = AppTheme.typography.titleMedium)
                DiuButton(onClick = {}, modifier = Modifier.fillMaxWidth()) { Text("Tiếp tục (Primary)") }
                DiuButton(onClick = {}, modifier = Modifier.fillMaxWidth(), variant = DiuButtonVariant.Outline) { Text("Viền ngoài (Outline)") }
                DiuButton(onClick = {}, modifier = Modifier.fillMaxWidth(), variant = DiuButtonVariant.Danger) { Text("Xóa cuộc trò chuyện (Danger)") }
                DiuButton(onClick = {}, modifier = Modifier.fillMaxWidth(), enabled = false) { Text("Vô hiệu hóa (Disabled)") }
                DiuTextButton("Để sau cũng được", {})
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DiuIconButton({}, "Quay lại", soft = true) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null)
                    }
                    DiuIconButton({}, "Quay lại", enabled = false) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null)
                    }
                }

                Text("DiuSwitch · DiuCheckbox · DiuRadioButton", style = AppTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DiuSwitch(checked, { checked = it }, "Tạm ngừng thông báo")
                    DiuSwitch(true, {}, "Thông báo vô hiệu", enabled = false)
                    DiuCheckbox(checked, { checked = it }, "Chọn người thương")
                    DiuRadioButton(radioSelected, { radioSelected = !radioSelected }, "Chọn âm thanh")
                }

                Text("DiuFilterChip · DiuMoodPicker", style = AppTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DiuFilterChip("Tất cả", chipSelected, { chipSelected = !chipSelected })
                    DiuFilterChip("Gọi nhỡ", !chipSelected, { chipSelected = !chipSelected })
                }
                DiuMoodPicker(moodOptions, selectedMood, { selectedMood = it })

                Text("DiuSelectedPersonChip", style = AppTheme.typography.titleMedium)
                if (personVisible) {
                    DiuSelectedPersonChip("Thảo", "T", moods.nho, "Bỏ chọn Thảo", { personVisible = false })
                } else {
                    DiuTextButton("Chọn lại Thảo", { personVisible = true })
                }

                Text("DiuSearchField · DiuTextField", style = AppTheme.typography.titleMedium)
                DiuSearchField(
                    value = query,
                    onValueChange = { query = it },
                    label = "Tìm người thân",
                    onClear = { query = "" },
                    modifier = Modifier.fillMaxWidth(),
                )
                DiuTextField(name, { name = it }, "Tên của bạn", Modifier.fillMaxWidth())
                DiuTextField("", {}, "Đôi dòng về bạn", Modifier.fillMaxWidth(), singleLine = false)

                Text("DiuCard · DiuSettingsRow", style = AppTheme.typography.titleMedium)
                DiuCard(Modifier.fillMaxWidth()) {
                    Text("Khoảng yên", style = AppTheme.typography.titleMedium)
                    DiuSettingsRow("Thông báo", subtitle = "Âm thanh và rung", onClick = {})
                    DiuSettingsRow("Chặn người này", onClick = { dialogVisible = true }, destructive = true)
                }

                Text("DiuTopBar · DiuSectionHeader · DiuPersonRow", style = AppTheme.typography.titleMedium)
                DiuTopBar(
                    title = "Thông tin trò chuyện",
                    subtitle = "Những điều nhỏ của hai người",
                    navigationIcon = {
                        DiuIconButton({}, "Quay lại") {
                            Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null)
                        }
                    },
                )
                DiuSectionHeader("NGƯỜI THƯƠNG", action = { DiuTextButton("Xem tất cả", {}) })
                DiuPersonRow("Minh", "M", moods.thuong, {}, subtitle = "Thật hả! Em vui quá", timestamp = "21:14")

                Text("DiuPaginationDots", style = AppTheme.typography.titleMedium)
                DiuPaginationDots(3, selectedPage, { "Trang ${it + 1}" }, { selectedPage = it })

                Text("DiuInlineNotice · DiuInviteCodeCard", style = AppTheme.typography.titleMedium)
                DiuInlineNotice("Giờ yên lặng · đến 6:00", Modifier.fillMaxWidth())
                DiuInviteCodeCard("DIU-7K2M", "Mã mời của bạn", "Sao chép mã mời", { copied = true }, Modifier.fillMaxWidth())
                if (copied) Text("Đã thử callback sao chép; Preview không ghi clipboard.")

                Text("DiuEmptyState", style = AppTheme.typography.titleMedium)
                DiuEmptyState(
                    title = "Chưa có lời nhắn nào",
                    description = "Mời người thương để bắt đầu trò chuyện.",
                    action = { DiuButton(onClick = {}) { Text("Mời người thương") } },
                )

                Text("DiuConfirmationDialog", style = AppTheme.typography.titleMedium)
                DiuTextButton("Mở hộp thoại mẫu", { dialogVisible = true })
            }

            if (dialogVisible) {
                DiuConfirmationDialog(
                    title = "Xóa trò chuyện?",
                    message = "Đây là bản mẫu, không xóa dữ liệu.",
                    confirmLabel = "Xác nhận",
                    dismissLabel = "Giữ lại",
                    onConfirm = { dialogVisible = false },
                    onDismiss = { dialogVisible = false },
                    destructive = true,
                )
            }
        }
    }
}

@Preview(name = "Confirmation — Light", showBackground = true)
@Preview(name = "Confirmation — Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DiuComponentsDialogPreview() {
    AppTheme {
        DiuConfirmationDialog(
            title = "Xóa trò chuyện?",
            message = "Đây là bản mẫu, không xóa dữ liệu.",
            confirmLabel = "Xác nhận",
            dismissLabel = "Giữ lại",
            onConfirm = {},
            onDismiss = {},
            destructive = true,
        )
    }
}
