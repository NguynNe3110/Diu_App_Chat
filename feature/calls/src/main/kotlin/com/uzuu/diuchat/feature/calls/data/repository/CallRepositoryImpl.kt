package com.uzuu.diuchat.feature.calls.data.repository

import com.uzuu.diuchat.feature.base.domain.media.AppSoundPlayer
import com.uzuu.diuchat.feature.base.domain.media.DiuSoundEffect
import com.uzuu.diuchat.feature.calls.data.CallMediaEngine
import com.uzuu.diuchat.feature.calls.data.CallServiceCoordinator
import com.uzuu.diuchat.feature.calls.data.CallSupabaseService
import com.uzuu.diuchat.feature.calls.data.CallTokenService
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
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID

private const val ERROR_NO_CALL = "Không có cuộc gọi"
private const val ERROR_CALL_ID_MISMATCH = "Mã cuộc gọi không khớp"
private const val DEFAULT_RINGING_TIMEOUT_MS = 45_000L

@Suppress("TooManyFunctions")
class CallRepositoryImpl(
    private val callServiceCoordinator: CallServiceCoordinator,
    private val webRtcSignalingAdapter: WebRtcSignalingAdapter,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val callTokenService: CallTokenService? = null,
    private val callMediaEngine: CallMediaEngine? = null,
    private val callSupabaseService: CallSupabaseService? = null,
    private val ringingTimeoutMs: Long = DEFAULT_RINGING_TIMEOUT_MS,
    private val appSoundPlayer: AppSoundPlayer? = null,
) : CallRepository {

    private val repositoryScope = CoroutineScope(coroutineDispatcher)

    private val _currentCallSession = MutableStateFlow<CallSession?>(null)
    override val currentCallSession: StateFlow<CallSession?> = _currentCallSession.asStateFlow()

    private val callLogsStateFlow = MutableStateFlow(
        if (callSupabaseService?.isConfigured() == true) emptyList() else createInitialCallLogs(),
    )

    private var durationTickerJob: Job? = null
    private var ringingTimeoutJob: Job? = null

    init {
        repositoryScope.launch {
            callSupabaseService?.fetchCallLogs()?.let { remoteLogs ->
                callLogsStateFlow.value = remoteLogs
            }
        }
        if (callSupabaseService?.isConfigured() == true) {
            repositoryScope.launch {
                callSupabaseService.observeRealtimeUpdates().collect {
                    callSupabaseService.fetchCallLogs()?.let { remoteLogs ->
                        callLogsStateFlow.value = remoteLogs
                    }
                }
            }
        }
        repositoryScope.launch {
            webRtcSignalingAdapter.signalingEvents.collect { message ->
                handleIncomingSignaling(message)
            }
        }
    }

    private fun currentActorId(): String = callSupabaseService?.currentUserId() ?: "user_me"

    private suspend fun handleIncomingSignaling(message: RtcSignalingMessage) {
        val current = _currentCallSession.value ?: return
        if (message.sessionId != current.callId) return
        val myId = currentActorId()
        if (message.senderId == myId || message.senderId == "local_peer") return

        when (message.type) {
            SignalingType.ANSWER -> {
                if (current.status == CallStatus.INVITING || current.status == CallStatus.CONNECTING) {
                    cancelRingingTimeout()
                    appSoundPlayer?.stopLooping()
                    _currentCallSession.value = current.copy(
                        status = CallStatus.ACTIVE,
                        startedAtEpochMs = System.currentTimeMillis(),
                    )
                    startDurationTicker()
                    connectMediaEngine(current.callId, current.type == CallType.VIDEO)
                    Timber.d("Incoming ANSWER transitioned call %s to ACTIVE", current.callId)
                }
            }

            SignalingType.BYE,
            SignalingType.DECLINE,
            -> {
                if (current.status != CallStatus.ENDED) {
                    val reason = if (message.type == SignalingType.DECLINE) {
                        CallEndReason.REJECTED
                    } else {
                        CallEndReason.COMPLETED
                    }
                    Timber.d("Incoming %s from %s ended call %s", message.type, message.senderId, current.callId)
                    finalizeCall(
                        current = current,
                        reason = reason,
                        notifyRemote = false,
                    )
                }
            }

            SignalingType.OFFER,
            SignalingType.ICE_CANDIDATE,
            -> {
                // LiveKit handles ICE/SDP negotiation once both peers join room callId
            }
        }
    }

    override fun observeIncomingCalls(): Flow<CallSession> = callSupabaseService?.observeIncomingCalls() ?: emptyFlow()

    override suspend fun onIncomingCallReceived(session: CallSession) {
        val current = _currentCallSession.value
        val isBusy = current != null &&
            current.status != CallStatus.IDLE &&
            current.status != CallStatus.ENDED &&
            current.callId != session.callId
        if (isBusy) return

        val incoming = session.copy(
            status = CallStatus.RINGING,
            isIncoming = true,
            isSpeakerOn = session.type == CallType.VIDEO,
            isVideoEnabled = session.type == CallType.VIDEO,
        )
        _currentCallSession.value = incoming
        appSoundPlayer?.startLooping(DiuSoundEffect.CALL_INCOMING_RINGTONE)
        webRtcSignalingAdapter.connect(incoming.callId)
        startRingingTimeout(incoming.callId)
        Timber.d("Loaded incoming call %s from %s", incoming.callId, incoming.callerName)
    }

    override suspend fun startCall(
        calleeId: String,
        calleeName: String,
        callType: CallType,
        conversationId: String,
    ): Result<CallSession> = runCatching {
        val current = _currentCallSession.value
        require(current == null || current.status == CallStatus.ENDED || current.status == CallStatus.IDLE) {
            "Cuộc gọi khác đang diễn ra"
        }

        val callId = UUID.randomUUID().toString()
        val callerId = currentActorId()
        val callerName = callSupabaseService?.currentUserDisplayName() ?: "Tôi"
        val session = CallSession(
            callId = callId,
            callerId = callerId,
            callerName = callerName,
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
            conversationId = conversationId,
        )

        _currentCallSession.value = session
        callServiceCoordinator.startCallService(callId, calleeName, callType == CallType.VIDEO)
        webRtcSignalingAdapter.connect(callId)
        webRtcSignalingAdapter.sendSignaling(
            RtcSignalingMessage(
                sessionId = callId,
                senderId = callerId,
                targetId = calleeId,
                type = SignalingType.OFFER,
            ),
        )
        val resolvedConvoId = callSupabaseService?.createCallLog(
            callId = callId,
            calleeId = calleeId,
            isVideo = callType == CallType.VIDEO,
            conversationId = conversationId,
        ) ?: conversationId
        if (resolvedConvoId != conversationId) {
            _currentCallSession.update { it?.copy(conversationId = resolvedConvoId) }
        }
        appSoundPlayer?.startLooping(DiuSoundEffect.CALL_OUTGOING_RINGBACK)
        if (callType == CallType.VIDEO) {
            connectMediaEngine(callId, enableVideo = true)
        }
        startRingingTimeout(callId)

        Timber.d("Started call: %s to %s (%s)", callId, calleeName, callType)
        _currentCallSession.value ?: session
    }

    override suspend fun acceptCall(callId: String): Result<CallSession> = runCatching {
        val current = _currentCallSession.value ?: error("Không có cuộc gọi để chấp nhận")
        require(current.callId == callId) { ERROR_CALL_ID_MISMATCH }

        // Monotonic check: only transition from RINGING or INVITING
        require(current.status == CallStatus.RINGING || current.status == CallStatus.INVITING) {
            "Trạng thái không hợp lệ để nhận cuộc gọi: ${current.status}"
        }

        cancelRingingTimeout()
        appSoundPlayer?.stopLooping()
        val activeSession = current.copy(
            status = CallStatus.ACTIVE,
            startedAtEpochMs = System.currentTimeMillis(),
        )
        _currentCallSession.value = activeSession

        callServiceCoordinator.startCallService(
            callId = current.callId,
            peerName = if (current.isIncoming) current.callerName else current.calleeName,
            isVideo = current.type == CallType.VIDEO,
        )

        webRtcSignalingAdapter.connect(callId)
        webRtcSignalingAdapter.sendSignaling(
            RtcSignalingMessage(
                sessionId = callId,
                senderId = currentActorId(),
                targetId = if (current.isIncoming) current.callerId else current.calleeId,
                type = SignalingType.ANSWER,
            ),
        )

        callSupabaseService?.markCallAccepted(callId)
        startDurationTicker()
        connectMediaEngine(callId, current.type == CallType.VIDEO)
        Timber.d("Accepted call: %s, transition to ACTIVE", callId)
        activeSession
    }

    override suspend fun rejectCall(
        callId: String,
        reason: CallEndReason,
        declineMessage: String?,
    ): Result<Unit> = runCatching {
        val current = _currentCallSession.value
        if (current != null && current.callId == callId && current.status != CallStatus.ENDED) {
            finalizeCall(
                current = current,
                reason = reason,
                declineMessage = declineMessage,
                notifyRemote = true,
            )
            Timber.d("Rejected call: %s, reason: %s", callId, reason)
        }
    }

    override suspend fun endCall(
        callId: String,
        reason: CallEndReason,
        declineMessage: String?,
    ): Result<Unit> = runCatching {
        val current = _currentCallSession.value
        if (current != null && current.callId == callId && current.status != CallStatus.ENDED) {
            finalizeCall(
                current = current,
                reason = reason,
                declineMessage = declineMessage,
                notifyRemote = true,
            )
            Timber.d("Ended call: %s, reason: %s", callId, reason)
        }
    }

    override suspend fun toggleMic(callId: String): Result<Boolean> = runCatching {
        val current = _currentCallSession.value ?: error(ERROR_NO_CALL)
        require(current.callId == callId) { ERROR_CALL_ID_MISMATCH }

        val newMuted = !current.isAudioMuted
        _currentCallSession.value = current.copy(isAudioMuted = newMuted)
        callServiceCoordinator.setMicrophoneMute(newMuted)
        callMediaEngine?.setMicrophoneMuted(newMuted)
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
        callMediaEngine?.setCameraEnabled(newVideo)
        newVideo
    }

    override suspend fun switchCamera(callId: String): Result<Boolean> = runCatching {
        val current = _currentCallSession.value ?: error(ERROR_NO_CALL)
        require(current.callId == callId) { ERROR_CALL_ID_MISMATCH }

        val newFront = callMediaEngine?.switchCamera() ?: callServiceCoordinator.switchCamera()
        _currentCallSession.value = current.copy(isFrontCamera = newFront)
        newFront
    }

    override suspend fun syncMediaPermissions(
        micGranted: Boolean,
        cameraGranted: Boolean,
    ) {
        val current = _currentCallSession.value ?: return
        if (current.status == CallStatus.ENDED || current.status == CallStatus.IDLE) return
        if (micGranted || cameraGranted) {
            callServiceCoordinator.refreshForegroundService()
        }
        if (micGranted) {
            callMediaEngine?.setMicrophoneMuted(current.isAudioMuted)
        }
        if (cameraGranted && current.type == CallType.VIDEO && current.isVideoEnabled) {
            callMediaEngine?.setCameraEnabled(true)
        }
    }

    override fun getCallLogs(): Flow<List<CallLog>> {
        if (callSupabaseService?.isConfigured() == true) {
            repositoryScope.launch {
                callSupabaseService.fetchCallLogs()?.let { remoteLogs ->
                    callLogsStateFlow.value = remoteLogs
                }
            }
        }
        return callLogsStateFlow.asStateFlow()
    }

    private fun startRingingTimeout(callId: String) {
        cancelRingingTimeout()
        ringingTimeoutJob = repositoryScope.launch {
            delay(ringingTimeoutMs)
            val current = _currentCallSession.value
            val isStillRinging = current != null &&
                current.callId == callId &&
                (current.status == CallStatus.INVITING || current.status == CallStatus.RINGING)
            if (isStillRinging && current != null) {
                Timber.d("Call %s timed out after %d ms", callId, ringingTimeoutMs)
                finalizeCall(
                    current = current,
                    reason = CallEndReason.MISSED,
                    notifyRemote = true,
                )
            }
        }
    }

    private fun cancelRingingTimeout() {
        ringingTimeoutJob?.cancel()
        ringingTimeoutJob = null
    }

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

    private suspend fun finalizeCall(
        current: CallSession,
        reason: CallEndReason,
        declineMessage: String? = null,
        notifyRemote: Boolean = true,
    ) {
        cancelRingingTimeout()
        appSoundPlayer?.stopLooping()
        durationTickerJob?.cancel()
        durationTickerJob = null

        val endedSession = current.copy(
            status = CallStatus.ENDED,
            endedAtEpochMs = System.currentTimeMillis(),
            endReason = reason,
        )
        _currentCallSession.value = endedSession

        if (notifyRemote) {
            val signalType = if (reason == CallEndReason.REJECTED) SignalingType.DECLINE else SignalingType.BYE
            webRtcSignalingAdapter.sendSignaling(
                RtcSignalingMessage(
                    sessionId = current.callId,
                    senderId = currentActorId(),
                    targetId = if (current.isIncoming) current.callerId else current.calleeId,
                    type = signalType,
                ),
            )
        }

        val peerId = if (current.isIncoming) current.callerId else current.calleeId
        val peerName = if (current.isIncoming) current.callerName else current.calleeName

        val log = CallLog(
            id = current.callId,
            callId = current.callId,
            peerId = peerId,
            peerName = peerName,
            type = current.type,
            endReason = reason,
            durationSeconds = current.durationSeconds,
            timestampEpochMs = current.startedAtEpochMs ?: System.currentTimeMillis(),
            isIncoming = current.isIncoming,
            peerMoodHex = current.peerMoodHex,
        )

        callLogsStateFlow.update { logs ->
            listOf(log) + logs.filterNot { it.callId == current.callId }
        }

        callServiceCoordinator.stopCallService()
        callMediaEngine?.disconnect()
        webRtcSignalingAdapter.disconnect()

        callSupabaseService?.updateCallLog(
            callId = current.callId,
            endReason = reason,
            durationSeconds = current.durationSeconds,
            declineMessage = declineMessage,
        )
    }

    private fun connectMediaEngine(
        roomName: String,
        enableVideo: Boolean,
    ) {
        val tokenService = callTokenService ?: return
        val mediaEngine = callMediaEngine ?: return
        repositoryScope.launch {
            tokenService.fetchToken(roomName)
                .onSuccess { payload ->
                    runCatching {
                        mediaEngine.connect(
                            url = payload.resolvedUrl(),
                            token = payload.token,
                            enableVideo = enableVideo,
                        )
                    }.onFailure { error ->
                        Timber.w(error, "Failed to connect LiveKit room %s", roomName)
                    }
                }
                .onFailure { error ->
                    Timber.w(error, "Unable to obtain LiveKit token for %s", roomName)
                }
        }
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
