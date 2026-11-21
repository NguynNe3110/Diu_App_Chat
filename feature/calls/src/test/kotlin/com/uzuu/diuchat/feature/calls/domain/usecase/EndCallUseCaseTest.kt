package com.uzuu.diuchat.feature.calls.domain.usecase

import com.uzuu.diuchat.feature.calls.domain.model.CallEndReason
import com.uzuu.diuchat.feature.calls.domain.repository.CallRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class EndCallUseCaseTest {

    private val callRepository: CallRepository = mockk(relaxed = true)

    val sut = EndCallUseCase(callRepository)

    @Test
    fun `endCall delegates to repository with given reason`() = runBlocking {
        coEvery { callRepository.endCall("call_123", CallEndReason.COMPLETED) } returns Result.success(Unit)

        val result = sut("call_123", CallEndReason.COMPLETED)

        result.isSuccess shouldBeEqualTo true
        coVerify { callRepository.endCall("call_123", CallEndReason.COMPLETED) }
    }

    @Test
    fun `endCall defaults to COMPLETED reason`() = runBlocking {
        coEvery { callRepository.endCall("call_123", CallEndReason.COMPLETED) } returns Result.success(Unit)

        val result = sut("call_123")

        result.isSuccess shouldBeEqualTo true
        coVerify { callRepository.endCall("call_123", CallEndReason.COMPLETED) }
    }
}
