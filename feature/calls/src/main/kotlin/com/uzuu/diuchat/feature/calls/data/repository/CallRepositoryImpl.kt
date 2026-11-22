package com.uzuu.diuchat.feature.calls.data.repository

import com.uzuu.diuchat.feature.calls.data.CallServiceCoordinator
import com.uzuu.diuchat.feature.calls.data.WebRtcSignalingAdapter
import com.uzuu.diuchat.feature.calls.domain.model.CallEndReason
import com.uzuu.diuchat.feature.calls.domain.model.CallLog
import com.uzuu.diuchat.feature.calls.domain.model.CallSession
import com.uzuu.diuchat.feature.calls.domain.model.CallStatus
import com.uzuu.diuchat.feature.calls.domain.model.CallType
import com.uzuu.diuchat.feature.calls.domain.model.RtcSignalingMessage
import com.uzuu.diuchat.feature.calls.domain.model.SignalingType
import com.uzuu.diuchat.feature.calls.domain.repository.CallRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID

private const val ERROR_NO_CALL = "Không có cuộc gọi"
private const val ERROR_CALL_ID_MISMATCH = "Mã cuộc gọi không khớp"

@Suppress("TooManyFunctions")
class CallRepositoryImpl(
    private val callServiceCoordinator: CallServiceCoordinator,
    private val webRtcSignalingAdapter: WebRtcSignalingAdapter,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.Default,
) : CallRepository {

    private val repositoryScope = CoroutineScope(coroutineDispatcher)

    private val _currentCallSession = MutableStateFlow<CallSession?>(null)
    override val currentCallSession: StateFlow<CallSession?> = _currentCallSession.asStateFlow()

    private val callLogsStateFlow = MutableStateFlow(createInitialCallLogs())

    private var durationTickerJob: Job? = null

    init {
        repositoryScope.launch {
            webRtcSignalingAdapter.signalingEvents.collect { message ->
                handleIncomingSignaling(message)
            }
        }
    }

    private suspend fun handleIncomingSignaling(message: RtcSignalingMessage) {
        val current = _currentCallSession.value ?: return
        if (message.sessionId != current.callId) return

        when (message.type) {
            SignalingType.ANSWER -> {
                if (current.status == CallStatus.INVITING || current.status == CallStatus.CONNECTING) {
                    _currentCallSession.value = current.copy(
                        status = CallStatus.ACTIVE,
                        startedAtEpochMs = current.startedAtEpochMs ?: System.currentTimeMillis(),
                    )
                    startDurationTicker()
                    Timber.d("Incoming ANSWER transitioned call %s to ACTIVE", current.callId)
                }
            }

            SignalingType.BYE,
            SignalingType.DECLINE,
            -> {
                val isFromRemotePeer = message.senderId != "local_peer" && message.senderId != current.callerId
                if (isFromRemotePeer && current.status != CallStatus.ENDED) {
                    val reason = if (message.type == SignalingType.DECLINE) {
                        CallEndReason.REJECTED
                    } else {
                        CallEndReason.COMPLETED
                    }
                    Timber.d("Incoming %s from %s ended call %s", message.type, message.senderId, current.callId)
                    finalizeCall(current, reason)
                }
            }

            SignalingType.OFFER,
            SignalingType.ICE_CANDIDATE,
            -> {
                // Handled by WebRTC peer connection layer when media engine is attached
            }
        }
    }

    override suspend fun startCall(
        calleeId: String,
        calleeName: String,
        callType: CallType,
    ): Result<CallSession> = runCatching {
        val current = _currentCallSession.value
        require(current == null || current.status == CallStatus.ENDED || current.status == CallStatus.IDLE) {
            "Cuộc gọi khác đang diễn ra"
        }

        val callId = "call_${UUID.randomUUID()}"
        val session = CallSession(
            callId = callId,
            callerId = "user_me",
            callerName = "Tôi",
            calleeId = calleeId,
            calleeName = calleeName,
            status = CallStatus.INVITING,
            type = callType,
            isIncoming = false,
            isAudioMuted = false,
            isSpeakerOn = callType == CallType.VIDEO,
            isVideoEnabled = callType == CallType.VIDEO,
            isFrontCamera = true,
            durationSeconds = 0L,
            startedAtEpochMs = System.currentTimeMillis(),
        )

        _currentCallSession.value = session
        callServiceCoordinator.startCallService(callId, calleeName, callType == CallType.VIDEO)
        webRtcSignalingAdapter.connect(callId)

        Timber.d("Started call: %s to %s (%s)", callId, calleeName, callType)
        session
    }

    override suspend fun acceptCall(callId: String): Result<CallSession> = runCatching {
        val current = _currentCallSession.value ?: error("Không có cuộc gọi để chấp nhận")
        require(current.callId == callId) { ERROR_CALL_ID_MISMATCH }

        // Monotonic check: only transition from RINGING or INVITING
        require(current.status == CallStatus.RINGING || current.status == CallStatus.INVITING) {
            "Trạng thái không hợp lệ để nhận cuộc gọi: ${current.status}"
        }

        val activeSession = current.copy(
            status = CallStatus.ACTIVE,
            startedAtEpochMs = current.startedAtEpochMs ?: System.currentTimeMillis(),
        )
        _currentCallSession.value = activeSession

        callServiceCoordinator.startCallService(
            callId = current.callId,
            peerName = if (current.isIncoming) current.callerName else current.calleeName,
            isVideo = current.type == CallType.VIDEO,
        )

        webRtcSignalingAdapter.sendSignaling(
            RtcSignalingMessage(
                sessionId = callId,
                senderId = "local_peer",
                targetId = "remote_peer",
                type = SignalingType.ANSWER,
            ),
        )

        startDurationTicker()
        Timber.d("Accepted call: %s, transition to ACTIVE", callId)
        activeSession
    }

    override suspend fun rejectCall(callId: String, reason: CallEndReason): Result<Unit> = runCatching {
        val current = _currentCallSession.value
        if (current != null && current.callId == callId && current.status != CallStatus.ENDED) {
            webRtcSignalingAdapter.sendSignaling(
                RtcSignalingMessage(
                    sessionId = callId,
                    senderId = "local_peer",
                    targetId = "remote_peer",
                    type = SignalingType.DECLINE,
                ),
            )
            finalizeCall(current, reason)
            Timber.d("Rejected call: %s, reason: %s", callId, reason)
        }
    }

    override suspend fun endCall(callId: String, reason: CallEndReason): Result<Unit> = runCatching {
        val current = _currentCallSession.value
        if (current != null && current.callId == callId && current.status != CallStatus.ENDED) {
            finalizeCall(current, reason)
            Timber.d("Ended call: %s, reason: %s", callId, reason)
        }
    }

    override suspend fun toggleMic(callId: String): Result<Boolean> = runCatching {
        val current = _currentCallSession.value ?: error(ERROR_NO_CALL)
        require(current.callId == callId) { ERROR_CALL_ID_MISMATCH }

        val newMuted = !current.isAudioMuted
        _currentCallSession.value = current.copy(isAudioMuted = newMuted)
        callServiceCoordinator.setMicrophoneMute(newMuted)
        newMuted
    }

    override suspend fun toggleSpeaker(callId: String): Result<Boolean> = runCatching {
        val current = _currentCallSession.value ?: error(ERROR_NO_CALL)
        require(current.callId == callId) { ERROR_CALL_ID_MISMATCH }

        val newSpeaker = !current.isSpeakerOn
        _currentCallSession.value = current.copy(isSpeakerOn = newSpeaker)
        callServiceCoordinator.setSpeakerphoneOn(newSpeaker)
        newSpeaker
    }

    override suspend fun toggleCamera(callId: String): Result<Boolean> = runCatching {
        val current = _currentCallSession.value ?: error(ERROR_NO_CALL)
        require(current.callId == callId) { ERROR_CALL_ID_MISMATCH }

        val newVideo = !current.isVideoEnabled
        _currentCallSession.value = current.copy(isVideoEnabled = newVideo)
        callServiceCoordinator.setCameraEnabled(newVideo)
        newVideo
    }

    override suspend fun switchCamera(callId: String): Result<Boolean> = runCatching {
        val current = _currentCallSession.value ?: error(ERROR_NO_CALL)
        require(current.callId == callId) { ERROR_CALL_ID_MISMATCH }

        val newFront = callServiceCoordinator.switchCamera()
        _currentCallSession.value = current.copy(isFrontCamera = newFront)
        newFront
    }

    override fun getCallLogs(): Flow<List<CallLog>> = callLogsStateFlow.asStateFlow()

    private fun startDurationTicker() {
        durationTickerJob?.cancel()
        durationTickerJob = repositoryScope.launch {
            while (isActive) {
                delay(1000L)
                _currentCallSession.update { current ->
                    if (current != null && current.status == CallStatus.ACTIVE) {
                        current.copy(durationSeconds = current.durationSeconds + 1L)
                    } else {
                        current
                    }
                }
            }
        }
    }

    private suspend fun finalizeCall(current: CallSession, reason: CallEndReason) {
        durationTickerJob?.cancel()
        durationTickerJob = null

        val endedSession = current.copy(
            status = CallStatus.ENDED,
            endedAtEpochMs = System.currentTimeMillis(),
            endReason = reason,
        )
        _currentCallSession.value = endedSession

        val peerId = if (current.isIncoming) current.callerId else current.calleeId
        val peerName = if (current.isIncoming) current.callerName else current.calleeName

        val log = CallLog(
            id = "log_${UUID.randomUUID()}",
            callId = current.callId,
            peerId = peerId,
            peerName = peerName,
            type = current.type,
            endReason = reason,
            durationSeconds = current.durationSeconds,
            timestampEpochMs = current.startedAtEpochMs ?: System.currentTimeMillis(),
            isIncoming = current.isIncoming,
        )

        callLogsStateFlow.update { logs -> listOf(log) + logs }

        callServiceCoordinator.stopCallService()
        webRtcSignalingAdapter.disconnect()
    }

    private fun createInitialCallLogs(): List<CallLog> = listOf(
        CallLog(
            id = "log_1",
            callId = "call_init_1",
            peerId = "user_linh",
            peerName = "Linh (Người thương)",
            type = CallType.AUDIO,
            endReason = CallEndReason.COMPLETED,
            durationSeconds = 428L,
            timestampEpochMs = System.currentTimeMillis() - 3_600_000L * 2,
            isIncoming = true,
        ),
        CallLog(
            id = "log_2",
            callId = "call_init_2",
            peerId = "user_linh",
            peerName = "Linh (Người thương)",
            type = CallType.VIDEO,
            endReason = CallEndReason.COMPLETED,
            durationSeconds = 1245L,
            timestampEpochMs = System.currentTimeMillis() - 86_400_000L,
            isIncoming = false,
        ),
        CallLog(
            id = "log_3",
            callId = "call_init_3",
            peerId = "user_me",
            peerName = "Mẹ yêu",
            type = CallType.AUDIO,
            endReason = CallEndReason.MISSED,
            durationSeconds = 0L,
            timestampEpochMs = System.currentTimeMillis() - 86_400_000L * 2,
            isIncoming = true,
        ),
    )
}
