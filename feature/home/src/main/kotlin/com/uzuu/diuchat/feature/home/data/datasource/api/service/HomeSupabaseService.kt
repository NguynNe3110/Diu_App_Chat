package com.uzuu.diuchat.feature.home.data.datasource.api.service

import com.uzuu.diuchat.feature.home.data.datasource.api.model.HomeStarDto
import com.uzuu.diuchat.feature.home.data.datasource.api.model.RecentChatDto
import com.uzuu.diuchat.feature.home.data.datasource.api.response.HomeOverviewResponse
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import timber.log.Timber
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.coroutines.cancellation.CancellationException

class HomeSupabaseService(
    private val supabaseClient: SupabaseClient?,
) {
    fun isConfigured(): Boolean = supabaseClient != null

    fun isAuthenticated(): Boolean = supabaseClient?.auth?.currentUserOrNull() != null

    @Suppress("TooGenericExceptionCaught")
    suspend fun fetchOverview(): HomeOverviewResponse? {
        val client = supabaseClient ?: return null
        val currentUserId = client.auth.currentUserOrNull()?.id ?: return null

        return try {
            val userDisplayName = fetchCurrentUserDisplayName(client, currentUserId)
            val myMemberships = fetchActiveMemberships(client, currentUserId)
            if (myMemberships.isEmpty()) {
                HomeOverviewResponse(
                    userDisplayName = userDisplayName,
                    stars = emptyList(),
                    recentChats = emptyList(),
                )
            } else {
                loadOverviewForMemberships(client, currentUserId, userDisplayName, myMemberships)
            }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Throwable) {
            Timber.w(error, "Failed to fetch home overview from Supabase")
            null
        }
    }

    private suspend fun fetchCurrentUserDisplayName(
        client: SupabaseClient,
        userId: String,
    ): String? = runCatching {
        client.postgrest
            .from(TABLE_USER_PROFILES)
            .select {
                filter { eq("id", userId) }
            }.decodeSingleOrNull<ProfileRow>()
            ?.displayName
    }.getOrNull()

    private suspend fun loadOverviewForMemberships(
        client: SupabaseClient,
        currentUserId: String,
        userDisplayName: String?,
        myMemberships: List<MemberRow>,
    ): HomeOverviewResponse {
        val convoIds = myMemberships.map { it.conversationId }.distinct()
        val conversations = fetchConversations(client, convoIds)
        val allMembers = fetchMembersForConversations(client, convoIds)
        val peerUserIds = allMembers.map { it.userId }.filter { it != currentUserId }.distinct()
        val profilesById = fetchProfilesById(client, peerUserIds)
        val latestMessagesByConvo = fetchLatestMessages(client, convoIds)

        val myMemberByConvo = myMemberships.associateBy { it.conversationId }
        val membersByConvo = allMembers.groupBy { it.conversationId }

        val recentChats = conversations.map { convo ->
            buildRecentChatDto(
                convo = convo,
                currentUserId = currentUserId,
                members = membersByConvo[convo.id].orEmpty(),
                myMember = myMemberByConvo[convo.id],
                profilesById = profilesById,
                latestMsg = latestMessagesByConvo[convo.id],
            )
        }
        val stars = buildStars(recentChats, conversations, membersByConvo)
        return HomeOverviewResponse(
            userDisplayName = userDisplayName,
            stars = stars,
            recentChats = recentChats,
        )
    }

    private suspend fun fetchActiveMemberships(
        client: SupabaseClient,
        userId: String,
    ): List<MemberRow> = client.postgrest
        .from(TABLE_CONVERSATION_MEMBERS)
        .select {
            filter { eq("user_id", userId) }
        }.decodeList<MemberRow>()
        .filter { it.leftAt == null }

    private suspend fun fetchConversations(
        client: SupabaseClient,
        convoIds: List<String>,
    ): List<ConversationRow> = client.postgrest
        .from(TABLE_CONVERSATIONS)
        .select {
            filter { isIn("id", convoIds) }
        }.decodeList<ConversationRow>()
        .sortedByDescending { it.updatedAt ?: "" }

    private suspend fun fetchMembersForConversations(
        client: SupabaseClient,
        convoIds: List<String>,
    ): List<MemberRow> = client.postgrest
        .from(TABLE_CONVERSATION_MEMBERS)
        .select {
            filter { isIn("conversation_id", convoIds) }
        }.decodeList<MemberRow>()
        .filter { it.leftAt == null }

    private suspend fun fetchProfilesById(
        client: SupabaseClient,
        userIds: List<String>,
    ): Map<String, ProfileRow> = if (userIds.isEmpty()) {
        emptyMap()
    } else {
        client.postgrest
            .from(TABLE_USER_PROFILES)
            .select {
                filter { isIn("id", userIds) }
            }.decodeList<ProfileRow>()
            .associateBy { it.id }
    }

    private suspend fun fetchLatestMessages(
        client: SupabaseClient,
        convoIds: List<String>,
    ): Map<String, MessageSummaryRow?> = runCatching {
        client.postgrest
            .from(TABLE_MESSAGES)
            .select {
                filter { isIn("conversation_id", convoIds) }
                order("created_at", Order.DESCENDING)
                limit(RECENT_MESSAGES_LIMIT)
            }.decodeList<MessageSummaryRow>()
            .filter { it.deletedAt == null }
            .groupBy { it.conversationId }
            .mapValues { (_, msgs) -> msgs.firstOrNull() }
    }.getOrDefault(emptyMap())

    @Suppress("LongParameterList")
    private fun buildRecentChatDto(
        convo: ConversationRow,
        currentUserId: String,
        members: List<MemberRow>,
        myMember: MemberRow?,
        profilesById: Map<String, ProfileRow>,
        latestMsg: MessageSummaryRow?,
    ): RecentChatDto {
        val peerMember = members.firstOrNull { it.userId != currentUserId }
        val peerProfile = peerMember?.userId?.let { profilesById[it] }
        val displayName = resolveDisplayName(convo, peerMember, peerProfile)
        val preview = resolveMessagePreview(latestMsg)
        val moodHex = moodToHex(peerProfile?.currentMood ?: convo.moodId)
        return RecentChatDto(
            id = convo.id,
            contactName = displayName,
            initials = displayName.trim().take(1).uppercase().ifEmpty { "?" },
            moodColorHex = moodHex,
            lastMessage = preview,
            timestamp = formatTimestamp(latestMsg?.createdAt ?: convo.updatedAt),
            unreadCount = 0,
            isPendingLetter = latestMsg?.type == "sealed_letter",
            isQuiet = myMember?.isMuted == true,
        )
    }

    private fun resolveDisplayName(
        convo: ConversationRow,
        peerMember: MemberRow?,
        peerProfile: ProfileRow?,
    ): String = when {
        convo.type == "group" -> convo.title?.takeIf { it.isNotBlank() } ?: "Nhóm dịu"
        !peerMember?.customNickname.isNullOrBlank() -> peerMember.customNickname.orEmpty()
        else -> peerProfile?.displayName ?: convo.title ?: "Người bạn dịu"
    }

    private fun resolveMessagePreview(latestMsg: MessageSummaryRow?): String = when (latestMsg?.type) {
        "photo" -> "📷 Đã gửi một ảnh"
        "voice" -> "🎙️ Tin nhắn thoại"
        "location" -> "📍 Đã chia sẻ vị trí"
        "sealed_letter" -> "✉️ Thư niêm phong"
        else -> latestMsg?.content?.takeIf { it.isNotBlank() } ?: "Bắt đầu cuộc trò chuyện"
    }

    private fun buildStars(
        recentChats: List<RecentChatDto>,
        conversations: List<ConversationRow>,
        membersByConvo: Map<String, List<MemberRow>>,
    ): List<HomeStarDto> = recentChats.take(STAR_POSITIONS.size).mapIndexed { index, chat ->
        val pos = STAR_POSITIONS[index]
        val memberCount = membersByConvo[chat.id]?.size ?: 1
        HomeStarDto(
            id = chat.id,
            name = chat.contactName,
            initials = chat.initials,
            moodColorHex = chat.moodColorHex,
            sizeDp = pos.sizeDp,
            xPercent = pos.xPercent,
            yPercent = pos.yPercent,
            isQuiet = chat.isQuiet,
            memberCount = if (conversations.getOrNull(index)?.type == "group") memberCount else 1,
        )
    }

    private fun moodToHex(mood: String?): String = when (mood?.lowercase()) {
        "thuong" -> "#F7A3A9"
        "vui" -> "#FCD36D"
        "nho" -> "#C4B5FD"
        "tram" -> "#94A3B8"
        else -> "#7ED9B4"
    }

    private fun formatTimestamp(iso: String?): String {
        if (iso.isNullOrBlank()) return ""
        return runCatching {
            val instant = Instant.parse(iso)
            TIME_FORMATTER.format(instant.atZone(ZoneId.systemDefault()))
        }.getOrDefault("")
    }

    private data class StarSlot(
        val sizeDp: Float,
        val xPercent: Float,
        val yPercent: Float,
    )

    @Serializable
    private data class MemberRow(
        @SerialName("conversation_id")
        val conversationId: String,
        @SerialName("user_id")
        val userId: String,
        @SerialName("custom_nickname")
        val customNickname: String? = null,
        @SerialName("is_muted")
        val isMuted: Boolean = false,
        @SerialName("left_at")
        val leftAt: String? = null,
    )

    @Serializable
    private data class ConversationRow(
        @SerialName("id")
        val id: String,
        @SerialName("type")
        val type: String = "direct",
        @SerialName("title")
        val title: String? = null,
        @SerialName("mood_id")
        val moodId: String? = null,
        @SerialName("updated_at")
        val updatedAt: String? = null,
    )

    @Serializable
    private data class ProfileRow(
        @SerialName("id")
        val id: String,
        @SerialName("display_name")
        val displayName: String,
        @SerialName("current_mood")
        val currentMood: String? = null,
    )

    @Serializable
    private data class MessageSummaryRow(
        @SerialName("conversation_id")
        val conversationId: String,
        @SerialName("type")
        val type: String = "text",
        @SerialName("content")
        val content: String? = null,
        @SerialName("deleted_at")
        val deletedAt: String? = null,
        @SerialName("created_at")
        val createdAt: String? = null,
    )

    private companion object {
        const val TABLE_CONVERSATION_MEMBERS = "conversation_members"
        const val TABLE_CONVERSATIONS = "conversations"
        const val TABLE_USER_PROFILES = "user_profiles"
        const val TABLE_MESSAGES = "messages"
        const val RECENT_MESSAGES_LIMIT = 100L
        val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        val STAR_POSITIONS = listOf(
            StarSlot(92f, 0.50f, 0.18f),
            StarSlot(66f, 0.18f, 0.38f),
            StarSlot(60f, 0.82f, 0.40f),
            StarSlot(46f, 0.32f, 0.72f),
            StarSlot(42f, 0.72f, 0.75f),
        )
    }
}
