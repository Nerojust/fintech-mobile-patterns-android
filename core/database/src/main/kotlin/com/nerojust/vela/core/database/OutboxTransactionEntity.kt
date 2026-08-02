package com.nerojust.vela.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "outbox_transactions")
data class OutboxTransactionEntity(
    @PrimaryKey val id: String,
    val amountMinorUnits: Long,
    val currency: String,
    val destinationCurrency: String,
    val cardId: String,
    val status: String,
    val failureReason: String?,
    val attempts: Int,
    val createdAtEpochMillis: Long,
)
