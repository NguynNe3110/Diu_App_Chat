package com.uzuu.diuchat.feature.calls.domain.model

data class CallSession(
    val callId: String,
    val callerId: String,
    val callerName: String,
    val calleeId: String,
    val calleeName: String,
    val status: CallStatus = CallStatus.IDLE,
    val type: CallType = CallType.AUDIO,
    val isIncoming: Boolean = false,
    val isAudioMuted: Boolean = false,
    val isSpeakerOn: Boolean = false,
    val isVideoEnabled: Boolean = false,
    val isFrontCamera: Boolean = true,
    val durationSeconds: Long = 0L,
    val startedAtEpochMs: Long? = null,
    val endedAtEpochMs: Long? = null,
    val endReason: CallEndReason? = null,
    val conversationId: String = "",
    val peerMoodHex: String = "#F7A3A9",
)
