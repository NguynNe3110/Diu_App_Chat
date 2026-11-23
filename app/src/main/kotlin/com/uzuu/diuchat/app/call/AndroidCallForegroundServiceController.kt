package com.uzuu.diuchat.app.call

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.uzuu.diuchat.feature.calls.data.CallForegroundServiceController
import timber.log.Timber

class AndroidCallForegroundServiceController(
    private val context: Context,
) : CallForegroundServiceController {
    @Volatile
    private var activeCallId: String? = null

    @Volatile
    private var activePeerName: String = ""

    @Volatile
    private var activeIsVideo: Boolean = false

    override fun start(callId: String, peerName: String, isVideo: Boolean) {
        activeCallId = callId
        activePeerName = peerName
        activeIsVideo = isVideo
        launchServiceIfPermitted(action = OngoingCallService.ACTION_START)
    }

    override fun updateVideo(isVideo: Boolean) {
        activeIsVideo = isVideo
        if (activeCallId != null) {
            launchServiceIfPermitted(action = OngoingCallService.ACTION_UPDATE)
        }
    }

    override fun refreshPermissions() {
        if (activeCallId != null) {
            launchServiceIfPermitted(action = OngoingCallService.ACTION_UPDATE)
        }
    }

    override fun stop() {
        activeCallId = null
        activePeerName = ""
        activeIsVideo = false
        runCatching {
            context.stopService(OngoingCallService.buildStopIntent(context))
        }.onFailure { error ->
            Timber.w(error, "Failed to stop OngoingCallService")
        }
    }

    override fun cancelIncomingNotification() {
        CallNotificationManager.cancelIncomingCallNotification(context)
    }

    private fun launchServiceIfPermitted(action: String) {
        val callId = activeCallId ?: return
        val hasMic = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO,
        ) == PackageManager.PERMISSION_GRANTED
        val hasCamera = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA,
        ) == PackageManager.PERMISSION_GRANTED

        val resolvedType = OngoingCallService.resolveForegroundServiceType(
            hasMicPermission = hasMic,
            hasCameraPermission = hasCamera,
            isVideo = activeIsVideo,
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE && resolvedType == 0) {
            Timber.d("Deferring OngoingCallService launch until RECORD_AUDIO/CAMERA permission is granted")
            return
        }

        val intent = OngoingCallService.buildStartIntent(
            context = context,
            callId = callId,
            peerName = activePeerName,
            isVideo = activeIsVideo,
            action = action,
        )
        runCatching {
            ContextCompat.startForegroundService(context, intent)
        }.onFailure { error ->
            Timber.w(error, "Unable to start OngoingCallService")
        }
    }
}
