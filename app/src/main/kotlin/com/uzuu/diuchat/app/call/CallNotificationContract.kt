package com.uzuu.diuchat.app.call

import android.content.Intent
import com.uzuu.diuchat.feature.calls.domain.model.CallSession
import com.uzuu.diuchat.feature.calls.domain.model.CallStatus
import com.uzuu.diuchat.feature.calls.domain.model.CallType

object CallNotificationContract {
    const val CHANNEL_INCOMING_CALLS = "incoming_calls"
    const val CHANNEL_ONGOING_CALLS = "ongoing_calls"

    const val INCOMING_CALL_NOTIFICATION_ID = 2001
    const val ONGOING_CALL_NOTIFICATION_ID = 2002

    const val INCOMING_CALL_TIMEOUT_MS = 45_000L
    const val INCOMING_CALL_MAX_AGE_MS = 60_000L

    const val ACTION_INCOMING_CALL_OPEN = "com.uzuu.diuchat.action.INCOMING_CALL_OPEN"
    const val ACTION_INCOMING_CALL_ACCEPT = "com.uzuu.diuchat.action.INCOMING_CALL_ACCEPT"
    const val ACTION_INCOMING_CALL_DECLINE = "com.uzuu.diuchat.action.INCOMING_CALL_DECLINE"
    const val ACTION_ONGOING_CALL_HANGUP = "com.uzuu.diuchat.action.ONGOING_CALL_HANGUP"

    const val EXTRA_CALL_ID = "extra_call_id"
    const val EXTRA_CONVERSATION_ID = "extra_conversation_id"
    const val EXTRA_CALLER_ID = "extra_caller_id"
    const val EXTRA_CALLER_NAME = "extra_caller_name"
    const val EXTRA_CALLER_MOOD_HEX = "extra_caller_mood_hex"
    const val EXTRA_IS_VIDEO = "extra_is_video"
    const val EXTRA_SENT_AT_MS = "extra_sent_at_ms"
    const val EXTRA_AUTO_ACCEPT = "extra_auto_accept"

    const val PUSH_TYPE_INCOMING_CALL = "incoming_call"
    const val DEFAULT_CALLER_NAME = "Người thương"
    const val DEFAULT_MOOD_HEX = "#F7A3A9"
}

data class IncomingCallPayload(
    val callId: String,
    val conversationId: String,
    val callerId: String,
    val callerName: String,
    val callerMoodHex: String,
    val isVideo: Boolean,
    val sentAtEpochMs: Long,
) {
    fun isExpired(
        nowEpochMs: Long = System.currentTimeMillis(),
        maxAgeMs: Long = CallNotificationContract.INCOMING_CALL_MAX_AGE_MS,
    ): Boolean = sentAtEpochMs > 0L && (nowEpochMs - sentAtEpochMs) > maxAgeMs

    fun toCallSession(): CallSession = CallSession(
        callId = callId,
        callerId = callerId,
        callerName = callerName,
        calleeId = "user_me",
        calleeName = "Tôi",
        status = CallStatus.RINGING,
        type = if (isVideo) CallType.VIDEO else CallType.AUDIO,
        isIncoming = true,
        isSpeakerOn = isVideo,
        isVideoEnabled = isVideo,
        startedAtEpochMs = sentAtEpochMs.takeIf { it > 0L } ?: System.currentTimeMillis(),
        conversationId = conversationId,
        peerMoodHex = callerMoodHex,
    )
}

data class IncomingCallLaunchRequest(
    val payload: IncomingCallPayload,
    val autoAccept: Boolean,
)

object IncomingCallPayloadParser {
    private val hexColorRegex = Regex("^#[0-9A-Fa-f]{6}([0-9A-Fa-f]{2})?$")

    fun fromFcmData(data: Map<String, String>): IncomingCallPayload? {
        if (data["type"] != CallNotificationContract.PUSH_TYPE_INCOMING_CALL) return null
        val callId = data["call_id"]?.trim().orEmpty()
        val callerId = data["caller_id"]?.trim().orEmpty()
        if (callId.isBlank() || callerId.isBlank()) return null

        val callerName = data["caller_name"]
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: CallNotificationContract.DEFAULT_CALLER_NAME
        val moodHex = data["caller_mood_hex"]
            ?.trim()
            ?.takeIf { hexColorRegex.matches(it) }
            ?: CallNotificationContract.DEFAULT_MOOD_HEX
        val isVideo = data["is_video"].equals("true", ignoreCase = true)
        val sentAtMs = data["sent_at_ms"]?.toLongOrNull() ?: 0L

        return IncomingCallPayload(
            callId = callId,
            conversationId = data["conversation_id"]?.trim().orEmpty(),
            callerId = callerId,
            callerName = callerName,
            callerMoodHex = moodHex,
            isVideo = isVideo,
            sentAtEpochMs = sentAtMs,
        )
    }

    fun writeToIntent(
        intent: Intent,
        payload: IncomingCallPayload,
        autoAccept: Boolean = false,
    ): Intent = intent.apply {
        putExtra(CallNotificationContract.EXTRA_CALL_ID, payload.callId)
        putExtra(CallNotificationContract.EXTRA_CONVERSATION_ID, payload.conversationId)
        putExtra(CallNotificationContract.EXTRA_CALLER_ID, payload.callerId)
        putExtra(CallNotificationContract.EXTRA_CALLER_NAME, payload.callerName)
        putExtra(CallNotificationContract.EXTRA_CALLER_MOOD_HEX, payload.callerMoodHex)
        putExtra(CallNotificationContract.EXTRA_IS_VIDEO, payload.isVideo)
        putExtra(CallNotificationContract.EXTRA_SENT_AT_MS, payload.sentAtEpochMs)
        putExtra(CallNotificationContract.EXTRA_AUTO_ACCEPT, autoAccept)
    }

    fun fromIntent(intent: Intent?): IncomingCallLaunchRequest? {
        val action = intent?.action
        val isSupportedAction =
            action == CallNotificationContract.ACTION_INCOMING_CALL_OPEN ||
                action == CallNotificationContract.ACTION_INCOMING_CALL_ACCEPT
        if (intent == null || !isSupportedAction) return null

        val callId = intent.getStringExtra(CallNotificationContract.EXTRA_CALL_ID)?.trim().orEmpty()
        val callerId = intent.getStringExtra(CallNotificationContract.EXTRA_CALLER_ID)?.trim().orEmpty()
        if (callId.isBlank() || callerId.isBlank()) return null

        val callerName = intent.getStringExtra(CallNotificationContract.EXTRA_CALLER_NAME)
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: CallNotificationContract.DEFAULT_CALLER_NAME
        val moodHex = intent.getStringExtra(CallNotificationContract.EXTRA_CALLER_MOOD_HEX)
            ?.trim()
            ?.takeIf { hexColorRegex.matches(it) }
            ?: CallNotificationContract.DEFAULT_MOOD_HEX
        val isVideo = intent.getBooleanExtra(CallNotificationContract.EXTRA_IS_VIDEO, false)
        val sentAtMs = intent.getLongExtra(CallNotificationContract.EXTRA_SENT_AT_MS, 0L)
        val autoAccept =
            action == CallNotificationContract.ACTION_INCOMING_CALL_ACCEPT ||
                intent.getBooleanExtra(CallNotificationContract.EXTRA_AUTO_ACCEPT, false)

        return IncomingCallLaunchRequest(
            payload = IncomingCallPayload(
                callId = callId,
                conversationId = intent.getStringExtra(CallNotificationContract.EXTRA_CONVERSATION_ID)?.trim().orEmpty(),
                callerId = callerId,
                callerName = callerName,
                callerMoodHex = moodHex,
                isVideo = isVideo,
                sentAtEpochMs = sentAtMs,
            ),
            autoAccept = autoAccept,
        )
    }
}
