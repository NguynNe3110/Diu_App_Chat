package com.uzuu.diuchat.app.call

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.uzuu.diuchat.feature.calls.domain.model.CallEndReason
import com.uzuu.diuchat.feature.calls.domain.repository.CallRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext
import timber.log.Timber

class CallActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        val callId = intent.getStringExtra(CallNotificationContract.EXTRA_CALL_ID)?.trim().orEmpty()
        if (callId.isBlank()) return

        when (action) {
            CallNotificationContract.ACTION_INCOMING_CALL_DECLINE -> {
                CallNotificationManager.cancelIncomingCallNotification(context)
                val callerId = intent.getStringExtra(CallNotificationContract.EXTRA_CALLER_ID)?.trim().orEmpty()
                val callerName = intent.getStringExtra(CallNotificationContract.EXTRA_CALLER_NAME)
                    ?.trim()
                    ?.takeIf { it.isNotBlank() }
                    ?: CallNotificationContract.DEFAULT_CALLER_NAME
                val conversationId = intent.getStringExtra(CallNotificationContract.EXTRA_CONVERSATION_ID).orEmpty()
                val moodHex = intent.getStringExtra(CallNotificationContract.EXTRA_CALLER_MOOD_HEX)
                    ?.takeIf { it.isNotBlank() }
                    ?: CallNotificationContract.DEFAULT_MOOD_HEX
                val isVideo = intent.getBooleanExtra(CallNotificationContract.EXTRA_IS_VIDEO, false)
                val sentAtMs = intent.getLongExtra(CallNotificationContract.EXTRA_SENT_AT_MS, 0L)

                val pendingResult = goAsync()
                CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                    try {
                        val callRepository = GlobalContext.getOrNull()?.getOrNull<CallRepository>() ?: return@launch
                        if (callRepository.currentCallSession.value?.callId != callId && callerId.isNotBlank()) {
                            callRepository.onIncomingCallReceived(
                                IncomingCallPayload(
                                    callId = callId,
                                    conversationId = conversationId,
                                    callerId = callerId,
                                    callerName = callerName,
                                    callerMoodHex = moodHex,
                                    isVideo = isVideo,
                                    sentAtEpochMs = sentAtMs,
                                ).toCallSession(),
                            )
                        }
                        callRepository.rejectCall(callId = callId, reason = CallEndReason.REJECTED)
                    } catch (error: Throwable) {
                        Timber.w(error, "Failed to reject call %s from notification action", callId)
                    } finally {
                        pendingResult.finish()
                    }
                }
            }

            CallNotificationContract.ACTION_ONGOING_CALL_HANGUP -> {
                CallNotificationManager.cancelIncomingCallNotification(context)
                val pendingResult = goAsync()
                CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                    try {
                        val callRepository = GlobalContext.getOrNull()?.getOrNull<CallRepository>() ?: return@launch
                        callRepository.endCall(callId = callId, reason = CallEndReason.COMPLETED)
                    } catch (error: Throwable) {
                        Timber.w(error, "Failed to end ongoing call %s from notification action", callId)
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
        }
    }
}
