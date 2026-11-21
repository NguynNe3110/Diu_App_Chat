package com.uzuu.diuchat.feature.calls.domain.usecase

import com.uzuu.diuchat.feature.calls.domain.model.CallSession
import com.uzuu.diuchat.feature.calls.domain.model.CallStatus
import com.uzuu.diuchat.feature.calls.domain.repository.CallRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class AcceptCallUseCaseTest {

    private val callRepository: CallRepository = mockk(relaxed = true)

    val sut = AcceptCallUseCase(callRepository)

    @Test
    fun `acceptCall delegates to repository`() = runBlocking {
        val expectedSession = CallSession(
            callId = "call_123",
            callerId = "user_peer",
            callerName = "Peer",
            calleeId = "user_me",
            calleeName = "Tôi",
            status = CallStatus.ACTIVE,
        )
        coEvery { callRepository.acceptCall("call_123") } returns Result.success(expectedSession)

        val result = sut("call_123")

        result.isSuccess shouldBeEqualTo true
        result.getOrNull() shouldBeEqualTo expectedSession
        coVerify { callRepository.acceptCall("call_123") }
    }
}
