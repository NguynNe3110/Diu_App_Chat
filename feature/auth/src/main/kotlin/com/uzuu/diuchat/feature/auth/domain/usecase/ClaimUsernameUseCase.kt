package com.uzuu.diuchat.feature.auth.domain.usecase

import com.uzuu.diuchat.feature.auth.domain.repository.AuthRepository

class ClaimUsernameUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(
        username: String,
        displayName: String? = null,
    ): Result<Unit> = authRepository.claimUsername(username, displayName)
}
