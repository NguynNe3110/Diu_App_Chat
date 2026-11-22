package com.uzuu.diuchat.app.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.uzuu.diuchat.feature.base.common.res.AppTheme
import com.uzuu.diuchat.feature.base.domain.repository.PushRegistration
import com.uzuu.diuchat.feature.settings.domain.usecase.GetThemeModeUseCase
import org.koin.android.ext.android.inject

class MainShowcaseActivity : ComponentActivity() {
    private val getThemeModeUseCase: GetThemeModeUseCase by inject()
    private val pushRegistration: PushRegistration by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setContent {
            val themeMode by getThemeModeUseCase().collectAsStateWithLifecycle(initialValue = "SYSTEM")
            val isDarkTheme =
                when (themeMode.uppercase()) {
                    "DARK" -> true
                    "LIGHT" -> false
                    else -> isSystemInDarkTheme()
                }

            AppTheme(darkTheme = isDarkTheme) {
                MainShowcaseScreen()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        pushRegistration.requestSync()
    }
}
