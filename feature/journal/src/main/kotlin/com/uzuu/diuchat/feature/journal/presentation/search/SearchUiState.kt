package com.uzuu.diuchat.feature.journal.presentation.search

import com.uzuu.diuchat.feature.base.presentation.viewmodel.BaseState
import com.uzuu.diuchat.feature.journal.domain.model.JournalMoodType
import com.uzuu.diuchat.feature.journal.domain.model.SearchResultItem

data class SearchUiState(
    val query: String = "",
    val selectedMood: JournalMoodType? = null,
    val selectedCategory: String = "all",
    val conversationId: String? = null,
    val conversationName: String? = null,
    val results: List<SearchResultItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
) : BaseState
