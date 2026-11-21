package com.uzuu.diuchat.feature.chat.domain.model

private const val COLOR_HEX_THUONG = 0xFFF7A3A9
private const val COLOR_HEX_VUI = 0xFFF4CA84
private const val COLOR_HEX_BINHYEN = 0xFFA2D7AC
private const val COLOR_HEX_NHO = 0xFFC5B3EA
private const val COLOR_HEX_TRAM = 0xFF95C4E7
private const val COLOR_HEX_LO = 0xFFE5A96A

enum class MoodType(
    val id: String,
    val displayName: String,
    val colorHex: Long,
) {
    THUONG("thuong", "Thương", COLOR_HEX_THUONG),
    VUI("vui", "Vui", COLOR_HEX_VUI),
    BINHYEN("binhyen", "Bình yên", COLOR_HEX_BINHYEN),
    NHO("nho", "Nhớ", COLOR_HEX_NHO),
    TRAM("tram", "Trầm", COLOR_HEX_TRAM),
    LO("lo", "Lo", COLOR_HEX_LO),
    ;

    companion object {
        fun fromId(id: String?): MoodType = entries.find { it.id.equals(id, ignoreCase = true) } ?: BINHYEN
    }
}
