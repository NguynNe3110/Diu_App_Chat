package com.uzuu.diuchat.feature.calls.domain.usecase

import com.uzuu.diuchat.feature.calls.domain.model.CallEndReason
import com.uzuu.diuchat.feature.calls.domain.repository.CallRepository

class EndCallUseCase(
    private val callRepository: CallRepository,
) {
    suspend operator fun invoke(
        callId: String,
        reason: CallEndReason = CallEndReason.COMPLETED,
    ): Result<Unit> = callRepository.endCall(
        callId = callId,
        reason = reason,
    )
}
