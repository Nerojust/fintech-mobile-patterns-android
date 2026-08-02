package com.nerojust.vela.feature.payment

import app.cash.turbine.test
import com.nerojust.vela.domain.model.Card
import com.nerojust.vela.domain.model.CardBrand
import com.nerojust.vela.domain.model.Currency
import com.nerojust.vela.domain.model.Money
import com.nerojust.vela.domain.model.Transaction
import com.nerojust.vela.domain.model.TransactionStatus
import com.nerojust.vela.domain.usecase.ConvertCurrencyUseCase
import com.nerojust.vela.domain.usecase.ListCardsUseCase
import com.nerojust.vela.domain.usecase.SendMoneyUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SendMoneyViewModelTest {
    private val sendMoney = mockk<SendMoneyUseCase>()
    private val convertCurrency = mockk<ConvertCurrencyUseCase>()
    private val listCards = mockk<ListCardsUseCase>()
    private val card = Card("card-1", CardBrand.VISA, "4242", 12, 2030, true)

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        coEvery { listCards() } returns listOf(card)
        coEvery { convertCurrency(any(), any()) } returns Money(920, Currency.EUR)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel() = SendMoneyViewModel(sendMoney, convertCurrency, listCards)

    @Test
    fun `loads cards on init and selects the first one`() =
        runTest {
            val vm = viewModel()
            vm.state.test {
                assertEquals(emptyList<Card>(), awaitItem().cards)
                val loaded = awaitItem()
                assertEquals(listOf(card), loaded.cards)
                assertEquals("card-1", loaded.selectedCardId)
            }
        }

    @Test
    fun `submit succeeds and records the result`() =
        runTest {
            val transaction =
                Transaction("txn-1", Money(1_000, Currency.USD), Currency.EUR, "card-1", TransactionStatus.Pending, 1L)
            coEvery { sendMoney(any(), any(), any()) } returns transaction

            val vm = viewModel()
            vm.state.test {
                awaitItem()
                awaitItem()
                vm.onIntent(SendMoneyIntent.AmountChanged("10.00"))
                awaitItem() // amountText updated
                awaitItem() // convertedAmount updated (async convertCurrency call completes)
                vm.onIntent(SendMoneyIntent.BiometricSucceeded)
                assertEquals(true, awaitItem().isSubmitting)
                val result = awaitItem()
                assertEquals(SendMoneyResult.Success(transaction), result.lastResult)
                assertEquals(false, result.isSubmitting)
            }
        }
}
