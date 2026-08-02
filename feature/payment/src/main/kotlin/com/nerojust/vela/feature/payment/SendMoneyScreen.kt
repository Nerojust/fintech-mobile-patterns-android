package com.nerojust.vela.feature.payment

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.nerojust.vela.core.security.BiometricAuthenticator
import com.nerojust.vela.core.security.BiometricResult
import com.nerojust.vela.core.ui.component.PrimaryButton
import com.nerojust.vela.domain.model.PaymentFailure

@Composable
fun SendMoneyScreen(viewModel: SendMoneyViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    if (state.isAwaitingBiometric) {
        LaunchedEffect(Unit) {
            val authenticator =
                BiometricAuthenticator(context as FragmentActivity, ContextCompat.getMainExecutor(context))
            authenticator.authenticate(title = "Confirm payment", subtitle = "Authenticate to send money").collect {
                    result ->
                val intent =
                    if (result is BiometricResult.Success) {
                        SendMoneyIntent.BiometricSucceeded
                    } else {
                        SendMoneyIntent.BiometricFailed
                    }
                viewModel.onIntent(intent)
            }
        }
    }

    SendMoneyContent(state = state, onIntent = viewModel::onIntent)
}

@Composable
fun SendMoneyContent(
    state: SendMoneyUiState,
    onIntent: (SendMoneyIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = state.amountText,
            onValueChange = { onIntent(SendMoneyIntent.AmountChanged(it)) },
            label = { Text("Amount (${state.sourceCurrency.code})") },
            modifier = Modifier.padding(bottom = 8.dp),
        )
        state.convertedAmount?.let { Text("You send: ${it.formatted()}") }

        when (val result = state.lastResult) {
            is SendMoneyResult.Success -> Text("Sent — ${result.transaction.amount.formatted()}")
            is SendMoneyResult.Failed ->
                when (result.failure) {
                    is PaymentFailure.InsufficientFunds -> Text("Insufficient funds")
                    else -> Text(result.failure.message)
                }
            null -> Unit
        }

        PrimaryButton(
            text = if (state.isSubmitting) "Sending..." else "Send money",
            enabled = !state.isSubmitting && state.selectedCardId != null,
            onClick = { onIntent(SendMoneyIntent.SubmitClicked) },
            modifier = Modifier.padding(top = 16.dp),
        )
    }
}
