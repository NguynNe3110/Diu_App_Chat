package com.uzuu.diuchat.feature.journal.presentation.search

import com.uzuu.diuchat.feature.base.presentation.viewmodel.BaseAction
import com.uzuu.diuchat.feature.journal.domain.model.JournalMoodType
import com.uzuu.diuchat.feature.journal.domain.model.SearchResultItem

sealed interface SearchAction : BaseAction<SearchUiState> {
    data class SetQuery(val query: String) : SearchAction {
        override fun reduce(state: SearchUiState): SearchUiState = state.copy(query = query)
    }

    data class SetMood(val mood: JournalMoodType?) : SearchAction {
        override fun reduce(state: SearchUiState): SearchUiState = state.copy(selectedMood = mood)
    }

    data class SetCategory(val category: String) : SearchAction {
        override fun reduce(state: SearchUiState): SearchUiState = state.copy(selectedCategory = category)
    }

    data class SetResults(val results: List<SearchResultItem>) : SearchAction {
        override fun reduce(state: SearchUiState): SearchUiState = state.copy(results = results, isLoading = false)
    }

    data class SetLoading(val isLoading: Boolean) : SearchAction {
        override fun reduce(state: SearchUiState): SearchUiState = state.copy(isLoading = isLoading)
    }

    data class SetConversationScope(
        val conversationId: String?,
        val conversationName: String?,
    ) : SearchAction {
        override fun reduce(state: SearchUiState): SearchUiState = state.copy(
            conversationId = conversationId,
            conversationName = conversationName,
        )
    }

    data class SetError(val message: String?) : SearchAction {
        override fun reduce(state: SearchUiState): SearchUiState = state.copy(errorMessage = message, isLoading = false)
    }
}
