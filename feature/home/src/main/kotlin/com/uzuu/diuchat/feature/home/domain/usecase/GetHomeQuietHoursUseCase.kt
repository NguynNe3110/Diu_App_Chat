package com.uzuu.diuchat.feature.home.domain.usecase

import com.uzuu.diuchat.feature.base.domain.repository.QuietHoursStatus
import com.uzuu.diuchat.feature.home.domain.repository.HomeRepository
import kotlinx.coroutines.flow.Flow

class GetHomeQuietHoursUseCase(
    private val homeRepository: HomeRepository,
) {
    operator fun invoke(): Flow<QuietHoursStatus> = homeRepository.observeQuietHoursStatus()
}
