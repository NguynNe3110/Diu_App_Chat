package com.uzuu.diuchat.feature.media.presentation.storage

import androidx.lifecycle.viewModelScope
import com.uzuu.diuchat.feature.base.presentation.viewmodel.BaseViewModel
import com.uzuu.diuchat.feature.media.domain.model.AutoDownloadPolicy
import com.uzuu.diuchat.feature.media.domain.model.ConversationStorage
import com.uzuu.diuchat.feature.media.domain.usecase.ClearCacheUseCase
import com.uzuu.diuchat.feature.media.domain.usecase.ClearConversationMediaUseCase
import com.uzuu.diuchat.feature.media.domain.usecase.GetAutoDownloadPolicyUseCase
import com.uzuu.diuchat.feature.media.domain.usecase.GetStorageStatsUseCase
import com.uzuu.diuchat.feature.media.domain.usecase.UpdateAutoDownloadPolicyUseCase
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class StorageViewModel(
    private val clearCacheUseCase: ClearCacheUseCase,
    private val clearConversationMediaUseCase: ClearConversationMediaUseCase,
    private val getAutoDownloadPolicyUseCase: GetAutoDownloadPolicyUseCase,
    private val getStorageStatsUseCase: GetStorageStatsUseCase,
    private val updateAutoDownloadPolicyUseCase: UpdateAutoDownloadPolicyUseCase,
) : BaseViewModel<StorageUiState, StorageAction>(StorageUiState()) {

    init {
        loadStorageStats()
        loadAutoDownloadPolicy()
    }

    fun loadStorageStats() {
        sendAction(StorageAction.SetLoading(true))
        getStorageStatsUseCase()
            .onEach { stats ->
                sendAction(StorageAction.SetStats(stats))
            }
            .launchIn(viewModelScope)
    }

    fun loadAutoDownloadPolicy() {
        getAutoDownloadPolicyUseCase()
            .onEach { policy ->
                sendAction(StorageAction.SetAutoDownloadPolicy(policy))
            }
            .launchIn(viewModelScope)
    }

    fun onSetAutoDownloadPolicy(policy: AutoDownloadPolicy) {
        viewModelScope.launch {
            updateAutoDownloadPolicyUseCase(policy)
            sendAction(StorageAction.SetAutoDownloadPolicy(policy))
        }
    }

    fun onToggleAutoDownloadWifi(enabled: Boolean) {
        val newPolicy = if (enabled) AutoDownloadPolicy.WIFI_ONLY else AutoDownloadPolicy.MANUAL
        onSetAutoDownloadPolicy(newPolicy)
    }

    fun onToggleAutoDownloadVoice(enabled: Boolean) {
        sendAction(StorageAction.ToggleAutoDownloadVoice(enabled))
    }

    fun onToggleKeepOriginalQuality(enabled: Boolean) {
        sendAction(StorageAction.ToggleKeepOriginalQuality(enabled))
    }

    fun onShowClearCacheDialog(show: Boolean) {
        sendAction(StorageAction.SetShowClearDialog(show))
    }

    fun onSelectConversationToClear(conversation: ConversationStorage?) {
        sendAction(StorageAction.SetSelectedConversationToClear(conversation))
    }

    fun onConfirmClearCache() {
        sendAction(StorageAction.SetClearing(true))
        viewModelScope.launch {
            clearCacheUseCase()
                .onSuccess { bytes ->
                    val sizeFormatted = ConversationStorage.formatBytes(bytes)
                    sendAction(StorageAction.CacheCleared("Đã dọn dẹp $sizeFormatted bộ nhớ đệm an toàn"))
                }
                .onFailure {
                    sendAction(StorageAction.SetClearing(false))
                }
        }
    }

    fun onConfirmClearConversationMedia() {
        val conv = uiStateFlow.value.selectedConversationToClear ?: return
        sendAction(StorageAction.SetClearingConversation(true))
        viewModelScope.launch {
            clearConversationMediaUseCase(conv.conversationId)
                .onSuccess { bytes ->
                    val sizeFormatted = ConversationStorage.formatBytes(bytes)
                    sendAction(
                        StorageAction.CacheCleared(
                            "Đã xóa $sizeFormatted tệp đệm của ${conv.contactName}",
                        ),
                    )
                }
                .onFailure {
                    sendAction(StorageAction.SetClearingConversation(false))
                }
        }
    }
}
