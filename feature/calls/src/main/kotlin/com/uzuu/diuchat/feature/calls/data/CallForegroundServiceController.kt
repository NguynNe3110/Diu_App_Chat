package com.uzuu.diuchat.feature.calls.data

/**
 * Abstraction bridging [CallServiceCoordinator] (in `:feature:calls`) with Android's
 * foreground service and incoming-call notification lifecycle owned by `:app`.
 */
interface CallForegroundServiceController {
    fun start(callId: String, peerName: String, isVideo: Boolean)

    fun updateVideo(isVideo: Boolean)

    fun refreshPermissions()

    fun stop()

    fun cancelIncomingNotification()
}
