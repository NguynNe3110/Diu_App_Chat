package com.uzuu.diuchat.feature.letters.presentation.screen

import androidx.lifecycle.viewModelScope
import com.uzuu.diuchat.feature.base.presentation.viewmodel.BaseViewModel
import com.uzuu.diuchat.feature.letters.domain.model.LetterScheduleType
import com.uzuu.diuchat.feature.letters.domain.model.LetterStatus
import com.uzuu.diuchat.feature.letters.domain.model.SealedLetter
import com.uzuu.diuchat.feature.letters.domain.usecase.CancelSealedLetterUseCase
import com.uzuu.diuchat.feature.letters.domain.usecase.GetLettersUseCase
import com.uzuu.diuchat.feature.letters.domain.usecase.OpenSealedLetterUseCase
import com.uzuu.diuchat.feature.letters.domain.usecase.SendLetterImmediatelyUseCase
import com.uzuu.diuchat.feature.letters.domain.usecase.SendSealedLetterUseCase
import com.uzuu.diuchat.feature.letters.domain.usecase.UpdateSealedLetterUseCase
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.UUID

class LettersViewModel(
    private val cancelSealedLetterUseCase: CancelSealedLetterUseCase,
    private val getLettersUseCase: GetLettersUseCase,
    private val openSealedLetterUseCase: OpenSealedLetterUseCase,
    private val sendLetterImmediatelyUseCase: SendLetterImmediatelyUseCase,
    private val sendSealedLetterUseCase: SendSealedLetterUseCase,
    private val updateSealedLetterUseCase: UpdateSealedLetterUseCase,
) : BaseViewModel<LettersUiState, LettersAction>(LettersUiState()) {

    init {
        observeLetters()
    }

    private fun observeLetters() {
        sendAction(LettersAction.SetLoading(true))
        getLettersUseCase()
            .onEach { sendAction(LettersAction.SetLetters(it)) }
            .launchIn(viewModelScope)
    }

    fun selectTab(tab: LetterTab) {
        sendAction(LettersAction.SetTab(tab))
    }

    fun startComposing() {
        sendAction(LettersAction.SetComposing(true))
    }

    fun startEditing(letter: SealedLetter) {
        sendAction(LettersAction.StartEditingLetter(letter))
    }

    fun cancelComposing() {
        sendAction(LettersAction.ResetDraft)
    }

    fun updateDraftTitle(title: String) {
        sendAction(LettersAction.UpdateDraftTitle(title))
    }

    fun updateDraftContent(content: String) {
        sendAction(LettersAction.UpdateDraftContent(content))
    }

    fun updateDraftMood(mood: String) {
        sendAction(LettersAction.UpdateDraftMood(mood))
    }

    fun updateDraftRecipient(recipientId: String, recipientName: String) {
        sendAction(LettersAction.UpdateDraftRecipient(recipientId, recipientName))
    }

    fun showScheduleSheet(show: Boolean) {
        sendAction(LettersAction.SetShowScheduleSheet(show))
    }

    fun setSchedule(type: LetterScheduleType, formatted: String, customEpochMs: Long? = null) {
        val epochMs = customEpochMs ?: when (type) {
            LetterScheduleType.WAKE_UP -> System.currentTimeMillis() + 8 * 3600 * 1000L
            LetterScheduleType.TOMORROW_MORNING -> System.currentTimeMillis() + 14 * 3600 * 1000L
            LetterScheduleType.TONIGHT -> System.currentTimeMillis() + 4 * 3600 * 1000L
            LetterScheduleType.CUSTOM_DATE -> System.currentTimeMillis() + 7 * 24 * 3600 * 1000L
        }
        sendAction(LettersAction.SetDraftSchedule(type, epochMs, formatted))
        sendAction(LettersAction.SetShowScheduleSheet(false))
    }

    fun sealAndSendLetter() {
        val currentState = uiStateFlow.value
        if (currentState.draftTitle.isBlank() || currentState.draftContent.isBlank()) return

        val editingId = currentState.draftEditingLetterId
        val letter = SealedLetter(
            id = editingId ?: "letter_${UUID.randomUUID().toString().take(8)}",
            senderId = "user_me",
            senderName = "Tôi",
            recipientId = currentState.draftRecipientId,
            recipientName = currentState.draftRecipientName,
            title = currentState.draftTitle.trim(),
            scheduledUnlockEpochMs = currentState.draftScheduledUnlockEpochMs.takeIf { it > 0 }
                ?: (System.currentTimeMillis() + 14 * 3600 * 1000L),
            scheduledUnlockFormatted = currentState.draftScheduledUnlockFormatted,
            status = LetterStatus.SEALED_PENDING,
            isSender = true,
            moodName = currentState.draftMood,
            contentPayload = currentState.draftContent.trim(),
        )

        viewModelScope.launch {
            if (editingId != null) {
                updateSealedLetterUseCase(letter)
            } else {
                sendSealedLetterUseCase(letter)
            }
            sendAction(LettersAction.ResetDraft)
        }
    }

    fun openLetter(letterId: String) {
        viewModelScope.launch {
            openSealedLetterUseCase(letterId)
            val updated = uiStateFlow.value.letters.find { it.id == letterId }
            if (updated != null) {
                sendAction(LettersAction.SetSelectedOpenedLetter(updated.copy(status = LetterStatus.OPENED)))
            }
        }
    }

    fun cancelLetter(letterId: String) {
        viewModelScope.launch {
            cancelSealedLetterUseCase(letterId)
        }
    }

    fun sendImmediately(letterId: String) {
        viewModelScope.launch {
            sendLetterImmediatelyUseCase(letterId)
        }
    }

    fun viewOpenedLetter(letter: SealedLetter) {
        sendAction(LettersAction.SetSelectedOpenedLetter(letter))
    }

    fun dismissOpenedLetter() {
        sendAction(LettersAction.SetSelectedOpenedLetter(null))
    }
}
