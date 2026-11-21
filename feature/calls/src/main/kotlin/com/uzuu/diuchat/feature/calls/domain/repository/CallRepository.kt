package com.uzuu.diuchat.feature.calls.domain.repository

import com.uzuu.diuchat.feature.calls.domain.model.CallEndReason
import com.uzuu.diuchat.feature.calls.domain.model.CallLog
import com.uzuu.diuchat.feature.calls.domain.model.CallSession
import com.uzuu.diuchat.feature.calls.domain.model.CallType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

@Suppress("ComplexInterface")
interface CallRepository {
    val currentCallSession: StateFlow<CallSession?>

    suspend fun startCall(calleeId: String, calleeName: String, callType: CallType): Result<CallSession>

    suspend fun acceptCall(callId: String): Result<CallSession>

    suspend fun rejectCall(callId: String, reason: CallEndReason = CallEndReason.REJECTED): Result<Unit>

    suspend fun endCall(callId: String, reason: CallEndReason = CallEndReason.COMPLETED): Result<Unit>

    suspend fun toggleMic(callId: String): Result<Boolean>

    suspend fun toggleSpeaker(callId: String): Result<Boolean>

    suspend fun toggleCamera(callId: String): Result<Boolean>

    suspend fun switchCamera(callId: String): Result<Boolean>

    fun getCallLogs(): Flow<List<CallLog>>
}
