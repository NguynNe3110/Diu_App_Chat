package com.uzuu.diuchat.feature.calls.data

import com.uzuu.diuchat.feature.calls.domain.model.RtcSignalingMessage
import com.uzuu.diuchat.feature.calls.domain.model.SignalingType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class WebRtcSignalingAdapterTest {

    val sut = WebRtcSignalingAdapterImpl()

    @Test
    fun `connect emits OFFER message`() = runBlocking {
        sut.connect("session_123")
        val event = sut.signalingEvents.first()

        event.sessionId shouldBeEqualTo "session_123"
        event.type shouldBeEqualTo SignalingType.OFFER
    }

    @Test
    fun `sendSignaling emits custom signaling event`() = runBlocking {
        val msg = RtcSignalingMessage(
            sessionId = "session_xyz",
            senderId = "self",
            targetId = "peer",
            type = SignalingType.ICE_CANDIDATE,
            payload = "candidate:1",
        )
        sut.sendSignaling(msg)
        val received = sut.signalingEvents.first()

        received shouldBeEqualTo msg
    }

    @Test
    fun `disconnect emits BYE message for active session`() = runBlocking {
        sut.connect("session_bye")
        sut.signalingEvents.first()

        val result = sut.disconnect()
        val bye = sut.signalingEvents.first()

        result.isSuccess shouldBeEqualTo true
        bye.sessionId shouldBeEqualTo "session_bye"
        bye.type shouldBeEqualTo SignalingType.BYE
    }

    @Test
    fun `RtcSignalingMessage serializes and deserializes cleanly`() {
        val msg = RtcSignalingMessage(
            sessionId = "session_json",
            senderId = "peer_a",
            targetId = "peer_b",
            type = SignalingType.ANSWER,
            payload = "sdp_answer",
            timestampEpochMs = 123_456_789L,
        )
        val encoded = Json.encodeToString(msg)
        val decoded = Json.decodeFromString<RtcSignalingMessage>(encoded)

        decoded shouldBeEqualTo msg
    }
}
