package com.nerojust.vela.core.network

data class NetworkConditions(
    val simulatedLatencyMillis: Long = 400,
    val failureRate: Double = 0.0,
    val random: () -> Double = { Math.random() },
)
