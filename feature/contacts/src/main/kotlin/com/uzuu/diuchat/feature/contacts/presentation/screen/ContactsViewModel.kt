package com.uzuu.diuchat.feature.contacts.presentation.screen

import androidx.lifecycle.viewModelScope
import com.uzuu.diuchat.feature.base.presentation.viewmodel.BaseViewModel
import com.uzuu.diuchat.feature.contacts.domain.model.ContactCircle
import com.uzuu.diuchat.feature.contacts.domain.usecase.GetContactsUseCase
import com.uzuu.diuchat.feature.contacts.domain.usecase.GetPendingRequestsUseCase
import com.uzuu.diuchat.feature.contacts.domain.usecase.RespondConnectionRequestUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

private const val COPIED_NOTICE_DURATION_MS = 2_000L

class ContactsViewModel(
    private val getContactsUseCase: GetContactsUseCase,
    private val getPendingRequestsUseCase: GetPendingRequestsUseCase,
    private val respondConnectionRequestUseCase: RespondConnectionRequestUseCase,
) : BaseViewModel<ContactsUiState, ContactsAction>(ContactsUiState()) {
    init {
        observeData()
    }

    private fun observeData() {
        sendAction(ContactsAction.SetLoading(true))

        getContactsUseCase()
            .onEach { contacts ->
                sendAction(ContactsAction.SetContacts(contacts))
            }.launchIn(viewModelScope)

        getPendingRequestsUseCase()
            .onEach { requests ->
                sendAction(ContactsAction.SetPendingRequests(requests))
            }.launchIn(viewModelScope)
    }

    fun onSelectCircle(circle: ContactCircle) {
        sendAction(ContactsAction.SelectCircle(circle))
    }

    fun onSelectMood(moodHex: String?) {
        sendAction(ContactsAction.SelectMood(moodHex))
    }

    fun onSearchQueryChange(query: String) {
        sendAction(ContactsAction.UpdateSearchQuery(query))
    }

    fun onClearSearch() {
        sendAction(ContactsAction.UpdateSearchQuery(""))
    }

    fun onShowQrSheet() {
        sendAction(ContactsAction.SetQrSheetVisible(true))
    }

    fun onDismissQrSheet() {
        sendAction(ContactsAction.SetQrSheetVisible(false))
    }

    fun onSelectQrTab(tab: QrSheetTab) {
        sendAction(ContactsAction.SelectQrTab(tab))
    }

    fun onInviteCodeInputChange(code: String) {
        sendAction(ContactsAction.UpdateInviteCodeInput(code))
    }

    fun onSubmitInviteCode(code: String = uiStateFlow.value.inviteCodeInput) {
        val trimmed = code.trim()
        if (trimmed.isEmpty()) {
            sendAction(ContactsAction.SetScanResultMessage("Vui lòng nhập mã kết nối"))
            return
        }
        sendAction(
            ContactsAction.SetScanResultMessage(
                message = "Đã gửi lời mời kết nối tới $trimmed",
                clearInput = true,
            ),
        )
    }

    fun onCopyInviteLink() {
        sendAction(ContactsAction.SetCopiedLink(true))
        viewModelScope.launch {
            delay(COPIED_NOTICE_DURATION_MS)
            sendAction(ContactsAction.SetCopiedLink(false))
        }
    }

    fun onAcceptRequest(
        requestId: String,
        circle: ContactCircle = ContactCircle.FRIENDS,
    ) {
        viewModelScope.launch {
            respondConnectionRequestUseCase(requestId = requestId, accept = true, circle = circle)
                .onFailure { error ->
                    sendAction(ContactsAction.SetError(error.message ?: "Không thể chấp nhận lời mời"))
                }
        }
    }

    fun onDeclineRequest(requestId: String) {
        viewModelScope.launch {
            respondConnectionRequestUseCase(requestId = requestId, accept = false)
                .onFailure { error ->
                    sendAction(ContactsAction.SetError(error.message ?: "Không thể từ chối lời mời"))
                }
        }
    }
}
