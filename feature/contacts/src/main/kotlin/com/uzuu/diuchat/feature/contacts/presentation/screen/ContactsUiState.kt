package com.uzuu.diuchat.feature.contacts.presentation.screen

import androidx.compose.runtime.Immutable
import com.uzuu.diuchat.feature.base.presentation.viewmodel.BaseState
import com.uzuu.diuchat.feature.contacts.domain.model.ConnectionRequest
import com.uzuu.diuchat.feature.contacts.domain.model.Contact
import com.uzuu.diuchat.feature.contacts.domain.model.ContactCircle

enum class QrSheetTab {
    MY_CODE,
    SCAN_CODE,
}

@Immutable
data class ContactsUiState(
    val contacts: List<Contact> = emptyList(),
    val pendingRequests: List<ConnectionRequest> = emptyList(),
    val selectedCircle: ContactCircle = ContactCircle.ALL,
    val selectedMoodHex: String? = null,
    val searchQuery: String = "",
    val isQrSheetVisible: Boolean = false,
    val selectedQrTab: QrSheetTab = QrSheetTab.MY_CODE,
    val inviteCodeInput: String = "",
    val scanResultMessage: String? = null,
    val myInviteLink: String = "",
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

            val byMood =
                if (selectedMoodHex == null) {
                    byCircle
                } else {
                    byCircle.filter { it.moodColorHex.equals(selectedMoodHex, ignoreCase = true) }
                }

            if (searchQuery.isBlank()) return byMood

            val query = searchQuery.trim().lowercase()
            return byMood.filter {
                it.name.lowercase().contains(query) ||
                    (it.nickname?.lowercase()?.contains(query) == true) ||
                    it.phone.contains(query)
            }
        }
}
