package com.uzuu.diuchat.feature.calls.domain.usecase

import com.uzuu.diuchat.feature.calls.domain.model.CallEndReason
import com.uzuu.diuchat.feature.calls.domain.model.CallLog
import com.uzuu.diuchat.feature.calls.domain.model.CallType
import com.uzuu.diuchat.feature.calls.domain.repository.CallRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class GetCallHistoryUseCaseTest {

    private val callRepository: CallRepository = mockk(relaxed = true)

    val sut = GetCallHistoryUseCase(callRepository)

    @Test
    fun `invoke delegates to repository getCallLogs`() = runBlocking {
        val expectedLogs = listOf(
            CallLog(
                id = "log_1",
                callId = "call_1",
                peerId = "peer_1",
                peerName = "Linh",
                type = CallType.AUDIO,
                endReason = CallEndReason.COMPLETED,
                durationSeconds = 120L,
                timestampEpochMs = 1000L,
                isIncoming = true,
            ),
        )
        every { callRepository.getCallLogs() } returns flowOf(expectedLogs)

        val result = sut().first()

        result shouldBeEqualTo expectedLogs
        verify { callRepository.getCallLogs() }
    }
}
