package com.nerojust.vela.data

import com.nerojust.vela.core.network.VelaApiService
import com.nerojust.vela.core.network.dto.ExchangeRateDto
import com.nerojust.vela.domain.RetryPolicy
import com.nerojust.vela.domain.model.Currency
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

class FxRateRepositoryImplTest {
    private val api = mockk<VelaApiService>()
    private val repository = FxRateRepositoryImpl(api, RetryPolicy(maxAttempts = 2, initialDelayMillis = 1))

    @Test
    fun `getExchangeRate returns the rate from the api`() =
        runTest {
            coEvery { api.getExchangeRate("USD", "EUR") } returns ExchangeRateDto("USD", "EUR", 0.92)
            val rate = repository.getExchangeRate(Currency.USD, Currency.EUR)
            assertEquals(0.92, rate, 0.0001)
        }

    @Test
    fun `getExchangeRate retries once on a transient IOException then succeeds`() =
        runTest {
            coEvery { api.getExchangeRate("USD", "EUR") } throws
                IOException("down") andThen
                ExchangeRateDto("USD", "EUR", 0.92)
            val rate = repository.getExchangeRate(Currency.USD, Currency.EUR)
            assertEquals(0.92, rate, 0.0001)
        }
}
