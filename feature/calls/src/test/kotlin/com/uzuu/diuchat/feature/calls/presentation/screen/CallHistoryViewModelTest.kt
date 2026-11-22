package com.uzuu.diuchat.feature.calls.presentation.screen

import com.uzuu.diuchat.feature.calls.domain.model.CallEndReason
import com.uzuu.diuchat.feature.calls.domain.model.CallLog
import com.uzuu.diuchat.feature.calls.domain.model.CallType
import com.uzuu.diuchat.feature.calls.domain.usecase.GetCallHistoryUseCase
import com.uzuu.diuchat.feature.calls.domain.usecase.StartCallUseCase
import com.uzuu.diuchat.library.testutils.CoroutinesTestDispatcherExtension
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@OptIn(ExperimentalCoroutinesApi::class)
class CallHistoryViewModelTest {

    @JvmField
    @RegisterExtension
    val coroutinesTestDispatcherExtension = CoroutinesTestDispatcherExtension()

    private val getCallHistoryUseCase: GetCallHistoryUseCase = mockk()
    private val startCallUseCase: StartCallUseCase = mockk(relaxed = true)

    private val sampleLogs = listOf(
        CallLog(
            id = "log_1",
            callId = "call_1",
            peerId = "user_linh",
            peerName = "Linh",
            type = CallType.AUDIO,
            endReason = CallEndReason.COMPLETED,
            durationSeconds = 120L,
            timestampEpochMs = 1000L,
            isIncoming = true,
        ),
        CallLog(
            id = "log_2",
            callId = "call_2",
            peerId = "user_mother",
            peerName = "Mẹ",
            type = CallType.VIDEO,
            endReason = CallEndReason.MISSED,
            durationSeconds = 0L,
            timestampEpochMs = 2000L,
            isIncoming = true,
        ),
    )

    lateinit var sut: CallHistoryViewModel

    @BeforeEach
    fun setUp() {
        every { getCallHistoryUseCase() } returns flowOf(sampleLogs)

        sut = CallHistoryViewModel(
            getCallHistoryUseCase = getCallHistoryUseCase,
            startCallUseCase = startCallUseCase,
        )
    }

    @Test
    fun `initial state collects call logs from usecase`() = runTest {
        advanceUntilIdle()

        sut.uiStateFlow.value.callLogs shouldBeEqualTo sampleLogs
        sut.uiStateFlow.value.filter shouldBeEqualTo CallHistoryFilter.ALL
        sut.uiStateFlow.value.filteredLogs.size shouldBeEqualTo 2
    }

    @Test
    fun `setFilter to MISSED filters only missed calls`() = runTest {
        advanceUntilIdle()

        sut.setFilter(CallHistoryFilter.MISSED)

        sut.uiStateFlow.value.filter shouldBeEqualTo CallHistoryFilter.MISSED
        sut.uiStateFlow.value.filteredLogs.size shouldBeEqualTo 1
        sut.uiStateFlow.value.filteredLogs.first().peerName shouldBeEqualTo "Mẹ"
    }

    @Test
    fun `redial triggers startCallUseCase`() = runTest {
        advanceUntilIdle()

        sut.redial(sampleLogs.first())
        advanceUntilIdle()

        coVerify {
            startCallUseCase(
                calleeId = "user_linh",
                calleeName = "Linh",
                callType = CallType.AUDIO,
            )
        }
    }

    @Test
    fun `startFavoriteCall initiates call with favorite caller info and groupedLogsByDate buckets logs`() = runTest {
        advanceUntilIdle()
        val fav = sut.uiStateFlow.value.favoriteCallers.first()
        sut.startFavoriteCall(fav, CallType.AUDIO)
        advanceUntilIdle()

        coVerify {
            startCallUseCase(
                calleeId = fav.peerId,
                calleeName = fav.name,
                callType = CallType.AUDIO,
            )
        }
        sut.uiStateFlow.value.groupedLogsByDate.values.flatten().size shouldBeEqualTo 2
    }
}
