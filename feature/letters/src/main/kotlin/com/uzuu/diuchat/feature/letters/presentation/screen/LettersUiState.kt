package com.uzuu.diuchat.feature.letters.presentation.screen

import com.uzuu.diuchat.feature.base.presentation.viewmodel.BaseState
import com.uzuu.diuchat.feature.letters.domain.model.LetterScheduleType
import com.uzuu.diuchat.feature.letters.domain.model.SealedLetter

data class LettersUiState(
    val letters: List<SealedLetter> = emptyList(),
    val selectedTab: LetterTab = LetterTab.ALL,
    val isComposing: Boolean = false,
    val showScheduleSheet: Boolean = false,
    val draftRecipientName: String = "Linh (Người thương)",
    val draftRecipientId: String = "user_linh",
    val draftTitle: String = "",
    val draftContent: String = "",
    val draftMood: String = "Thương",
    val draftScheduleType: LetterScheduleType = LetterScheduleType.TOMORROW_MORNING,
    val draftScheduledUnlockEpochMs: Long = 0L,
    val draftScheduledUnlockFormatted: String = "08:00 Sáng mai",
    val selectedOpenedLetter: SealedLetter? = null,
) : BaseState {
    val filteredLetters: List<SealedLetter>
        get() = when (selectedTab) {
            LetterTab.ALL -> letters
            LetterTab.PENDING -> letters.filter { it.status == com.uzuu.diuchat.feature.letters.domain.model.LetterStatus.SEALED_PENDING }
            LetterTab.READY_TO_OPEN -> letters.filter { it.status == com.uzuu.diuchat.feature.letters.domain.model.LetterStatus.READY_TO_OPEN }
            LetterTab.OPENED -> letters.filter { it.status == com.uzuu.diuchat.feature.letters.domain.model.LetterStatus.OPENED }
        }
}
