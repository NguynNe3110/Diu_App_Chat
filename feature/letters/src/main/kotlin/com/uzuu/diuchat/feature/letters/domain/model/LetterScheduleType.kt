package com.uzuu.diuchat.feature.letters.domain.model

enum class LetterScheduleType(val label: String, val description: String) {
    WAKE_UP(
        label = "Khi thức dậy (06:00)",
        description = "Mở khóa tự động theo khung giờ yên lặng kết thúc của đối phương.",
    ),
    TOMORROW_MORNING(
        label = "08:00 Sáng mai",
        description = "Gửi lời chào ngày mới nhẹ nhàng khi người ấy bắt đầu ngày làm việc.",
    ),
    TONIGHT(
        label = "22:00 Tối nay",
        description = "Lá thư tâm sự cuối ngày trước khi chìm vào giấc ngủ.",
    ),
    CUSTOM_DATE(
        label = "Ngày hẹn ước cụ thể",
        description = "Mở vào ngày kỷ niệm, sinh nhật hoặc ngày hẹn đặc biệt của hai người.",
    ),
}
