package com.uzuu.diuchat.feature.contacts.domain.usecase

import com.uzuu.diuchat.feature.contacts.domain.model.ConnectionRequest
import com.uzuu.diuchat.feature.contacts.domain.repository.ContactsRepository
import kotlinx.coroutines.flow.Flow

class GetPendingRequestsUseCase(
    private val contactsRepository: ContactsRepository,
) {
    operator fun invoke(): Flow<List<ConnectionRequest>> = contactsRepository.getPendingRequests()
}
