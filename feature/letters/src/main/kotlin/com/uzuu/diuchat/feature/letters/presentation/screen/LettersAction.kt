package com.uzuu.diuchat.feature.letters.presentation.screen

import com.uzuu.diuchat.feature.base.presentation.viewmodel.BaseAction
import com.uzuu.diuchat.feature.letters.domain.model.LetterScheduleType
import com.uzuu.diuchat.feature.letters.domain.model.SealedLetter

sealed class LettersAction : BaseAction<LettersUiState> {
    data class SetLetters(val letters: List<SealedLetter>) : LettersAction() {
        override fun reduce(state: LettersUiState): LettersUiState = state.copy(letters = letters)
    }

    data class SetTab(val tab: LetterTab) : LettersAction() {
        override fun reduce(state: LettersUiState): LettersUiState = state.copy(selectedTab = tab)
    }

    data class SetComposing(val isComposing: Boolean) : LettersAction() {
        override fun reduce(state: LettersUiState): LettersUiState = state.copy(isComposing = isComposing)
    }

    data class SetShowScheduleSheet(val show: Boolean) : LettersAction() {
        override fun reduce(state: LettersUiState): LettersUiState = state.copy(showScheduleSheet = show)
    }

    data class UpdateDraftTitle(val title: String) : LettersAction() {
        override fun reduce(state: LettersUiState): LettersUiState = state.copy(draftTitle = title)
    }

    data class UpdateDraftContent(val content: String) : LettersAction() {
        override fun reduce(state: LettersUiState): LettersUiState = state.copy(draftContent = content)
    }

    data class UpdateDraftMood(val mood: String) : LettersAction() {
        override fun reduce(state: LettersUiState): LettersUiState = state.copy(draftMood = mood)
    }

    data class UpdateDraftRecipient(
        val recipientId: String,
        val recipientName: String,
    ) : LettersAction() {
        override fun reduce(state: LettersUiState): LettersUiState = state.copy(
            draftRecipientId = recipientId,
            draftRecipientName = recipientName,
        )
    }

    data class StartEditingLetter(val letter: SealedLetter) : LettersAction() {
        override fun reduce(state: LettersUiState): LettersUiState = state.copy(
            isComposing = true,
            draftEditingLetterId = letter.id,
            draftRecipientId = letter.recipientId,
            draftRecipientName = letter.recipientName,
            draftTitle = letter.title,
            draftContent = letter.contentPayload,
            draftMood = letter.moodName,
            draftScheduledUnlockEpochMs = letter.scheduledUnlockEpochMs,
            draftScheduledUnlockFormatted = letter.scheduledUnlockFormatted,
        )
    }

    data class SetDraftSchedule(
        val scheduleType: LetterScheduleType,
        val epochMs: Long,
        val formatted: String,
    ) : LettersAction() {
        override fun reduce(state: LettersUiState): LettersUiState = state.copy(
            draftScheduleType = scheduleType,
            draftScheduledUnlockEpochMs = epochMs,
            draftScheduledUnlockFormatted = formatted,
        )
    }

    data class SetSelectedOpenedLetter(val letter: SealedLetter?) : LettersAction() {
        override fun reduce(state: LettersUiState): LettersUiState = state.copy(selectedOpenedLetter = letter)
    }

    data object ResetDraft : LettersAction() {
        override fun reduce(state: LettersUiState): LettersUiState = state.copy(
            isComposing = false,
            draftEditingLetterId = null,
            draftTitle = "",
            draftContent = "",
            draftMood = "Thương",
            draftScheduleType = LetterScheduleType.TOMORROW_MORNING,
            draftScheduledUnlockEpochMs = 0L,
            draftScheduledUnlockFormatted = "08:00 Sáng mai",
        )
    }
}
