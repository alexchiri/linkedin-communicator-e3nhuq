package com.kroslabs.linkedincommunicator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.kroslabs.linkedincommunicator.logging.DebugLogger
import com.kroslabs.linkedincommunicator.ui.MainViewModel
import com.kroslabs.linkedincommunicator.ui.navigation.AppNavigation
import com.kroslabs.linkedincommunicator.ui.theme.LinkedInCommunicatorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        DebugLogger.i("MainActivity", "App started")

        setContent {
            LinkedInCommunicatorTheme {
                val navController = rememberNavController()
                val viewModel: MainViewModel = viewModel(
                    factory = MainViewModel.Factory(applicationContext)
                )

                AppNavigation(
                    navController = navController,
                    viewModel = viewModel
                )
            }
        }
    }
}
