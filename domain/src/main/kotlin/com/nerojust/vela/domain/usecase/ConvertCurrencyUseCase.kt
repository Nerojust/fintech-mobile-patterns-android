package com.nerojust.vela.domain.usecase

import com.nerojust.vela.domain.model.Currency
import com.nerojust.vela.domain.model.Money
import com.nerojust.vela.domain.repository.FxRateRepository
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

class ConvertCurrencyUseCase
    @Inject
    constructor(
        private val repository: FxRateRepository,
    ) {
        suspend operator fun invoke(
            amount: Money,
            to: Currency,
        ): Money {
            val rate = repository.getExchangeRate(amount.currency, to)
            val minorUnits =
                BigDecimal(amount.minorUnits)
                    .multiply(BigDecimal.valueOf(rate))
                    .setScale(0, RoundingMode.HALF_UP)
                    .toLong()
            return Money(minorUnits, to)
        }
    }
