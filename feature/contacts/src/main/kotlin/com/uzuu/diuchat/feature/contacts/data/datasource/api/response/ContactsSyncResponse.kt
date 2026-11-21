package com.uzuu.diuchat.feature.contacts.data.datasource.api.response

import com.uzuu.diuchat.feature.contacts.data.datasource.api.model.ConnectionRequestDto
import com.uzuu.diuchat.feature.contacts.data.datasource.api.model.ContactDto
import com.uzuu.diuchat.feature.contacts.data.datasource.api.model.InviteLinkDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ContactsSyncResponse(
    @SerialName("contacts")
    val contacts: List<ContactDto> = emptyList(),
    @SerialName("pending_requests")
    val pendingRequests: List<ConnectionRequestDto> = emptyList(),
    @SerialName("invite_link")
    val inviteLink: InviteLinkDto? = null,
)
