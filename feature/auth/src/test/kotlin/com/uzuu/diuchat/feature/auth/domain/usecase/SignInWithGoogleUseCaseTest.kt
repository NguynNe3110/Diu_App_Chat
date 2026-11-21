package com.uzuu.diuchat.feature.auth.domain.usecase

import com.uzuu.diuchat.feature.auth.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class SignInWithGoogleUseCaseTest {
    private val mockAuthRepository: AuthRepository = mockk()

    private val sut = SignInWithGoogleUseCase(mockAuthRepository)

    @Test
    fun `signInWithGoogle delegates to repository`() = runBlocking {
        coEvery { mockAuthRepository.signInWithGoogle("valid_token") } returns Result.success(Unit)

        val actual = sut("valid_token")

        actual shouldBeEqualTo Result.success(Unit)
        coVerify(exactly = 1) { mockAuthRepository.signInWithGoogle("valid_token") }
    }
}
