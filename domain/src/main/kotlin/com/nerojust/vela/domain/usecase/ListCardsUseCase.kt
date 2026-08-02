package com.nerojust.vela.domain.usecase

import com.nerojust.vela.domain.model.Card
import com.nerojust.vela.domain.repository.CardRepository
import javax.inject.Inject

class ListCardsUseCase
    @Inject
    constructor(
        private val repository: CardRepository,
    ) {
        suspend operator fun invoke(): List<Card> = repository.getCards()
    }
