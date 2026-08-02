package com.nerojust.vela.core.network

import com.nerojust.vela.core.network.dto.AddCardRequest
import com.nerojust.vela.core.network.dto.SubmitTransactionRequest
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class FakeVelaApiServiceTest {
    private val noLatency = NetworkConditions(simulatedLatencyMillis = 0, failureRate = 0.0)

    @Test
    fun `submitTransaction returns a synced transaction`() =
        runTest {
            val api = FakeVelaApiService(noLatency)
            val result =
                api.submitTransaction(
                    SubmitTransactionRequest(
                        amountMinorUnits = 1_000,
                        currency = "USD",
                        destinationCurrency = "EUR",
                        cardId = "card-1",
                    ),
                )
            assertEquals(1_000, result.amountMinorUnits)
            assertEquals("SYNCED", result.status)
        }

    @Test
    fun `submitTransaction above the threshold throws InsufficientFundsException`() =
        runTest {
            val api = FakeVelaApiService(noLatency)
            assertThrows(InsufficientFundsException::class.java) {
                kotlinx.coroutines.runBlocking {
                    api.submitTransaction(
                        SubmitTransactionRequest(
                            amountMinorUnits = 10_000_000,
                            currency = "USD",
                            destinationCurrency = "EUR",
                            cardId = "card-1",
                        ),
                    )
                }
            }
        }

    @Test
    fun `failureRate of 1 always throws IOException`() =
        runTest {
            val api = FakeVelaApiService(noLatency.copy(failureRate = 1.0))
            assertThrows(IOException::class.java) {
                kotlinx.coroutines.runBlocking {
                    api.getCards()
                }
            }
        }

    @Test
    fun `addCard then getCards returns the added card`() =
        runTest {
            val api = FakeVelaApiService(noLatency)
            api.addCard(AddCardRequest(brand = "VISA", last4 = "1234", expiryMonth = 1, expiryYear = 2030))
            val cards = api.getCards()
            assertTrue(cards.any { it.last4 == "1234" })
        }

    @Test
    fun `getExchangeRate returns 1_0 for identical currencies`() =
        runTest {
            val api = FakeVelaApiService(noLatency)
            val rate = api.getExchangeRate("USD", "USD")
            assertEquals(1.0, rate.rate, 0.0001)
        }
}
