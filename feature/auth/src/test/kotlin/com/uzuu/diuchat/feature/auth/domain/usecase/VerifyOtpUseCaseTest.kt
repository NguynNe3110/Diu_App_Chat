package com.uzuu.diuchat.feature.auth.domain.usecase

import com.uzuu.diuchat.feature.auth.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class VerifyOtpUseCaseTest {

    private val mockAuthRepository: AuthRepository = mockk()

    private val sut = VerifyOtpUseCase(mockAuthRepository)

    @Test
    fun `verifyOtp delegates to repository`() = runBlocking {
        // given
        coEvery { mockAuthRepository.verifyOtp("912345678", "123456") } returns Result.success(Unit)

        // when
        val actual = sut("912345678", "123456")

        // then
        actual shouldBeEqualTo Result.success(Unit)
        coVerify(exactly = 1) { mockAuthRepository.verifyOtp("912345678", "123456") }
    }
}
