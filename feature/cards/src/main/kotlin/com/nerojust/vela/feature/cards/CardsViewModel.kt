package com.nerojust.vela.feature.cards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nerojust.vela.domain.usecase.AddCardUseCase
import com.nerojust.vela.domain.usecase.ListCardsUseCase
import com.nerojust.vela.domain.usecase.RemoveCardUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CardsViewModel
    @Inject
    constructor(
        private val listCards: ListCardsUseCase,
        private val addCard: AddCardUseCase,
        private val removeCard: RemoveCardUseCase,
    ) : ViewModel() {
        private val _state = MutableStateFlow(CardsUiState())
        val state: StateFlow<CardsUiState> = _state.asStateFlow()

        fun onIntent(intent: CardsIntent) {
            when (intent) {
                CardsIntent.LoadCards -> load()
                is CardsIntent.AddCardClicked ->
                    viewModelScope.launch {
                        // The fake backend injects transient failures even after
                        // CardRepositoryImpl's own retries are exhausted — don't crash on one.
                        runCatching { addCard(intent.brand, intent.last4, intent.expiryMonth, intent.expiryYear) }
                        load()
                    }
                is CardsIntent.RemoveCard ->
                    viewModelScope.launch {
                        removeCard(intent.cardId)
                        load()
                    }
            }
        }

        private fun load() {
            viewModelScope.launch {
                _state.update { it.copy(isLoading = true) }
                runCatching { listCards() }
                    .onSuccess { cards -> _state.update { it.copy(cards = cards, isLoading = false) } }
                    .onFailure { _state.update { it.copy(isLoading = false) } }
            }
        }
    }
