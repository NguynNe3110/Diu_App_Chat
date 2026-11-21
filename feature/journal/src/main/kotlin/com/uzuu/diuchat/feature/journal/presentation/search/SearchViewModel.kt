package com.uzuu.diuchat.feature.journal.presentation.search

import androidx.lifecycle.viewModelScope
import com.uzuu.diuchat.feature.base.presentation.viewmodel.BaseViewModel
import com.uzuu.diuchat.feature.journal.domain.model.JournalMoodType
import com.uzuu.diuchat.feature.journal.domain.usecase.SearchMessagesUseCase
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class SearchViewModel(
    private val searchMessagesUseCase: SearchMessagesUseCase,
) : BaseViewModel<SearchUiState, SearchAction>(SearchUiState()) {

    init {
        performSearch()
    }

    fun initScope(conversationId: String?, conversationName: String?) {
        sendAction(SearchAction.SetConversationScope(conversationId, conversationName))
        performSearch()
    }

    fun onQueryChange(query: String) {
        sendAction(SearchAction.SetQuery(query))
        performSearch()
    }

    fun onSelectMood(mood: JournalMoodType?) {
        val newMood = if (uiStateFlow.value.selectedMood == mood) null else mood
        sendAction(SearchAction.SetMood(newMood))
        performSearch()
    }

    fun onSelectCategory(category: String) {
        sendAction(SearchAction.SetCategory(category))
    }

    private fun performSearch() {
        sendAction(SearchAction.SetLoading(true))
        viewModelScope.launch {
            val state = uiStateFlow.value
            searchMessagesUseCase(
                query = state.query,
                mood = state.selectedMood,
                conversationId = state.conversationId,
            )
                .catch { sendAction(SearchAction.SetError(it.message)) }
                .collect { results ->
                    sendAction(SearchAction.SetResults(results))
                }
        }
    }
}
