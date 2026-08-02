package com.nerojust.vela.feature.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nerojust.vela.domain.model.PaymentFailure
import com.nerojust.vela.domain.model.TransactionStatus
import com.nerojust.vela.domain.usecase.ConvertCurrencyUseCase
import com.nerojust.vela.domain.usecase.ListCardsUseCase
import com.nerojust.vela.domain.usecase.SendMoneyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.RoundingMode
import javax.inject.Inject

@HiltViewModel
class SendMoneyViewModel
    @Inject
    constructor(
        private val sendMoney: SendMoneyUseCase,
        private val convertCurrency: ConvertCurrencyUseCase,
        private val listCards: ListCardsUseCase,
    ) : ViewModel() {
        private val _state = MutableStateFlow(SendMoneyUiState())
        val state: StateFlow<SendMoneyUiState> = _state.asStateFlow()

        init {
            viewModelScope.launch {
                // The fake backend injects transient failures even after CardRepositoryImpl's
                // own retries are exhausted — never let that crash the screen on load.
                runCatching { listCards() }.onSuccess { cards ->
                    _state.update { it.copy(cards = cards, selectedCardId = cards.firstOrNull()?.id) }
                }
            }
        }

        fun onIntent(intent: SendMoneyIntent) {
            when (intent) {
                is SendMoneyIntent.AmountChanged -> onAmountChanged(intent.text)
                is SendMoneyIntent.DestinationCurrencySelected ->
                    _state.update {
                        it.copy(
                            destinationCurrency = intent.currency,
                        )
                    }
                is SendMoneyIntent.CardSelected -> _state.update { it.copy(selectedCardId = intent.cardId) }
                SendMoneyIntent.SubmitClicked -> _state.update { it.copy(isAwaitingBiometric = true) }
                SendMoneyIntent.BiometricSucceeded -> submit()
                SendMoneyIntent.BiometricFailed -> _state.update { it.copy(isAwaitingBiometric = false) }
                SendMoneyIntent.RetryClicked -> submit()
            }
        }

        private fun onAmountChanged(text: String) {
            _state.update { it.copy(amountText = text) }
            val minorUnits = text.toMinorUnitsOrNull() ?: return
            viewModelScope.launch {
                val current = _state.value
                // Same rationale as init{}: a still-failing FX lookup must not crash the screen.
                runCatching {
                    convertCurrency(
                        com.nerojust.vela.domain.model.Money(minorUnits, current.sourceCurrency),
                        current.destinationCurrency,
                    )
                }.onSuccess { converted ->
                    _state.update { it.copy(convertedAmount = converted) }
                }
            }
        }

        private fun submit() {
            val current = _state.value
            val cardId = current.selectedCardId ?: return
            val minorUnits = current.amountText.toMinorUnitsOrNull() ?: return
            _state.update { it.copy(isAwaitingBiometric = false, isSubmitting = true) }
            viewModelScope.launch {
                val result =
                    runCatching {
                        sendMoney(
                            com.nerojust.vela.domain.model.Money(minorUnits, current.sourceCurrency),
                            current.destinationCurrency,
                            cardId,
                        )
                    }
                _state.update {
                    it.copy(
                        isSubmitting = false,
                        lastResult =
                            result.fold(
                                onSuccess = { transaction ->
                                    val status = transaction.status
                                    if (status is TransactionStatus.FailedPermanent) {
                                        SendMoneyResult.Failed(status.reason)
                                    } else {
                                        SendMoneyResult.Success(transaction)
                                    }
                                },
                                onFailure = {
                                        throwable ->
                                    SendMoneyResult.Failed(PaymentFailure.Unknown(throwable.message ?: "Unknown error"))
                                },
                            ),
                    )
                }
            }
        }
    }

private fun String.toMinorUnitsOrNull(): Long? =
    toBigDecimalOrNull()?.movePointRight(2)?.setScale(0, RoundingMode.HALF_UP)?.toLong()
