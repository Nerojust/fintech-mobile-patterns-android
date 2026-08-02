package com.nerojust.vela.feature.transactions

import com.nerojust.vela.domain.model.Transaction

sealed interface TransactionsIntent {
    data object RetryClicked : TransactionsIntent
}

data class TransactionsUiState(
    val transactions: List<Transaction> = emptyList(),
    val isSyncing: Boolean = false,
)
