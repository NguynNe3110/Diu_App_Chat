package com.uzuu.diuchat.feature.contacts.domain.usecase

import com.uzuu.diuchat.feature.contacts.domain.model.ContactCircle
import com.uzuu.diuchat.feature.contacts.domain.repository.ContactsRepository

class RespondConnectionRequestUseCase(
    private val contactsRepository: ContactsRepository,
) {
    suspend operator fun invoke(
        requestId: String,
        accept: Boolean,
        circle: ContactCircle = ContactCircle.FRIENDS,
    ): Result<Unit> = contactsRepository.respondToRequest(requestId, accept, circle)
}
