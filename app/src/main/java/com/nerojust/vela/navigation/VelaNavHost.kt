package com.nerojust.vela.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.nerojust.vela.feature.cards.CardsScreen
import com.nerojust.vela.feature.payment.SendMoneyScreen
import com.nerojust.vela.feature.transactions.TransactionsScreen

private const val ROUTE_SEND_MONEY = "sendMoney"
private const val ROUTE_CARDS = "cards"
private const val ROUTE_TRANSACTIONS = "transactions"

private data class VelaDestination(
    val route: String,
    val label: String,
)

private val BOTTOM_NAV_DESTINATIONS =
    listOf(
        VelaDestination(ROUTE_SEND_MONEY, "Send"),
        VelaDestination(ROUTE_CARDS, "Cards"),
        VelaDestination(ROUTE_TRANSACTIONS, "History"),
    )

@Composable
fun VelaNavHost(navController: NavHostController = rememberNavController()) {
    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            NavigationBar {
                BOTTOM_NAV_DESTINATIONS.forEach { destination ->
                    val selected =
                        currentDestination?.hierarchy?.any { it.route == destination.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(destinationIcon(destination.route), contentDescription = destination.label) },
                        label = { Text(destination.label) },
                    )
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = ROUTE_SEND_MONEY,
            modifier = Modifier.padding(padding),
        ) {
            composable(ROUTE_SEND_MONEY) { SendMoneyScreen() }
            composable(ROUTE_CARDS) { CardsScreen() }
            composable(ROUTE_TRANSACTIONS) { TransactionsScreen() }
        }
    }
}

private fun destinationIcon(route: String) =
    when (route) {
        ROUTE_SEND_MONEY -> Icons.AutoMirrored.Filled.Send
        ROUTE_CARDS -> Icons.AutoMirrored.Filled.List
        else -> Icons.Default.DateRange
    }
