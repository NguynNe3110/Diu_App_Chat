package com.uzuu.diuchat.feature.contacts.presentation.screen

import androidx.compose.runtime.Immutable
import com.uzuu.diuchat.feature.base.presentation.viewmodel.BaseState
import com.uzuu.diuchat.feature.contacts.domain.model.ConnectionRequest
import com.uzuu.diuchat.feature.contacts.domain.model.Contact
import com.uzuu.diuchat.feature.contacts.domain.model.ContactCircle

@Immutable
data class ContactsUiState(
    val contacts: List<Contact> = emptyList(),
    val pendingRequests: List<ConnectionRequest> = emptyList(),
    val selectedCircle: ContactCircle = ContactCircle.ALL,
    val searchQuery: String = "",
    val isQrSheetVisible: Boolean = false,
    val myInviteLink: String = "diu.vn/linh/7K2M9P",
    val copiedLink: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
) : BaseState {
    val filteredContacts: List<Contact>
        get() {
            val byCircle =
                if (selectedCircle == ContactCircle.ALL) {
                    contacts
                } else {
                    contacts.filter { it.circle == selectedCircle }
                }

            if (searchQuery.isBlank()) return byCircle

            val query = searchQuery.trim().lowercase()
            return byCircle.filter {
                it.name.lowercase().contains(query) ||
                    (it.nickname?.lowercase()?.contains(query) == true) ||
                    it.phone.contains(query)
            }
        }
}
