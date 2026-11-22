package com.uzuu.diuchat.app

import android.app.Application
import com.uzuu.diuchat.app.di.appModule
import com.uzuu.diuchat.feature.auth.featureAuthModules
import com.uzuu.diuchat.feature.calls.featureCallsModules
import com.uzuu.diuchat.feature.chat.featureChatModules
import com.uzuu.diuchat.feature.contacts.featureContactsModules
import com.uzuu.diuchat.feature.home.di.featureHomeModule
import com.uzuu.diuchat.feature.journal.featureJournalModules
import com.uzuu.diuchat.feature.letters.featureLettersModules
import com.uzuu.diuchat.feature.location.featureLocationModule
import com.uzuu.diuchat.feature.media.featureMediaModule
import com.uzuu.diuchat.feature.onboarding.featureOnboardingModules
import com.uzuu.diuchat.feature.settings.featureSettingsModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import android.app.NotificationChannel
import android.app.NotificationManager
import com.uzuu.diuchat.app.data.push.SupabasePushRegistration
import org.koin.core.context.GlobalContext
import timber.log.Timber

class ShowcaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        createNotificationChannels()
        initKoin()
        initTimber()

        GlobalContext
            .get()
            .get<SupabasePushRegistration>()
            .start()
    }

    private fun initKoin() {
        GlobalContext.startKoin {
            androidLogger()
            androidContext(this@ShowcaseApplication)

            modules(appModule)
            modules(featureOnboardingModules)
            modules(featureAuthModules)
            modules(featureChatModules)
            modules(featureContactsModules)
            modules(featureHomeModule)
            modules(featureMediaModule)
            modules(featureSettingsModules)
            modules(featureLettersModules)
            modules(featureJournalModules)
            modules(featureCallsModules)
            modules(featureLocationModule)
        }
    }

    private fun initTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    private fun createNotificationChannels() {
        val channel = NotificationChannel(
            "chat_messages",
            "Tin nhắn",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Thông báo khi có tin nhắn mới"
        }

        getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }
}
