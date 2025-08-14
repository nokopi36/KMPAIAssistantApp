package com.nokopi.kmpaiassistantapp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nokopi.kmpaiassistantapp.presentation.viewmodel.SettingsViewModel
import com.nokopi.kmpaiassistantapp.data.network.McpApiService
import com.nokopi.kmpaiassistantapp.data.storage.SecureStorage
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun DesktopTestApp() {
    MaterialTheme {
        val settingsViewModel: SettingsViewModel = koinInject()
        val secureStorage: SecureStorage = koinInject()
        val uiState by settingsViewModel.uiState.collectAsState()
        val coroutineScope = rememberCoroutineScope()
        
        val scrollState = rememberScrollState()
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "KMP AI Assistant App - Desktop Test",
                style = MaterialTheme.typography.headlineMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "状態確認",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text("API Key設定済み: ${if (uiState.hasApiKey) "はい" else "いいえ"}")
                    Text("読み込み中: ${if (uiState.isLoading) "はい" else "いいえ"}")
                    Text("エラー: ${uiState.error ?: "なし"}")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (!uiState.hasApiKey) {
                var apiKeyInput by remember { mutableStateOf("") }
                
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "API Key設定",
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = apiKeyInput,
                            onValueChange = { apiKeyInput = it },
                            label = { Text("Anthropic API Key") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Button(
                            onClick = { 
                                settingsViewModel.updateApiKey(apiKeyInput)
                                settingsViewModel.saveApiKey()
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("保存")
                        }
                    }
                }
            } else {
                Text(
                    text = "✅ デスクトップ版の基本機能が正常に動作しています",
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                var testQuestion by remember { mutableStateOf("") }
                var testResponse by remember { mutableStateOf<String?>(null) }
                var isTestingApi by remember { mutableStateOf(false) }
                
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "MCP API テスト",
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = testQuestion,
                            onValueChange = { testQuestion = it },
                            label = { Text("質問を入力") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isTestingApi
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row {
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        isTestingApi = true
                                        testResponse = null
                                        
                                        try {
                                            val apiKey = secureStorage.getApiKey()
                                            if (apiKey.isNullOrBlank()) {
                                                testResponse = "エラー: API Keyが設定されていません"
                                                return@launch
                                            }
                                            
                                            val mcpApiService = McpApiService(apiKey)
                                            val result = mcpApiService.sendMessage(testQuestion)
                                            
                                            testResponse = result.fold(
                                                onSuccess = { response -> 
                                                    "✅ 成功:\n$response"
                                                },
                                                onFailure = { error -> 
                                                    "❌ エラー: ${error.message}"
                                                }
                                            )
                                        } catch (e: Exception) {
                                            testResponse = "❌ 例外: ${e.message}"
                                        } finally {
                                            isTestingApi = false
                                        }
                                    }
                                },
                                enabled = !isTestingApi && testQuestion.isNotBlank()
                            ) {
                                Text(if (isTestingApi) "送信中..." else "テスト実行")
                            }
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Button(
                                onClick = { settingsViewModel.clearApiKey() }
                            ) {
                                Text("API Key削除")
                            }
                        }
                        
                        testResponse?.let { response ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 400.dp), // 最大高さを制限
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                )
                            ) {
                                val responseScrollState = rememberScrollState()
                                Column(
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .verticalScroll(responseScrollState)
                                ) {
                                    Text(
                                        text = "レスポンス:",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = response,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}