package com.nerojust.vela.domain.model

data class Card(
    val id: String,
    val brand: CardBrand,
    val last4: String,
    val expiryMonth: Int,
    val expiryYear: Int,
    val isDefault: Boolean,
)

enum class CardBrand {
    VISA,
    MASTERCARD,
    VERVE,
}
