package com.nerojust.vela.domain.model

data class Money(
    val minorUnits: Long,
    val currency: Currency,
) {
    operator fun plus(other: Money): Money {
        require(currency == other.currency) { "Cannot add ${other.currency} to $currency" }
        return copy(minorUnits = minorUnits + other.minorUnits)
    }

    fun formatted(): String {
        val divisor = Math.pow(10.0, currency.minorUnitDigits.toDouble()).toLong()
        val whole = minorUnits / divisor
        val fraction = (minorUnits % divisor).toString().padStart(currency.minorUnitDigits, '0')
        return "${currency.code} $whole.$fraction"
    }
}

enum class Currency(
    val code: String,
    val minorUnitDigits: Int,
) {
    USD("USD", 2),
    EUR("EUR", 2),
    GBP("GBP", 2),
    NGN("NGN", 2),
}
