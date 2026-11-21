package com.uzuu.diuchat.feature.home.domain.repository

import com.uzuu.diuchat.feature.home.domain.model.RecentChat
import com.uzuu.diuchat.feature.home.domain.model.StarPerson
import kotlinx.coroutines.flow.Flow

interface HomeRepository {
    fun getStars(isQuiet: Boolean): Flow<List<StarPerson>>

    fun getRecentChats(): Flow<List<RecentChat>>
}
