package com.uzuu.diuchat.feature.calls.data

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
}

class CallServiceCoordinatorImpl : CallServiceCoordinator {
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
        internalSpeakerOn = isVideo
        internalMuted = false
        internalFrontCamera = true
        Timber.d("CallServiceCoordinator started: callId=%s, peer=%s, isVideo=%s", callId, peerName, isVideo)
    }

    override fun stopCallService() {
        Timber.d("CallServiceCoordinator stopped: callId=%s", activeCallId)
        activeCallId = null
        internalSpeakerOn = false
        internalMuted = false
        internalCameraEnabled = false
        internalFrontCamera = true
    }

    override fun setSpeakerphoneOn(on: Boolean) {
        internalSpeakerOn = on
        Timber.d("CallServiceCoordinator audio route speakerphone=%s", on)
    }

    override fun setMicrophoneMute(muted: Boolean) {
        internalMuted = muted
        Timber.d("CallServiceCoordinator microphone muted=%s", muted)
    }

    override fun setCameraEnabled(enabled: Boolean) {
        internalCameraEnabled = enabled
        Timber.d("CallServiceCoordinator camera enabled=%s", enabled)
    }

    override fun switchCamera(): Boolean {
        internalFrontCamera = !internalFrontCamera
        Timber.d("CallServiceCoordinator camera flipped to front=%s", internalFrontCamera)
        return internalFrontCamera
    }
}
