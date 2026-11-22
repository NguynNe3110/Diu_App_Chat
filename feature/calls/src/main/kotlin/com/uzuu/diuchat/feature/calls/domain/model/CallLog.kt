package com.uzuu.diuchat.feature.calls.domain.model

data class CallLog(
    val id: String,
    val callId: String,
    val peerId: String,
    val peerName: String,
    val type: CallType,
    val endReason: CallEndReason,
    val durationSeconds: Long,
    val timestampEpochMs: Long,
    val isIncoming: Boolean,
    val peerMoodHex: String = "#F7A3A9",
    val peerAvatarUrl: String? = null,
)
