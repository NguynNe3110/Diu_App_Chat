package com.uzuu.diuchat.feature.chat.domain.model

enum class MoodType(
    val id: String,
    val displayName: String,
    val colorHex: Long,
) {
    THUONG("thuong", "Thương", 0xFFF7A3A9),
    VUI("vui", "Vui", 0xFFF4CA84),
    BINHYEN("binhyen", "Bình yên", 0xFFA2D7AC),
    NHO("nho", "Nhớ", 0xFFC5B3EA),
    TRAM("tram", "Trầm", 0xFF95C4E7),
    LO("lo", "Lo", 0xFFE5A96A);

    companion object {
        fun fromId(id: String?): MoodType = entries.find { it.id.equals(id, ignoreCase = true) } ?: BINHYEN
    }
}
