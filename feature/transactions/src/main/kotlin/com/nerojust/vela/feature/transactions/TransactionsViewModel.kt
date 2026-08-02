package com.nerojust.vela.feature.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nerojust.vela.domain.usecase.ObserveTransactionsUseCase
import com.nerojust.vela.domain.usecase.RetrySyncUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionsViewModel
    @Inject
    constructor(
        observeTransactions: ObserveTransactionsUseCase,
        private val retrySync: RetrySyncUseCase,
    ) : ViewModel() {
        private val _state = MutableStateFlow(TransactionsUiState())
        val state: StateFlow<TransactionsUiState> = _state.asStateFlow()

        init {
            viewModelScope.launch {
                observeTransactions().collect { transactions ->
                    _state.update { it.copy(transactions = transactions) }
                }
            }
        }

        fun onIntent(intent: TransactionsIntent) {
            when (intent) {
                TransactionsIntent.RetryClicked ->
                    viewModelScope.launch {
                        _state.update { it.copy(isSyncing = true) }
                        retrySync()
                        _state.update { it.copy(isSyncing = false) }
                    }
            }
        }
    }
