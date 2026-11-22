package com.uzuu.diuchat.feature.journal.domain.model

import androidx.compose.ui.graphics.Color
import com.uzuu.diuchat.feature.base.common.res.AppPalette

enum class JournalMoodType(
    val id: String,
    val displayName: String,
    val color: Color,
) {
    THUONG("thuong", "Thương", AppPalette.MoodThuong),
    VUI("vui", "Vui", AppPalette.MoodVui),
    BINH_YEN("binhyen", "Bình yên", AppPalette.MoodBinhYen),
    NHO("nho", "Nhớ", AppPalette.MoodNho),
    TRAM("tram", "Trầm", AppPalette.MoodTram),
    LO("lo", "Lo", Color(0xFF9E9E9E)),
    ;

    companion object {
        fun fromId(id: String): JournalMoodType = entries.find { it.id.equals(id, ignoreCase = true) } ?: BINH_YEN

        fun coreFiveMoods(): List<JournalMoodType> = listOf(THUONG, VUI, BINH_YEN, NHO, TRAM)
    }
}
