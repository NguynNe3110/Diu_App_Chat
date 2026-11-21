package com.uzuu.diuchat.feature.letters.presentation.screen

import com.uzuu.diuchat.feature.letters.domain.model.LetterScheduleType
import com.uzuu.diuchat.feature.letters.domain.model.LetterStatus
import com.uzuu.diuchat.feature.letters.domain.model.SealedLetter
import com.uzuu.diuchat.feature.letters.domain.usecase.CancelSealedLetterUseCase
import com.uzuu.diuchat.feature.letters.domain.usecase.GetLettersUseCase
import com.uzuu.diuchat.feature.letters.domain.usecase.OpenSealedLetterUseCase
import com.uzuu.diuchat.feature.letters.domain.usecase.SendLetterImmediatelyUseCase
import com.uzuu.diuchat.feature.letters.domain.usecase.SendSealedLetterUseCase
import com.uzuu.diuchat.feature.letters.domain.usecase.UpdateSealedLetterUseCase
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
class LettersViewModelTest {

    @JvmField
    @RegisterExtension
    val coroutinesTestDispatcherExtension = CoroutinesTestDispatcherExtension()

    private val cancelSealedLetterUseCase: CancelSealedLetterUseCase = mockk(relaxed = true)
    private val getLettersUseCase: GetLettersUseCase = mockk()
    private val openSealedLetterUseCase: OpenSealedLetterUseCase = mockk(relaxed = true)
    private val sendLetterImmediatelyUseCase: SendLetterImmediatelyUseCase = mockk(relaxed = true)
    private val sendSealedLetterUseCase: SendSealedLetterUseCase = mockk(relaxed = true)
    private val updateSealedLetterUseCase: UpdateSealedLetterUseCase = mockk(relaxed = true)

    private val testLetters = listOf(
        SealedLetter(
            id = "l1",
            senderId = "user_me",
            senderName = "Tôi",
            recipientId = "user_linh",
            recipientName = "Linh",
            title = "Thư hẹn ước",
            scheduledUnlockEpochMs = 1000L,
            scheduledUnlockFormatted = "08:00 Sáng mai",
            status = LetterStatus.SEALED_PENDING,
            isSender = true,
            contentPayload = "Lời nhắn gửi",
        ),
    )

    lateinit var sut: LettersViewModel

    @BeforeEach
    fun setUp() {
        every { getLettersUseCase() } returns flowOf(testLetters)

        sut = LettersViewModel(
            cancelSealedLetterUseCase = cancelSealedLetterUseCase,
            getLettersUseCase = getLettersUseCase,
            openSealedLetterUseCase = openSealedLetterUseCase,
            sendLetterImmediatelyUseCase = sendLetterImmediatelyUseCase,
            sendSealedLetterUseCase = sendSealedLetterUseCase,
            updateSealedLetterUseCase = updateSealedLetterUseCase,
        )
    }

    @Test
    fun `initial state collects letters from usecase`() = runTest {
        advanceUntilIdle()

        sut.uiStateFlow.value.letters shouldBeEqualTo testLetters
        sut.uiStateFlow.value.selectedTab shouldBeEqualTo LetterTab.ALL
    }

    @Test
    fun `selectTab updates selectedTab in state`() = runTest {
        sut.selectTab(LetterTab.PENDING)
        sut.uiStateFlow.value.selectedTab shouldBeEqualTo LetterTab.PENDING
    }

    @Test
    fun `composing draft updates title content and mood`() = runTest {
        sut.startComposing()
        sut.uiStateFlow.value.isComposing shouldBeEqualTo true

        sut.updateDraftTitle("Tiêu đề")
        sut.updateDraftContent("Nội dung")
        sut.updateDraftMood("Vui")

        sut.uiStateFlow.value.draftTitle shouldBeEqualTo "Tiêu đề"
        sut.uiStateFlow.value.draftContent shouldBeEqualTo "Nội dung"
        sut.uiStateFlow.value.draftMood shouldBeEqualTo "Vui"
    }

    @Test
    fun `setSchedule updates draft schedule`() = runTest {
        sut.setSchedule(LetterScheduleType.TONIGHT, "22:00 Tối nay")

        sut.uiStateFlow.value.draftScheduleType shouldBeEqualTo LetterScheduleType.TONIGHT
        sut.uiStateFlow.value.draftScheduledUnlockFormatted shouldBeEqualTo "22:00 Tối nay"
        sut.uiStateFlow.value.showScheduleSheet shouldBeEqualTo false
    }

    @Test
    fun `sealAndSendLetter delegates to send usecase and resets draft`() = runTest {
        sut.updateDraftTitle("Lá thư sáng mai")
        sut.updateDraftContent("Chúc em ngày mới tốt lành")

        sut.sealAndSendLetter()
        advanceUntilIdle()

        coVerify { sendSealedLetterUseCase(any()) }
        sut.uiStateFlow.value.isComposing shouldBeEqualTo false
        sut.uiStateFlow.value.draftTitle shouldBeEqualTo ""
    }

    @Test
    fun `openLetter invokes open usecase`() = runTest {
        sut.openLetter("l1")
        advanceUntilIdle()

        coVerify { openSealedLetterUseCase("l1") }
    }

    @Test
    fun `cancelLetter invokes cancel usecase`() = runTest {
        sut.cancelLetter("l1")
        advanceUntilIdle()

        coVerify { cancelSealedLetterUseCase("l1") }
    }

    @Test
    fun `sendImmediately invokes send immediately usecase`() = runTest {
        sut.sendImmediately("l1")
        advanceUntilIdle()

        coVerify { sendLetterImmediatelyUseCase("l1") }
    }

    @Test
    fun `startEditing populates draft and sealAndSendLetter invokes updateSealedLetterUseCase`() = runTest {
        val existing = testLetters.first()
        sut.startEditing(existing)

        sut.uiStateFlow.value.isComposing shouldBeEqualTo true
        sut.uiStateFlow.value.draftEditingLetterId shouldBeEqualTo existing.id
        sut.uiStateFlow.value.draftTitle shouldBeEqualTo existing.title

        sut.updateDraftTitle("Tiêu đề đã sửa")
        sut.updateDraftRecipient("user_minh", "Minh")
        sut.sealAndSendLetter()
        advanceUntilIdle()

        coVerify {
            updateSealedLetterUseCase(
                match {
                    it.id == existing.id &&
                        it.title == "Tiêu đề đã sửa" &&
                        it.recipientId == "user_minh" &&
                        it.recipientName == "Minh"
                },
            )
        }
        sut.uiStateFlow.value.isComposing shouldBeEqualTo false
        sut.uiStateFlow.value.draftEditingLetterId shouldBeEqualTo null
    }
}
