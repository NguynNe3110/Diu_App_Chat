package com.uzuu.diuchat.feature.media.data.repository

import android.content.Context
import com.uzuu.diuchat.feature.media.data.datasource.api.service.MediaSupabaseService
import com.uzuu.diuchat.feature.media.data.datasource.preferences.MediaPreferencesDataSource
import com.uzuu.diuchat.feature.media.data.mapper.MediaMapper
import com.uzuu.diuchat.feature.media.domain.model.AutoDownloadPolicy
import com.uzuu.diuchat.feature.media.domain.model.MediaVaultItem
import com.uzuu.diuchat.feature.media.domain.model.StorageStats
import com.uzuu.diuchat.feature.media.domain.repository.MediaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import java.io.File

class MediaRepositoryImpl(
    private val context: Context,
    private val preferencesDataSource: MediaPreferencesDataSource,
    private val mediaMapper: MediaMapper = MediaMapper(),
    private val supabaseService: MediaSupabaseService = MediaSupabaseService(null),
) : MediaRepository {

    private val mediaItemsFlow = MutableStateFlow<List<MediaVaultItem>>(emptyList())

    private val storageStatsFlow = MutableStateFlow(computeLocalStorageStats())

    private fun computeLocalStorageStats(): StorageStats {
        val cacheBytes = runCatching { computeDirectorySize(context.cacheDir) }.getOrDefault(0L)
        val filesBytes = runCatching { computeDirectorySize(context.filesDir) }.getOrDefault(0L)
        return StorageStats(
            totalStorageBytes = cacheBytes + filesBytes,
            photosBytes = filesBytes,
            voiceBytes = 0L,
            cacheBytes = cacheBytes,
            conversations = emptyList(),
        )
    }

    private fun computeDirectorySize(dir: File?): Long {
        if (dir == null || !dir.exists()) return 0L
        var total = 0L
        dir.listFiles()?.forEach { file ->
            total += if (file.isDirectory) computeDirectorySize(file) else file.length()
        }
        return total
    }

    override fun getVaultMedia(conversationId: String): Flow<List<MediaVaultItem>> = mediaItemsFlow
        .asStateFlow()
        .onStart {
            supabaseService.fetchVaultMedia(conversationId).onSuccess { response ->
                if (response.items.isNotEmpty()) {
                    val mapped = response.items.map(mediaMapper::dtoToDomain)
                    mediaItemsFlow.value = mapped
                }
            }
        }.map { items ->
            if (conversationId.isBlank() || conversationId == "all") {
                items
            } else {
                items.filter { it.conversationId == conversationId }
            }
        }

    override fun getStorageStats(): Flow<StorageStats> = storageStatsFlow.asStateFlow()

    override fun getAutoDownloadPolicy(): Flow<AutoDownloadPolicy> = preferencesDataSource.autoDownloadPolicyFlow

    override suspend fun updateAutoDownloadPolicy(policy: AutoDownloadPolicy) {
        preferencesDataSource.updateAutoDownloadPolicy(policy)
    }

    override suspend fun clearCache(): Result<Long> = withContext(Dispatchers.IO) {
        runCatching {
            val actualDeletedBytes = clearDirectoryContents(context.cacheDir)
            val current = storageStatsFlow.value
            val clearedBytes = if (actualDeletedBytes > 0L) actualDeletedBytes else current.cacheBytes
            storageStatsFlow.value = current.copy(
                totalStorageBytes = (current.totalStorageBytes - clearedBytes).coerceAtLeast(0L),
                cacheBytes = 0L,
            )
            clearedBytes
        }
    }

    private fun clearDirectoryContents(directory: File?): Long {
        if (directory == null || !directory.exists()) return 0L
        var deletedBytes = 0L
        directory.listFiles()?.forEach { child ->
            deletedBytes += if (child.isDirectory) {
                val nested = clearDirectoryContents(child)
                child.delete()
                nested
            } else {
                val size = child.length()
                if (child.delete()) size else 0L
            }
        }
        return deletedBytes
    }

    override suspend fun clearConversationMedia(conversationId: String): Result<Long> {
        val currentStats = storageStatsFlow.value
        val conv = currentStats.conversations.find { it.conversationId == conversationId }
            ?: return Result.success(0L)

        val clearedBytes = conv.totalBytes
        val updatedConversations = currentStats.conversations.map {
            if (it.conversationId == conversationId) {
                it.copy(totalBytes = 0L, photoCount = 0, voiceCount = 0)
            } else {
                it
            }
        }

        val newTotal = (currentStats.totalStorageBytes - clearedBytes).coerceAtLeast(0L)
        storageStatsFlow.value = currentStats.copy(
            totalStorageBytes = newTotal,
            conversations = updatedConversations,
        )

        mediaItemsFlow.value = mediaItemsFlow.value.filterNot { it.conversationId == conversationId }
        supabaseService.deleteConversationMedia(conversationId)

        return Result.success(clearedBytes)
    }
}
