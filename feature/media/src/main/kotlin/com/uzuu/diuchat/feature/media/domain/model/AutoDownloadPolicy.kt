package com.uzuu.diuchat.feature.media.domain.model

enum class AutoDownloadPolicy(
    val label: String,
    val description: String,
) {
    WIFI_ONLY(
        label = "Chỉ Wi-Fi",
        description = "Tự động tải trước hình ảnh và tin nhắn thoại khi kết nối Wi-Fi.",
    ),
    CELLULAR_AND_WIFI(
        label = "Wi-Fi & Dữ liệu di động",
        description = "Luôn tự động tải hình ảnh và tin nhắn thoại trên mọi kết nối.",
    ),
    MANUAL(
        label = "Thủ công",
        description = "Chỉ tải xuống khi bạn chạm trực tiếp vào tệp phương tiện.",
    ),
}
