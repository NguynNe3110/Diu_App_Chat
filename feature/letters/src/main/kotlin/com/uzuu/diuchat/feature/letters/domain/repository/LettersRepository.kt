package com.uzuu.diuchat.feature.letters.domain.repository

import com.uzuu.diuchat.feature.letters.domain.model.SealedLetter
import kotlinx.coroutines.flow.Flow

interface LettersRepository {
    fun getLetters(): Flow<List<SealedLetter>>
    suspend fun sendSealedLetter(letter: SealedLetter)
    suspend fun updateSealedLetter(letter: SealedLetter)
    suspend fun openSealedLetter(letterId: String)
    suspend fun cancelSealedLetter(letterId: String)
    suspend fun sendLetterImmediately(letterId: String)
}
