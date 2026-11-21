package com.uzuu.diuchat.feature.location.data.datasource.api.response

import com.uzuu.diuchat.feature.location.data.datasource.api.model.LiveLocationDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LiveLocationResponse(
    @SerialName("session") val session: LiveLocationDto? = null,
)
