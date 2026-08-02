package com.nerojust.vela.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface OutboxDao {
    @Insert
    suspend fun insert(entity: OutboxTransactionEntity)

    @Update
    suspend fun update(entity: OutboxTransactionEntity)

    @Query("SELECT * FROM outbox_transactions ORDER BY createdAtEpochMillis DESC")
    fun observeAll(): Flow<List<OutboxTransactionEntity>>

    @Query("SELECT * FROM outbox_transactions WHERE status = 'PENDING'")
    suspend fun getPending(): List<OutboxTransactionEntity>

    @Query(
        "SELECT * FROM outbox_transactions WHERE status = 'FAILED_PERMANENT' AND failureReason = 'NETWORK_UNAVAILABLE'",
    )
    suspend fun getFailedNetworkRows(): List<OutboxTransactionEntity>
}
