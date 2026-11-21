package com.uzuu.diuchat.feature.media.presentation.vault

import androidx.lifecycle.viewModelScope
import com.uzuu.diuchat.feature.base.presentation.viewmodel.BaseViewModel
import com.uzuu.diuchat.feature.media.domain.model.MediaType
import com.uzuu.diuchat.feature.media.domain.usecase.GetVaultMediaUseCase
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class MediaVaultViewModel(
    private val getVaultMediaUseCase: GetVaultMediaUseCase,
) : BaseViewModel<MediaVaultUiState, MediaVaultAction>(MediaVaultUiState()) {

    init {
        loadMedia()
    }

    fun loadMedia(conversationId: String = "all") {
        sendAction(MediaVaultAction.SetLoading(true))
        getVaultMediaUseCase(conversationId)
            .onEach { items ->
                sendAction(MediaVaultAction.SetItems(items))
            }
            .launchIn(viewModelScope)
    }

    fun onSelectType(type: MediaType) {
        sendAction(MediaVaultAction.SelectType(type))
    }

    fun onTogglePlayVoice(voiceId: String) {
        sendAction(MediaVaultAction.TogglePlayVoice(voiceId))
    }
}
