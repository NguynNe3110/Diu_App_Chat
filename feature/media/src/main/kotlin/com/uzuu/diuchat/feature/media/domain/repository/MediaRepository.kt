package com.uzuu.diuchat.feature.media.domain.repository

import com.uzuu.diuchat.feature.media.domain.model.MediaVaultItem
import com.uzuu.diuchat.feature.media.domain.model.StorageStats
import kotlinx.coroutines.flow.Flow

interface MediaRepository {
    fun getVaultMedia(conversationId: String): Flow<List<MediaVaultItem>>
    fun getStorageStats(): Flow<StorageStats>
    suspend fun clearCache(): Result<Long>
}
