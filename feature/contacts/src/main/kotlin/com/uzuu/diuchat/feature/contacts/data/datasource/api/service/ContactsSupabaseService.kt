package com.uzuu.diuchat.feature.contacts.data.datasource.api.service

import com.uzuu.diuchat.feature.contacts.data.datasource.api.model.ConnectionRequestDto
import com.uzuu.diuchat.feature.contacts.data.datasource.api.model.ContactDto
import com.uzuu.diuchat.feature.contacts.data.datasource.api.model.InviteLinkDto
import com.uzuu.diuchat.feature.contacts.data.datasource.api.response.ContactsSyncResponse
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlin.coroutines.cancellation.CancellationException
import timber.log.Timber

interface ContactsSupabaseService {
    suspend fun syncContacts(): ContactsSyncResponse?

    suspend fun fetchContacts(): List<ContactDto>?

    suspend fun fetchPendingRequests(): List<ConnectionRequestDto>?

    suspend fun respondToRequest(
        requestId: String,
        accept: Boolean,
        circle: String,
    ): Boolean

    suspend fun searchUserByPhone(phone: String): ContactDto?

    suspend fun fetchMyInviteLink(): InviteLinkDto?
}

class ContactsSupabaseServiceImpl(
    private val supabaseClient: SupabaseClient?,
) : ContactsSupabaseService {
    override suspend fun syncContacts(): ContactsSyncResponse? {
        val contacts = fetchContacts() ?: return null
        val pendingRequests = fetchPendingRequests() ?: emptyList()
        val inviteLink = fetchMyInviteLink()
        return ContactsSyncResponse(
            contacts = contacts,
            pendingRequests = pendingRequests,
            inviteLink = inviteLink,
        )
    }

    override suspend fun fetchContacts(): List<ContactDto>? {
        val client = supabaseClient ?: return null
        return runCatching {
            client.postgrest
                .from(TABLE_CONTACTS)
                .select()
                .decodeList<ContactDto>()
        }.onFailure { error ->
            if (error is CancellationException) throw error
            Timber.w(error, "Failed to fetch contacts from Supabase")
        }.getOrNull()
    }

    override suspend fun fetchPendingRequests(): List<ConnectionRequestDto>? {
        val client = supabaseClient ?: return null
        return runCatching {
            client.postgrest
                .from(TABLE_CONNECTION_REQUESTS)
                .select()
                .decodeList<ConnectionRequestDto>()
        }.onFailure { error ->
            if (error is CancellationException) throw error
            Timber.w(error, "Failed to fetch connection requests from Supabase")
        }.getOrNull()
    }

    override suspend fun respondToRequest(
        requestId: String,
        accept: Boolean,
        circle: String,
    ): Boolean {
        val client = supabaseClient ?: return false
        return runCatching {
            if (accept) {
                client.postgrest
                    .from(TABLE_CONNECTION_REQUESTS)
                    .update(mapOf("status" to "ACCEPTED", "proposed_circle" to circle)) {
                        filter { eq("id", requestId) }
                    }
            } else {
                client.postgrest
                    .from(TABLE_CONNECTION_REQUESTS)
                    .delete {
                        filter { eq("id", requestId) }
                    }
            }
            true
        }.onFailure { error ->
            if (error is CancellationException) throw error
            Timber.w(error, "Failed to respond to connection request in Supabase")
        }.getOrDefault(false)
    }

    override suspend fun searchUserByPhone(phone: String): ContactDto? {
        val client = supabaseClient ?: return null
        return runCatching {
            client.postgrest
                .from(TABLE_CONTACTS)
                .select {
                    filter { eq("phone", phone) }
                }.decodeSingleOrNull<ContactDto>()
        }.onFailure { error ->
            if (error is CancellationException) throw error
            Timber.w(error, "Failed to search user by phone in Supabase")
        }.getOrNull()
    }

    override suspend fun fetchMyInviteLink(): InviteLinkDto? {
        val client = supabaseClient ?: return null
        return runCatching {
            client.postgrest
                .from(TABLE_INVITE_LINKS)
                .select()
                .decodeSingleOrNull<InviteLinkDto>()
        }.onFailure { error ->
            if (error is CancellationException) throw error
            Timber.w(error, "Failed to fetch invite link from Supabase")
        }.getOrNull()
    }

    private companion object {
        const val TABLE_CONTACTS = "contacts"
        const val TABLE_CONNECTION_REQUESTS = "connection_requests"
        const val TABLE_INVITE_LINKS = "invite_links"
    }
}
