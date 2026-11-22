package com.uzuu.diuchat.feature.letters.data.datasource.api.service

import com.uzuu.diuchat.feature.letters.data.datasource.api.model.LetterDto
import com.uzuu.diuchat.feature.letters.data.datasource.api.response.LettersResponse
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import timber.log.Timber
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

@Suppress("TooManyFunctions")
class LettersSupabaseService(
    private val supabaseClient: SupabaseClient?,
) {
    val isRemoteEnabled: Boolean get() = supabaseClient != null

    fun currentUserId(): String? = runCatching { supabaseClient?.auth?.currentUserOrNull()?.id }.getOrNull()

    suspend fun fetchAvailableRecipients(): Result<List<Pair<String, String>>> {
        val client = supabaseClient ?: return Result.success(emptyList())
        val myId = currentUserId() ?: return Result.success(emptyList())
        return runCatching {
            val contactRows = runCatching {
                client.postgrest
                    .from(TABLE_CONTACTS)
                    .select()
                    .decodeList<ContactRow>()
            }.getOrDefault(emptyList())

            val peerIdsFromContacts = contactRows.mapNotNull { row ->
                when {
                    row.userId == myId -> row.contactUserId
                    row.contactUserId == myId -> row.userId
                    else -> null
                }
            }

            val myConvoIds = runCatching {
                client.postgrest
                    .from(TABLE_CONVERSATION_MEMBERS)
                    .select {
                        filter { eq("user_id", myId) }
                    }.decodeList<MemberRow>()
                    .filter { it.leftAt == null }
                    .map { it.conversationId }
                    .distinct()
            }.getOrDefault(emptyList())

            val peerIdsFromConvos = if (myConvoIds.isEmpty()) {
                emptyList()
            } else {
                runCatching {
                    client.postgrest
                        .from(TABLE_CONVERSATION_MEMBERS)
                        .select {
                            filter { isIn("conversation_id", myConvoIds) }
                        }.decodeList<MemberRow>()
                        .filter { it.leftAt == null && it.userId != myId }
                        .map { it.userId }
                }.getOrDefault(emptyList())
            }

            val allPeerIds = (peerIdsFromContacts + peerIdsFromConvos).distinct()
            if (allPeerIds.isEmpty()) {
                emptyList()
            } else {
                val profiles = fetchProfilesById(client, allPeerIds)
                allPeerIds.mapNotNull { peerId ->
                    profiles[peerId]?.displayName?.takeIf { it.isNotBlank() }?.let { name ->
                        peerId to name
                    }
                }
            }
        }.onFailure(::rethrowCancellation)
    }

    suspend fun fetchLetters(): Result<LettersResponse> {
        val client = supabaseClient ?: return Result.success(LettersResponse())
        val myId = currentUserId() ?: return Result.success(LettersResponse())
        return runCatching {
            val letterRows = client.postgrest
                .from(TABLE_SEALED_LETTERS)
                .select {
                    order("created_at", Order.DESCENDING)
                }.decodeList<SealedLetterRow>()

            val messagesById = fetchMessagesByIds(client, letterRows.map { it.messageId }.distinct())
            val lockedIncomingMessages = fetchLockedIncomingMessages(client, myId, messagesById.keys)

            val allUserIds = buildSet {
                letterRows.forEach {
                    add(it.senderId)
                    add(it.recipientId)
                }
                lockedIncomingMessages.forEach { add(it.senderId) }
                add(myId)
            }.toList()
            val profilesById = fetchProfilesById(client, allUserIds)
            val nowMs = System.currentTimeMillis()

            val mappedFromTable = letterRows.map { row ->
                mapLetterRowToDto(row, messagesById[row.messageId], profilesById, myId, nowMs)
            }
            val mappedLockedIncoming = lockedIncomingMessages.map { msg ->
                mapLockedMessageToDto(msg, profilesById, myId, nowMs)
            }

            LettersResponse(letters = mappedFromTable + mappedLockedIncoming)
        }.onFailure { error ->
            rethrowCancellation(error)
            Timber.w(error, "Failed to fetch sealed letters from Supabase")
        }
    }

    private suspend fun fetchMessagesByIds(
        client: SupabaseClient,
        messageIds: List<String>,
    ): Map<String, SealedMessageRow> = if (messageIds.isEmpty()) {
        emptyMap()
    } else {
        runCatching {
            client.postgrest
                .from(TABLE_MESSAGES)
                .select {
                    filter { isIn("id", messageIds) }
                }.decodeList<SealedMessageRow>()
                .associateBy { it.id }
        }.getOrDefault(emptyMap())
    }

    private suspend fun fetchLockedIncomingMessages(
        client: SupabaseClient,
        myId: String,
        knownMessageIds: Set<String>,
    ): List<SealedMessageRow> = runCatching {
        client.postgrest
            .from(TABLE_MESSAGES)
            .select {
                filter { eq("type", TYPE_SEALED_LETTER) }
                order("created_at", Order.DESCENDING)
            }.decodeList<SealedMessageRow>()
            .filter { msg ->
                msg.deletedAt == null && msg.senderId != myId && msg.id !in knownMessageIds
            }
    }.getOrDefault(emptyList())

    private fun mapLetterRowToDto(
        row: SealedLetterRow,
        linkedMsg: SealedMessageRow?,
        profilesById: Map<String, ProfileRow>,
        myId: String,
        nowMs: Long,
    ): LetterDto {
        val unlockEpochMs = parseEpochMs(row.unlockAt) ?: nowMs
        val openedEpochMs = row.openedAt?.let(::parseEpochMs)
        val status = when {
            row.isOpened -> "OPENED"
            unlockEpochMs <= nowMs -> "READY_TO_OPEN"
            else -> "SEALED_PENDING"
        }
        val title = resolveMessageTitle(linkedMsg)
        val moodName = resolveMessageMoodName(linkedMsg)
        val isSender = row.senderId == myId
        val senderName = if (isSender) "Tôi" else profilesById[row.senderId]?.displayName ?: DEFAULT_PEER_NAME
        val recipientName = if (!isSender) "Tôi" else profilesById[row.recipientId]?.displayName ?: DEFAULT_PEER_NAME

        return LetterDto(
            id = row.id,
            senderId = row.senderId,
            senderName = senderName,
            recipientId = row.recipientId,
            recipientName = recipientName,
            title = title,
            scheduledUnlockEpochMs = unlockEpochMs,
            scheduledUnlockFormatted = formatEpoch(unlockEpochMs),
            status = status,
            isSender = isSender,
            moodName = moodName,
            contentPayload = row.sealedContent,
            openedAtEpochMs = openedEpochMs,
            openedAtFormatted = openedEpochMs?.let(::formatEpoch),
        )
    }

    private fun mapLockedMessageToDto(
        msg: SealedMessageRow,
        profilesById: Map<String, ProfileRow>,
        myId: String,
        nowMs: Long,
    ): LetterDto {
        val unlockIso = msg.metadata[KEY_UNLOCK_AT]?.jsonPrimitive?.contentOrNull
        val unlockEpochMs = parseEpochMs(unlockIso) ?: (nowMs + ONE_HOUR_MS)
        val scheduledFormatted = msg.metadata[KEY_SCHEDULED_LABEL]?.jsonPrimitive?.contentOrNull
            ?.takeIf { it.isNotBlank() }
            ?: formatEpoch(unlockEpochMs)
        return LetterDto(
            id = msg.id,
            senderId = msg.senderId,
            senderName = profilesById[msg.senderId]?.displayName ?: DEFAULT_PEER_NAME,
            recipientId = myId,
            recipientName = "Tôi",
            title = resolveMessageTitle(msg),
            scheduledUnlockEpochMs = unlockEpochMs,
            scheduledUnlockFormatted = scheduledFormatted,
            status = "SEALED_PENDING",
            isSender = false,
            moodName = resolveMessageMoodName(msg),
            contentPayload = "",
        )
    }

    private fun resolveMessageTitle(msg: SealedMessageRow?): String {
        val metaTitle = msg?.metadata?.get(KEY_TITLE)?.jsonPrimitive?.contentOrNull
        return metaTitle?.takeIf { it.isNotBlank() }
            ?: msg?.content?.takeIf { it.isNotBlank() }
            ?: DEFAULT_LETTER_TITLE
    }

    private fun resolveMessageMoodName(msg: SealedMessageRow?): String {
        val metaMood = msg?.metadata?.get(KEY_MOOD_NAME)?.jsonPrimitive?.contentOrNull
        return metaMood?.takeIf { it.isNotBlank() } ?: moodIdToName(msg?.moodId)
    }

    suspend fun createLetter(letter: LetterDto): Result<Unit> {
        val client = supabaseClient ?: return Result.success(Unit)
        val myId = currentUserId() ?: return Result.failure(IllegalStateException("Chưa đăng nhập"))
        return runCatching {
            val conversationId = getOrCreateDirectConversation(client, myId, letter.recipientId)
            val messageId = UUID.randomUUID().toString()
            val unlockIso = Instant.ofEpochMilli(letter.scheduledUnlockEpochMs).toString()
            val moodId = moodNameToId(letter.moodName)

            client.postgrest.from(TABLE_MESSAGES).insert(
                buildJsonObject {
                    put("id", messageId)
                    put("conversation_id", conversationId)
                    put("sender_id", myId)
                    put("type", TYPE_SEALED_LETTER)
                    put("content", letter.title)
                    put("mood_id", moodId)
                    put(
                        "metadata",
                        buildJsonObject {
                            put(KEY_TITLE, letter.title)
                            put(KEY_MOOD_NAME, letter.moodName)
                            put(KEY_UNLOCK_AT, unlockIso)
                            put(KEY_SCHEDULED_LABEL, letter.scheduledUnlockFormatted)
                        },
                    )
                },
            )

            client.postgrest.from(TABLE_SEALED_LETTERS).insert(
                buildJsonObject {
                    put("id", letter.id)
                    put("message_id", messageId)
                    put("sender_id", myId)
                    put("recipient_id", letter.recipientId)
                    put(KEY_UNLOCK_AT, unlockIso)
                    put("sealed_content", letter.contentPayload)
                    put("is_opened", false)
                },
            )
        }.map { }.onFailure { error ->
            rethrowCancellation(error)
            Timber.w(error, "Failed to create sealed letter %s", letter.id)
        }
    }

    suspend fun updateLetter(letter: LetterDto): Result<Unit> {
        val client = supabaseClient ?: return Result.success(Unit)
        val myId = currentUserId() ?: return Result.success(Unit)
        return runCatching {
            val unlockIso = Instant.ofEpochMilli(letter.scheduledUnlockEpochMs).toString()
            val existing = client.postgrest
                .from(TABLE_SEALED_LETTERS)
                .select {
                    filter { eq("id", letter.id) }
                }.decodeSingleOrNull<SealedLetterRow>()

            client.postgrest.from(TABLE_SEALED_LETTERS).update(
                buildJsonObject {
                    put(KEY_UNLOCK_AT, unlockIso)
                    put("sealed_content", letter.contentPayload)
                },
            ) {
                filter {
                    eq("id", letter.id)
                    eq("sender_id", myId)
                }
            }

            if (existing != null) {
                client.postgrest.from(TABLE_MESSAGES).update(
                    buildJsonObject {
                        put("content", letter.title)
                        put("mood_id", moodNameToId(letter.moodName))
                        put(
                            "metadata",
                            buildJsonObject {
                                put(KEY_TITLE, letter.title)
                                put(KEY_MOOD_NAME, letter.moodName)
                                put(KEY_UNLOCK_AT, unlockIso)
                                put(KEY_SCHEDULED_LABEL, letter.scheduledUnlockFormatted)
                            },
                        )
                    },
                ) {
                    filter { eq("id", existing.messageId) }
                }
            }
        }.map { }.onFailure { error ->
            rethrowCancellation(error)
            Timber.w(error, "Failed to update sealed letter %s", letter.id)
        }
    }

    suspend fun openLetter(letterId: String): Result<Boolean> {
        val client = supabaseClient ?: return Result.success(true)
        return runCatching {
            val rpcResult = runCatching {
                client.postgrest.rpc(
                    function = "open_sealed_letter",
                    parameters = buildJsonObject {
                        put("p_letter_id", letterId)
                    },
                ).decodeAs<Boolean>()
            }.getOrNull()

            if (rpcResult != null) {
                rpcResult
            } else {
                client.postgrest.from(TABLE_SEALED_LETTERS).update(
                    buildJsonObject {
                        put("is_opened", true)
                        put("opened_at", Instant.now().toString())
                    },
                ) {
                    filter { eq("id", letterId) }
                }
                true
            }
        }.onFailure { error ->
            rethrowCancellation(error)
            Timber.w(error, "Failed to open sealed letter %s", letterId)
        }
    }

    suspend fun sendLetterImmediately(letterId: String): Result<Unit> {
        val client = supabaseClient ?: return Result.success(Unit)
        val myId = currentUserId() ?: return Result.success(Unit)
        return runCatching {
            val nowIso = Instant.now().toString()
            client.postgrest.from(TABLE_SEALED_LETTERS).update(
                buildJsonObject {
                    put("unlock_at", nowIso)
                },
            ) {
                filter {
                    eq("id", letterId)
                    eq("sender_id", myId)
                }
            }
        }.map { }.onFailure { error ->
            rethrowCancellation(error)
            Timber.w(error, "Failed to send sealed letter immediately %s", letterId)
        }
    }

    suspend fun upsertLetter(letter: LetterDto): Result<Unit> = createLetter(letter)

    suspend fun deleteLetter(letterId: String): Result<Unit> {
        val client = supabaseClient ?: return Result.success(Unit)
        return runCatching {
            val existing = runCatching {
                client.postgrest
                    .from(TABLE_SEALED_LETTERS)
                    .select {
                        filter { eq("id", letterId) }
                    }.decodeSingleOrNull<SealedLetterRow>()
            }.getOrNull()

            client.postgrest.from(TABLE_SEALED_LETTERS).delete {
                filter {
                    eq("id", letterId)
                }
            }

            if (existing != null) {
                runCatching {
                    client.postgrest.from(TABLE_MESSAGES).update(
                        buildJsonObject {
                            put("deleted_at", Instant.now().toString())
                        },
                    ) {
                        filter { eq("id", existing.messageId) }
                    }
                }
            }
        }.map { }.onFailure(::rethrowCancellation)
    }

    private suspend fun getOrCreateDirectConversation(
        client: SupabaseClient,
        myId: String,
        recipientId: String,
    ): String {
        val rpcConvoId = runCatching {
            client.postgrest.rpc(
                function = "get_or_create_direct_conversation",
                parameters = buildJsonObject {
                    put("p_user_a", myId)
                    put("p_user_b", recipientId)
                },
            ).decodeAs<String>()
        }.getOrNull()
        if (!rpcConvoId.isNullOrBlank()) return rpcConvoId

        // Fallback: find existing direct conversation
        val myMemberships = client.postgrest
            .from(TABLE_CONVERSATION_MEMBERS)
            .select {
                filter { eq("user_id", myId) }
            }.decodeList<MemberRow>()
            .filter { it.leftAt == null }
            .map { it.conversationId }
        if (myMemberships.isNotEmpty()) {
            val peerMatch = client.postgrest
                .from(TABLE_CONVERSATION_MEMBERS)
                .select {
                    filter {
                        isIn("conversation_id", myMemberships)
                        eq("user_id", recipientId)
                    }
                }.decodeList<MemberRow>()
                .firstOrNull { it.leftAt == null }
            if (peerMatch != null) return peerMatch.conversationId
        }
        error("Không tìm thấy cuộc hội thoại với người nhận")
    }

    private suspend fun fetchProfilesById(
        client: SupabaseClient,
        userIds: List<String>,
    ): Map<String, ProfileRow> = if (userIds.isEmpty()) {
        emptyMap()
    } else {
        runCatching {
            client.postgrest
                .from(TABLE_USER_PROFILES)
                .select {
                    filter { isIn("id", userIds) }
                }.decodeList<ProfileRow>()
                .associateBy { it.id }
        }.getOrDefault(emptyMap())
    }

    private fun parseEpochMs(iso: String?): Long? = iso?.let {
        runCatching { Instant.parse(it).toEpochMilli() }.getOrNull()
    }

    private fun formatEpoch(epochMs: Long): String = runCatching {
        val zoned = Instant.ofEpochMilli(epochMs).atZone(ZoneId.systemDefault())
        LETTER_TIME_FORMATTER.format(zoned)
    }.getOrDefault("")

    private fun moodIdToName(moodId: String?): String = when (moodId?.lowercase()) {
        "vui" -> "Vui"
        "binhyen" -> "Bình yên"
        "nho" -> "Nhớ"
        "tram" -> "Trầm"
        else -> "Thương"
    }

    private fun moodNameToId(moodName: String): String = when {
        moodName.contains("Vui", ignoreCase = true) -> "vui"
        moodName.contains("Bình yên", ignoreCase = true) -> "binhyen"
        moodName.contains("Nhớ", ignoreCase = true) -> "nho"
        moodName.contains("Trầm", ignoreCase = true) -> "tram"
        else -> "thuong"
    }

    private fun rethrowCancellation(error: Throwable) {
        if (error is CancellationException) throw error
    }

    @Serializable
    private data class SealedLetterRow(
        @SerialName("id")
        val id: String,
        @SerialName("message_id")
        val messageId: String,
        @SerialName("sender_id")
        val senderId: String,
        @SerialName("recipient_id")
        val recipientId: String,
        @SerialName("unlock_at")
        val unlockAt: String,
        @SerialName("is_opened")
        val isOpened: Boolean = false,
        @SerialName("opened_at")
        val openedAt: String? = null,
        @SerialName("sealed_content")
        val sealedContent: String = "",
    )

    @Serializable
    private data class SealedMessageRow(
        @SerialName("id")
        val id: String,
        @SerialName("sender_id")
        val senderId: String,
        @SerialName("content")
        val content: String? = null,
        @SerialName("mood_id")
        val moodId: String? = null,
        @SerialName("metadata")
        val metadata: JsonObject = JsonObject(emptyMap()),
        @SerialName("deleted_at")
        val deletedAt: String? = null,
    )

    @Serializable
    private data class ContactRow(
        @SerialName("user_id")
        val userId: String,
        @SerialName("contact_user_id")
        val contactUserId: String,
    )

    @Serializable
    private data class MemberRow(
        @SerialName("conversation_id")
        val conversationId: String,
        @SerialName("user_id")
        val userId: String,
        @SerialName("left_at")
        val leftAt: String? = null,
    )

    @Serializable
    private data class ProfileRow(
        @SerialName("id")
        val id: String,
        @SerialName("display_name")
        val displayName: String,
    )

    private companion object {
        const val TABLE_SEALED_LETTERS = "sealed_letters"
        const val TABLE_MESSAGES = "messages"
        const val TABLE_CONTACTS = "contacts"
        const val TABLE_CONVERSATION_MEMBERS = "conversation_members"
        const val TABLE_USER_PROFILES = "user_profiles"
        const val TYPE_SEALED_LETTER = "sealed_letter"
        const val KEY_TITLE = "title"
        const val KEY_MOOD_NAME = "mood_name"
        const val KEY_UNLOCK_AT = "unlock_at"
        const val KEY_SCHEDULED_LABEL = "scheduled_label"
        const val DEFAULT_PEER_NAME = "Người thương"
        const val DEFAULT_LETTER_TITLE = "Thư niêm phong"
        const val ONE_HOUR_MS = 3_600_000L
        val LETTER_TIME_FORMATTER: DateTimeFormatter =
            DateTimeFormatter.ofPattern("HH:mm · dd/MM/yyyy", Locale.forLanguageTag("vi-VN"))
    }
}
