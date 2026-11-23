package com.uzuu.diuchat.feature.calls.data

import android.content.Context
import io.livekit.android.LiveKit
import io.livekit.android.events.RoomEvent
import io.livekit.android.events.collect
import io.livekit.android.renderer.SurfaceViewRenderer
import io.livekit.android.room.Room
import io.livekit.android.room.track.CameraPosition
import io.livekit.android.room.track.LocalVideoTrack
import io.livekit.android.room.track.Track
import io.livekit.android.room.track.VideoTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

interface CallMediaEngine {
    val localVideoTrack: StateFlow<VideoTrack?>
    val remoteVideoTrack: StateFlow<VideoTrack?>

    suspend fun connect(
        url: String,
        token: String,
        enableVideo: Boolean,
    )

    suspend fun setMicrophoneMuted(muted: Boolean)

    suspend fun setCameraEnabled(enabled: Boolean)

    suspend fun switchCamera(): Boolean

    fun initVideoRenderer(renderer: SurfaceViewRenderer)

    suspend fun disconnect()
}

class LiveKitCallMediaEngine(
    private val context: Context?,
) : CallMediaEngine {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var room: Room? = null
    private var activeRoomName: String? = null
    private var eventsJob: Job? = null
    private var isFrontCamera = true

    private val _localVideoTrack = MutableStateFlow<VideoTrack?>(null)
    override val localVideoTrack: StateFlow<VideoTrack?> = _localVideoTrack.asStateFlow()

    private val _remoteVideoTrack = MutableStateFlow<VideoTrack?>(null)
    override val remoteVideoTrack: StateFlow<VideoTrack?> = _remoteVideoTrack.asStateFlow()

    @Suppress("TooGenericExceptionCaught")
    override suspend fun connect(
        url: String,
        token: String,
        enableVideo: Boolean,
    ) {
        val appContext = context?.applicationContext ?: return
        val existingRoom = room
        val isAlreadyConnected = existingRoom != null &&
            activeRoomName == existingRoom.name &&
            existingRoom.state == Room.State.CONNECTED
        if (isAlreadyConnected) {
            reuseConnectedRoom(existingRoom, enableVideo)
            return
        }
        disconnect()

        val lkRoom = LiveKit.create(appContext)
        room = lkRoom
        isFrontCamera = true
        eventsJob = observeRoomEvents(lkRoom)

        try {
            lkRoom.connect(url = url, token = token)
            activeRoomName = lkRoom.name
            refreshRemoteVideoTrack(lkRoom)
            enableInitialLocalMedia(lkRoom, enableVideo)
            Timber.d("LiveKit connected to room %s (video=%s)", lkRoom.name, enableVideo)
        } catch (error: Throwable) {
            Timber.w(error, "LiveKit room connect failed")
            disconnect()
            throw error
        }
    }

    private suspend fun reuseConnectedRoom(
        existingRoom: Room,
        enableVideo: Boolean,
    ) {
        runCatching { existingRoom.localParticipant.setMicrophoneEnabled(true) }
        if (enableVideo) {
            runCatching {
                existingRoom.localParticipant.setCameraEnabled(true)
                refreshLocalVideoTrack(existingRoom)
            }
        }
    }

    private fun observeRoomEvents(lkRoom: Room): Job = scope.launch {
        lkRoom.events.collect { event ->
            handleRoomEvent(lkRoom, event)
        }
    }

    private fun handleRoomEvent(
        lkRoom: Room,
        event: RoomEvent,
    ) {
        when (event) {
            is RoomEvent.TrackPublished -> {
                if (isLocalCameraPublication(lkRoom, event.participant, event.publication.source)) {
                    refreshLocalVideoTrack(lkRoom)
                }
            }

            is RoomEvent.TrackUnpublished -> {
                if (isLocalCameraPublication(lkRoom, event.participant, event.publication.source)) {
                    _localVideoTrack.value = null
                }
            }

            is RoomEvent.TrackSubscribed -> {
                val track = event.track as? VideoTrack
                if (track != null) {
                    _remoteVideoTrack.value = track
                }
            }

            is RoomEvent.TrackUnsubscribed -> {
                if (event.track is VideoTrack && _remoteVideoTrack.value == event.track) {
                    _remoteVideoTrack.value = null
                }
            }

            is RoomEvent.Disconnected -> {
                _localVideoTrack.value = null
                _remoteVideoTrack.value = null
            }

            else -> {
            }
        }
    }

    private fun isLocalCameraPublication(
        lkRoom: Room,
        participant: Any?,
        source: Track.Source,
    ): Boolean = participant == lkRoom.localParticipant && source == Track.Source.CAMERA

    private suspend fun enableInitialLocalMedia(
        lkRoom: Room,
        enableVideo: Boolean,
    ) {
        runCatching {
            lkRoom.localParticipant.setMicrophoneEnabled(true)
        }.onFailure { error ->
            Timber.w(error, "Unable to enable microphone immediately on connect (awaiting permission)")
        }
        if (enableVideo) {
            runCatching {
                lkRoom.localParticipant.setCameraEnabled(true)
                refreshLocalVideoTrack(lkRoom)
            }.onFailure { error ->
                Timber.w(error, "Unable to enable camera immediately on connect (awaiting permission)")
            }
        }
    }

    override suspend fun setMicrophoneMuted(muted: Boolean) {
        runCatching {
            room?.localParticipant?.setMicrophoneEnabled(!muted)
        }
    }

    override suspend fun setCameraEnabled(enabled: Boolean) {
        val activeRoom = room ?: return
        runCatching {
            activeRoom.localParticipant.setCameraEnabled(enabled)
            if (enabled) {
                refreshLocalVideoTrack(activeRoom)
            } else {
                _localVideoTrack.value = null
            }
        }
    }

    override suspend fun switchCamera(): Boolean {
        val activeRoom = room ?: return !isFrontCamera
        isFrontCamera = !isFrontCamera
        val targetPos = if (isFrontCamera) CameraPosition.FRONT else CameraPosition.BACK
        val localTrack = activeRoom.localParticipant
            .getTrackPublication(Track.Source.CAMERA)
            ?.track as? LocalVideoTrack
        localTrack?.switchCamera(position = targetPos)
        return isFrontCamera
    }

    override fun initVideoRenderer(renderer: SurfaceViewRenderer) {
        runCatching {
            room?.initVideoRenderer(renderer)
        }
    }

    override suspend fun disconnect() {
        eventsJob?.cancel()
        eventsJob = null
        _localVideoTrack.value = null
        _remoteVideoTrack.value = null
        runCatching {
            room?.disconnect()
            room?.release()
        }
        room = null
        activeRoomName = null
    }

    private fun refreshLocalVideoTrack(activeRoom: Room) {
        val track = activeRoom.localParticipant
            .getTrackPublication(Track.Source.CAMERA)
            ?.track as? VideoTrack
        _localVideoTrack.value = track
    }

    private fun refreshRemoteVideoTrack(activeRoom: Room) {
        val existingRemoteTrack = activeRoom.remoteParticipants.values
            .asSequence()
            .mapNotNull { participant ->
                participant.getTrackPublication(Track.Source.CAMERA)?.track as? VideoTrack
            }.firstOrNull()
        if (existingRemoteTrack != null) {
            _remoteVideoTrack.value = existingRemoteTrack
        }
    }
}
