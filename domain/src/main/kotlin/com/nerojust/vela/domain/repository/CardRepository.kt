package com.nerojust.vela.domain.repository

import com.nerojust.vela.domain.model.Card
import com.nerojust.vela.domain.model.CardBrand

interface CardRepository {
    suspend fun getCards(): List<Card>

    suspend fun addCard(
        brand: CardBrand,
        last4: String,
        expiryMonth: Int,
        expiryYear: Int,
    ): Card

    suspend fun removeCard(cardId: String)
}
