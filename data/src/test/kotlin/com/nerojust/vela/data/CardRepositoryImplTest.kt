package com.nerojust.vela.data

import com.nerojust.vela.core.network.VelaApiService
import com.nerojust.vela.core.network.dto.CardDto
import com.nerojust.vela.domain.RetryPolicy
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

class CardRepositoryImplTest {
    private val api = mockk<VelaApiService>()
    private val repository = CardRepositoryImpl(api, RetryPolicy(maxAttempts = 2, initialDelayMillis = 1))

    private val dto =
        CardDto(id = "card-1", brand = "VISA", last4 = "4242", expiryMonth = 12, expiryYear = 2030, isDefault = true)

    @Test
    fun `getCards maps DTOs to domain models`() =
        runTest {
            coEvery { api.getCards() } returns listOf(dto)
            val result = repository.getCards()
            assertEquals("4242", result.first().last4)
        }

    @Test
    fun `getCards retries once on a transient IOException then succeeds`() =
        runTest {
            coEvery { api.getCards() } throws IOException("down") andThen listOf(dto)
            val result = repository.getCards()
            assertEquals("4242", result.first().last4)
        }

    @Test
    fun `removeCard is a no-op the fake backend does not expose yet`() =
        runTest {
            repository.removeCard("card-1")
            coVerify(exactly = 0) { api.getCards() }
        }
}
