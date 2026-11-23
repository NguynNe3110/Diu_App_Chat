package com.uzuu.diuchat.app.data.push

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.uzuu.diuchat.app.call.CallNotificationManager
import com.uzuu.diuchat.app.call.IncomingCallPayload
import com.uzuu.diuchat.app.call.IncomingCallPayloadParser
import com.uzuu.diuchat.app.presentation.MainShowcaseActivity
import com.uzuu.diuchat.feature.base.domain.repository.PushRegistration
import com.uzuu.diuchat.feature.calls.domain.model.CallStatus
import com.uzuu.diuchat.feature.calls.domain.repository.CallRepository
import org.koin.android.ext.android.inject
import timber.log.Timber

class DiuFirebaseMessagingService : FirebaseMessagingService() {
    private val pushRegistration: PushRegistration by inject()
    private val callRepository: CallRepository by inject()

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        pushRegistration.requestSync()
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val incomingCall = IncomingCallPayloadParser.fromFcmData(message.data)
        if (incomingCall != null) {
            handleIncomingCallPush(incomingCall)
            return
        }

        handleChatMessagePush(message)
    }

    private fun handleIncomingCallPush(payload: IncomingCallPayload) {
        if (payload.isExpired()) {
            Timber.d("Ignored expired incoming call push for call %s", payload.callId)
            return
        }

        val current = callRepository.currentCallSession.value
        val isBusyOrDuplicate = current != null &&
            current.status != CallStatus.IDLE &&
            current.status != CallStatus.ENDED
        if (isBusyOrDuplicate) {
            Timber.d("Ignored incoming call push %s: another call session is already active", payload.callId)
            return
        }

        CallNotificationManager.showIncomingCallNotification(
            context = this,
            payload = payload,
        )
    }

    private fun handleChatMessagePush(message: RemoteMessage) {
        Timber.d("Received chat FCM push")

        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Timber.w("Skipping chat notification: POST_NOTIFICATIONS not granted")
            return
        }

        val openApp = Intent(this, MainShowcaseActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            openApp,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(this, "chat_messages")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(message.notification?.title ?: "Dịu")
            .setContentText(message.notification?.body ?: "Bạn có thông báo mới")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(this).notify(CHAT_NOTIFICATION_ID, notification)
    }

    private companion object {
        const val CHAT_NOTIFICATION_ID = 1001
    }
}
