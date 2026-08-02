package com.nerojust.vela.feature.cards

import app.cash.turbine.test
import com.nerojust.vela.domain.model.Card
import com.nerojust.vela.domain.model.CardBrand
import com.nerojust.vela.domain.usecase.AddCardUseCase
import com.nerojust.vela.domain.usecase.ListCardsUseCase
import com.nerojust.vela.domain.usecase.RemoveCardUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class CardsViewModelTest {
    private val listCards = mockk<ListCardsUseCase>()
    private val addCard = mockk<AddCardUseCase>()
    private val removeCard = mockk<RemoveCardUseCase>()
    private val card = Card("card-1", CardBrand.VISA, "4242", 12, 2030, true)

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        coEvery { listCards() } returns listOf(card)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `LoadCards populates state`() =
        runTest {
            val vm = CardsViewModel(listCards, addCard, removeCard)
            vm.state.test {
                assertEquals(emptyList<Card>(), awaitItem().cards)
                vm.onIntent(CardsIntent.LoadCards)
                assertEquals(true, awaitItem().isLoading)
                val loaded = awaitItem()
                assertEquals(listOf(card), loaded.cards)
                assertEquals(false, loaded.isLoading)
            }
        }

    @Test
    fun `RemoveCard delegates to the use case`() =
        runTest {
            coEvery { removeCard("card-1") } returns Unit
            val vm = CardsViewModel(listCards, addCard, removeCard)
            vm.onIntent(CardsIntent.RemoveCard("card-1"))
            advanceUntilIdle()
            coVerify { removeCard("card-1") }
        }
}
