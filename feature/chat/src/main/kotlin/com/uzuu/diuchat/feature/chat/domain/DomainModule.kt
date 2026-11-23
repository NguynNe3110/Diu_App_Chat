package com.uzuu.diuchat.feature.chat.domain

import com.uzuu.diuchat.feature.chat.domain.usecase.CreateGroupUseCase
import com.uzuu.diuchat.feature.chat.domain.usecase.DeleteConversationUseCase
import com.uzuu.diuchat.feature.chat.domain.usecase.DeleteMessageUseCase
import com.uzuu.diuchat.feature.chat.domain.usecase.ForwardMessageUseCase
import com.uzuu.diuchat.feature.chat.domain.usecase.GetConversationUseCase
import com.uzuu.diuchat.feature.chat.domain.usecase.GetConversationsUseCase
import com.uzuu.diuchat.feature.chat.domain.usecase.GetMessagesUseCase
import com.uzuu.diuchat.feature.chat.domain.usecase.LeaveGroupUseCase
import com.uzuu.diuchat.feature.chat.domain.usecase.LoadOlderMessagesUseCase
import com.uzuu.diuchat.feature.chat.domain.usecase.ManageGroupMemberUseCase
import com.uzuu.diuchat.feature.chat.domain.usecase.ObserveTypingStatusUseCase
import com.uzuu.diuchat.feature.chat.domain.usecase.SendMessageUseCase
import com.uzuu.diuchat.feature.chat.domain.usecase.SendPhotoMessageUseCase
import com.uzuu.diuchat.feature.chat.domain.usecase.SendSealedMessageUseCase
import com.uzuu.diuchat.feature.chat.domain.usecase.SendTypingStatusUseCase
import com.uzuu.diuchat.feature.chat.domain.usecase.SendVoiceMessageUseCase
import com.uzuu.diuchat.feature.chat.domain.usecase.StarMessageUseCase
import com.uzuu.diuchat.feature.chat.domain.usecase.TogglePinMessageUseCase
import com.uzuu.diuchat.feature.chat.domain.usecase.ToggleReactionUseCase
import com.uzuu.diuchat.feature.chat.domain.usecase.UpdateGroupInfoUseCase
import org.koin.dsl.module

val domainModule = module {
    factory { CreateGroupUseCase(get()) }
    factory { DeleteConversationUseCase(get()) }
    factory { DeleteMessageUseCase(get()) }
    factory { ForwardMessageUseCase(get()) }
    factory { GetConversationUseCase(get()) }
    factory { GetConversationsUseCase(get()) }
    factory { GetMessagesUseCase(get()) }
    factory { LeaveGroupUseCase(get()) }
    factory { LoadOlderMessagesUseCase(get()) }
    factory { ManageGroupMemberUseCase(get()) }
    factory { ObserveTypingStatusUseCase(get()) }
    factory { SendMessageUseCase(get()) }
    factory { SendPhotoMessageUseCase(get()) }
    factory { SendSealedMessageUseCase(get()) }
    factory { SendTypingStatusUseCase(get()) }
    factory { SendVoiceMessageUseCase(get()) }
    factory { StarMessageUseCase(get()) }
    factory { TogglePinMessageUseCase(get()) }
    factory { ToggleReactionUseCase(get()) }
    factory { UpdateGroupInfoUseCase(get()) }
}
