package com.nerojust.vela.domain.model

data class Transaction(
    val id: String,
    val amount: Money,
    val destinationCurrency: Currency,
    val cardId: String,
    val status: TransactionStatus,
    val createdAtEpochMillis: Long,
)

sealed interface TransactionStatus {
    data object Pending : TransactionStatus

    data object Synced : TransactionStatus

    data class FailedPermanent(val reason: PaymentFailure) : TransactionStatus
}
