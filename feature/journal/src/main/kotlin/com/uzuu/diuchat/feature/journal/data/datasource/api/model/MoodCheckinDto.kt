package com.uzuu.diuchat.feature.journal.data.datasource.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MoodCheckinDto(
    @SerialName("date_iso") val dateIso: String,
    @SerialName("day_number") val dayNumber: Int,
    @SerialName("mood_id") val moodId: String,
    @SerialName("private_note") val privateNote: String? = null,
    @SerialName("shared_with_contact_ids") val sharedWithContactIds: List<String> = emptyList(),
    @SerialName("display_date") val displayDate: String = "",
)
