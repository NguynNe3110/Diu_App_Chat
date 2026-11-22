package com.uzuu.diuchat.feature.letters.domain.model

enum class LetterScheduleType(val label: String, val description: String) {
    IN_ONE_HOUR(
        label = "Sau 1 giờ",
        description = "Gửi chậm nhẹ nhàng, mở khóa sau 60 phút nữa.",
    ),
    TONIGHT(
        label = "22:00 Tối nay",
        description = "Lá thư tâm sự cuối ngày trước khi chìm vào giấc ngủ.",
    ),
    WAKE_UP(
        label = "Khi thức dậy (06:00)",
        description = "Mở khóa tự động theo khung giờ yên lặng kết thúc của đối phương.",
    ),
    TOMORROW_MORNING(
        label = "08:00 Sáng mai",
        description = "Gửi lời chào ngày mới nhẹ nhàng khi người ấy bắt đầu ngày làm việc.",
    ),
    CUSTOM_DATE(
        label = "Tuỳ chọn ngày & giờ",
        description = "Tự chọn bất kỳ ngày và khung giờ nào bạn muốn mở thư.",
    ),
}
