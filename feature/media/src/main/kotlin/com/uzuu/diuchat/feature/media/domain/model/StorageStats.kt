package com.uzuu.diuchat.feature.media.domain.model

data class StorageStats(
    val totalStorageBytes: Long,
    val photosBytes: Long,
    val voiceBytes: Long,
    val cacheBytes: Long,
    val conversations: List<ConversationStorage> = emptyList(),
) {
    val formattedTotal: String get() = ConversationStorage.formatBytes(totalStorageBytes)
    val formattedPhotos: String get() = ConversationStorage.formatBytes(photosBytes)
    val formattedVoice: String get() = ConversationStorage.formatBytes(voiceBytes)
    val formattedCache: String get() = ConversationStorage.formatBytes(cacheBytes)

    val photosPercent: Float
        get() = if (totalStorageBytes > 0) photosBytes.toFloat() / totalStorageBytes.toFloat() else 0f

    val voicePercent: Float
        get() = if (totalStorageBytes > 0) voiceBytes.toFloat() / totalStorageBytes.toFloat() else 0f

    val cachePercent: Float
        get() = if (totalStorageBytes > 0) cacheBytes.toFloat() / totalStorageBytes.toFloat() else 0f
}
