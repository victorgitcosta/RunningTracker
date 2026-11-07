package com.exemplo.runningtracker.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.exemplo.runningtracker.presentation.history.HistoryScreen
import com.exemplo.runningtracker.presentation.history.HistoryViewModel
import com.exemplo.runningtracker.presentation.tracking.TrackingScreen
import com.exemplo.runningtracker.presentation.tracking.TrackingViewModel
import com.exemplo.runningtracker.ui.theme.Ink
import com.exemplo.runningtracker.ui.theme.LimeSoft

object Route {
    const val Tracking = "tracking"
    const val History = "history"
}

@Composable
fun AppNavigation(trackingViewModel: TrackingViewModel, historyViewModel: HistoryViewModel) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val route = backStack?.destination?.route

    Scaffold(
        containerColor = Color.White,
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    selected = route == Route.Tracking,
                    onClick = {
                        navController.navigate(Route.Tracking) {
                            launchSingleTop = true
                            popUpTo(Route.Tracking) { inclusive = false }
                        }
                    },
                    label = { Text("Mapa") },
                    icon = { Icon(Icons.Default.Map, contentDescription = "Mapa") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Ink,
                        selectedTextColor = Ink,
                        indicatorColor = LimeSoft
                    )
                )
                NavigationBarItem(
                    selected = route == Route.History,
                    onClick = {
                        navController.navigate(Route.History) { launchSingleTop = true }
                    },
                    label = { Text("Histórico") },
                    icon = { Icon(Icons.Default.History, contentDescription = "Histórico") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Ink,
                        selectedTextColor = Ink,
                        indicatorColor = LimeSoft
                    )
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Route.Tracking,
            modifier = androidx.compose.ui.Modifier.padding(padding)
        ) {
            composable(Route.Tracking) {
                TrackingScreen(
                    viewModel = trackingViewModel,
                    onRunSaved = {
                        navController.navigate(Route.History) {
                            launchSingleTop = true
                            popUpTo(Route.Tracking) { inclusive = false }
                        }
                    }
                )
            }
            composable(Route.History) {
                HistoryScreen(viewModel = historyViewModel)
            }
        }
    }
}
