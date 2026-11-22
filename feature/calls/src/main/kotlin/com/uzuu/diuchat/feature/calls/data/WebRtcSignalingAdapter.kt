package com.uzuu.diuchat.feature.calls.data

import com.uzuu.diuchat.feature.calls.domain.model.RtcSignalingMessage
import com.uzuu.diuchat.feature.calls.domain.model.SignalingType
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.broadcast
import io.github.jan.supabase.realtime.broadcastFlow
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber

interface WebRtcSignalingAdapter {
    val signalingEvents: Flow<RtcSignalingMessage>

    suspend fun sendSignaling(message: RtcSignalingMessage): Result<Unit>

    suspend fun connect(sessionId: String): Result<Unit>

    suspend fun disconnect(): Result<Unit>
}

class WebRtcSignalingAdapterImpl(
    private val supabaseClient: SupabaseClient? = null,
) : WebRtcSignalingAdapter {
    private val adapterScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _signalingEvents = MutableSharedFlow<RtcSignalingMessage>(extraBufferCapacity = 64)
    override val signalingEvents: Flow<RtcSignalingMessage> = _signalingEvents.asSharedFlow()

    private var activeSessionId: String? = null
    private var activeChannel: RealtimeChannel? = null
    private var collectionJob: Job? = null

    override suspend fun sendSignaling(message: RtcSignalingMessage): Result<Unit> = runCatching {
        Timber.d("WebRtcSignaling sending type: %s for session: %s", message.type, message.sessionId)
        _signalingEvents.emit(message)
        if (supabaseClient != null) {
            runCatching {
                activeChannel?.broadcast(event = SIGNALING_EVENT, message = message)
            }.onFailure { error ->
                Timber.w(error, "Failed to broadcast signaling message %s", message.type)
            }
        }
    }

    override suspend fun connect(sessionId: String): Result<Unit> = runCatching {
        activeSessionId = sessionId
        Timber.d("WebRtcSignaling connected for session: %s", sessionId)

        val initialOffer = RtcSignalingMessage(
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
        )
        _signalingEvents.emit(initialOffer)

        val client = supabaseClient
        if (client != null) {
            collectionJob?.cancel()
            runCatching {
                val channel = client.realtime.channel("calls:$sessionId")
                activeChannel = channel
                collectionJob = adapterScope.launch {
                    channel.broadcastFlow<RtcSignalingMessage>(event = SIGNALING_EVENT)
                        .collect { incoming ->
                            _signalingEvents.emit(incoming)
                        }
                }
                channel.subscribe()
                channel.broadcast(event = SIGNALING_EVENT, message = initialOffer)
            }.onFailure { error ->
                Timber.w(error, "Failed to connect Supabase Realtime channel for session %s", sessionId)
            }
        }
    }

    override suspend fun disconnect(): Result<Unit> = runCatching {
        val sessionId = activeSessionId
        if (sessionId != null) {
            Timber.d("WebRtcSignaling disconnected for session: %s", sessionId)
            val byeMessage = RtcSignalingMessage(
                sessionId = sessionId,
                senderId = "local_peer",
                targetId = "remote_peer",
                type = SignalingType.BYE,
            )
            _signalingEvents.emit(byeMessage)

            val channel = activeChannel
            if (supabaseClient != null && channel != null) {
                runCatching {
                    channel.broadcast(event = SIGNALING_EVENT, message = byeMessage)
                }.onFailure { error ->
                    Timber.w(error, "Failed to broadcast BYE for session %s", sessionId)
                }
                collectionJob?.cancel()
                collectionJob = null
                runCatching {
                    channel.unsubscribe()
                }.onFailure { error ->
                    Timber.w(error, "Failed to unsubscribe Realtime channel for session %s", sessionId)
                }
            } else {
                collectionJob?.cancel()
                collectionJob = null
            }

            activeChannel = null
            activeSessionId = null
        }
    }

    private companion object {
        const val SIGNALING_EVENT = "webrtc_signal"
    }
}
