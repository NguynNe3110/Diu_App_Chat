package com.uzuu.diuchat.feature.journal.data.datasource.api.response

import com.uzuu.diuchat.feature.journal.data.datasource.api.model.FavouriteMemoryDto
import com.uzuu.diuchat.feature.journal.data.datasource.api.model.MoodCheckinDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class JournalCheckinsResponse(
    @SerialName("checkins") val checkins: List<MoodCheckinDto> = emptyList(),
    @SerialName("favourites") val favourites: List<FavouriteMemoryDto> = emptyList(),
)
