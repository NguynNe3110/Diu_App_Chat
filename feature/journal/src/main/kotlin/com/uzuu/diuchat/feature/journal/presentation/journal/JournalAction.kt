package com.uzuu.diuchat.feature.journal.presentation.journal

import com.uzuu.diuchat.feature.base.presentation.viewmodel.BaseAction
import com.uzuu.diuchat.feature.journal.domain.model.DailyMoodCheckin
import com.uzuu.diuchat.feature.journal.domain.model.JournalMoodType
import com.uzuu.diuchat.feature.journal.domain.model.MoodComparison

sealed interface JournalAction : BaseAction<JournalUiState> {
    data class SetLoading(val isLoading: Boolean) : JournalAction {
        override fun reduce(state: JournalUiState): JournalUiState = state.copy(isLoading = isLoading)
    }

    data class SetCheckins(val checkins: List<DailyMoodCheckin>) : JournalAction {
        override fun reduce(state: JournalUiState): JournalUiState {
            val breakdown = calculateBreakdown(
                monthName = "Tháng ${state.month}",
                checkins = checkins,
            )
            val selected = if (checkins.any { it.dayNumber == state.selectedDay }) {
                state.selectedDay
            } else {
                checkins.maxOfOrNull { it.dayNumber } ?: 1
            }
            return state.copy(
                checkins = checkins,
                selectedDay = selected,
                breakdown = breakdown,
                isLoading = false,
            )
        }
    }

    data class SelectDay(val dayNumber: Int) : JournalAction {
        override fun reduce(state: JournalUiState): JournalUiState = state.copy(selectedDay = dayNumber)
    }

    data class SetComparison(val comparison: MoodComparison) : JournalAction {
        override fun reduce(state: JournalUiState): JournalUiState = state.copy(comparison = comparison)
    }

    data class SetCheckinSheetVisible(val isVisible: Boolean) : JournalAction {
        override fun reduce(state: JournalUiState): JournalUiState = state.copy(isCheckinSheetVisible = isVisible)
    }

    data class SetCheckinSelectedMood(val mood: JournalMoodType?) : JournalAction {
        override fun reduce(state: JournalUiState): JournalUiState = state.copy(checkinSelectedMood = mood)
    }

    data class SetCheckinNote(val note: String) : JournalAction {
        override fun reduce(state: JournalUiState): JournalUiState = state.copy(checkinNote = note)
    }

    data class ToggleCheckinContact(val contactId: String) : JournalAction {
        override fun reduce(state: JournalUiState): JournalUiState {
            val updated = state.checkinAllowlist.toMutableSet()
            if (updated.contains(contactId)) {
                updated.remove(contactId)
            } else {
                updated.add(contactId)
            }
            return state.copy(checkinAllowlist = updated)
        }
    }

    data class SetSavingCheckin(val isSaving: Boolean) : JournalAction {
        override fun reduce(state: JournalUiState): JournalUiState = state.copy(isSavingCheckin = isSaving)
    }

    data class SetError(val message: String?) : JournalAction {
        override fun reduce(state: JournalUiState): JournalUiState = state.copy(
            errorMessage = message,
            isLoading = false,
            isSavingCheckin = false,
        )
    }
}
