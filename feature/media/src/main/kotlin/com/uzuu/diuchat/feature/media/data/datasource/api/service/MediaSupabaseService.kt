package com.uzuu.diuchat.feature.media.data.datasource.api.service

import com.uzuu.diuchat.feature.media.data.datasource.api.model.MediaVaultItemDto
import com.uzuu.diuchat.feature.media.data.datasource.api.response.MediaVaultResponse
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.time.Duration.Companion.days

class MediaSupabaseService(
    private val supabaseClient: SupabaseClient?,
) {
    suspend fun fetchVaultMedia(conversationId: String): Result<MediaVaultResponse> {
        val client = supabaseClient ?: return Result.success(MediaVaultResponse(conversationId = conversationId))
        return runCatching {
            val rawMessages =
                client.postgrest
                    .from(TABLE_MESSAGES)
                    .select {
                        if (conversationId.isNotBlank() && conversationId != "all") {
                            filter {
                                eq("conversation_id", conversationId)
                            }
                        }
                        order("created_at", Order.DESCENDING)
                    }.decodeList<MediaMessageRow>()
                    .filter { row ->
                        row.deletedAt == null && row.type in MEDIA_MESSAGE_TYPES
                    }

            val senderIds = rawMessages.map { it.senderId }.distinct()
            val profilesById = fetchProfilesById(client, senderIds)
            val bucket = client.storage.from(BUCKET_CHAT_MEDIA)

            val items = rawMessages.map { row ->
                mapMessageRowToVaultDto(row, profilesById, bucket)
            }

            MediaVaultResponse(
                conversationId = conversationId,
                items = items,
            )
        }.onFailure(::rethrowCancellation)
    }

    private suspend fun mapMessageRowToVaultDto(
        row: MediaMessageRow,
        profilesById: Map<String, ProfileRow>,
        bucket: io.github.jan.supabase.storage.BucketApi,
    ): MediaVaultItemDto {
        val resolvedUrl = resolveSignedUrl(bucket, row.mediaUrl)
        val mappedType = when (row.type) {
            "photo" -> "PHOTO"
            "voice" -> "VOICE"
            "sealed_letter" -> "LETTER"
            else -> "PHOTO"
        }
        val (moodName, moodHex) = mapMood(row.moodId)
        val durationMs = row.metadata["audio_duration_ms"]?.jsonPrimitive?.longOrNull ?: 0L
        val senderName =
            row.metadata["sender_name"]?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() }
                ?: profilesById[row.senderId]?.displayName?.takeIf { it.isNotBlank() }
                ?: "Người thương"
        val (timestamp, monthGroup) = formatTimestampAndMonth(row.createdAt)
        val caption = row.content?.takeIf { it.isNotBlank() }
            ?: defaultCaptionForType(row.type)

        return MediaVaultItemDto(
            id = row.id,
            conversationId = row.conversationId,
            type = mappedType,
            url = resolvedUrl,
            caption = caption,
            moodName = moodName,
            moodColorHex = moodHex,
            durationSeconds = (durationMs / 1000L).toInt(),
            timestamp = timestamp,
            monthGroup = monthGroup,
            senderName = senderName,
        )
    }

    private fun defaultCaptionForType(type: String): String = when (type) {
        "photo" -> "Hình ảnh"
        "voice" -> "Lời nhắn thoại"
        "sealed_letter" -> "Thư niêm phong"
        else -> ""
    }

    suspend fun uploadMediaFile(
        bucketPath: String,
        bytes: ByteArray,
    ): Result<String> {
        val client = supabaseClient ?: return Result.success("")
        return runCatching {
            val bucket = client.storage.from(BUCKET_CHAT_MEDIA)
            bucket.upload(path = bucketPath, data = bytes) {
                upsert = true
            }
            runCatching {
                bucket.createSignedUrl(path = bucketPath, expiresIn = SIGNED_URL_EXPIRY_DAYS.days)
            }.getOrElse {
                bucket.publicUrl(bucketPath)
            }
        }.onFailure(::rethrowCancellation)
    }

    @Suppress("UnusedParameter")
    suspend fun upsertMediaItem(item: MediaVaultItemDto): Result<Unit> = Result.success(Unit)

    @Suppress("UnusedParameter")
    suspend fun deleteConversationMedia(conversationId: String): Result<Unit> = Result.success(Unit)

    private suspend fun resolveSignedUrl(
        bucket: io.github.jan.supabase.storage.BucketApi,
        rawUrl: String?,
    ): String = when {
        rawUrl.isNullOrBlank() -> ""
        rawUrl.contains("/object/sign/") -> rawUrl
        else -> signStorageUrl(bucket, rawUrl)
    }

    private suspend fun signStorageUrl(
        bucket: io.github.jan.supabase.storage.BucketApi,
        rawUrl: String,
    ): String {
        val storagePath = extractStoragePath(rawUrl) ?: return rawUrl
        return runCatching {
            bucket.createSignedUrl(path = storagePath, expiresIn = SIGNED_URL_EXPIRY_DAYS.days)
        }.getOrNull()?.takeIf { it.isNotBlank() } ?: rawUrl
    }

    private fun extractStoragePath(url: String): String? {
        val marker = "/object/public/$BUCKET_CHAT_MEDIA/"
        val idx = url.indexOf(marker)
        return if (idx >= 0) {
            url.substring(idx + marker.length).substringBefore("?").takeIf { it.isNotBlank() }
        } else if (!url.startsWith("http://") && !url.startsWith("https://") && !url.startsWith("content://")) {
            url.takeIf { it.isNotBlank() }
        } else {
            null
        }
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

    private fun mapMood(moodId: String?): Pair<String, String> = when (moodId?.lowercase()) {
        "thuong" -> "Thương" to "#F7A3A9"
        "vui" -> "Vui" to "#F4CA84"
        "nho" -> "Nhớ" to "#C5B3EA"
        "tram" -> "Trầm" to "#95C4E7"
        else -> "Bình yên" to "#A2D7AC"
    }

    private fun formatTimestampAndMonth(iso: String?): Pair<String, String> {
        if (iso.isNullOrBlank()) return "" to "Gần đây"
        return runCatching {
            val zoned = Instant.parse(iso).atZone(ZoneId.systemDefault())
            val ts = TIME_FORMATTER.format(zoned)
            val month = "Tháng ${zoned.monthValue}, ${zoned.year}"
            ts to month
        }.getOrDefault("" to "Gần đây")
    }

    private fun rethrowCancellation(error: Throwable) {
        if (error is CancellationException) throw error
    }

    @Serializable
    private data class MediaMessageRow(
        @SerialName("id")
        val id: String,
        @SerialName("conversation_id")
        val conversationId: String,
        @SerialName("sender_id")
        val senderId: String,
        @SerialName("type")
        val type: String = "text",
        @SerialName("content")
        val content: String? = null,
        @SerialName("media_url")
        val mediaUrl: String? = null,
        @SerialName("mood_id")
        val moodId: String? = null,
        @SerialName("metadata")
        val metadata: JsonObject = JsonObject(emptyMap()),
        @SerialName("deleted_at")
        val deletedAt: String? = null,
        @SerialName("created_at")
        val createdAt: String? = null,
    )

    @Serializable
    private data class ProfileRow(
        @SerialName("id")
        val id: String,
        @SerialName("display_name")
        val displayName: String,
    )

    private companion object {
        const val TABLE_MESSAGES = "messages"
        const val TABLE_USER_PROFILES = "user_profiles"
        const val BUCKET_CHAT_MEDIA = "chat-media"
        const val SIGNED_URL_EXPIRY_DAYS = 30
        val MEDIA_MESSAGE_TYPES = setOf("photo", "voice", "sealed_letter")
        val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm dd/MM", Locale.forLanguageTag("vi-VN"))
    }
}
