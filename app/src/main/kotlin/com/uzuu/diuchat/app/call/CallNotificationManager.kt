package com.uzuu.diuchat.app.call

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.content.ContextCompat
import com.uzuu.diuchat.app.presentation.MainShowcaseActivity
import com.uzuu.diuchat.feature.base.R as BaseR
import timber.log.Timber

object CallNotificationManager {
    private const val REQUEST_CODE_OPEN_INCOMING = 2101
    private const val REQUEST_CODE_ACCEPT_INCOMING = 2102
    private const val REQUEST_CODE_DECLINE_INCOMING = 2103
    private const val REQUEST_CODE_OPEN_ONGOING = 2104
    private const val REQUEST_CODE_HANGUP_ONGOING = 2105

    private val incomingVibrationPattern = longArrayOf(0L, 600L, 400L, 600L)

    fun createCallNotificationChannels(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return

        val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val incomingChannel = NotificationChannel(
            CallNotificationContract.CHANNEL_INCOMING_CALLS,
            "Cuộc gọi đến",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Đổ chuông và hiển thị toàn màn hình khi có cuộc gọi đến"
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            enableVibration(true)
            vibrationPattern = incomingVibrationPattern
            setSound(ringtoneUri, audioAttributes)
        }

        val ongoingChannel = NotificationChannel(
            CallNotificationContract.CHANNEL_ONGOING_CALLS,
            "Cuộc gọi đang diễn ra",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "Giữ kết nối mic và camera khi bạn thu nhỏ ứng dụng trong lúc gọi"
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            setSound(null, null)
            enableVibration(false)
        }

        manager.createNotificationChannel(incomingChannel)
        manager.createNotificationChannel(ongoingChannel)
    }

    fun showIncomingCallNotification(
        context: Context,
        payload: IncomingCallPayload,
    ) {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Timber.w("Skipping incoming call notification: POST_NOTIFICATIONS not granted")
            return
        }

        val fullScreenPendingIntent = buildActivityPendingIntent(
            context = context,
            action = CallNotificationContract.ACTION_INCOMING_CALL_OPEN,
            payload = payload,
            autoAccept = false,
            requestCode = REQUEST_CODE_OPEN_INCOMING,
        )
        val acceptPendingIntent = buildActivityPendingIntent(
            context = context,
            action = CallNotificationContract.ACTION_INCOMING_CALL_ACCEPT,
            payload = payload,
            autoAccept = true,
            requestCode = REQUEST_CODE_ACCEPT_INCOMING,
        )
        val declineIntent = IncomingCallPayloadParser.writeToIntent(
            intent = Intent(context, CallActionReceiver::class.java).apply {
                action = CallNotificationContract.ACTION_INCOMING_CALL_DECLINE
            },
            payload = payload,
            autoAccept = false,
        )
        val declinePendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_DECLINE_INCOMING,
            declineIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val caller = Person.Builder()
            .setName(payload.callerName)
            .setImportant(true)
            .build()
        val subtitle = if (payload.isVideo) "Cuộc gọi video đến · Dịu" else "Cuộc gọi thoại đến · Dịu"

        val notification = NotificationCompat.Builder(context, CallNotificationContract.CHANNEL_INCOMING_CALLS)
            .setSmallIcon(BaseR.drawable.ic_callin)
            .setContentTitle(payload.callerName)
            .setContentText(subtitle)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setAutoCancel(false)
            .setTimeoutAfter(CallNotificationContract.INCOMING_CALL_TIMEOUT_MS)
            .setContentIntent(fullScreenPendingIntent)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setStyle(
                NotificationCompat.CallStyle.forIncomingCall(
                    caller,
                    declinePendingIntent,
                    acceptPendingIntent,
                ).setIsVideo(payload.isVideo),
            )
            .build()

        runCatching {
            NotificationManagerCompat.from(context).notify(
                CallNotificationContract.INCOMING_CALL_NOTIFICATION_ID,
                notification,
            )
        }.onFailure { error ->
            Timber.w(error, "Failed to post incoming call notification")
        }
    }

    fun buildOngoingCallNotification(
        context: Context,
        callId: String,
        peerName: String,
        isVideo: Boolean,
    ): Notification {
        val openAppIntent = Intent(context, MainShowcaseActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            REQUEST_CODE_OPEN_ONGOING,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val hangUpIntent = Intent(context, CallActionReceiver::class.java).apply {
            action = CallNotificationContract.ACTION_ONGOING_CALL_HANGUP
            putExtra(CallNotificationContract.EXTRA_CALL_ID, callId)
        }
        val hangUpPendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_HANGUP_ONGOING,
            hangUpIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val displayPeer = peerName.ifBlank { CallNotificationContract.DEFAULT_CALLER_NAME }
        val peer = Person.Builder()
            .setName(displayPeer)
            .setImportant(true)
            .build()
        val subtitle = if (isVideo) "Cuộc gọi video đang diễn ra" else "Cuộc gọi thoại đang diễn ra"

        return NotificationCompat.Builder(context, CallNotificationContract.CHANNEL_ONGOING_CALLS)
            .setSmallIcon(BaseR.drawable.ic_callout)
            .setContentTitle(displayPeer)
            .setContentText(subtitle)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setSilent(true)
            .setUsesChronometer(true)
            .setContentIntent(contentPendingIntent)
            .setStyle(
                NotificationCompat.CallStyle.forOngoingCall(
                    peer,
                    hangUpPendingIntent,
                ).setIsVideo(isVideo),
            )
            .build()
    }

    fun cancelIncomingCallNotification(context: Context) {
        runCatching {
            NotificationManagerCompat.from(context).cancel(CallNotificationContract.INCOMING_CALL_NOTIFICATION_ID)
        }
    }

    private fun buildActivityPendingIntent(
        context: Context,
        action: String,
        payload: IncomingCallPayload,
        autoAccept: Boolean,
        requestCode: Int,
    ): PendingIntent {
        val intent = IncomingCallPayloadParser.writeToIntent(
            intent = Intent(context, MainShowcaseActivity::class.java).apply {
                this.action = action
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            payload = payload,
            autoAccept = autoAccept,
        )
        return PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
