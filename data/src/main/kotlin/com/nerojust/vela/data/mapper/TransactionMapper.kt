package com.nerojust.vela.data.mapper

import com.nerojust.vela.core.database.OutboxTransactionEntity
import com.nerojust.vela.core.network.dto.SubmitTransactionRequest
import com.nerojust.vela.domain.model.Currency
import com.nerojust.vela.domain.model.Money
import com.nerojust.vela.domain.model.PaymentFailure
import com.nerojust.vela.domain.model.Transaction
import com.nerojust.vela.domain.model.TransactionStatus

fun OutboxTransactionEntity.toRequest(): SubmitTransactionRequest =
    SubmitTransactionRequest(
        amountMinorUnits = amountMinorUnits,
        currency = currency,
        destinationCurrency = destinationCurrency,
        cardId = cardId,
    )

fun OutboxTransactionEntity.toDomain(): Transaction =
    Transaction(
        id = id,
        amount = Money(amountMinorUnits, Currency.valueOf(currency)),
        destinationCurrency = Currency.valueOf(destinationCurrency),
        cardId = cardId,
        status = toDomainStatus(),
        createdAtEpochMillis = createdAtEpochMillis,
    )

private fun OutboxTransactionEntity.toDomainStatus(): TransactionStatus =
    when (status) {
        "PENDING" -> TransactionStatus.Pending
        "SYNCED" -> TransactionStatus.Synced
        "FAILED_PERMANENT" -> TransactionStatus.FailedPermanent(failureReason.toPaymentFailure())
        else -> error("Unknown outbox status: $status")
    }

private fun String?.toPaymentFailure(): PaymentFailure =
    when (this) {
        "INSUFFICIENT_FUNDS" -> PaymentFailure.InsufficientFunds
        "DECLINED" -> PaymentFailure.Declined
        "NETWORK_UNAVAILABLE" -> PaymentFailure.NetworkUnavailable
        null -> PaymentFailure.Unknown("No reason recorded")
        else -> PaymentFailure.Unknown(this)
    }
