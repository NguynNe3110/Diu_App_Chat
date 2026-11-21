package com.uzuu.diuchat.feature.media.data.datasource.api.service

import com.uzuu.diuchat.feature.media.data.datasource.api.model.MediaVaultItemDto
import com.uzuu.diuchat.feature.media.data.datasource.api.response.MediaVaultResponse
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.CancellationException

class MediaSupabaseService(
    private val supabaseClient: SupabaseClient?,
) {
    suspend fun fetchVaultMedia(conversationId: String): Result<MediaVaultResponse> {
        val client = supabaseClient ?: return Result.success(MediaVaultResponse(conversationId = conversationId))
        return runCatching {
            val items =
                client.postgrest
                    .from(TABLE_MEDIA_VAULT)
                    .select {
                        if (conversationId.isNotBlank() && conversationId != "all") {
                            filter {
                                eq("conversation_id", conversationId)
                            }
                        }
                    }.decodeList<MediaVaultItemDto>()
            MediaVaultResponse(
                conversationId = conversationId,
                items = items,
            )
        }.onFailure(::rethrowCancellation)
    }

    suspend fun uploadMediaFile(
        bucketPath: String,
        bytes: ByteArray,
    ): Result<String> {
        val client = supabaseClient ?: return Result.success("")
        return runCatching {
            val bucket = client.storage.from(BUCKET_MEDIA_VAULT)
            bucket.upload(path = bucketPath, data = bytes) {
                upsert = true
            }
            bucket.publicUrl(bucketPath)
        }.onFailure(::rethrowCancellation)
    }

    suspend fun upsertMediaItem(item: MediaVaultItemDto): Result<Unit> {
        val client = supabaseClient ?: return Result.success(Unit)
        return runCatching {
            client.postgrest.from(TABLE_MEDIA_VAULT).upsert(item)
        }.map { }.onFailure(::rethrowCancellation)
    }

    suspend fun deleteConversationMedia(conversationId: String): Result<Unit> {
        val client = supabaseClient ?: return Result.success(Unit)
        return runCatching {
            client.postgrest.from(TABLE_MEDIA_VAULT).delete {
                filter {
                    eq("conversation_id", conversationId)
                }
            }
        }.map { }.onFailure(::rethrowCancellation)
    }

    private fun rethrowCancellation(error: Throwable) {
        if (error is CancellationException) throw error
    }

    private companion object {
        const val TABLE_MEDIA_VAULT = "media_vault_items"
        const val BUCKET_MEDIA_VAULT = "media-vault"
    }
}
