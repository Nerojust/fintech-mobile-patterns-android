package com.nerojust.vela.feature.payment

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.nerojust.vela.domain.model.Currency
import com.nerojust.vela.domain.model.Money
import com.nerojust.vela.domain.model.PaymentFailure
import org.junit.Rule
import org.junit.Test

class SendMoneyContentTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun showsConvertedAmountWhenPresent() {
        composeRule.setContent {
            SendMoneyContent(
                state = SendMoneyUiState(convertedAmount = Money(920, Currency.EUR)),
                onIntent = {},
            )
        }
        composeRule.onNodeWithText("You send: EUR 9.20").assertIsDisplayed()
    }

    @Test
    fun showsInsufficientFundsMessageOnFailure() {
        composeRule.setContent {
            SendMoneyContent(
                state = SendMoneyUiState(lastResult = SendMoneyResult.Failed(PaymentFailure.InsufficientFunds)),
                onIntent = {},
            )
        }
        composeRule.onNodeWithText("Insufficient funds").assertIsDisplayed()
    }
}
