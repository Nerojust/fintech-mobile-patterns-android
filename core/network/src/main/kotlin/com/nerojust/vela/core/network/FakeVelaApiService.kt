package com.nerojust.vela.core.network

import com.nerojust.vela.core.network.dto.AddCardRequest
import com.nerojust.vela.core.network.dto.CardDto
import com.nerojust.vela.core.network.dto.ExchangeRateDto
import com.nerojust.vela.core.network.dto.SubmitTransactionRequest
import com.nerojust.vela.core.network.dto.TransactionDto
import kotlinx.coroutines.delay
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private const val INSUFFICIENT_FUNDS_THRESHOLD_MINOR_UNITS = 1_000_000L

@Singleton
class FakeVelaApiService
    @Inject
    constructor(
        private val conditions: NetworkConditions,
    ) : VelaApiService {
        private val cards =
            mutableListOf(
                CardDto(
                    id = "card-1",
                    brand = "VISA",
                    last4 = "4242",
                    expiryMonth = 12,
                    expiryYear = 2030,
                    isDefault = true,
                ),
            )
        private val transactions = mutableListOf<TransactionDto>()

        // Illustrative FX rates for the fake backend, not meaningful named constants.
        @Suppress("MagicNumber")
        private val fxRates =
            mapOf(
                "USD" to mapOf("EUR" to 0.92, "GBP" to 0.79, "NGN" to 1550.0),
                "EUR" to mapOf("USD" to 1.09, "GBP" to 0.86, "NGN" to 1685.0),
                "GBP" to mapOf("USD" to 1.27, "EUR" to 1.16, "NGN" to 1965.0),
            )

        private suspend fun simulateNetwork() {
            delay(conditions.simulatedLatencyMillis)
            if (conditions.random() < conditions.failureRate) {
                throw IOException("Simulated network failure")
            }
        }

        override suspend fun submitTransaction(request: SubmitTransactionRequest): TransactionDto {
            if (request.amountMinorUnits > INSUFFICIENT_FUNDS_THRESHOLD_MINOR_UNITS) {
                throw InsufficientFundsException()
            }
            simulateNetwork()
            val dto =
                TransactionDto(
                    id = UUID.randomUUID().toString(),
                    amountMinorUnits = request.amountMinorUnits,
                    currency = request.currency,
                    destinationCurrency = request.destinationCurrency,
                    cardId = request.cardId,
                    status = "SYNCED",
                    createdAtEpochMillis = System.currentTimeMillis(),
                )
            transactions.add(dto)
            return dto
        }

        override suspend fun getTransactions(): List<TransactionDto> {
            simulateNetwork()
            return transactions.toList()
        }

        override suspend fun getExchangeRate(
            from: String,
            to: String,
        ): ExchangeRateDto {
            simulateNetwork()
            val rate = if (from == to) 1.0 else fxRates[from]?.get(to) ?: error("No rate for $from->$to")
            return ExchangeRateDto(from = from, to = to, rate = rate)
        }

        override suspend fun getCards(): List<CardDto> {
            simulateNetwork()
            return cards.toList()
        }

        override suspend fun addCard(request: AddCardRequest): CardDto {
            simulateNetwork()
            val dto =
                CardDto(
                    id = UUID.randomUUID().toString(),
                    brand = request.brand,
                    last4 = request.last4,
                    expiryMonth = request.expiryMonth,
                    expiryYear = request.expiryYear,
                    isDefault = cards.isEmpty(),
                )
            cards.add(dto)
            return dto
        }
    }
