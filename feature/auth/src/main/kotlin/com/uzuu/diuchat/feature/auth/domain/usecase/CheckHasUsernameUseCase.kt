package com.uzuu.diuchat.feature.auth.domain.usecase

import com.uzuu.diuchat.feature.auth.domain.repository.AuthRepository

class CheckHasUsernameUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(): Result<Boolean> = authRepository.checkHasUsername()
}
