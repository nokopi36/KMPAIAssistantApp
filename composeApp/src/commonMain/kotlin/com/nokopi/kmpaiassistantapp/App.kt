package com.nokopi.kmpaiassistantapp

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.nokopi.kmpaiassistantapp.presentation.screen.MainScreen
import com.nokopi.kmpaiassistantapp.presentation.screen.SetupScreen
import com.nokopi.kmpaiassistantapp.presentation.screen.SettingsScreen
import com.nokopi.kmpaiassistantapp.presentation.viewmodel.MainViewModel
import com.nokopi.kmpaiassistantapp.presentation.viewmodel.SettingsViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject

@Composable
@Preview
fun App() {
    MaterialTheme {
        val navController = rememberNavController()
        val settingsViewModel: SettingsViewModel = koinInject()
        val mainViewModel: MainViewModel = koinInject()
        
        val settingsUiState by settingsViewModel.uiState.collectAsStateWithLifecycle()
        
        // 初回起動判定：API Keyが設定されているかチェック
        val startDestination = if (settingsUiState.hasApiKey) "main" else "setup"
        
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.fillMaxSize()
        ) {
            // セットアップ画面（初回起動時）
            composable("setup") {
                SetupScreen(
                    settingsViewModel = settingsViewModel,
                    onSetupComplete = {
                        navController.navigate("main") {
                            popUpTo("setup") { inclusive = true }
                        }
                    }
                )
            }
            
            // メイン画面
            composable("main") {
                MainScreen(
                    viewModel = mainViewModel,
                    onSettingsClick = {
                        navController.navigate("settings")
                    }
                )
            }
            
            // 設定画面
            composable("settings") {
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}