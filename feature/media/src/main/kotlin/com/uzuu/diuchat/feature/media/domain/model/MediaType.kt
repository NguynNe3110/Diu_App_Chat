package com.uzuu.diuchat.feature.media.domain.model

/**
 * Các loại phương tiện được hỗ trợ trong ứng dụng Dịu.
 *
 * DEC-11: Loại trừ hoàn toàn định dạng Video để giữ trọn vẹn trải nghiệm
 * yên tĩnh, chậm rãi, nhẹ nhàng và tiết kiệm dữ liệu/bộ nhớ thiết bị.
 */
enum class MediaType(val label: String) {
    ALL("Tất cả"),
    PHOTO("Hình ảnh"),
    VOICE("Ghi âm"),
    LETTER("Thư đã mở"),
    ;

    companion object {
        const val IS_VIDEO_SUPPORTED = false
    }
}
