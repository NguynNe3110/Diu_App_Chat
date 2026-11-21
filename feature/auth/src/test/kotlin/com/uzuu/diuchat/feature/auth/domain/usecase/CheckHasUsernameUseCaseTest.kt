package com.uzuu.diuchat.feature.auth.domain.usecase

import com.uzuu.diuchat.feature.auth.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class CheckHasUsernameUseCaseTest {
    private val mockAuthRepository: AuthRepository = mockk()

    private val sut = CheckHasUsernameUseCase(mockAuthRepository)

    @Test
    fun `checkHasUsername delegates to repository`() = runBlocking {
        coEvery { mockAuthRepository.checkHasUsername() } returns Result.success(true)

        val actual = sut()

        actual shouldBeEqualTo Result.success(true)
        coVerify(exactly = 1) { mockAuthRepository.checkHasUsername() }
    }
}
