package com.nerojust.vela.core.network

import com.nerojust.vela.core.network.dto.AddCardRequest
import com.nerojust.vela.core.network.dto.CardDto
import com.nerojust.vela.core.network.dto.ExchangeRateDto
import com.nerojust.vela.core.network.dto.SubmitTransactionRequest
import com.nerojust.vela.core.network.dto.TransactionDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Shape a real backend integration would implement via Retrofit. The only
 * implementation in this repo is [FakeVelaApiService] — there is no real host.
 */
interface VelaApiService {
    @POST("transactions")
    suspend fun submitTransaction(
        @Body request: SubmitTransactionRequest,
    ): TransactionDto

    @GET("transactions")
    suspend fun getTransactions(): List<TransactionDto>

    @GET("fx-rates/{from}/{to}")
    suspend fun getExchangeRate(
        @Path("from") from: String,
        @Path("to") to: String,
    ): ExchangeRateDto

    @GET("cards")
    suspend fun getCards(): List<CardDto>

    @POST("cards")
    suspend fun addCard(
        @Body request: AddCardRequest,
    ): CardDto
}
