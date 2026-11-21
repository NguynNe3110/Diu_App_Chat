package com.uzuu.diuchat.feature.calls.domain.usecase

import com.uzuu.diuchat.feature.calls.domain.model.CallSession
import com.uzuu.diuchat.feature.calls.domain.repository.CallRepository

class AcceptCallUseCase(
    private val callRepository: CallRepository,
) {
    suspend operator fun invoke(callId: String): Result<CallSession> = callRepository.acceptCall(callId = callId)
}
