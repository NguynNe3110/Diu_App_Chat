package com.uzuu.diuchat.feature.home.domain.usecase

import com.uzuu.diuchat.feature.base.domain.repository.QuietHoursStatus
import com.uzuu.diuchat.feature.home.domain.repository.HomeRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GetHomeQuietHoursUseCaseTest {
    private val homeRepository: HomeRepository = mockk()
    private val sut = GetHomeQuietHoursUseCase(homeRepository)

    @Test
    fun `invoke returns quiet hours status flow from repository`() = runTest {
        val expected = QuietHoursStatus(isActive = true, bannerText = "Giờ yên lặng đang bật · đến 06:00")
        every { homeRepository.observeQuietHoursStatus() } returns flowOf(expected)

        assertEquals(expected, sut().first())
    }
}
