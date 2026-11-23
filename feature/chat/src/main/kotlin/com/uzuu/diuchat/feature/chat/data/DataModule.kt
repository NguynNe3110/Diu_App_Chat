package com.uzuu.diuchat.feature.chat.data

import androidx.room.Room
import com.uzuu.diuchat.feature.base.data.media.AndroidImageDownscaler
import com.uzuu.diuchat.feature.base.data.media.ImageDownscaler
import com.uzuu.diuchat.feature.chat.data.datasource.api.service.ChatSupabaseService
import com.uzuu.diuchat.feature.chat.data.datasource.database.DiuChatDatabase
import com.uzuu.diuchat.feature.chat.data.mapper.ChatMapper
import com.uzuu.diuchat.feature.chat.data.media.AndroidVoiceAudioPlayer
import com.uzuu.diuchat.feature.chat.data.media.AndroidVoiceAudioRecorder
import com.uzuu.diuchat.feature.chat.data.repository.ChatRepositoryImpl
import com.uzuu.diuchat.feature.chat.domain.media.VoiceAudioPlayer
import com.uzuu.diuchat.feature.chat.domain.media.VoiceAudioRecorder
import com.uzuu.diuchat.feature.chat.domain.repository.ChatRepository
import io.github.jan.supabase.SupabaseClient
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val dataModule = module {
    single {
        Room
            .databaseBuilder(
                get(),
                DiuChatDatabase::class.java,
                "diu_chat.db",
            ).fallbackToDestructiveMigration()
            .build()
    }

    single { get<DiuChatDatabase>().conversationDao() }
    single { get<DiuChatDatabase>().messageDao() }
    single { get<DiuChatDatabase>().outboxDao() }

    single<ChatSupabaseService> {
        ChatSupabaseService(
            supabaseClient = getOrNull<SupabaseClient>(),
        )
    }
    singleOf(::ChatMapper)
    single<ImageDownscaler> { AndroidImageDownscaler(androidContext()) }
    single<VoiceAudioRecorder> { AndroidVoiceAudioRecorder(androidContext()) }
    single<VoiceAudioPlayer> { AndroidVoiceAudioPlayer(androidContext()) }
    single<ChatRepository> {
        ChatRepositoryImpl(
            conversationDao = get(),
            messageDao = get(),
            outboxDao = get(),
            database = getOrNull<DiuChatDatabase>(),
            chatMapper = get(),
            supabaseService = get(),
            context = androidContext(),
            imageDownscaler = get(),
        )
    }
}
