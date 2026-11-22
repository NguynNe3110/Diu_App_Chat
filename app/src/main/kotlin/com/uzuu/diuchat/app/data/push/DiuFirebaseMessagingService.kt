package com.uzuu.diuchat.app.data.push

import com.google.firebase.messaging.FirebaseMessagingService
import com.uzuu.diuchat.feature.base.domain.repository.PushRegistration
import org.koin.android.ext.android.inject

class DiuFirebaseMessagingService : FirebaseMessagingService() {
    private val pushRegistration: PushRegistration by inject()

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        pushRegistration.requestSync()
    }
}
