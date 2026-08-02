package com.nerojust.vela.domain.usecase

import com.nerojust.vela.domain.repository.TransactionRepository
import javax.inject.Inject

class RetrySyncUseCase
    @Inject
    constructor(
        private val repository: TransactionRepository,
    ) {
        suspend operator fun invoke(): Boolean = repository.syncPendingTransactions()
    }
