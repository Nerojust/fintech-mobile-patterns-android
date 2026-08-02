package com.nerojust.vela.feature.transactions

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.nerojust.vela.core.ui.component.PrimaryButton
import com.nerojust.vela.domain.model.PaymentFailure
import com.nerojust.vela.domain.model.TransactionStatus

@Composable
fun TransactionsScreen(viewModel: TransactionsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    TransactionsContent(state = state, onIntent = viewModel::onIntent)
}

@Composable
fun TransactionsContent(
    state: TransactionsUiState,
    onIntent: (TransactionsIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier) {
        items(state.transactions) { transaction ->
            ListItem(
                headlineContent = { Text(transaction.amount.formatted()) },
                supportingContent = {
                    when (val status = transaction.status) {
                        TransactionStatus.Pending -> Text("Pending")
                        TransactionStatus.Synced -> Text("Sent")
                        is TransactionStatus.FailedPermanent ->
                            Text(status.reason.message)
                    }
                },
                trailingContent = {
                    // Only network-exhaustion failures are revived by RetrySyncUseCase; an
                    // insufficient-funds decline needs the user's balance to change, not a retry.
                    val failed = transaction.status as? TransactionStatus.FailedPermanent
                    if (failed?.reason == PaymentFailure.NetworkUnavailable) {
                        PrimaryButton(text = "Retry", onClick = { onIntent(TransactionsIntent.RetryClicked) })
                    }
                },
            )
        }
    }
}
