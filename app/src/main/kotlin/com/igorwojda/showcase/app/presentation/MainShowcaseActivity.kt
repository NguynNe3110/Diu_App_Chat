package com.uzuu.diuchat.app.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.uzuu.diuchat.feature.base.common.res.AppTheme

class MainShowcaseActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                MainShowcaseScreen()
            }
        }
    }
}
