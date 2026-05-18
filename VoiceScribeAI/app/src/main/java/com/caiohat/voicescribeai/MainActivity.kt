package com.caiohat.voicescribeai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.caiohat.voicescribeai.ui.screens.MainScreen
import com.caiohat.voicescribeai.ui.screens.SettingsScreen
import com.caiohat.voicescribeai.ui.theme.VoiceScribeAITheme
import com.caiohat.voicescribeai.viewmodel.MainViewModel
import com.caiohat.voicescribeai.viewmodel.SettingsViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VoiceScribeAITheme {
                VoiceScribeNavGraph(context = this)
            }
        }
    }
}

@Composable
private fun VoiceScribeNavGraph(context: android.content.Context) {
    val navController = rememberNavController()
    val mainViewModel: MainViewModel = viewModel(factory = MainViewModel.Factory(context))
    val settingsViewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory(context))

    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            MainScreen(
                viewModel = mainViewModel,
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }
        composable("settings") {
            SettingsScreen(
                viewModel = settingsViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
