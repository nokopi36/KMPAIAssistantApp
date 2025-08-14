package com.nokopi.kmpaiassistantapp.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nokopi.kmpaiassistantapp.presentation.viewmodel.SettingsViewModel

@Composable
fun SetupScreen(
    settingsViewModel: SettingsViewModel,
    onSetupComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settingsUiState by settingsViewModel.uiState.collectAsStateWithLifecycle()
    
    // API Keyが設定されたらメイン画面に遷移
    LaunchedEffect(settingsUiState.hasApiKey) {
        if (settingsUiState.hasApiKey) {
            onSetupComplete()
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // アプリアイコン/ロゴ
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // ウェルカムメッセージ
        Text(
            text = "AIアシスタントへようこそ！",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "使用を開始するには、MCP APIキーの設定が必要です。",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // API Key設定カード
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Key,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "API Key設定",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Yumemi OpenHandbook MCP APIを使用するためのAPIキーを設定してください。",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // API Key入力フィールド
                OutlinedTextField(
                    value = settingsUiState.apiKey,
                    onValueChange = settingsViewModel::updateApiKey,
                    label = { Text("API Key") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !settingsUiState.isLoading,
                    placeholder = { Text("APIキーを入力してください") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 設定ボタン
                Button(
                    onClick = settingsViewModel::saveApiKey,
                    enabled = !settingsUiState.isLoading && settingsUiState.apiKey.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (settingsUiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("設定中...")
                    } else {
                        Text("APIキーを設定して開始")
                    }
                }
                
                // エラー表示
                settingsUiState.error?.let { error ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // 説明文
        Text(
            text = "APIキーは安全に暗号化されて保存されます。\n後で設定画面から変更することも可能です。",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}