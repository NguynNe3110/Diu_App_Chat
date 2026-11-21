package com.uzuu.diuchat.feature.chat

import androidx.room.Room
import com.uzuu.diuchat.feature.chat.data.datasource.database.DiuChatDatabase
import com.uzuu.diuchat.feature.chat.data.repository.ChatRepositoryImpl
import com.uzuu.diuchat.feature.chat.domain.repository.ChatRepository
import com.uzuu.diuchat.feature.chat.domain.usecase.CreateGroupUseCase
import com.uzuu.diuchat.feature.chat.domain.usecase.ForwardMessageUseCase
import com.uzuu.diuchat.feature.chat.domain.usecase.GetMessagesUseCase
import com.uzuu.diuchat.feature.chat.domain.usecase.LeaveGroupUseCase
import com.uzuu.diuchat.feature.chat.domain.usecase.ManageGroupMemberUseCase
import com.uzuu.diuchat.feature.chat.domain.usecase.SendMessageUseCase
import com.uzuu.diuchat.feature.chat.domain.usecase.SendPhotoMessageUseCase
import com.uzuu.diuchat.feature.chat.domain.usecase.SendVoiceMessageUseCase
import com.uzuu.diuchat.feature.chat.domain.usecase.TogglePinMessageUseCase
import com.uzuu.diuchat.feature.chat.domain.usecase.ToggleReactionUseCase
import com.uzuu.diuchat.feature.chat.domain.usecase.UpdateGroupInfoUseCase
import com.uzuu.diuchat.feature.chat.presentation.screen.ChatViewModel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val featureChatModules =
    module {
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

        singleOf(::ChatRepositoryImpl) { bind<ChatRepository>() }
        singleOf(::CreateGroupUseCase)
        singleOf(::ForwardMessageUseCase)
        singleOf(::GetMessagesUseCase)
        singleOf(::LeaveGroupUseCase)
        singleOf(::ManageGroupMemberUseCase)
        singleOf(::SendMessageUseCase)
        singleOf(::SendPhotoMessageUseCase)
        singleOf(::SendVoiceMessageUseCase)
        singleOf(::TogglePinMessageUseCase)
        singleOf(::ToggleReactionUseCase)
        singleOf(::UpdateGroupInfoUseCase)

        viewModelOf(::ChatViewModel)
    }
