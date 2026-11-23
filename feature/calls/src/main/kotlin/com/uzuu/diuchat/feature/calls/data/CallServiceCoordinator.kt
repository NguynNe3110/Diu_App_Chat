package com.uzuu.diuchat.feature.calls.data

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import timber.log.Timber

@Suppress("ComplexInterface")
interface CallServiceCoordinator {
    val isSpeakerOn: Boolean
    val isMuted: Boolean
    val isCameraEnabled: Boolean
    val isFrontCamera: Boolean

    fun startCallService(callId: String, peerName: String, isVideo: Boolean)

    fun stopCallService()

    fun setSpeakerphoneOn(on: Boolean)

    fun setMicrophoneMute(muted: Boolean)

    fun setCameraEnabled(enabled: Boolean)

    fun switchCamera(): Boolean

    fun refreshForegroundService() {
        // Default no-op for test doubles
    }
}

class CallServiceCoordinatorImpl(
    private val context: Context? = null,
    private val foregroundServiceController: CallForegroundServiceController? = null,
) : CallServiceCoordinator {
    private val audioManager: AudioManager? by lazy {
        context?.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    }

    private var internalSpeakerOn = false
    private var internalMuted = false
    private var internalCameraEnabled = false
    private var internalFrontCamera = true
    private var activeCallId: String? = null

    override val isSpeakerOn: Boolean get() = internalSpeakerOn
    override val isMuted: Boolean get() = internalMuted
    override val isCameraEnabled: Boolean get() = internalCameraEnabled
    override val isFrontCamera: Boolean get() = internalFrontCamera

    override fun startCallService(callId: String, peerName: String, isVideo: Boolean) {
        activeCallId = callId
        internalCameraEnabled = isVideo
        internalMuted = false
        internalFrontCamera = true
        runCatching {
            audioManager?.mode = AudioManager.MODE_IN_COMMUNICATION
            audioManager?.isMicrophoneMute = false
        }
        setSpeakerphoneOn(isVideo)
        foregroundServiceController?.cancelIncomingNotification()
        foregroundServiceController?.start(callId = callId, peerName = peerName, isVideo = isVideo)
        Timber.d("CallServiceCoordinator started: callId=%s, peer=%s, isVideo=%s", callId, peerName, isVideo)
    }

    override fun stopCallService() {
        Timber.d("CallServiceCoordinator stopped: callId=%s", activeCallId)
        activeCallId = null
        internalMuted = false
        internalCameraEnabled = false
        internalFrontCamera = true
        setSpeakerphoneOn(false)
        runCatching {
            audioManager?.mode = AudioManager.MODE_NORMAL
            audioManager?.isMicrophoneMute = false
        }
        foregroundServiceController?.stop()
        foregroundServiceController?.cancelIncomingNotification()
    }

    override fun setSpeakerphoneOn(on: Boolean) {
        internalSpeakerOn = on
        audioManager?.let { manager ->
            runCatching { applySpeakerRoute(manager, on) }
        }
        Timber.d("CallServiceCoordinator audio route speakerphone=%s", on)
    }

    private fun applySpeakerRoute(manager: AudioManager, on: Boolean) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            @Suppress("DEPRECATION")
            manager.isSpeakerphoneOn = on
            return
        }
        if (!on) {
            manager.clearCommunicationDevice()
            return
        }
        val speakerDevice = manager.availableCommunicationDevices
            .firstOrNull { it.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER }
        if (speakerDevice != null) {
            manager.setCommunicationDevice(speakerDevice)
        }
    }

    override fun setMicrophoneMute(muted: Boolean) {
        internalMuted = muted
        runCatching {
            audioManager?.isMicrophoneMute = muted
        }
        Timber.d("CallServiceCoordinator microphone muted=%s", muted)
    }

    override fun setCameraEnabled(enabled: Boolean) {
        internalCameraEnabled = enabled
        if (activeCallId != null) {
            foregroundServiceController?.updateVideo(enabled)
        }
        Timber.d("CallServiceCoordinator camera enabled=%s", enabled)
    }

    override fun switchCamera(): Boolean {
        internalFrontCamera = !internalFrontCamera
        Timber.d("CallServiceCoordinator camera flipped to front=%s", internalFrontCamera)
        return internalFrontCamera
    }

    override fun refreshForegroundService() {
        if (activeCallId != null) {
            foregroundServiceController?.refreshPermissions()
        }
    }
}
