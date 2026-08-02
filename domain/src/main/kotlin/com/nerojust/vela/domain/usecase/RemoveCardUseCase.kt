package com.nerojust.vela.domain.usecase

import com.nerojust.vela.domain.repository.CardRepository
import javax.inject.Inject

class RemoveCardUseCase
    @Inject
    constructor(
        private val repository: CardRepository,
    ) {
        suspend operator fun invoke(cardId: String) = repository.removeCard(cardId)
    }
