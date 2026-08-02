package com.nerojust.vela.feature.transactions

import app.cash.turbine.test
import com.nerojust.vela.domain.model.Currency
import com.nerojust.vela.domain.model.Money
import com.nerojust.vela.domain.model.Transaction
import com.nerojust.vela.domain.model.TransactionStatus
import com.nerojust.vela.domain.usecase.ObserveTransactionsUseCase
import com.nerojust.vela.domain.usecase.RetrySyncUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionsViewModelTest {
    private val observeTransactions = mockk<ObserveTransactionsUseCase>()
    private val retrySync = mockk<RetrySyncUseCase>()
    private val transaction =
        Transaction("txn-1", Money(1_000, Currency.USD), Currency.EUR, "card-1", TransactionStatus.Pending, 1L)

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `state reflects the observed transaction list`() =
        runTest {
            every { observeTransactions() } returns flowOf(listOf(transaction))
            val vm = TransactionsViewModel(observeTransactions, retrySync)
            advanceUntilIdle()
            vm.state.test {
                assertEquals(listOf(transaction), awaitItem().transactions)
            }
        }

    @Test
    fun `RetryClicked calls the retry use case`() =
        runTest {
            every { observeTransactions() } returns flowOf(emptyList())
            coEvery { retrySync() } returns true
            val vm = TransactionsViewModel(observeTransactions, retrySync)
            vm.onIntent(TransactionsIntent.RetryClicked)
            advanceUntilIdle()
            coVerify { retrySync() }
        }
}
