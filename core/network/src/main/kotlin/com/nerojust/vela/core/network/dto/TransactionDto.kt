package com.nerojust.vela.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class SubmitTransactionRequest(
    val amountMinorUnits: Long,
    val currency: String,
    val destinationCurrency: String,
    val cardId: String,
)

@Serializable
data class TransactionDto(
    val id: String,
    val amountMinorUnits: Long,
    val currency: String,
    val destinationCurrency: String,
    val cardId: String,
    val status: String,
    val createdAtEpochMillis: Long,
)
