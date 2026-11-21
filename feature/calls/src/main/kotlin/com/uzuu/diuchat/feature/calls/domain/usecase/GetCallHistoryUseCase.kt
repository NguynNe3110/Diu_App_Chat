package com.uzuu.diuchat.feature.calls.domain.usecase

import com.uzuu.diuchat.feature.calls.domain.model.CallLog
import com.uzuu.diuchat.feature.calls.domain.repository.CallRepository
import kotlinx.coroutines.flow.Flow

class GetCallHistoryUseCase(
    private val callRepository: CallRepository,
) {
    operator fun invoke(): Flow<List<CallLog>> = callRepository.getCallLogs()
}
