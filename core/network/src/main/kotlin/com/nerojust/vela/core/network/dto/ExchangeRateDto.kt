package com.nerojust.vela.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class ExchangeRateDto(
    val from: String,
    val to: String,
    val rate: Double,
)
