package com.nerojust.vela.data

import com.nerojust.vela.core.common.DispatcherProvider
import com.nerojust.vela.core.database.OutboxDao
import com.nerojust.vela.core.database.OutboxTransactionEntity
import com.nerojust.vela.core.network.InsufficientFundsException
import com.nerojust.vela.core.network.VelaApiService
import com.nerojust.vela.data.mapper.toDomain
import com.nerojust.vela.data.mapper.toRequest
import com.nerojust.vela.data.sync.SyncScheduler
import com.nerojust.vela.domain.RetryPolicy
import com.nerojust.vela.domain.model.Currency
import com.nerojust.vela.domain.model.Money
import com.nerojust.vela.domain.model.Transaction
import com.nerojust.vela.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.UUID
import javax.inject.Inject

private const val MAX_ATTEMPTS = 5

class TransactionRepositoryImpl
    @Inject
    constructor(
        private val api: VelaApiService,
        private val outboxDao: OutboxDao,
        private val retryPolicy: RetryPolicy,
        private val syncScheduler: SyncScheduler,
        private val dispatcherProvider: DispatcherProvider,
    ) : TransactionRepository {
        override fun observeTransactions(): Flow<List<Transaction>> =
            outboxDao.observeAll().map { rows -> rows.map { it.toDomain() } }

        override suspend fun submitPayment(
            amount: Money,
            destinationCurrency: Currency,
            cardId: String,
        ): Transaction =
            withContext(dispatcherProvider.io) {
                val entity =
                    OutboxTransactionEntity(
                        id = UUID.randomUUID().toString(),
                        amountMinorUnits = amount.minorUnits,
                        currency = amount.currency.name,
                        destinationCurrency = destinationCurrency.name,
                        cardId = cardId,
                        status = "PENDING",
                        failureReason = null,
                        attempts = 0,
                        createdAtEpochMillis = System.currentTimeMillis(),
                    )
                outboxDao.insert(entity)
                val updated = attemptSubmit(entity)
                updated.toDomain()
            }

        override suspend fun syncPendingTransactions(): Boolean =
            withContext(dispatcherProvider.io) {
                reviveExhaustedNetworkFailures()
                outboxDao.getPending().forEach { runCatching { attemptSubmit(it) } }
                outboxDao.getPending().isEmpty()
            }

        // Rows that hit FAILED_PERMANENT purely because the network was exhausted (not because
        // of an issuer decision like insufficient funds) are eligible for another try: reset them
        // to PENDING with a fresh attempt count so the normal pending-row sweep below picks them
        // up. This is what makes the Transactions screen's "Retry" button (which fires
        // syncPendingTransactions) actually do something for those rows.
        private suspend fun reviveExhaustedNetworkFailures() {
            outboxDao.getFailedNetworkRows().forEach { row ->
                outboxDao.update(row.copy(status = "PENDING", attempts = 0, failureReason = null))
            }
        }

        // The caught exceptions are intentionally unused: each branch only needs to bump the
        // outbox row's state (permanent failure vs. retry-with-backoff), and there's no logging
        // sink wired up in this demo worth forwarding the exception to.
        @Suppress("SwallowedException")
        private suspend fun attemptSubmit(entity: OutboxTransactionEntity): OutboxTransactionEntity {
            val updated =
                try {
                    retryPolicy.execute(isRetryable = { it is IOException }) {
                        api.submitTransaction(entity.toRequest())
                    }
                    entity.copy(status = "SYNCED")
                } catch (e: InsufficientFundsException) {
                    entity.copy(status = "FAILED_PERMANENT", failureReason = "INSUFFICIENT_FUNDS")
                } catch (e: IOException) {
                    val nextAttempts = entity.attempts + 1
                    if (nextAttempts >= MAX_ATTEMPTS) {
                        entity.copy(
                            status = "FAILED_PERMANENT",
                            failureReason = "NETWORK_UNAVAILABLE",
                            attempts = nextAttempts,
                        )
                    } else {
                        entity.copy(attempts = nextAttempts)
                    }
                }
            outboxDao.update(updated)
            if (updated.status == "PENDING") {
                syncScheduler.scheduleSync()
            }
            return updated
        }
    }
