package com.uzuu.diuchat.app.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.uzuu.diuchat.app.call.IncomingCallLaunchRequest
import com.uzuu.diuchat.app.call.IncomingCallPayloadParser
import com.uzuu.diuchat.feature.base.common.res.AppTheme
import com.uzuu.diuchat.feature.base.domain.repository.PushRegistration
import com.uzuu.diuchat.feature.contacts.domain.model.InviteDeepLinkParser
import com.uzuu.diuchat.feature.settings.domain.usecase.GetThemeModeUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.android.ext.android.inject

class MainShowcaseActivity : ComponentActivity() {
    private val getThemeModeUseCase: GetThemeModeUseCase by inject()
    private val pushRegistration: PushRegistration by inject()
    private val pendingIncomingCallFlow = MutableStateFlow<IncomingCallLaunchRequest?>(null)
    private val pendingInviteCodeFlow = MutableStateFlow<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)
        handleCallLaunchIntent(intent)
        handleInviteDeepLinkIntent(intent)

        setContent {
            val themeMode by getThemeModeUseCase().collectAsStateWithLifecycle(initialValue = "SYSTEM")
            val pendingIncomingCall by pendingIncomingCallFlow.collectAsStateWithLifecycle()
            val pendingInviteCode by pendingInviteCodeFlow.collectAsStateWithLifecycle()
            val isDarkTheme =
                when (themeMode.uppercase()) {
                    "DARK" -> true
                    "LIGHT" -> false
                    else -> isSystemInDarkTheme()
                }

            AppTheme(darkTheme = isDarkTheme) {
                MainShowcaseScreen(
                    pendingIncomingCall = pendingIncomingCall,
                    onConsumePendingIncomingCall = { pendingIncomingCallFlow.value = null },
                    pendingInviteCode = pendingInviteCode,
                    onConsumePendingInviteCode = { pendingInviteCodeFlow.value = null },
                    onCallLockScreenVisibilityChange = ::updateCallLockScreenFlags,
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleCallLaunchIntent(intent)
        handleInviteDeepLinkIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        pushRegistration.requestSync()
    }

    private fun handleCallLaunchIntent(intent: Intent?) {
        val request = IncomingCallPayloadParser.fromIntent(intent) ?: return
        if (!request.payload.isExpired()) {
            updateCallLockScreenFlags(showOnLockScreen = true)
            pendingIncomingCallFlow.value = request
        }
    }

    private fun handleInviteDeepLinkIntent(intent: Intent?) {
        val code = InviteDeepLinkParser.extractInviteCode(intent?.dataString) ?: return
        pendingInviteCodeFlow.value = code
    }

    private fun updateCallLockScreenFlags(showOnLockScreen: Boolean) {
        setShowWhenLocked(showOnLockScreen)
        setTurnScreenOn(showOnLockScreen)
    }
}
