package com.uzuu.diuchat.feature.chat.data.datasource.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.uzuu.diuchat.feature.chat.data.datasource.database.entity.OutboxOperationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OutboxDao {
    @Query("SELECT * FROM outbox_operations WHERE status = :status ORDER BY createdAtEpochMs ASC")
    fun observeByStatus(status: String): Flow<List<OutboxOperationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(operation: OutboxOperationEntity)

    @Query("UPDATE outbox_operations SET status = :status, attemptCount = attemptCount + 1 WHERE id = :id")
    suspend fun updateStatus(
        id: String,
        status: String,
    )

    @Query("DELETE FROM outbox_operations WHERE id = :id")
    suspend fun delete(id: String)
}
