package com.uzuu.diuchat.feature.contacts.presentation.screen

import androidx.lifecycle.viewModelScope
import com.uzuu.diuchat.feature.base.presentation.viewmodel.BaseViewModel
import com.uzuu.diuchat.feature.contacts.domain.model.ContactCircle
import com.uzuu.diuchat.feature.contacts.domain.repository.ContactsRepository
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
    private val contactsRepository: ContactsRepository,
) : BaseViewModel<ContactsUiState, ContactsAction>(ContactsUiState()) {
    init {
        observeData()
        viewModelScope.launch {
            contactsRepository.refreshFromRemote()
        }
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
            sendAction(ContactsAction.SetScanResultMessage("Vui lòng nhập @username, số điện thoại hoặc mã QR"))
            return
        }
        viewModelScope.launch {
            contactsRepository.connectByInviteCode(trimmed)
                .onSuccess { contact ->
                    sendAction(
                        ContactsAction.SetScanResultMessage(
                            message = "Đã kết nối với ${contact.name}",
                            clearInput = true,
                        ),
                    )
                }
                .onFailure { error ->
                    sendAction(
                        ContactsAction.SetScanResultMessage(
                            message = error.message ?: "Không tìm thấy người dùng để kết nối",
                        ),
                    )
                }
        }
    }

    fun onOpenContactChat(
        contactId: String,
        onNavigateToChat: (String) -> Unit,
    ) {
        viewModelScope.launch {
            contactsRepository.openDirectChat(contactId)
                .onSuccess { conversationId ->
                    onNavigateToChat(conversationId)
                }
                .onFailure {
                    onNavigateToChat(contactId)
                }
        }
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
