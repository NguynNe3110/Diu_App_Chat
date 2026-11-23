package com.uzuu.diuchat.feature.calls.domain.repository

import com.uzuu.diuchat.feature.calls.domain.model.CallEndReason
import com.uzuu.diuchat.feature.calls.domain.model.CallLog
import com.uzuu.diuchat.feature.calls.domain.model.CallSession
import com.uzuu.diuchat.feature.calls.domain.model.CallType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow

@Suppress("ComplexInterface")
interface CallRepository {
    val currentCallSession: StateFlow<CallSession?>

    suspend fun startCall(
        calleeId: String,
        calleeName: String,
        callType: CallType,
        conversationId: String = "",
    ): Result<CallSession>

    suspend fun acceptCall(callId: String): Result<CallSession>

    suspend fun rejectCall(
        callId: String,
        reason: CallEndReason = CallEndReason.REJECTED,
        declineMessage: String? = null,
    ): Result<Unit>

    suspend fun endCall(
        callId: String,
        reason: CallEndReason = CallEndReason.COMPLETED,
        declineMessage: String? = null,
    ): Result<Unit>

    suspend fun toggleMic(callId: String): Result<Boolean>

    suspend fun toggleSpeaker(callId: String): Result<Boolean>

    suspend fun toggleCamera(callId: String): Result<Boolean>

    suspend fun switchCamera(callId: String): Result<Boolean>

    fun getCallLogs(): Flow<List<CallLog>>

    fun observeIncomingCalls(): Flow<CallSession> = emptyFlow()

    suspend fun onIncomingCallReceived(session: CallSession) {
        // Default no-op for test doubles
    }

    suspend fun syncMediaPermissions(
        micGranted: Boolean,
        cameraGranted: Boolean,
    ) {
        // Default no-op for test doubles
    }
}
