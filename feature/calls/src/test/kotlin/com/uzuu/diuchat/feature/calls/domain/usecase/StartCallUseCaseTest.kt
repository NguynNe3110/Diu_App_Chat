package com.uzuu.diuchat.feature.calls.domain.usecase

import com.uzuu.diuchat.feature.calls.domain.model.CallSession
import com.uzuu.diuchat.feature.calls.domain.model.CallType
import com.uzuu.diuchat.feature.calls.domain.repository.CallRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class StartCallUseCaseTest {

    private val callRepository: CallRepository = mockk(relaxed = true)

    val sut = StartCallUseCase(callRepository)

    @Test
    fun `startCall delegates to repository and returns session`() = runBlocking {
        val expectedSession = CallSession(
            callId = "call_test",
            callerId = "user_me",
            callerName = "Tôi",
            calleeId = "user_callee",
            calleeName = "Linh",
            type = CallType.AUDIO,
        )
        coEvery {
            callRepository.startCall("user_callee", "Linh", CallType.AUDIO)
        } returns Result.success(expectedSession)

        val result = sut(
            calleeId = "user_callee",
            calleeName = "Linh",
            callType = CallType.AUDIO,
        )

        result.isSuccess shouldBeEqualTo true
        result.getOrNull() shouldBeEqualTo expectedSession
        coVerify { callRepository.startCall("user_callee", "Linh", CallType.AUDIO) }
    }
}
