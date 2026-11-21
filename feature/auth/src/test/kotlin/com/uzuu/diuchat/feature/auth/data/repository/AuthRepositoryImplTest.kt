package com.uzuu.diuchat.feature.auth.data.repository

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthRepositoryImplTest {
    private val sut = AuthRepositoryImpl()

    @Test
    fun `sending propagates coroutine cancellation instead of returning fallback success`() = runTest {
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
    fun `verification propagates coroutine cancellation instead of returning fallback success`() = runTest {
        var cancellationPropagated = false
        var returnedNormally = false
        val request =
            launch {
                try {
                    sut.verifyOtp("912345678", "123456")
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
    fun `invalid OTP returns a validation failure`() = runTest {
        sut.verifyOtp("912345678", "12x").isFailure shouldBeEqualTo true
    }
}
