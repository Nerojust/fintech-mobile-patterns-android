package com.nerojust.vela.data

import com.nerojust.vela.core.common.DefaultDispatcherProvider
import com.nerojust.vela.core.database.OutboxDao
import com.nerojust.vela.core.database.OutboxTransactionEntity
import com.nerojust.vela.core.network.InsufficientFundsException
import com.nerojust.vela.core.network.VelaApiService
import com.nerojust.vela.core.network.dto.TransactionDto
import com.nerojust.vela.data.sync.SyncScheduler
import com.nerojust.vela.domain.RetryPolicy
import com.nerojust.vela.domain.model.Currency
import com.nerojust.vela.domain.model.Money
import com.nerojust.vela.domain.model.TransactionStatus
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class TransactionRepositoryImplTest {
    private val api = mockk<VelaApiService>()
    private val dao = mockk<OutboxDao>(relaxed = true)
    private val syncScheduler = mockk<SyncScheduler>(relaxed = true)
    private val repository =
        TransactionRepositoryImpl(
            api = api,
            outboxDao = dao,
            retryPolicy = RetryPolicy(maxAttempts = 2, initialDelayMillis = 1),
            syncScheduler = syncScheduler,
            dispatcherProvider = DefaultDispatcherProvider(),
        )

    @Test
    fun `submitPayment inserts a pending row then marks it synced on success`() =
        runTest {
            coEvery { api.submitTransaction(any()) } returns
                TransactionDto(
                    id = "ignored",
                    amountMinorUnits = 1_000,
                    currency = "USD",
                    destinationCurrency = "EUR",
                    cardId = "card-1",
                    status = "SYNCED",
                    createdAtEpochMillis = 1L,
                )

            repository.submitPayment(Money(1_000, Currency.USD), Currency.EUR, "card-1")

            val updated = slot<OutboxTransactionEntity>()
            coVerify { dao.insert(any()) }
            coVerify { dao.update(capture(updated)) }
            assertEquals("SYNCED", updated.captured.status)
        }

    @Test
    fun `submitPayment leaves the row pending after transient failures exhaust retries`() =
        runTest {
            coEvery { api.submitTransaction(any()) } throws IOException("down")

            repository.submitPayment(Money(1_000, Currency.USD), Currency.EUR, "card-1")

            val updated = slot<OutboxTransactionEntity>()
            coVerify { dao.update(capture(updated)) }
            assertEquals("PENDING", updated.captured.status)
            assertEquals(1, updated.captured.attempts)
        }

    @Test
    fun `submitPayment marks the row failed permanent on insufficient funds without retrying`() =
        runTest {
            coEvery { api.submitTransaction(any()) } throws InsufficientFundsException()

            repository.submitPayment(Money(10_000_000, Currency.USD), Currency.EUR, "card-1")

            val updated = slot<OutboxTransactionEntity>()
            coVerify(exactly = 1) { api.submitTransaction(any()) }
            coVerify { dao.update(capture(updated)) }
            assertEquals("FAILED_PERMANENT", updated.captured.status)
            assertEquals("INSUFFICIENT_FUNDS", updated.captured.failureReason)
        }

    @Test
    fun `syncPendingTransactions returns true once no pending rows remain`() =
        runTest {
            coEvery { dao.getPending() } returns emptyList()
            assertTrue(repository.syncPendingTransactions())
        }

    @Test
    fun `syncPendingTransactions moves a row to FAILED_PERMANENT once the attempt cap is exceeded`() =
        runTest {
            val row =
                OutboxTransactionEntity(
                    id = "txn-1",
                    amountMinorUnits = 1_000,
                    currency = "USD",
                    destinationCurrency = "EUR",
                    cardId = "card-1",
                    status = "PENDING",
                    failureReason = null,
                    attempts = 4,
                    createdAtEpochMillis = 1L,
                )
            coEvery { dao.getFailedNetworkRows() } returns emptyList()
            coEvery { dao.getPending() } returns listOf(row)
            coEvery { api.submitTransaction(any()) } throws IOException("down")

            repository.syncPendingTransactions()

            val updated = slot<OutboxTransactionEntity>()
            coVerify { dao.update(capture(updated)) }
            assertEquals("FAILED_PERMANENT", updated.captured.status)
            assertEquals("NETWORK_UNAVAILABLE", updated.captured.failureReason)
            assertEquals(5, updated.captured.attempts)
        }

    @Test
    fun `syncPendingTransactions revives an expired network failure and re-attempts it`() =
        runTest {
            val staleRow =
                OutboxTransactionEntity(
                    id = "txn-1",
                    amountMinorUnits = 1_000,
                    currency = "USD",
                    destinationCurrency = "EUR",
                    cardId = "card-1",
                    status = "FAILED_PERMANENT",
                    failureReason = "NETWORK_UNAVAILABLE",
                    attempts = 5,
                    createdAtEpochMillis = 1L,
                )
            val revivedRow = staleRow.copy(status = "PENDING", attempts = 0, failureReason = null)
            coEvery { dao.getFailedNetworkRows() } returns listOf(staleRow)
            coEvery { dao.getPending() } returns listOf(revivedRow)
            coEvery { api.submitTransaction(any()) } returns
                TransactionDto(
                    id = "ignored",
                    amountMinorUnits = 1_000,
                    currency = "USD",
                    destinationCurrency = "EUR",
                    cardId = "card-1",
                    status = "SYNCED",
                    createdAtEpochMillis = 1L,
                )

            repository.syncPendingTransactions()

            coVerify { dao.update(revivedRow) }
            coVerify(exactly = 1) { api.submitTransaction(any()) }
            val updates = mutableListOf<OutboxTransactionEntity>()
            coVerify { dao.update(capture(updates)) }
            assertEquals("SYNCED", updates.last().status)
        }

    @Test
    fun `syncPendingTransactions does not let one failing row stop the rest of the batch`() =
        runTest {
            val badRow =
                OutboxTransactionEntity(
                    id = "txn-bad",
                    amountMinorUnits = 1_000,
                    currency = "BOGUS",
                    destinationCurrency = "EUR",
                    cardId = "card-1",
                    status = "PENDING",
                    failureReason = null,
                    attempts = 0,
                    createdAtEpochMillis = 1L,
                )
            val goodRow =
                OutboxTransactionEntity(
                    id = "txn-good",
                    amountMinorUnits = 1_000,
                    currency = "USD",
                    destinationCurrency = "EUR",
                    cardId = "card-1",
                    status = "PENDING",
                    failureReason = null,
                    attempts = 0,
                    createdAtEpochMillis = 1L,
                )
            coEvery { dao.getFailedNetworkRows() } returns emptyList()
            coEvery { dao.getPending() } returns listOf(badRow, goodRow)
            coEvery { api.submitTransaction(match { it.currency == "BOGUS" }) } throws
                IllegalStateException("corrupt row")
            coEvery { api.submitTransaction(match { it.currency == "USD" }) } returns
                TransactionDto(
                    id = "ignored",
                    amountMinorUnits = 1_000,
                    currency = "USD",
                    destinationCurrency = "EUR",
                    cardId = "card-1",
                    status = "SYNCED",
                    createdAtEpochMillis = 1L,
                )

            repository.syncPendingTransactions()

            coVerify { dao.update(match { it.id == "txn-good" && it.status == "SYNCED" }) }
        }

    @Test
    fun `observeTransactions maps outbox rows to domain transactions`() =
        runTest {
            val row =
                OutboxTransactionEntity(
                    id = "txn-1",
                    amountMinorUnits = 500,
                    currency = "USD",
                    destinationCurrency = "EUR",
                    cardId = "card-1",
                    status = "PENDING",
                    failureReason = null,
                    attempts = 0,
                    createdAtEpochMillis = 1L,
                )
            every { dao.observeAll() } returns flowOf(listOf(row))
            val result = repository.observeTransactions()
            val transactions = result.first()
            assertEquals(TransactionStatus.Pending, transactions.first().status)
        }
}
