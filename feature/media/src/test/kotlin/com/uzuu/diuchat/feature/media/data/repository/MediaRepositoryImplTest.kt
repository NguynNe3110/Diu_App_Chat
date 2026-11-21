package com.uzuu.diuchat.feature.media.data.repository

import android.content.Context
import com.uzuu.diuchat.feature.media.data.datasource.preferences.MediaPreferencesDataSource
import com.uzuu.diuchat.feature.media.domain.model.AutoDownloadPolicy
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import java.io.File
import java.nio.file.Files
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class MediaRepositoryImplTest {

    private val preferencesDataSource: MediaPreferencesDataSource = mockk(relaxed = true)

    @Test
    fun `clearCache deletes files in cacheDir and resets cacheBytes`() = runTest {
        val tempCacheDir = Files.createTempDirectory("diu_media_cache_test").toFile()
        try {
            val cachedFile = File(tempCacheDir, "sample.tmp").apply { writeText("1234567890") }
            val subDir = File(tempCacheDir, "coil").apply { mkdirs() }
            val nestedFile = File(subDir, "img.bin").apply { writeText("abc") }

            val context: Context = mockk {
                every { cacheDir } returns tempCacheDir
            }
            val sut = MediaRepositoryImpl(context = context, preferencesDataSource = preferencesDataSource)

            val result = sut.clearCache()

            result.isSuccess shouldBeEqualTo true
            result.getOrThrow() shouldBeEqualTo 13L
            cachedFile.exists() shouldBeEqualTo false
            nestedFile.exists() shouldBeEqualTo false
            tempCacheDir.exists() shouldBeEqualTo true
            sut.getStorageStats().first().cacheBytes shouldBeEqualTo 0L
        } finally {
            tempCacheDir.deleteRecursively()
        }
    }

    @Test
    fun `autoDownloadPolicy delegates to preferencesDataSource`() = runTest {
        val context: Context = mockk(relaxed = true)
        every { preferencesDataSource.autoDownloadPolicyFlow } returns flowOf(AutoDownloadPolicy.CELLULAR_AND_WIFI)
        val sut = MediaRepositoryImpl(context = context, preferencesDataSource = preferencesDataSource)

        sut.getAutoDownloadPolicy().first() shouldBeEqualTo AutoDownloadPolicy.CELLULAR_AND_WIFI

        sut.updateAutoDownloadPolicy(AutoDownloadPolicy.MANUAL)
        coVerify { preferencesDataSource.updateAutoDownloadPolicy(AutoDownloadPolicy.MANUAL) }
    }
}
