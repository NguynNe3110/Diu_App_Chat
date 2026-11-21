package com.uzuu.diuchat.feature.calls.domain.usecase

import com.uzuu.diuchat.feature.calls.domain.model.CallSession
import com.uzuu.diuchat.feature.calls.domain.model.CallType
import com.uzuu.diuchat.feature.calls.domain.repository.CallRepository

class StartCallUseCase(
    private val callRepository: CallRepository,
) {
    suspend operator fun invoke(
        calleeId: String,
        calleeName: String,
        callType: CallType,
    ): Result<CallSession> = callRepository.startCall(
        calleeId = calleeId,
        calleeName = calleeName,
        callType = callType,
    )
}
