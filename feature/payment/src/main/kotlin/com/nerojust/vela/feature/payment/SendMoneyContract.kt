package com.nerojust.vela.feature.payment

import com.nerojust.vela.domain.model.Card
import com.nerojust.vela.domain.model.Currency
import com.nerojust.vela.domain.model.Money
import com.nerojust.vela.domain.model.PaymentFailure
import com.nerojust.vela.domain.model.Transaction

sealed interface SendMoneyIntent {
    data class AmountChanged(val text: String) : SendMoneyIntent

    data class DestinationCurrencySelected(val currency: Currency) : SendMoneyIntent

    data class CardSelected(val cardId: String) : SendMoneyIntent

    data object SubmitClicked : SendMoneyIntent

    data object BiometricSucceeded : SendMoneyIntent

    data object BiometricFailed : SendMoneyIntent

    data object RetryClicked : SendMoneyIntent
}

sealed interface SendMoneyResult {
    data class Success(val transaction: Transaction) : SendMoneyResult

    data class Failed(val failure: PaymentFailure) : SendMoneyResult
}

data class SendMoneyUiState(
    val amountText: String = "",
    val sourceCurrency: Currency = Currency.USD,
    val destinationCurrency: Currency = Currency.EUR,
    val convertedAmount: Money? = null,
    val cards: List<Card> = emptyList(),
    val selectedCardId: String? = null,
    val isAwaitingBiometric: Boolean = false,
    val isSubmitting: Boolean = false,
    val lastResult: SendMoneyResult? = null,
)
