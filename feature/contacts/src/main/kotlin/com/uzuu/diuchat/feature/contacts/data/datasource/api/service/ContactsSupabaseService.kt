package com.uzuu.diuchat.feature.contacts.data.datasource.api.service

import com.uzuu.diuchat.feature.contacts.data.datasource.api.model.ConnectionRequestDto
import com.uzuu.diuchat.feature.contacts.data.datasource.api.model.ContactDto
import com.uzuu.diuchat.feature.contacts.data.datasource.api.model.InviteLinkDto
import com.uzuu.diuchat.feature.contacts.data.datasource.api.response.ContactsSyncResponse
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import timber.log.Timber
import java.util.UUID
import kotlin.coroutines.cancellation.CancellationException

class ContactsSupabaseService(
    private val supabaseClient: SupabaseClient?,
) {
    suspend fun syncContacts(): ContactsSyncResponse? {
        val client = supabaseClient ?: return null
        val userId = client.auth.currentUserOrNull()?.id ?: return null
        val contacts = fetchContactsForUser(client, userId)
        return contacts?.let { list ->
            val pendingRequests = fetchPendingRequestsForUser(client, userId, list.map { it.id }.toSet())
            val inviteLink = fetchMyInviteLinkForUser(client, userId)
            ContactsSyncResponse(
                contacts = list,
                pendingRequests = pendingRequests,
                inviteLink = inviteLink,
            )
        }
    }

    suspend fun fetchContacts(): List<ContactDto>? {
        val client = supabaseClient ?: return null
        val userId = client.auth.currentUserOrNull()?.id ?: return null
        return fetchContactsForUser(client, userId)
    }

    suspend fun fetchPendingRequests(): List<ConnectionRequestDto>? {
        val client = supabaseClient ?: return null
        val userId = client.auth.currentUserOrNull()?.id ?: return null
        val existingIds = fetchContactsForUser(client, userId)?.map { it.id }?.toSet() ?: emptySet()
        return fetchPendingRequestsForUser(client, userId, existingIds)
    }

    @Suppress("TooGenericExceptionCaught")
    suspend fun respondToRequest(
        requestId: String,
        accept: Boolean,
        circle: String,
        senderId: String? = null,
    ): Boolean {
        val client = supabaseClient ?: return false
        val currentUserId = client.auth.currentUserOrNull()?.id ?: return false
        return try {
            if (accept) {
                val targetUserId = senderId ?: requestId
                client.postgrest.from(TABLE_CONTACTS).upsert(
                    ContactInsertRow(
                        userId = currentUserId,
                        contactUserId = targetUserId,
                        circle = circle.toDbCircle(),
                    ),
                ) {
                    onConflict = "user_id,contact_user_id"
                }
                getOrCreateDirectConversation(targetUserId)
            }
            true
        } catch (error: CancellationException) {
            throw error
        } catch (error: Throwable) {
            Timber.w(error, "Failed to respond to connection request in Supabase")
            false
        }
    }

    suspend fun searchUserByPhone(phone: String): ContactDto? {
        val lookup = lookupUser(phone) ?: return null
        return lookup.toContactDto(circle = "FRIENDS")
    }

    suspend fun connectByQuery(
        query: String,
        circle: String = "FRIENDS",
    ): Result<ContactDto> = runCatching {
        val client = supabaseClient ?: error("Chưa cấu hình Supabase")
        val currentUserId = client.auth.currentUserOrNull()?.id ?: error("Bạn cần đăng nhập để kết bạn")
        val target = lookupUser(query)
            ?: error("Không tìm thấy người dùng với mã hoặc tên \"$query\"")

        check(target.userId != currentUserId) { "Không thể tự kết bạn với chính mình" }

        client.postgrest.from(TABLE_CONTACTS).upsert(
            ContactInsertRow(
                userId = currentUserId,
                contactUserId = target.userId,
                circle = circle.toDbCircle(),
            ),
        ) {
            onConflict = "user_id,contact_user_id"
        }

        getOrCreateDirectConversation(target.userId)
        target.toContactDto(circle = circle)
    }

    @Suppress("TooGenericExceptionCaught")
    suspend fun getOrCreateDirectConversation(contactUserId: String): String? {
        val client = supabaseClient ?: return null
        val currentUserId = client.auth.currentUserOrNull()?.id ?: return null
        return try {
            val result = client.postgrest.rpc(
                function = RPC_GET_OR_CREATE_DIRECT_CONVERSATION,
                parameters = buildJsonObject {
                    put("p_user_a", currentUserId)
                    put("p_user_b", contactUserId)
                },
            )
            result.data.trim('"')
        } catch (error: CancellationException) {
            throw error
        } catch (error: Throwable) {
            Timber.w(error, "Failed to get_or_create_direct_conversation")
            null
        }
    }

    suspend fun fetchMyInviteLink(): InviteLinkDto? {
        val client = supabaseClient ?: return null
        val userId = client.auth.currentUserOrNull()?.id ?: return null
        return fetchMyInviteLinkForUser(client, userId)
    }

    @Suppress("TooGenericExceptionCaught")
    private suspend fun fetchContactsForUser(
        client: SupabaseClient,
        userId: String,
    ): List<ContactDto>? = try {
        val rows = client.postgrest
            .from(TABLE_CONTACTS)
            .select {
                filter { eq("user_id", userId) }
            }.decodeList<ContactRow>()

        if (rows.isEmpty()) {
            emptyList()
        } else {
            val targetIds = rows.map { it.contactUserId }.distinct()
            val profiles = client.postgrest
                .from(TABLE_USER_PROFILES)
                .select {
                    filter { isIn("id", targetIds) }
                }.decodeList<UserProfileRow>()
                .associateBy { it.id }

            rows.mapNotNull { row ->
                profiles[row.contactUserId]?.let { profile ->
                    val displayName = row.customNickname?.takeIf { it.isNotBlank() } ?: profile.displayName
                    ContactDto(
                        id = profile.id,
                        name = displayName,
                        initials = displayName.trim().take(1).uppercase().ifEmpty { "?" },
                        phone = profile.phoneNumber,
                        circle = row.circle.toDomainCircle(),
                        moodColorHex = moodToHex(profile.currentMood),
                        nickname = row.customNickname,
                        anniversary = row.anniversaryDate,
                        isQuiet = false,
                        lastSeen = profile.username?.let { "@$it" },
                    )
                }
            }
        }
    } catch (error: CancellationException) {
        throw error
    } catch (error: Throwable) {
        Timber.w(error, "Failed to fetch contacts from Supabase")
        null
    }

    @Suppress("TooGenericExceptionCaught")
    private suspend fun fetchPendingRequestsForUser(
        client: SupabaseClient,
        userId: String,
        alreadyAddedIds: Set<String>,
    ): List<ConnectionRequestDto> = try {
        val incomingRows = client.postgrest
            .from(TABLE_CONTACTS)
            .select {
                filter { eq("contact_user_id", userId) }
            }.decodeList<ContactRow>()
            .filter { it.userId !in alreadyAddedIds }

        if (incomingRows.isEmpty()) {
            emptyList()
        } else {
            val senderIds = incomingRows.map { it.userId }.distinct()
            val profiles = client.postgrest
                .from(TABLE_USER_PROFILES)
                .select {
                    filter { isIn("id", senderIds) }
                }.decodeList<UserProfileRow>()
                .associateBy { it.id }

            incomingRows.mapNotNull { row ->
                profiles[row.userId]?.let { profile ->
                    ConnectionRequestDto(
                        id = row.id,
                        senderId = profile.id,
                        senderName = profile.displayName,
                        senderInitials = profile.displayName.trim().take(1).uppercase().ifEmpty { "?" },
                        senderPhone = profile.phoneNumber,
                        senderMoodColorHex = moodToHex(profile.currentMood),
                        proposedCircle = row.circle.toDomainCircle(),
                        createdAt = row.createdAt ?: "Mới đây",
                    )
                }
            }
        }
    } catch (error: CancellationException) {
        throw error
    } catch (error: Throwable) {
        Timber.w(error, "Failed to fetch incoming connection requests from Supabase")
        emptyList()
    }

    @Suppress("TooGenericExceptionCaught")
    private suspend fun fetchMyInviteLinkForUser(
        client: SupabaseClient,
        userId: String,
    ): InviteLinkDto? = try {
        val profile = client.postgrest
            .from(TABLE_USER_PROFILES)
            .select {
                filter { eq("id", userId) }
            }.decodeSingleOrNull<UserProfileRow>()

        val settings = client.postgrest
            .from(TABLE_USER_SETTINGS)
            .select {
                filter { eq("user_id", userId) }
            }.decodeSingleOrNull<UserSettingsTokenRow>()

        val slug = profile?.username?.takeIf { it.isNotBlank() } ?: "diu"
        val token = settings?.qrToken ?: userId
        InviteLinkDto(
            code = token,
            url = "diu.vn/$slug/$token",
        )
    } catch (error: CancellationException) {
        throw error
    } catch (error: Throwable) {
        Timber.w(error, "Failed to fetch invite link from Supabase")
        null
    }

    private suspend fun lookupUser(query: String): LookupUserResult? {
        val client = supabaseClient
        val trimmed = query.trim()
        return if (client == null || trimmed.isEmpty()) {
            null
        } else {
            lookupViaConnectRpc(client, trimmed) ?: lookupViaQrFallback(client, trimmed)
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private suspend fun lookupViaConnectRpc(
        client: SupabaseClient,
        query: String,
    ): LookupUserResult? = try {
        client.postgrest.rpc(
            function = RPC_LOOKUP_USER_FOR_CONNECT,
            parameters = buildJsonObject { put("p_query", query) },
        ).decodeList<LookupUserResult>().firstOrNull()
    } catch (error: CancellationException) {
        throw error
    } catch (error: Throwable) {
        Timber.w(error, "lookup_user_for_connect RPC failed, trying fallback")
        null
    }

    @Suppress("TooGenericExceptionCaught")
    private suspend fun lookupViaQrFallback(
        client: SupabaseClient,
        query: String,
    ): LookupUserResult? {
        val extractedUuid = extractUuid(query) ?: return null
        return try {
            client.postgrest.rpc(
                function = RPC_LOOKUP_BY_QR_TOKEN,
                parameters = buildJsonObject { put("p_token", extractedUuid) },
            ).decodeList<QrLookupResult>().firstOrNull()?.let { qr ->
                LookupUserResult(
                    userId = qr.userId,
                    displayName = qr.displayName,
                    username = qr.username,
                    phoneNumber = "",
                    avatarUrl = qr.avatarUrl,
                    bio = qr.bio,
                    currentMood = "binhyen",
                )
            }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Throwable) {
            Timber.w(error, "lookup_by_qr_token fallback failed")
            null
        }
    }

    private fun extractUuid(raw: String): String? {
        val candidate = raw
            .removePrefix("https://")
            .removePrefix("http://")
            .substringAfterLast('/')
            .trim()
        return runCatching { UUID.fromString(candidate).toString() }.getOrNull()
    }

    private fun LookupUserResult.toContactDto(circle: String): ContactDto = ContactDto(
        id = userId,
        name = displayName,
        initials = displayName.trim().take(1).uppercase().ifEmpty { "?" },
        phone = phoneNumber,
        circle = circle,
        moodColorHex = moodToHex(currentMood),
        lastSeen = username?.let { "@$it" },
    )

    private fun String.toDbCircle(): String = when (uppercase()) {
        "LOVE" -> "love"
        "FAMILY" -> "family"
        else -> "friends"
    }

    private fun String.toDomainCircle(): String = when (lowercase()) {
        "love" -> "LOVE"
        "family" -> "FAMILY"
        else -> "FRIENDS"
    }

    private fun moodToHex(mood: String?): String = when (mood?.lowercase()) {
        "thuong" -> "#F7A3A9"
        "vui" -> "#FCD36D"
        "nho" -> "#C4B5FD"
        "tram" -> "#94A3B8"
        else -> "#7ED9B4"
    }

    @Serializable
    private data class ContactRow(
        @SerialName("id")
        val id: String,
        @SerialName("user_id")
        val userId: String,
        @SerialName("contact_user_id")
        val contactUserId: String,
        @SerialName("circle")
        val circle: String = "friends",
        @SerialName("custom_nickname")
        val customNickname: String? = null,
        @SerialName("anniversary_date")
        val anniversaryDate: String? = null,
        @SerialName("created_at")
        val createdAt: String? = null,
    )

    @Serializable
    private data class ContactInsertRow(
        @SerialName("user_id")
        val userId: String,
        @SerialName("contact_user_id")
        val contactUserId: String,
        @SerialName("circle")
        val circle: String = "friends",
    )

    @Serializable
    private data class UserProfileRow(
        @SerialName("id")
        val id: String,
        @SerialName("phone_number")
        val phoneNumber: String = "",
        @SerialName("display_name")
        val displayName: String,
        @SerialName("username")
        val username: String? = null,
        @SerialName("current_mood")
        val currentMood: String? = null,
    )

    @Serializable
    private data class UserSettingsTokenRow(
        @SerialName("qr_token")
        val qrToken: String? = null,
    )

    @Serializable
    private data class LookupUserResult(
        @SerialName("user_id")
        val userId: String,
        @SerialName("display_name")
        val displayName: String,
        @SerialName("username")
        val username: String? = null,
        @SerialName("phone_number")
        val phoneNumber: String = "",
        @SerialName("avatar_url")
        val avatarUrl: String? = null,
        @SerialName("bio")
        val bio: String? = null,
        @SerialName("current_mood")
        val currentMood: String? = null,
    )

    @Serializable
    private data class QrLookupResult(
        @SerialName("user_id")
        val userId: String,
        @SerialName("display_name")
        val displayName: String,
        @SerialName("username")
        val username: String? = null,
        @SerialName("avatar_url")
        val avatarUrl: String? = null,
        @SerialName("bio")
        val bio: String? = null,
    )

    private companion object {
        const val TABLE_CONTACTS = "contacts"
        const val TABLE_USER_PROFILES = "user_profiles"
        const val TABLE_USER_SETTINGS = "user_settings"
        const val RPC_LOOKUP_USER_FOR_CONNECT = "lookup_user_for_connect"
        const val RPC_LOOKUP_BY_QR_TOKEN = "lookup_by_qr_token"
        const val RPC_GET_OR_CREATE_DIRECT_CONVERSATION = "get_or_create_direct_conversation"
    }
}
