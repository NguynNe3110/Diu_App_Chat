package com.uzuu.diuchat.feature.contacts.domain.repository

import com.uzuu.diuchat.feature.contacts.domain.model.ConnectionRequest
import com.uzuu.diuchat.feature.contacts.domain.model.Contact
import com.uzuu.diuchat.feature.contacts.domain.model.ContactCircle
import kotlinx.coroutines.flow.Flow

interface ContactsRepository {
    fun getContacts(): Flow<List<Contact>>
    fun getPendingRequests(): Flow<List<ConnectionRequest>>
    suspend fun respondToRequest(requestId: String, accept: Boolean, circle: ContactCircle): Result<Unit>
    suspend fun searchUserByPhone(phone: String): Result<Contact?>
    fun getMyInviteLink(): Flow<String>
}
