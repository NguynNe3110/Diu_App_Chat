package com.uzuu.diuchat.feature.contacts.data.datasource.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InviteLinkDto(
    @SerialName("code")
    val code: String,
    @SerialName("url")
    val url: String,
)
