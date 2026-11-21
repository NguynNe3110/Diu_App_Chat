package com.uzuu.diuchat.feature.letters.presentation.screen

import com.uzuu.diuchat.feature.letters.domain.model.LetterScheduleType
import com.uzuu.diuchat.feature.letters.domain.model.LetterStatus
import com.uzuu.diuchat.feature.letters.domain.model.SealedLetter
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class LettersActionTest {

    @Test
    fun `SetLetters reduces letters in state`() {
        val initial = LettersUiState()
        val letters = listOf(
            SealedLetter(
                id = "l1",
                senderId = "s",
                senderName = "S",
                recipientId = "r",
                recipientName = "R",
                title = "Test",
                scheduledUnlockEpochMs = 10L,
                scheduledUnlockFormatted = "Now",
                status = LetterStatus.SEALED_PENDING,
                isSender = true,
            ),
        )
        val action = LettersAction.SetLetters(letters)

        val newState = action.reduce(initial)

        newState.letters shouldBeEqualTo letters
    }

    @Test
    fun `SetTab reduces selectedTab in state`() {
        val initial = LettersUiState()
        val action = LettersAction.SetTab(LetterTab.OPENED)

        val newState = action.reduce(initial)

        newState.selectedTab shouldBeEqualTo LetterTab.OPENED
    }

    @Test
    fun `SetComposing and ResetDraft reduce state properly`() {
        val initial = LettersUiState()
        val composingState = LettersAction.SetComposing(true).reduce(initial)
        composingState.isComposing shouldBeEqualTo true

        val resetState = LettersAction.ResetDraft.reduce(composingState)
        resetState.isComposing shouldBeEqualTo false
        resetState.draftTitle shouldBeEqualTo ""
        resetState.draftContent shouldBeEqualTo ""
    }

    @Test
    fun `UpdateDraftTitle and UpdateDraftContent reduce state`() {
        val initial = LettersUiState()
        val s1 = LettersAction.UpdateDraftTitle("Tiêu đề mới").reduce(initial)
        s1.draftTitle shouldBeEqualTo "Tiêu đề mới"

        val s2 = LettersAction.UpdateDraftContent("Nội dung tâm sự").reduce(s1)
        s2.draftContent shouldBeEqualTo "Nội dung tâm sự"
    }

    @Test
    fun `SetDraftSchedule reduces schedule in state`() {
        val initial = LettersUiState()
        val action = LettersAction.SetDraftSchedule(
            scheduleType = LetterScheduleType.TONIGHT,
            epochMs = 99999L,
            formatted = "22:00 Tối nay",
        )

        val newState = action.reduce(initial)

        newState.draftScheduleType shouldBeEqualTo LetterScheduleType.TONIGHT
        newState.draftScheduledUnlockEpochMs shouldBeEqualTo 99999L
        newState.draftScheduledUnlockFormatted shouldBeEqualTo "22:00 Tối nay"
    }
}
