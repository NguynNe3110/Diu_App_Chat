package com.uzuu.diuchat.feature.journal.presentation.journal

import androidx.lifecycle.viewModelScope
import com.uzuu.diuchat.feature.base.presentation.viewmodel.BaseViewModel
import com.uzuu.diuchat.feature.journal.domain.model.DailyMoodCheckin
import com.uzuu.diuchat.feature.journal.domain.model.JournalMoodType
import com.uzuu.diuchat.feature.journal.domain.usecase.GetMonthlyMoodCheckinsUseCase
import com.uzuu.diuchat.feature.journal.domain.usecase.GetMoodComparisonUseCase
import com.uzuu.diuchat.feature.journal.domain.usecase.SaveMoodCheckinUseCase
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class JournalViewModel(
    private val getMonthlyMoodCheckinsUseCase: GetMonthlyMoodCheckinsUseCase,
    private val getMoodComparisonUseCase: GetMoodComparisonUseCase,
    private val saveMoodCheckinUseCase: SaveMoodCheckinUseCase,
) : BaseViewModel<JournalUiState, JournalAction>(JournalUiState()) {

    init {
        loadData()
    }

    fun loadData(month: Int = 11, year: Int = 2026) {
        sendAction(JournalAction.SetLoading(true))

        viewModelScope.launch {
            getMonthlyMoodCheckinsUseCase(month = month, year = year)
                .catch { sendAction(JournalAction.SetError(it.message)) }
                .collect { checkins ->
                    sendAction(JournalAction.SetCheckins(checkins))
                }
        }

        viewModelScope.launch {
            getMoodComparisonUseCase("minh")
                .catch { /* ignore */ }
                .collect { comparison ->
                    sendAction(JournalAction.SetComparison(comparison))
                }
        }
    }

    fun onSelectDay(dayNumber: Int) {
        sendAction(JournalAction.SelectDay(dayNumber))
    }

    fun onOpenCheckinSheet() {
        val today = 24
        val existing = uiStateFlow.value.checkins.find { it.dayNumber == today }
        sendAction(JournalAction.SetCheckinSelectedMood(existing?.mood ?: JournalMoodType.BINH_YEN))
        sendAction(JournalAction.SetCheckinNote(existing?.privateNote ?: ""))
        sendAction(JournalAction.SetCheckinSheetVisible(true))
    }

    fun onDismissCheckinSheet() {
        sendAction(JournalAction.SetCheckinSheetVisible(false))
    }

    fun onSelectCheckinMood(mood: JournalMoodType) {
        sendAction(JournalAction.SetCheckinSelectedMood(mood))
    }

    fun onChangeCheckinNote(note: String) {
        sendAction(JournalAction.SetCheckinNote(note))
    }

    fun onToggleCheckinContact(contactId: String) {
        sendAction(JournalAction.ToggleCheckinContact(contactId))
    }

    fun onSaveCheckin() {
        val selectedMood = uiStateFlow.value.checkinSelectedMood ?: return
        val dayNumber = 24
        val checkin = DailyMoodCheckin(
            dateIso = "2026-11-%02d".format(dayNumber),
            dayNumber = dayNumber,
            mood = selectedMood,
            privateNote = uiStateFlow.value.checkinNote.ifBlank { null },
            sharedWithContactIds = uiStateFlow.value.checkinAllowlist.toList(),
            displayDate = "%d tháng 11".format(dayNumber),
        )

        sendAction(JournalAction.SetSavingCheckin(true))
        viewModelScope.launch {
            saveMoodCheckinUseCase(checkin)
                .onSuccess {
                    sendAction(JournalAction.SetSavingCheckin(false))
                    sendAction(JournalAction.SetCheckinSheetVisible(false))
                    sendAction(JournalAction.SelectDay(dayNumber))
                }
                .onFailure {
                    sendAction(JournalAction.SetError(it.message ?: "Lỗi khi lưu check-in"))
                }
        }
    }
}
