package com.uzuu.diuchat.feature.home.domain.usecase

import com.uzuu.diuchat.feature.home.domain.model.RecentChat
import com.uzuu.diuchat.feature.home.domain.repository.HomeRepository
import kotlinx.coroutines.flow.Flow

class GetRecentChatsUseCase(
    private val homeRepository: HomeRepository,
) {
    operator fun invoke(): Flow<List<RecentChat>> = homeRepository.getRecentChats()
}
