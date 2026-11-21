package com.uzuu.diuchat.feature.home.domain.usecase

import com.uzuu.diuchat.feature.home.domain.model.StarPerson
import com.uzuu.diuchat.feature.home.domain.repository.HomeRepository
import kotlinx.coroutines.flow.Flow

class GetHomeStarsUseCase(
    private val homeRepository: HomeRepository,
) {
    operator fun invoke(isQuiet: Boolean): Flow<List<StarPerson>> = homeRepository.getStars(isQuiet)
}
