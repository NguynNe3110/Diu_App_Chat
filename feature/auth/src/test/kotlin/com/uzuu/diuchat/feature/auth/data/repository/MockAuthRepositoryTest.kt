package com.uzuu.diuchat.feature.auth.data.repository

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MockAuthRepositoryTest {
    private val sut = MockAuthRepository(initialHasUsername = false)

    @Test
    fun `sending propagates coroutine cancellation`() = runTest {
        var cancellationPropagated = false
        var returnedNormally = false
        val request =
            launch {
                try {
                    sut.sendOtp("912345678")
                    returnedNormally = true
                } catch (_: CancellationException) {
                    cancellationPropagated = true
                }
            }
        runCurrent()

        request.cancel()
        request.join()

        cancellationPropagated shouldBeEqualTo true
        returnedNormally shouldBeEqualTo false
    }

    @Test
    fun `claimUsername updates checkHasUsername to true`() = runTest {
        sut.checkHasUsername().getOrNull() shouldBeEqualTo false

        sut.claimUsername("@minh_an2026").isSuccess shouldBeEqualTo true

        sut.checkHasUsername().getOrNull() shouldBeEqualTo true
    }

    @Test
    fun `signInWithGoogle rejects blank id token`() = runTest {
        sut.signInWithGoogle("  ").isFailure shouldBeEqualTo true
    }
}
