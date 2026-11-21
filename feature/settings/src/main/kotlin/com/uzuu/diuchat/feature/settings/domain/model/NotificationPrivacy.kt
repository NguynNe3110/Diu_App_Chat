package com.uzuu.diuchat.feature.settings.domain.model

enum class NotificationPrivacy(
    val label: String,
    val description: String,
) {
    MOOD_COLOR_ONLY(
        label = "Màu cảm xúc",
        description = "Chỉ hiển thị màu cảm xúc và chỉ báo tin nhắn mới, " +
            "không hiển thị tên người gửi hoặc nội dung trên màn hình khoá và push notification.",
    ),
    SENDER_NAME(
        label = "Tên người gửi",
        description = "Hiển thị tên người gửi và màu cảm xúc, ẩn hoàn toàn nội dung tin nhắn trên thông báo đẩy.",
    ),
    FULL_CONTENT(
        label = "Đầy đủ nội dung",
        description = "Hiển thị tên người gửi, màu cảm xúc và trích đoạn nội dung tin nhắn.",
    ),
}
