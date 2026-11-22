package com.uzuu.diuchat.feature.home.domain.repository

import com.uzuu.diuchat.feature.base.domain.repository.QuietHoursStatus
import com.uzuu.diuchat.feature.home.domain.model.RecentChat
import com.uzuu.diuchat.feature.home.domain.model.StarPerson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

interface HomeRepository {
    fun getStars(isQuiet: Boolean): Flow<List<StarPerson>>

    fun getRecentChats(): Flow<List<RecentChat>>

    fun getUserDisplayName(): Flow<String> = flowOf("")

    fun observeQuietHoursStatus(): Flow<QuietHoursStatus> = flowOf(QuietHoursStatus())

    suspend fun refreshFromRemote() {}

    suspend fun clearLocalData() {}

    suspend fun deleteConversation(conversationId: String) {}
}
