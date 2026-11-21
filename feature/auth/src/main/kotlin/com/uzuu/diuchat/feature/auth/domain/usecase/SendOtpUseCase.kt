package com.uzuu.diuchat.feature.auth.domain.usecase

import com.uzuu.diuchat.feature.auth.domain.repository.AuthRepository

class SendOtpUseCase(
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(phoneNumber: String): Result<Unit> =
        authRepository.sendOtp(phoneNumber)
}
