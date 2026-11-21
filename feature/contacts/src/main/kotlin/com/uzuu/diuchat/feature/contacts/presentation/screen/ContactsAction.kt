package com.uzuu.diuchat.feature.contacts.presentation.screen

import com.uzuu.diuchat.feature.base.presentation.viewmodel.BaseAction
import com.uzuu.diuchat.feature.contacts.domain.model.ConnectionRequest
import com.uzuu.diuchat.feature.contacts.domain.model.Contact
import com.uzuu.diuchat.feature.contacts.domain.model.ContactCircle

sealed interface ContactsAction : BaseAction<ContactsUiState> {

    data class SetContacts(val contacts: List<Contact>) : ContactsAction {
        override fun reduce(state: ContactsUiState): ContactsUiState =
            state.copy(contacts = contacts, isLoading = false)
    }

    data class SetPendingRequests(val requests: List<ConnectionRequest>) : ContactsAction {
        override fun reduce(state: ContactsUiState): ContactsUiState =
            state.copy(pendingRequests = requests)
    }

    data class SelectCircle(val circle: ContactCircle) : ContactsAction {
        override fun reduce(state: ContactsUiState): ContactsUiState =
            state.copy(selectedCircle = circle)
    }

    data class UpdateSearchQuery(val query: String) : ContactsAction {
        override fun reduce(state: ContactsUiState): ContactsUiState =
            state.copy(searchQuery = query)
    }

    data class SetQrSheetVisible(val visible: Boolean) : ContactsAction {
        override fun reduce(state: ContactsUiState): ContactsUiState =
            state.copy(isQrSheetVisible = visible)
    }

    data class SetCopiedLink(val copied: Boolean) : ContactsAction {
        override fun reduce(state: ContactsUiState): ContactsUiState =
            state.copy(copiedLink = copied)
    }

    data class SetLoading(val loading: Boolean) : ContactsAction {
        override fun reduce(state: ContactsUiState): ContactsUiState =
            state.copy(isLoading = loading)
    }

    data class SetError(val message: String?) : ContactsAction {
        override fun reduce(state: ContactsUiState): ContactsUiState =
            state.copy(errorMessage = message, isLoading = false)
    }
}
