package com.uzuu.diuchat.app.call

import android.Manifest
import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import timber.log.Timber

class OngoingCallService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopForegroundAndSelf()
                return START_NOT_STICKY
            }

            ACTION_START, ACTION_UPDATE -> {
                val callId = intent.getStringExtra(EXTRA_CALL_ID).orEmpty()
                val peerName = intent.getStringExtra(EXTRA_PEER_NAME).orEmpty()
                val isVideo = intent.getBooleanExtra(EXTRA_IS_VIDEO, false)
                promoteToForeground(callId = callId, peerName = peerName, isVideo = isVideo)
            }

            else -> {
                stopForegroundAndSelf()
            }
        }
        return START_NOT_STICKY
    }

    private fun promoteToForeground(
        callId: String,
        peerName: String,
        isVideo: Boolean,
    ) {
        val hasMic = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO,
        ) == PackageManager.PERMISSION_GRANTED
        val hasCamera = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA,
        ) == PackageManager.PERMISSION_GRANTED

        val serviceType = resolveForegroundServiceType(
            hasMicPermission = hasMic,
            hasCameraPermission = hasCamera,
            isVideo = isVideo,
        )

        // Android 14+ requires at least one matching runtime permission for microphone/camera FGS.
        // If neither is granted yet, stop cleanly and wait for refreshPermissions() after user grants.
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) && (serviceType == 0)) {
            Timber.d("OngoingCallService deferred: waiting for RECORD_AUDIO/CAMERA runtime permission")
            stopForegroundAndSelf()
            return
        }

        val notification = CallNotificationManager.buildOngoingCallNotification(
            context = this,
            callId = callId,
            peerName = peerName,
            isVideo = isVideo,
        )

        runCatching {
            ServiceCompat.startForeground(
                this,
                CallNotificationContract.ONGOING_CALL_NOTIFICATION_ID,
                notification,
                serviceType,
            )
            Timber.d("OngoingCallService foreground active (video=%s, type=%d)", isVideo, serviceType)
        }.onFailure { error ->
            Timber.w(error, "Unable to promote OngoingCallService to foreground")
            stopForegroundAndSelf()
        }
    }

    private fun stopForegroundAndSelf() {
        runCatching {
            ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        }
        stopSelf()
    }

    companion object {
        const val ACTION_START = "com.uzuu.diuchat.service.ONGOING_CALL_START"
        const val ACTION_UPDATE = "com.uzuu.diuchat.service.ONGOING_CALL_UPDATE"
        const val ACTION_STOP = "com.uzuu.diuchat.service.ONGOING_CALL_STOP"

        const val EXTRA_CALL_ID = "extra_service_call_id"
        const val EXTRA_PEER_NAME = "extra_service_peer_name"
        const val EXTRA_IS_VIDEO = "extra_service_is_video"

        @SuppressLint("InlinedApi")
        fun resolveForegroundServiceType(
            hasMicPermission: Boolean,
            hasCameraPermission: Boolean,
            isVideo: Boolean,
            sdkInt: Int = Build.VERSION.SDK_INT,
        ): Int {
            if (sdkInt < Build.VERSION_CODES.R) return 0
            var type = 0
            if (hasMicPermission) {
                type = type or ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            }
            if (isVideo && hasCameraPermission) {
                type = type or ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA
            }
            return type
        }

        fun buildStartIntent(
            context: Context,
            callId: String,
            peerName: String,
            isVideo: Boolean,
            action: String = ACTION_START,
        ): Intent = Intent(context, OngoingCallService::class.java).apply {
            this.action = action
            putExtra(EXTRA_CALL_ID, callId)
            putExtra(EXTRA_PEER_NAME, peerName)
            putExtra(EXTRA_IS_VIDEO, isVideo)
        }

        fun buildStopIntent(context: Context): Intent = Intent(context, OngoingCallService::class.java).apply {
            action = ACTION_STOP
        }
    }
}
