package com.uzuu.diuchat.feature.auth.domain.usecase

import com.uzuu.diuchat.feature.auth.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class SendOtpUseCaseTest {
    private val mockAuthRepository: AuthRepository = mockk()

    private val sut = SendOtpUseCase(mockAuthRepository)

    @Test
    fun `sendOtp delegates to repository`() = runBlocking {
        // given
        coEvery { mockAuthRepository.sendOtp("912345678") } returns Result.success(Unit)

        // when
        val actual = sut("912345678")

        // then
        actual shouldBeEqualTo Result.success(Unit)
        coVerify(exactly = 1) { mockAuthRepository.sendOtp("912345678") }
    }
}
