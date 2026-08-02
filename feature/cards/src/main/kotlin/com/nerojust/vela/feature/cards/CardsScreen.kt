package com.nerojust.vela.feature.cards

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun CardsScreen(viewModel: CardsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(Unit) { viewModel.onIntent(CardsIntent.LoadCards) }
    CardsContent(state = state, onIntent = viewModel::onIntent)
}

// `onIntent` is unused today — the cards list is currently view-only (removeCard is a
// no-op, see README "Known simplifications") — but is kept for MVI consistency with the
// other feature Content composables (SendMoneyContent, TransactionsContent), which do
// dispatch through it. Wire it up when a real card action (e.g. remove) is added.
@Suppress("UnusedParameter")
@Composable
fun CardsContent(
    state: CardsUiState,
    onIntent: (CardsIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier) {
        items(state.cards) { card ->
            ListItem(
                headlineContent = { Text("${card.brand} •••• ${card.last4}") },
                supportingContent = { Text("${card.expiryMonth}/${card.expiryYear}") },
            )
        }
    }
}
