package com.uzuu.diuchat.feature.calls.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class SignalingType {
    OFFER,
    ANSWER,
    ICE_CANDIDATE,
    BYE,
    DECLINE,
}

@Serializable
data class RtcSignalingMessage(
    val sessionId: String,
    val senderId: String,
    val targetId: String,
    val type: SignalingType,
    val payload: String = "",
    val timestampEpochMs: Long = System.currentTimeMillis(),
)
