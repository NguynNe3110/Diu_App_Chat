package com.uzuu.diuchat.app

import android.app.Application
import com.uzuu.diuchat.app.di.appModule
import com.uzuu.diuchat.feature.album.featureAlbumModules
import com.uzuu.diuchat.feature.auth.featureAuthModules
import com.uzuu.diuchat.feature.chat.featureChatModules
import com.uzuu.diuchat.feature.contacts.featureContactsModules
import com.uzuu.diuchat.feature.home.di.featureHomeModule
import com.uzuu.diuchat.feature.media.featureMediaModule
import com.uzuu.diuchat.feature.onboarding.featureOnboardingModules
import com.uzuu.diuchat.feature.settings.featureSettingsModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext
import timber.log.Timber

class ShowcaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        initKoin()
        initTimber()
    }

    private fun initKoin() {
        GlobalContext.startKoin {
            androidLogger()
            androidContext(this@ShowcaseApplication)

            modules(appModule)
            modules(featureAlbumModules)
            modules(featureOnboardingModules)
            modules(featureAuthModules)
            modules(featureChatModules)
            modules(featureContactsModules)
            modules(featureHomeModule)
            modules(featureMediaModule)
            modules(featureSettingsModules)
        }
    }

    private fun initTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
