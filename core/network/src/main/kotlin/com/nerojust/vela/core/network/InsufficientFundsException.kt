package com.nerojust.vela.core.network

/** Business decline, not a transient error — never retried by [com.nerojust.vela.domain.RetryPolicy]. */
class InsufficientFundsException : RuntimeException("Insufficient funds")
