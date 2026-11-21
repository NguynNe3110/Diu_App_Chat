package com.uzuu.diuchat.feature.auth.domain.usecase

import com.uzuu.diuchat.feature.auth.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class ClaimUsernameUseCaseTest {
    private val mockAuthRepository: AuthRepository = mockk()

    private val sut = ClaimUsernameUseCase(mockAuthRepository)

    @Test
    fun `claimUsername delegates to repository`() = runBlocking {
        coEvery { mockAuthRepository.claimUsername("minh_an", "Minh An") } returns Result.success(Unit)

        val actual = sut("minh_an", "Minh An")

        actual shouldBeEqualTo Result.success(Unit)
        coVerify(exactly = 1) { mockAuthRepository.claimUsername("minh_an", "Minh An") }
    }
}
