package com.uzuu.diuchat.feature.calls.data

import com.uzuu.diuchat.feature.calls.domain.model.RtcSignalingMessage
import com.uzuu.diuchat.feature.calls.domain.model.SignalingType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import timber.log.Timber

interface WebRtcSignalingAdapter {
    val signalingEvents: Flow<RtcSignalingMessage>

    suspend fun sendSignaling(message: RtcSignalingMessage): Result<Unit>

    suspend fun connect(sessionId: String): Result<Unit>

    suspend fun disconnect(): Result<Unit>
}

class WebRtcSignalingAdapterImpl : WebRtcSignalingAdapter {
    private val _signalingEvents = MutableSharedFlow<RtcSignalingMessage>(extraBufferCapacity = 64)
    override val signalingEvents: Flow<RtcSignalingMessage> = _signalingEvents.asSharedFlow()

    private var activeSessionId: String? = null

    override suspend fun sendSignaling(message: RtcSignalingMessage): Result<Unit> = runCatching {
        Timber.d("WebRtcSignaling sending type: %s for session: %s", message.type, message.sessionId)
        _signalingEvents.emit(message)
    }

    override suspend fun connect(sessionId: String): Result<Unit> = runCatching {
        activeSessionId = sessionId
        Timber.d("WebRtcSignaling connected for session: %s", sessionId)
        // Simulated local ICE & offer signaling message
        _signalingEvents.emit(
            RtcSignalingMessage(
                sessionId = sessionId,
                senderId = "local_peer",
                targetId = "remote_peer",
                type = SignalingType.OFFER,
                payload = """
                    v=0
                    o=- 12345 2 IN IP4 127.0.0.1
                    s=DiuCall
                    t=0 0
                """.trimIndent(),
            ),
        )
    }

    override suspend fun disconnect(): Result<Unit> = runCatching {
        val sessionId = activeSessionId
        if (sessionId != null) {
            Timber.d("WebRtcSignaling disconnected for session: %s", sessionId)
            _signalingEvents.emit(
                RtcSignalingMessage(
                    sessionId = sessionId,
                    senderId = "local_peer",
                    targetId = "remote_peer",
                    type = SignalingType.BYE,
                ),
            )
            activeSessionId = null
        }
    }
}
