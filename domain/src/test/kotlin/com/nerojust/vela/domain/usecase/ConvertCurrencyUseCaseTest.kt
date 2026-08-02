package com.nerojust.vela.domain.usecase

import com.nerojust.vela.domain.model.Currency
import com.nerojust.vela.domain.model.Money
import com.nerojust.vela.domain.repository.FxRateRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

private class FakeFxRateRepository(
    private val rate: Double,
) : FxRateRepository {
    override suspend fun getExchangeRate(
        from: Currency,
        to: Currency,
    ): Double = rate
}

class ConvertCurrencyUseCaseTest {
    @Test
    fun `converts minor units using the exchange rate`() =
        runTest {
            val useCase = ConvertCurrencyUseCase(FakeFxRateRepository(rate = 0.9))
            val result = useCase(Money(1_000, Currency.USD), Currency.EUR)
            assertEquals(Currency.EUR, result.currency)
            assertEquals(900, result.minorUnits)
        }

    @Test
    fun `rounds half up instead of truncating`() =
        runTest {
            // 1_005 minorUnits * 0.5 = 502.5 exactly: truncation would yield 502, HALF_UP yields 503.
            val useCase = ConvertCurrencyUseCase(FakeFxRateRepository(rate = 0.5))
            val result = useCase(Money(1_005, Currency.USD), Currency.EUR)
            assertEquals(503, result.minorUnits)
        }
}
