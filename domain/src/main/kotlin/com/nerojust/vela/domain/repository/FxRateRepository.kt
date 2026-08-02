package com.nerojust.vela.domain.repository

import com.nerojust.vela.domain.model.Currency

interface FxRateRepository {
    suspend fun getExchangeRate(
        from: Currency,
        to: Currency,
    ): Double
}
