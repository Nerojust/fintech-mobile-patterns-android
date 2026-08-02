package com.nerojust.vela.domain.usecase

import com.nerojust.vela.domain.model.Currency
import com.nerojust.vela.domain.model.Money
import com.nerojust.vela.domain.model.Transaction
import com.nerojust.vela.domain.repository.TransactionRepository
import javax.inject.Inject

class SendMoneyUseCase
    @Inject
    constructor(
        private val repository: TransactionRepository,
    ) {
        suspend operator fun invoke(
            amount: Money,
            destinationCurrency: Currency,
            cardId: String,
        ): Transaction = repository.submitPayment(amount, destinationCurrency, cardId)
    }
