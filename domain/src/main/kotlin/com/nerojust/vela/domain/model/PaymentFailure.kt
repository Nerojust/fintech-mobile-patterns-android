package com.nerojust.vela.domain.model

sealed class PaymentFailure(val message: String) {
    data object InsufficientFunds : PaymentFailure("Insufficient funds")

    data object NetworkUnavailable : PaymentFailure("No network connection")

    data object Timeout : PaymentFailure("Request timed out")

    data object Declined : PaymentFailure("Payment declined by issuer")

    data class Unknown(val cause: String) : PaymentFailure(cause)
}
