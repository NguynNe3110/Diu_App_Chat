package com.uzuu.diuchat.feature.home.domain.usecase

import com.uzuu.diuchat.feature.home.domain.repository.HomeRepository

class DeleteHomeConversationUseCase(
    private val homeRepository: HomeRepository,
) {
    suspend operator fun invoke(conversationId: String) {
        homeRepository.deleteConversation(conversationId)
    }
}
