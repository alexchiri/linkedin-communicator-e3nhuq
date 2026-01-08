package com.kroslabs.linkedincommunicator.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.kroslabs.linkedincommunicator.ui.MainViewModel
import com.kroslabs.linkedincommunicator.ui.screens.DebugLogsScreen
import com.kroslabs.linkedincommunicator.ui.screens.MainScreen
import com.kroslabs.linkedincommunicator.ui.screens.SettingsScreen

object Routes {
    const val MAIN = "main"
    const val SETTINGS = "settings"
    const val DEBUG_LOGS = "debug_logs"
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    viewModel: MainViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Routes.MAIN
    ) {
        composable(Routes.MAIN) {
            MainScreen(
                viewModel = viewModel,
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDebugLogs = { navController.navigate(Routes.DEBUG_LOGS) }
            )
        }
        composable(Routes.DEBUG_LOGS) {
            DebugLogsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
