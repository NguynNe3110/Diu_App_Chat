package com.uzuu.diuchat.app.call

import android.content.pm.ServiceInfo
import android.os.Build
import com.uzuu.diuchat.feature.calls.domain.model.CallStatus
import com.uzuu.diuchat.feature.calls.domain.model.CallType
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeNull
import org.amshove.kluent.shouldNotBeNull
import org.junit.jupiter.api.Test

class CallNotificationContractTest {

    @Test
    fun `fromFcmData parses valid incoming video call payload`() {
        val data = mapOf(
            "type" to "incoming_call",
            "call_id" to "11111111-1111-4111-8111-111111111111",
            "conversation_id" to "22222222-2222-4222-8222-222222222222",
            "caller_id" to "33333333-3333-4333-8333-333333333333",
            "caller_name" to "Minh",
            "caller_mood_hex" to "#C5B3EA",
            "is_video" to "true",
            "sent_at_ms" to "1700000000000",
        )

        val payload = IncomingCallPayloadParser.fromFcmData(data)

        payload.shouldNotBeNull()
        payload.callId shouldBeEqualTo "11111111-1111-4111-8111-111111111111"
        payload.conversationId shouldBeEqualTo "22222222-2222-4222-8222-222222222222"
        payload.callerId shouldBeEqualTo "33333333-3333-4333-8333-333333333333"
        payload.callerName shouldBeEqualTo "Minh"
        payload.callerMoodHex shouldBeEqualTo "#C5B3EA"
        payload.isVideo shouldBeEqualTo true
        payload.sentAtEpochMs shouldBeEqualTo 1700000000000L

        val session = payload.toCallSession()
        session.status shouldBeEqualTo CallStatus.RINGING
        session.type shouldBeEqualTo CallType.VIDEO
        session.isIncoming shouldBeEqualTo true
        session.isVideoEnabled shouldBeEqualTo true
        session.peerMoodHex shouldBeEqualTo "#C5B3EA"
    }

    @Test
    fun `fromFcmData rejects non-call pushes and missing identifiers`() {
        IncomingCallPayloadParser.fromFcmData(mapOf("type" to "chat_message")).shouldBeNull()
        IncomingCallPayloadParser.fromFcmData(
            mapOf("type" to "incoming_call", "call_id" to "", "caller_id" to "u1"),
        ).shouldBeNull()
        IncomingCallPayloadParser.fromFcmData(
            mapOf("type" to "incoming_call", "call_id" to "c1"),
        ).shouldBeNull()
    }

    @Test
    fun `fromFcmData sanitizes invalid mood hex and blank caller name`() {
        val payload = IncomingCallPayloadParser.fromFcmData(
            mapOf(
                "type" to "incoming_call",
                "call_id" to "call-1",
                "caller_id" to "user-1",
                "caller_name" to "   ",
                "caller_mood_hex" to "not-a-color",
                "is_video" to "false",
            ),
        )

        payload.shouldNotBeNull()
        payload.callerName shouldBeEqualTo CallNotificationContract.DEFAULT_CALLER_NAME
        payload.callerMoodHex shouldBeEqualTo CallNotificationContract.DEFAULT_MOOD_HEX
        payload.isVideo shouldBeEqualTo false
    }

    @Test
    fun `isExpired enforces max age window`() {
        val payload = IncomingCallPayload(
            callId = "call-1",
            conversationId = "conv-1",
            callerId = "caller-1",
            callerName = "Minh",
            callerMoodHex = "#F7A3A9",
            isVideo = false,
            sentAtEpochMs = 10_000L,
        )

        payload.isExpired(nowEpochMs = 55_000L, maxAgeMs = 60_000L) shouldBeEqualTo false
        payload.isExpired(nowEpochMs = 75_000L, maxAgeMs = 60_000L) shouldBeEqualTo true
    }

    @Test
    fun `resolveForegroundServiceType includes only granted permissions matching call type`() {
        OngoingCallService.resolveForegroundServiceType(
            hasMicPermission = true,
            hasCameraPermission = true,
            isVideo = true,
            sdkInt = Build.VERSION_CODES.Q,
        ) shouldBeEqualTo 0

        OngoingCallService.resolveForegroundServiceType(
            hasMicPermission = false,
            hasCameraPermission = false,
            isVideo = true,
            sdkInt = Build.VERSION_CODES.UPSIDE_DOWN_CAKE,
        ) shouldBeEqualTo 0

        OngoingCallService.resolveForegroundServiceType(
            hasMicPermission = true,
            hasCameraPermission = true,
            isVideo = false,
            sdkInt = Build.VERSION_CODES.UPSIDE_DOWN_CAKE,
        ) shouldBeEqualTo ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE

        OngoingCallService.resolveForegroundServiceType(
            hasMicPermission = true,
            hasCameraPermission = false,
            isVideo = true,
            sdkInt = Build.VERSION_CODES.UPSIDE_DOWN_CAKE,
        ) shouldBeEqualTo ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE

        OngoingCallService.resolveForegroundServiceType(
            hasMicPermission = true,
            hasCameraPermission = true,
            isVideo = true,
            sdkInt = Build.VERSION_CODES.UPSIDE_DOWN_CAKE,
        ) shouldBeEqualTo (
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE or
                ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA
            )
    }
}
