package com.uzuu.diuchat.feature.calls.data

import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

class CallServiceCoordinatorTest {

    val sut = CallServiceCoordinatorImpl()

    @Test
    fun `startCallService configures initial state based on video`() {
        sut.startCallService("call_1", "Linh", true)

        sut.isCameraEnabled shouldBeEqualTo true
        sut.isSpeakerOn shouldBeEqualTo true
        sut.isMuted shouldBeEqualTo false
        sut.isFrontCamera shouldBeEqualTo true
    }

    @Test
    fun `setSpeakerphoneOn and setMicrophoneMute update state`() {
        sut.setSpeakerphoneOn(true)
        sut.isSpeakerOn shouldBeEqualTo true

        sut.setMicrophoneMute(true)
        sut.isMuted shouldBeEqualTo true
    }

    @Test
    fun `switchCamera toggles between front and back`() {
        sut.isFrontCamera shouldBeEqualTo true

        val flipped = sut.switchCamera()
        flipped shouldBeEqualTo false
        sut.isFrontCamera shouldBeEqualTo false

        val flippedAgain = sut.switchCamera()
        flippedAgain shouldBeEqualTo true
        sut.isFrontCamera shouldBeEqualTo true
    }

    @Test
    fun `stopCallService resets flags to default`() {
        sut.startCallService("call_1", "Linh", true)
        sut.setMicrophoneMute(true)

        sut.stopCallService()

        sut.isSpeakerOn shouldBeEqualTo false
        sut.isMuted shouldBeEqualTo false
        sut.isCameraEnabled shouldBeEqualTo false
        sut.isFrontCamera shouldBeEqualTo true
    }
}
