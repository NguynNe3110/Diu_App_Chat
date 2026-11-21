package com.uzuu.diuchat.feature.chat.domain

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
import org.koin.dsl.module

val domainModule = module {
    factory { CreateGroupUseCase(get()) }
    factory { ForwardMessageUseCase(get()) }
    factory { GetMessagesUseCase(get()) }
    factory { LeaveGroupUseCase(get()) }
    factory { ManageGroupMemberUseCase(get()) }
    factory { SendMessageUseCase(get()) }
    factory { SendPhotoMessageUseCase(get()) }
    factory { SendVoiceMessageUseCase(get()) }
    factory { TogglePinMessageUseCase(get()) }
    factory { ToggleReactionUseCase(get()) }
    factory { UpdateGroupInfoUseCase(get()) }
}
