package com.uzuu.diuchat.feature.chat.data.datasource.api.service

import com.uzuu.diuchat.feature.chat.data.datasource.api.model.ConversationDto
import com.uzuu.diuchat.feature.chat.data.datasource.api.model.MessageDto
import com.uzuu.diuchat.feature.chat.data.datasource.api.response.ChatMessagesResponse
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.HasRecord
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.broadcast
import io.github.jan.supabase.realtime.broadcastFlow
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.decodeRecordOrNull
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatSupabaseService(
    private val supabaseClient: SupabaseClient?,
) {
    fun currentUserId(): String? = runCatching { supabaseClient?.auth?.currentUserOrNull()?.id }.getOrNull()

    fun currentUserDisplayName(): String? = runCatching {
        supabaseClient
            ?.auth
            ?.currentUserOrNull()
            ?.userMetadata
            ?.get("display_name")
            ?.toString()
            ?.trim('"')
    }.getOrNull()

    suspend fun fetchConversations(): Result<List<ConversationDto>> {
        val client = supabaseClient ?: return Result.success(emptyList())
        return runCatching {
            client.postgrest
                .from(TABLE_CONVERSATIONS)
                .select()
                .decodeList<ConversationDto>()
        }.onFailure(::rethrowCancellation)
    }

    suspend fun fetchMessages(conversationId: String): Result<ChatMessagesResponse> {
        val client = supabaseClient ?: return Result.success(ChatMessagesResponse(conversationId = conversationId))
        return runCatching {
            val messages =
                client.postgrest
                    .from(TABLE_MESSAGES)
                    .select {
                        filter {
                            eq("conversation_id", conversationId)
                        }
                    }.decodeList<MessageDto>()
            val conversation =
                client.postgrest
                    .from(TABLE_CONVERSATIONS)
                    .select {
                        filter {
                            eq("id", conversationId)
                        }
                    }.decodeSingleOrNull<ConversationDto>()
            ChatMessagesResponse(
                conversationId = conversationId,
                messages = messages,
                conversation = conversation,
            )
        }.onFailure(::rethrowCancellation)
    }

    /**
     * Lắng nghe tin nhắn mới của một cuộc hội thoại qua hai nguồn trên cùng một channel:
     * - Postgres Changes (cần bật publication `supabase_realtime` cho bảng `messages`).
     * - Broadcast event [EVENT_NEW_MESSAGE] (fallback khi chưa bật replication).
     * Channel được unsubscribe/remove khi collector bị hủy.
     */
    fun observeRealtimeMessages(conversationId: String): Flow<MessageDto> {
        val client = supabaseClient ?: return emptyFlow()
        return channelFlow {
            val channel = client.channel(channelName(conversationId))
            val postgresMessages =
                channel
                    .postgresChangeFlow<PostgresAction>(schema = SCHEMA_PUBLIC) {
                        table = TABLE_MESSAGES
                        filter("conversation_id", FilterOperator.EQ, conversationId)
                    }.mapNotNull { action ->
                        (action as? HasRecord)?.let { record ->
                            runCatching { record.decodeRecordOrNull<MessageDto>() }.getOrNull()
                        }
                    }
            val broadcastMessages = channel.broadcastFlow<MessageDto>(event = EVENT_NEW_MESSAGE)

            launch {
                merge(postgresMessages, broadcastMessages)
                    .filter { it.conversationId == conversationId }
                    .collect { send(it) }
            }

            runCatching { channel.subscribe() }.onFailure(::rethrowCancellation)

            awaitClose {
                CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                    removeChannelSafely(client, channel)
                }
            }
        }
    }

    suspend fun upsertMessage(message: MessageDto): Result<Unit> {
        val client = supabaseClient ?: return Result.success(Unit)
        return runCatching {
            client.postgrest.from(TABLE_MESSAGES).upsert(message)
        }.map { }
            .onFailure(::rethrowCancellation)
            .onSuccess { broadcastNewMessage(client, message) }
    }

    suspend fun upsertConversation(conversation: ConversationDto): Result<Unit> {
        val client = supabaseClient ?: return Result.success(Unit)
        return runCatching {
            client.postgrest.from(TABLE_CONVERSATIONS).upsert(conversation)
        }.map { }.onFailure(::rethrowCancellation)
    }

    suspend fun uploadMedia(
        bucketPath: String,
        bytes: ByteArray,
    ): Result<String> {
        val client = supabaseClient ?: return Result.success("")
        return runCatching {
            val bucket = client.storage.from(BUCKET_CHAT_MEDIA)
            bucket.upload(path = bucketPath, data = bytes) {
                upsert = true
            }
            bucket.publicUrl(bucketPath)
        }.onFailure(::rethrowCancellation)
    }

    /** Broadcast không bắt buộc: lỗi ở đây không làm hỏng ACK của upsert. */
    private suspend fun broadcastNewMessage(
        client: SupabaseClient,
        message: MessageDto,
    ) {
        runCatching {
            client
                .channel(channelName(message.conversationId))
                .broadcast(event = EVENT_NEW_MESSAGE, message = message)
        }.onFailure(::rethrowCancellation)
    }

    private suspend fun removeChannelSafely(
        client: SupabaseClient,
        channel: RealtimeChannel,
    ) {
        withContext(NonCancellable) {
            runCatching { channel.unsubscribe() }
            runCatching { client.realtime.removeChannel(channel) }
        }
    }

    private fun rethrowCancellation(error: Throwable) {
        if (error is CancellationException) throw error
    }

    private companion object {
        const val TABLE_CONVERSATIONS = "conversations"
        const val TABLE_MESSAGES = "messages"
        const val BUCKET_CHAT_MEDIA = "chat-media"
        const val SCHEMA_PUBLIC = "public"
        const val EVENT_NEW_MESSAGE = "new_message"

        fun channelName(conversationId: String): String = "chat_messages_$conversationId"
    }
}
