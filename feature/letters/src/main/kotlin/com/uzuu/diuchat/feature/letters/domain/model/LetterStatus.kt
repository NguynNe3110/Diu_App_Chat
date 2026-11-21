package com.uzuu.diuchat.feature.letters.domain.model

enum class LetterStatus {
    SEALED_PENDING, // Đang niêm phong chờ đến hạn
    READY_TO_OPEN, // Đã đến giờ hẹn, người nhận có thể mở
    OPENED, // Đã mở niêm phong và đọc thư
    CANCELLED, // Người gửi đã hủy thư trước khi mở
}
