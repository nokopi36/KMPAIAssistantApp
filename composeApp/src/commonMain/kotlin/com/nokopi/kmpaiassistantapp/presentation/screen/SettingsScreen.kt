package com.nokopi.kmpaiassistantapp.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nokopi.kmpaiassistantapp.presentation.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showPassword by remember { mutableStateOf(false) }
    
    // エラーメッセージのSnackbar表示
    LaunchedEffect(uiState.error) {
        if (uiState.error != null) {
            // TODO: SnackbarHostStateを使用してエラー表示
            viewModel.clearError()
        }
    }
    
    // 保存成功メッセージの表示
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            // TODO: SnackbarHostStateを使用して成功メッセージ表示
            viewModel.clearSavedFlag()
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // トップバー
        TopAppBar(
            title = { Text("設定") },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "戻る"
                    )
                }
            }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // API Key設定セクション
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "MCP API Key",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Yumemi OpenHandbook MCP APIを使用するためのAPIキーを設定してください。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // API Key入力フィールド
                OutlinedTextField(
                    value = uiState.apiKey,
                    onValueChange = viewModel::updateApiKey,
                    label = { Text("API Key") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading,
                    visualTransformation = if (showPassword || uiState.apiKey != "••••••••••••••••") {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    trailingIcon = {
                        if (uiState.hasApiKey) {
                            TextButton(
                                onClick = { showPassword = !showPassword }
                            ) {
                                Text(if (showPassword) "隠す" else "表示")
                            }
                        }
                    },
                    placeholder = { Text("API Keyを入力してください") }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // ボタン群
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 保存ボタン
                    Button(
                        onClick = viewModel::saveApiKey,
                        enabled = !uiState.isLoading && uiState.apiKey.isNotEmpty(),
                        modifier = Modifier.weight(1f)
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = "保存"
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (uiState.isLoading) "保存中..." else "保存")
                    }
                    
                    // 削除ボタン（API Keyが設定されている場合のみ表示）
                    if (uiState.hasApiKey) {
                        OutlinedButton(
                            onClick = viewModel::clearApiKey,
                            enabled = !uiState.isLoading
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "削除"
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("削除")
                        }
                    }
                }
                
                // ステータス表示
                if (uiState.hasApiKey) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "API Keyが設定されています",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                
                // エラー表示
                uiState.error?.let { error ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = error,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // 使用方法の説明
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "使用方法",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "1. Yumemi OpenHandbook MCP APIのAPIキーを取得してください。\n" +
                            "2. 上記の入力フィールドにAPIキーを入力してください。\n" +
                            "3. 「保存」ボタンをクリックしてAPIキーを保存してください。\n" +
                            "4. APIキーは安全に暗号化されて保存されます。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}