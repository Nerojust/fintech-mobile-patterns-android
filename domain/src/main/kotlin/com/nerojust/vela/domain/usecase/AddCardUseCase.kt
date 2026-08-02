package com.nerojust.vela.domain.usecase

import com.nerojust.vela.domain.model.Card
import com.nerojust.vela.domain.model.CardBrand
import com.nerojust.vela.domain.repository.CardRepository
import javax.inject.Inject

class AddCardUseCase
    @Inject
    constructor(
        private val repository: CardRepository,
    ) {
        suspend operator fun invoke(
            brand: CardBrand,
            last4: String,
            expiryMonth: Int,
            expiryYear: Int,
        ): Card = repository.addCard(brand, last4, expiryMonth, expiryYear)
    }
