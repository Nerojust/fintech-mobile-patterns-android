package com.nerojust.vela.data

import com.nerojust.vela.core.network.VelaApiService
import com.nerojust.vela.core.network.dto.AddCardRequest
import com.nerojust.vela.data.mapper.toDomain
import com.nerojust.vela.domain.RetryPolicy
import com.nerojust.vela.domain.model.Card
import com.nerojust.vela.domain.model.CardBrand
import com.nerojust.vela.domain.repository.CardRepository
import java.io.IOException
import javax.inject.Inject

class CardRepositoryImpl
    @Inject
    constructor(
        private val api: VelaApiService,
        private val retryPolicy: RetryPolicy,
    ) : CardRepository {
        override suspend fun getCards(): List<Card> =
            retryPolicy.execute(isRetryable = { it is IOException }) { api.getCards() }.map { it.toDomain() }

        override suspend fun addCard(
            brand: CardBrand,
            last4: String,
            expiryMonth: Int,
            expiryYear: Int,
        ): Card =
            retryPolicy.execute(isRetryable = { it is IOException }) {
                val request =
                    AddCardRequest(
                        brand = brand.name,
                        last4 = last4,
                        expiryMonth = expiryMonth,
                        expiryYear = expiryYear,
                    )
                api.addCard(request)
            }.toDomain()

        override suspend fun removeCard(cardId: String) {
            // The fake backend has no delete endpoint yet — extension point, see README.
        }
    }
