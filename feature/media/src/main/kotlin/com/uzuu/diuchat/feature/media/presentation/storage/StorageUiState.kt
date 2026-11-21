package com.uzuu.diuchat.feature.media.presentation.storage

import androidx.compose.runtime.Immutable
import com.uzuu.diuchat.feature.base.presentation.viewmodel.BaseState
import com.uzuu.diuchat.feature.media.domain.model.AutoDownloadPolicy
import com.uzuu.diuchat.feature.media.domain.model.ConversationStorage
import com.uzuu.diuchat.feature.media.domain.model.StorageStats

@Immutable
data class StorageUiState(
    val isLoading: Boolean = false,
    val stats: StorageStats = StorageStats(0L, 0L, 0L, 0L),
    val autoDownloadPolicy: AutoDownloadPolicy = AutoDownloadPolicy.WIFI_ONLY,
    val autoDownloadWifi: Boolean = true,
    val autoDownloadVoice: Boolean = true,
    val keepOriginalQuality: Boolean = false,
    val showClearDialog: Boolean = false,
    val isClearing: Boolean = false,
    val selectedConversationToClear: ConversationStorage? = null,
    val showConversationClearDialog: Boolean = false,
    val isClearingConversation: Boolean = false,
    val lastClearedMessage: String? = null,
) : BaseState
