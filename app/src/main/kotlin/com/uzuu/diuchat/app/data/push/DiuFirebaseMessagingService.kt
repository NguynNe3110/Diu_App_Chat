package com.uzuu.diuchat.app.data.push

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.uzuu.diuchat.app.presentation.MainShowcaseActivity
import com.uzuu.diuchat.feature.base.domain.repository.PushRegistration
import org.koin.android.ext.android.inject

class DiuFirebaseMessagingService : FirebaseMessagingService() {
    private val pushRegistration: PushRegistration by inject()

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        pushRegistration.requestSync()
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Log.d("DEBUG-TESTING", "Đã nhận FCM trên thiết bị")

        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w("DEBUG-TESTING", "Chưa được cấp quyền thông báo")
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
            // Icon hệ thống để thử; sau này thay bằng icon notification của Dịu.
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(message.notification?.title ?: "Dịu")
            .setContentText(message.notification?.body ?: "Bạn có thông báo mới")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(this).notify(1001, notification)
    }
}
