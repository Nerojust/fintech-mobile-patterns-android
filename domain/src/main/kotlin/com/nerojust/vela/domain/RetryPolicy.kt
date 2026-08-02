package com.nerojust.vela.domain

import kotlinx.coroutines.delay

class RetryPolicy(
    private val maxAttempts: Int = 3,
    private val initialDelayMillis: Long = 200,
    private val factor: Double = 2.0,
) {
    // A generic retry utility must catch broad exceptions to hand every failure to the
    // caller-supplied `isRetryable` classifier — narrowing the catch type further would
    // defeat the whole point of this class. Still bounded to Exception (not Throwable),
    // so fatal JVM errors (OutOfMemoryError, StackOverflowError) propagate immediately
    // instead of being delayed and retried.
    @Suppress("TooGenericExceptionCaught")
    suspend fun <T> execute(
        isRetryable: (Throwable) -> Boolean = { true },
        block: suspend (attempt: Int) -> T,
    ): T {
        var attempt = 0
        var currentDelay = initialDelayMillis
        while (true) {
            attempt++
            try {
                return block(attempt)
            } catch (e: Exception) {
                if (attempt >= maxAttempts || !isRetryable(e)) throw e
                delay(currentDelay)
                currentDelay = (currentDelay * factor).toLong()
            }
        }
    }
}
