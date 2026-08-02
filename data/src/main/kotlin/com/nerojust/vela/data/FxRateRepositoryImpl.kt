package com.nerojust.vela.data

import com.nerojust.vela.core.network.VelaApiService
import com.nerojust.vela.domain.RetryPolicy
import com.nerojust.vela.domain.model.Currency
import com.nerojust.vela.domain.repository.FxRateRepository
import java.io.IOException
import javax.inject.Inject

class FxRateRepositoryImpl
    @Inject
    constructor(
        private val api: VelaApiService,
        private val retryPolicy: RetryPolicy,
    ) : FxRateRepository {
        override suspend fun getExchangeRate(
            from: Currency,
            to: Currency,
        ): Double =
            retryPolicy.execute(isRetryable = { it is IOException }) {
                api.getExchangeRate(from.code, to.code)
            }.rate
    }
