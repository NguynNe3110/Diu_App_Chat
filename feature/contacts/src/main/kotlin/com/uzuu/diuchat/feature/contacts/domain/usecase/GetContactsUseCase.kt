package com.uzuu.diuchat.feature.contacts.domain.usecase

import com.uzuu.diuchat.feature.contacts.domain.model.Contact
import com.uzuu.diuchat.feature.contacts.domain.repository.ContactsRepository
import kotlinx.coroutines.flow.Flow

class GetContactsUseCase(
    private val contactsRepository: ContactsRepository,
) {
    operator fun invoke(): Flow<List<Contact>> = contactsRepository.getContacts()
}
