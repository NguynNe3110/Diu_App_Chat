package com.uzuu.diuchat.feature.auth.domain.usecase

import com.uzuu.diuchat.feature.auth.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class SignInWithGoogleUseCaseTest {
    private val authRepository: AuthRepository = mockk()

    val sut = SignInWithGoogleUseCase(authRepository)

    @Test
    fun `signInWithGoogle delegates to repository`() = runBlocking {
        coEvery { authRepository.signInWithGoogle("sample-id-token") } returns Result.success(Unit)

        val actual = sut("sample-id-token")

        actual shouldBeEqualTo Result.success(Unit)
        coVerify(exactly = 1) { authRepository.signInWithGoogle("sample-id-token") }
    }
}
