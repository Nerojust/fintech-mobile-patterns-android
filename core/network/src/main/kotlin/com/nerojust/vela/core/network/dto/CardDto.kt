package com.nerojust.vela.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class AddCardRequest(
    val brand: String,
    val last4: String,
    val expiryMonth: Int,
    val expiryYear: Int,
)

@Serializable
data class CardDto(
    val id: String,
    val brand: String,
    val last4: String,
    val expiryMonth: Int,
    val expiryYear: Int,
    val isDefault: Boolean,
)
