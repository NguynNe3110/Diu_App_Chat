package com.uzuu.diuchat.feature.media.presentation.storage

import androidx.lifecycle.viewModelScope
import com.uzuu.diuchat.feature.base.presentation.viewmodel.BaseViewModel
import com.uzuu.diuchat.feature.media.domain.model.ConversationStorage
import com.uzuu.diuchat.feature.media.domain.usecase.ClearCacheUseCase
import com.uzuu.diuchat.feature.media.domain.usecase.GetStorageStatsUseCase
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class StorageViewModel(
    private val clearCacheUseCase: ClearCacheUseCase,
    private val getStorageStatsUseCase: GetStorageStatsUseCase,
) : BaseViewModel<StorageUiState, StorageAction>(StorageUiState()) {

    init {
        loadStorageStats()
    }

    fun loadStorageStats() {
        sendAction(StorageAction.SetLoading(true))
        getStorageStatsUseCase()
            .onEach { stats ->
                sendAction(StorageAction.SetStats(stats))
            }
            .launchIn(viewModelScope)
    }

    fun onToggleAutoDownloadWifi(enabled: Boolean) {
        sendAction(StorageAction.ToggleAutoDownloadWifi(enabled))
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
}
