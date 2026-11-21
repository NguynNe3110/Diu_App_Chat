package com.uzuu.diuchat.feature.media.data.repository

import android.content.Context
import com.uzuu.diuchat.feature.base.common.res.AppPalette
import com.uzuu.diuchat.feature.media.data.datasource.preferences.MediaPreferencesDataSource
import com.uzuu.diuchat.feature.media.domain.model.AutoDownloadPolicy
import com.uzuu.diuchat.feature.media.domain.model.ConversationStorage
import com.uzuu.diuchat.feature.media.domain.model.MediaType
import com.uzuu.diuchat.feature.media.domain.model.MediaVaultItem
import com.uzuu.diuchat.feature.media.domain.model.StorageStats
import com.uzuu.diuchat.feature.media.domain.repository.MediaRepository
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class MediaRepositoryImpl(
    private val context: Context,
    private val preferencesDataSource: MediaPreferencesDataSource,
) : MediaRepository {

    private val mediaItemsFlow = MutableStateFlow(createInitialMediaItems())

    private val storageStatsFlow = MutableStateFlow(
        StorageStats(
            totalStorageBytes = 1_258_000_000L,
            photosBytes = 755_000_000L,
            voiceBytes = 220_000_000L,
            cacheBytes = 283_000_000L,
            conversations = listOf(
                ConversationStorage(
                    conversationId = "minh",
                    contactName = "Minh",
                    initials = "M",
                    moodColor = AppPalette.MoodThuong,
                    totalBytes = 503_316_480L,
                    photoCount = 14,
                    voiceCount = 6,
                ),
                ConversationStorage(
                    conversationId = "nha",
                    contactName = "Nhà mình",
                    initials = "N",
                    moodColor = AppPalette.MoodVui,
                    totalBytes = 367_001_600L,
                    photoCount = 9,
                    voiceCount = 4,
                ),
                ConversationStorage(
                    conversationId = "thao",
                    contactName = "Thảo",
                    initials = "T",
                    moodColor = AppPalette.MoodNho,
                    totalBytes = 146_800_640L,
                    photoCount = 5,
                    voiceCount = 2,
                ),
            ),
        ),
    )

    override fun getVaultMedia(conversationId: String): Flow<List<MediaVaultItem>> = mediaItemsFlow.asStateFlow().map { items ->
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
                cacheBytes = 0L,
                totalStorageBytes = current.photosBytes + current.voiceBytes,
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

        // Clear local cached files for this conversation without deleting outbox or remote items
        mediaItemsFlow.value = mediaItemsFlow.value.filterNot { it.conversationId == conversationId }

        return Result.success(clearedBytes)
    }

    private fun createInitialMediaItems(): List<MediaVaultItem> = listOf(
        MediaVaultItem(
            id = "p1",
            conversationId = "minh",
            type = MediaType.PHOTO,
            url = "file:///android_asset/photo_garden.jpg",
            caption = "Rau lên đẹp chưa ba mẹ ơi",
            moodName = "Bình yên",
            moodColor = AppPalette.MoodBinhYen,
            timestamp = "19:42 · 21/11/2026",
            monthGroup = MONTH_NOV_2026,
            senderName = "Ba",
        ),
        MediaVaultItem(
            id = "p2",
            conversationId = "minh",
            type = MediaType.PHOTO,
            url = "file:///android_asset/photo_sunset.jpg",
            caption = "Hoàng hôn hồ Tây chiều nay mộng mơ",
            moodName = "Thương",
            moodColor = AppPalette.MoodThuong,
            timestamp = "17:15 · 18/11/2026",
            monthGroup = MONTH_NOV_2026,
            senderName = "Minh",
        ),
        MediaVaultItem(
            id = "p3",
            conversationId = "nha",
            type = MediaType.PHOTO,
            url = "file:///android_asset/photo_food.jpg",
            caption = "Bát bún ốc nóng hổi ngày đầu đông",
            moodName = "Vui",
            moodColor = AppPalette.MoodVui,
            timestamp = "12:30 · 15/11/2026",
            monthGroup = MONTH_NOV_2026,
            senderName = "Bạn",
        ),
        MediaVaultItem(
            id = "p4",
            conversationId = "minh",
            type = MediaType.PHOTO,
            url = "file:///android_asset/photo_flower.jpg",
            caption = "Góc làm việc cúc họa mi dịu dàng",
            moodName = "Bình yên",
            moodColor = AppPalette.MoodBinhYen,
            timestamp = "09:00 · 10/11/2026",
            monthGroup = MONTH_NOV_2026,
            senderName = "Minh",
        ),
        MediaVaultItem(
            id = "v1",
            conversationId = "minh",
            type = MediaType.VOICE,
            caption = "Minh dặn nhớ mang áo ấm nha",
            moodName = "Thương",
            moodColor = AppPalette.MoodThuong,
            durationSeconds = 42,
            timestamp = "21:14 · 20/11/2026",
            monthGroup = MONTH_NOV_2026,
            senderName = "Minh",
        ),
        MediaVaultItem(
            id = "v2",
            conversationId = "nha",
            type = MediaType.VOICE,
            caption = "Mẹ dặn cuối tuần nhớ về sớm",
            moodName = "Vui",
            moodColor = AppPalette.MoodVui,
            durationSeconds = 18,
            timestamp = "19:30 · 18/11/2026",
            monthGroup = MONTH_NOV_2026,
            senderName = "Nhà mình",
        ),
        MediaVaultItem(
            id = "l1",
            conversationId = "minh",
            type = MediaType.LETTER,
            caption = "Thư gửi ngày mưa lạnh đầu mùa",
            moodName = "Thương",
            moodColor = AppPalette.MoodThuong,
            timestamp = "19/11/2026",
            monthGroup = MONTH_NOV_2026,
            senderName = "Minh",
        ),
        MediaVaultItem(
            id = "p5",
            conversationId = "thao",
            type = MediaType.PHOTO,
            url = "file:///android_asset/photo_fog.jpg",
            caption = "Chuyến đi Tam Đảo sương mù tháng trước",
            moodName = "Nhớ",
            moodColor = AppPalette.MoodNho,
            timestamp = "16:20 · 28/10/2026",
            monthGroup = "Tháng 10, 2026",
            senderName = "Thảo",
        ),
        MediaVaultItem(
            id = "p6",
            conversationId = "minh",
            type = MediaType.PHOTO,
            url = "file:///android_asset/photo_cat.jpg",
            caption = "Mèo con phơi nắng ban công",
            moodName = "Bình yên",
            moodColor = AppPalette.MoodBinhYen,
            timestamp = "13:10 · 20/10/2026",
            monthGroup = "Tháng 10, 2026",
            senderName = "Bạn",
        ),
        MediaVaultItem(
            id = "v3",
            conversationId = "minh",
            type = MediaType.VOICE,
            caption = "Tiếng mưa rơi nhẹ nhàng ngoài hiên",
            moodName = "Bình yên",
            moodColor = AppPalette.MoodBinhYen,
            durationSeconds = 65,
            timestamp = "22:00 · 12/10/2026",
            monthGroup = "Tháng 10, 2026",
            senderName = "Minh",
        ),
        MediaVaultItem(
            id = "l2",
            conversationId = "minh",
            type = MediaType.LETTER,
            caption = "Thư chúc mừng sinh nhật ngọt ngào",
            moodName = "Vui",
            moodColor = AppPalette.MoodVui,
            timestamp = "05/10/2026",
            monthGroup = "Tháng 10, 2026",
            senderName = "Minh",
        ),
    )

    private companion object {
        private const val MONTH_NOV_2026 = "Tháng 11, 2026"
    }
}
