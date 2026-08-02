package com.nerojust.vela.feature.cards

import com.nerojust.vela.domain.model.Card
import com.nerojust.vela.domain.model.CardBrand

sealed interface CardsIntent {
    data object LoadCards : CardsIntent

    data class AddCardClicked(
        val brand: CardBrand,
        val last4: String,
        val expiryMonth: Int,
        val expiryYear: Int,
    ) : CardsIntent

    data class RemoveCard(val cardId: String) : CardsIntent
}

data class CardsUiState(
    val cards: List<Card> = emptyList(),
    val isLoading: Boolean = false,
)
