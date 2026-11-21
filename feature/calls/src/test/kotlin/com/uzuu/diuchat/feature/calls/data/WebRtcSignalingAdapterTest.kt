package com.uzuu.diuchat.feature.calls.data

import com.uzuu.diuchat.feature.calls.domain.model.RtcSignalingMessage
import com.uzuu.diuchat.feature.calls.domain.model.SignalingType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
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
}
