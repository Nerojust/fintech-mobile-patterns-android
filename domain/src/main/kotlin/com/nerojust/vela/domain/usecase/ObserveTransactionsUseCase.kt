package com.nerojust.vela.domain.usecase

import com.nerojust.vela.domain.model.Transaction
import com.nerojust.vela.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveTransactionsUseCase
    @Inject
    constructor(
        private val repository: TransactionRepository,
    ) {
        operator fun invoke(): Flow<List<Transaction>> = repository.observeTransactions()
    }
