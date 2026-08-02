package com.nerojust.vela.domain.repository

import com.nerojust.vela.domain.model.Currency
import com.nerojust.vela.domain.model.Money
import com.nerojust.vela.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun observeTransactions(): Flow<List<Transaction>>

    suspend fun submitPayment(
        amount: Money,
        destinationCurrency: Currency,
        cardId: String,
    ): Transaction

    /** Attempts every PENDING outbox row; returns true only if none remain PENDING afterwards. */
    suspend fun syncPendingTransactions(): Boolean
}
