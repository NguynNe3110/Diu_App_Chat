package com.uzuu.diuchat.feature.calls.data

import android.content.Context
import android.media.AudioManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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

    @Test
    fun `coordinator configures AudioManager mode and mute when context is provided`() {
        val audioManager = mockk<AudioManager>(relaxed = true)
        val context = mockk<Context> {
            every { getSystemService(Context.AUDIO_SERVICE) } returns audioManager
        }
        val coordinator = CallServiceCoordinatorImpl(context = context)

        coordinator.startCallService("call_audio", "Linh", false)
        verify { audioManager.mode = AudioManager.MODE_IN_COMMUNICATION }
        verify { audioManager.isMicrophoneMute = false }

        coordinator.setMicrophoneMute(true)
        verify { audioManager.isMicrophoneMute = true }

        coordinator.stopCallService()
        verify { audioManager.mode = AudioManager.MODE_NORMAL }
    }

    @Test
    fun `coordinator delegates foreground service lifecycle and incoming notification cleanup`() {
        val controller = mockk<CallForegroundServiceController>(relaxed = true)
        val coordinator = CallServiceCoordinatorImpl(foregroundServiceController = controller)

        coordinator.startCallService("call_fgs", "Minh", false)
        verify { controller.cancelIncomingNotification() }
        verify { controller.start("call_fgs", "Minh", false) }

        coordinator.setCameraEnabled(true)
        verify { controller.updateVideo(true) }

        coordinator.refreshForegroundService()
        verify { controller.refreshPermissions() }

        coordinator.stopCallService()
        verify { controller.stop() }
        verify(exactly = 2) { controller.cancelIncomingNotification() }
    }

    @Test
    fun `setCameraEnabled and refreshForegroundService do not trigger controller when idle`() {
        val controller = mockk<CallForegroundServiceController>(relaxed = true)
        val coordinator = CallServiceCoordinatorImpl(foregroundServiceController = controller)

        coordinator.setCameraEnabled(true)
        coordinator.refreshForegroundService()

        verify(exactly = 0) { controller.updateVideo(any()) }
        verify(exactly = 0) { controller.refreshPermissions() }
    }
}
