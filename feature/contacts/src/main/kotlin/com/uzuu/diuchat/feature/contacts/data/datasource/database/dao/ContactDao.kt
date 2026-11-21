package com.uzuu.diuchat.feature.contacts.data.datasource.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.uzuu.diuchat.feature.contacts.data.datasource.database.entity.ConnectionRequestEntity
import com.uzuu.diuchat.feature.contacts.data.datasource.database.entity.ContactEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {
    @Query("SELECT * FROM contacts ORDER BY name ASC")
    fun observeContacts(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE id = :id LIMIT 1")
    suspend fun getContactById(id: String): ContactEntity?

    @Query("SELECT * FROM contacts")
    suspend fun getAllContacts(): List<ContactEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contact: ContactEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(contacts: List<ContactEntity>)

    @Query("SELECT COUNT(*) FROM contacts")
    suspend fun getCount(): Int

    @Query("SELECT * FROM connection_requests")
    fun observeRequests(): Flow<List<ConnectionRequestEntity>>

    @Query("SELECT * FROM connection_requests WHERE id = :id LIMIT 1")
    suspend fun getRequestById(id: String): ConnectionRequestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: ConnectionRequestEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllRequests(requests: List<ConnectionRequestEntity>)

    @Query("DELETE FROM connection_requests WHERE id = :id")
    suspend fun deleteRequest(id: String)

    @Query("SELECT COUNT(*) FROM connection_requests")
    suspend fun getRequestCount(): Int

    @Transaction
    suspend fun acceptConnectionRequest(
        requestId: String,
        newContact: ContactEntity,
    ) {
        deleteRequest(requestId)
        insert(newContact)
    }
}
