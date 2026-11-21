package com.uzuu.diuchat.feature.letters.presentation.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.LockClock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uzuu.diuchat.feature.base.common.res.AppTheme
import com.uzuu.diuchat.feature.letters.domain.model.LetterScheduleType
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleSheet(
    selectedType: LetterScheduleType,
    onSelectSchedule: (LetterScheduleType, String, Long?) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val colors = AppTheme.colors
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var pickedDateMillis by remember { mutableStateOf<Long?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.LockClock,
                    contentDescription = null,
                    tint = colors.accent,
                )
                Text(
                    text = "Thời điểm mở thư",
                    style = AppTheme.typography.titleLarge,
                    color = colors.ink,
                )
            }

            Text(
                text = "Lá thư sẽ được niêm phong an toàn và chỉ mở khóa khi chạm đúng thời khắc đã chọn.",
                style = AppTheme.typography.bodySmall,
                color = colors.inkSecondary,
                lineHeight = 18.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
            )

            LetterScheduleType.entries.forEach { option ->
                val isSelected = option == selectedType
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) colors.surface else colors.background)
                        .clickable {
                            if (option == LetterScheduleType.CUSTOM_DATE) {
                                showDatePicker = true
                            } else {
                                val formattedTime = when (option) {
                                    LetterScheduleType.WAKE_UP -> "06:00 Sáng (Khi đối phương thức dậy)"
                                    LetterScheduleType.TOMORROW_MORNING -> "08:00 Sáng mai"
                                    LetterScheduleType.TONIGHT -> "22:00 Tối nay"
                                    LetterScheduleType.CUSTOM_DATE -> ""
                                }
                                onSelectSchedule(option, formattedTime, null)
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = option.label,
                            style = AppTheme.typography.bodyLarge,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                            color = if (isSelected) colors.accent else colors.ink,
                        )
                        Text(
                            text = option.description,
                            style = AppTheme.typography.bodySmall,
                            color = colors.inkSecondary,
                            lineHeight = 16.sp,
                        )
                    }
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Đã chọn",
                            tint = colors.accent,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = System.currentTimeMillis() + 24 * 3600 * 1000L,
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickedDateMillis = datePickerState.selectedDateMillis
                        showDatePicker = false
                        showTimePicker = true
                    },
                ) {
                    Text("Chọn giờ", color = colors.accent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Hủy", color = colors.inkSecondary)
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = 20,
            initialMinute = 0,
            is24Hour = true,
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = {
                Text(
                    text = "Chọn giờ mở thư",
                    style = AppTheme.typography.titleMedium,
                    color = colors.ink,
                )
            },
            text = {
                TimePicker(state = timePickerState)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val baseDate = Instant.ofEpochMilli(
                            pickedDateMillis ?: System.currentTimeMillis(),
                        ).atZone(ZoneId.of("UTC")).toLocalDate()
                        val selectedDateTime = LocalDateTime.of(
                            baseDate,
                            LocalTime.of(timePickerState.hour, timePickerState.minute),
                        )
                        val epochMs = selectedDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                        val formatted = selectedDateTime.format(
                            DateTimeFormatter.ofPattern("HH:mm · dd/MM/yyyy", Locale.forLanguageTag("vi-VN")),
                        )
                        showTimePicker = false
                        onSelectSchedule(LetterScheduleType.CUSTOM_DATE, formatted, epochMs)
                    },
                ) {
                    Text("Xác nhận", color = colors.accent, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Hủy", color = colors.inkSecondary)
                }
            },
        )
    }
}
