package com.uzuu.diuchat.feature.chat.data

import androidx.room.Room
import com.uzuu.diuchat.feature.chat.data.datasource.api.service.ChatSupabaseService
import com.uzuu.diuchat.feature.chat.data.datasource.database.DiuChatDatabase
import com.uzuu.diuchat.feature.chat.data.mapper.ChatMapper
import com.uzuu.diuchat.feature.chat.data.repository.ChatRepositoryImpl
import com.uzuu.diuchat.feature.chat.domain.repository.ChatRepository
import io.github.jan.supabase.SupabaseClient
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
    single<ChatRepository> {
        ChatRepositoryImpl(
            conversationDao = get(),
            messageDao = get(),
            outboxDao = get(),
            database = getOrNull<DiuChatDatabase>(),
            chatMapper = get(),
            supabaseService = get(),
        )
    }
}
