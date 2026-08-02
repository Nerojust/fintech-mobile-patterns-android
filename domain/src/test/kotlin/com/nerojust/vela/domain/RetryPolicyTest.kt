package com.nerojust.vela.domain

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.IOException

class RetryPolicyTest {
    @Test
    fun `retries a transient failure then succeeds`() =
        runTest {
            var calls = 0
            val result =
                RetryPolicy(maxAttempts = 3, initialDelayMillis = 10).execute<String> {
                    calls++
                    if (calls < 3) throw IOException("boom") else "ok"
                }
            assertEquals("ok", result)
            assertEquals(3, calls)
        }

    @Test
    fun `stops after maxAttempts and rethrows`() =
        runTest {
            var calls = 0
            assertThrows(IOException::class.java) {
                kotlinx.coroutines.runBlocking {
                    RetryPolicy(maxAttempts = 2, initialDelayMillis = 10).execute<String> {
                        calls++
                        throw IOException("boom")
                    }
                }
            }
            assertEquals(2, calls)
        }

    @Test
    fun `does not retry when isRetryable returns false`() =
        runTest {
            var calls = 0
            assertThrows(IllegalStateException::class.java) {
                kotlinx.coroutines.runBlocking {
                    RetryPolicy(maxAttempts = 3).execute<String>(isRetryable = { false }) {
                        calls++
                        error("permanent")
                    }
                }
            }
            assertEquals(1, calls)
        }
}
