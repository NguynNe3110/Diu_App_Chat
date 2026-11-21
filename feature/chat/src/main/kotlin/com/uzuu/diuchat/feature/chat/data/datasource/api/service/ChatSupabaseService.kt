package com.uzuu.diuchat.feature.chat.data.datasource.api.service

import com.uzuu.diuchat.feature.chat.data.datasource.api.model.ConversationDto
import com.uzuu.diuchat.feature.chat.data.datasource.api.model.MessageDto
import com.uzuu.diuchat.feature.chat.data.datasource.api.response.ChatMessagesResponse
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.CancellationException

interface ChatSupabaseService {
    suspend fun fetchConversations(): Result<List<ConversationDto>>

    suspend fun fetchMessages(conversationId: String): Result<ChatMessagesResponse>

    suspend fun upsertMessage(message: MessageDto): Result<Unit>

    suspend fun upsertConversation(conversation: ConversationDto): Result<Unit>

    suspend fun uploadMedia(
        bucketPath: String,
        bytes: ByteArray,
    ): Result<String>
}

class ChatSupabaseServiceImpl(
    private val supabaseClient: SupabaseClient?,
) : ChatSupabaseService {
    override suspend fun fetchConversations(): Result<List<ConversationDto>> {
        val client = supabaseClient ?: return Result.success(emptyList())
        return runCatching {
            client.postgrest
                .from(TABLE_CONVERSATIONS)
                .select()
                .decodeList<ConversationDto>()
        }.onFailure(::rethrowCancellation)
    }

    override suspend fun fetchMessages(conversationId: String): Result<ChatMessagesResponse> {
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

    override suspend fun upsertMessage(message: MessageDto): Result<Unit> {
        val client = supabaseClient ?: return Result.success(Unit)
        return runCatching {
            client.postgrest.from(TABLE_MESSAGES).upsert(message)
        }.map { }.onFailure(::rethrowCancellation)
    }

    override suspend fun upsertConversation(conversation: ConversationDto): Result<Unit> {
        val client = supabaseClient ?: return Result.success(Unit)
        return runCatching {
            client.postgrest.from(TABLE_CONVERSATIONS).upsert(conversation)
        }.map { }.onFailure(::rethrowCancellation)
    }

    override suspend fun uploadMedia(
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

    private fun rethrowCancellation(error: Throwable) {
        if (error is CancellationException) throw error
    }

    private companion object {
        const val TABLE_CONVERSATIONS = "conversations"
        const val TABLE_MESSAGES = "messages"
        const val BUCKET_CHAT_MEDIA = "chat-media"
    }
}
