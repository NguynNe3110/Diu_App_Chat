package com.uzuu.diuchat.feature.letters.data.datasource.api.response

import com.uzuu.diuchat.feature.letters.data.datasource.api.model.LetterDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LettersResponse(
    @SerialName("letters")
    val letters: List<LetterDto> = emptyList(),
)
