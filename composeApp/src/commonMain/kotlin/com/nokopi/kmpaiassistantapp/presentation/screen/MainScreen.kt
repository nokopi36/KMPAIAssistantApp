package com.nokopi.kmpaiassistantapp.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nokopi.kmpaiassistantapp.data.model.ConversationItem
import com.nokopi.kmpaiassistantapp.presentation.viewmodel.MainViewModel
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onSettingsClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    LaunchedEffect(uiState.error) {
        if (uiState.error != null) {
            viewModel.clearError()
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TopAppBar(
            title = { Text("AIアシスタント") },
            actions = {
                IconButton(
                    onClick = { viewModel.toggleFavoritesFilter() }
                ) {
                    Icon(
                        imageVector = if (uiState.showFavoritesOnly) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (uiState.showFavoritesOnly) "全て表示" else "お気に入りのみ表示"
                    )
                }
                IconButton(
                    onClick = onSettingsClick
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "設定"
                    )
                }
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 質問入力部分
        QuestionInputSection(
            question = uiState.currentQuestion,
            isLoading = uiState.isLoading,
            isRecording = uiState.isRecording,
            onQuestionChange = viewModel::updateQuestion,
            onSendClick = viewModel::sendQuestion,
            onVoiceStart = viewModel::startVoiceRecording,
            onVoiceStop = viewModel::stopVoiceRecording
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 会話履歴
        ConversationHistory(
            conversations = uiState.conversations,
            isSpeaking = uiState.isSpeaking,
            onFavoriteToggle = viewModel::toggleFavorite,
            onDelete = viewModel::deleteConversation,
            onSpeak = viewModel::speakAnswer,
            onStopSpeaking = viewModel::stopSpeaking
        )
    }
    
    // エラー表示
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // ここでSnackbarを表示する場合はSnackbarHostStateが必要
        }
    }
}

@Composable
private fun QuestionInputSection(
    question: String,
    isLoading: Boolean,
    isRecording: Boolean,
    onQuestionChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onVoiceStart: () -> Unit,
    onVoiceStop: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            OutlinedTextField(
                value = question,
                onValueChange = onQuestionChange,
                label = { Text("質問を入力してください") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && !isRecording,
                minLines = 2
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = if (isRecording) onVoiceStop else onVoiceStart,
                    enabled = !isLoading,
                    colors = if (isRecording) ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ) else ButtonDefaults.buttonColors()
                ) {
                    Icon(
                        imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = if (isRecording) "録音停止" else "音声入力"
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isRecording) "停止" else "音声")
                }
                
                Button(
                    onClick = onSendClick,
                    enabled = !isLoading && !isRecording && question.isNotBlank(),
                    modifier = Modifier.weight(1f)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "送信"
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isLoading) "送信中..." else "送信")
                }
            }
        }
    }
}

@Composable
private fun ConversationHistory(
    conversations: List<ConversationItem>,
    isSpeaking: Boolean,
    onFavoriteToggle: (String, Boolean) -> Unit,
    onDelete: (String) -> Unit,
    onSpeak: (String) -> Unit,
    onStopSpeaking: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "会話履歴",
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (conversations.isEmpty()) {
                Text(
                    text = "まだ会話がありません",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(conversations, key = { it.id }) { conversation ->
                        ConversationItem(
                            conversation = conversation,
                            isSpeaking = isSpeaking,
                            onFavoriteToggle = { onFavoriteToggle(conversation.id, !conversation.isFavorite) },
                            onDelete = { onDelete(conversation.id) },
                            onSpeak = { onSpeak(conversation.answer) },
                            onStopSpeaking = onStopSpeaking
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConversationItem(
    conversation: ConversationItem,
    isSpeaking: Boolean,
    onFavoriteToggle: () -> Unit,
    onDelete: () -> Unit,
    onSpeak: () -> Unit,
    onStopSpeaking: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // 質問
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "質問:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Row {
                    IconButton(
                        onClick = if (isSpeaking) onStopSpeaking else onSpeak,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (isSpeaking) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                            contentDescription = if (isSpeaking) "読み上げ停止" else "読み上げ",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    
                    IconButton(
                        onClick = onFavoriteToggle,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (conversation.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (conversation.isFavorite) "お気に入り削除" else "お気に入り追加",
                            modifier = Modifier.size(16.dp),
                            tint = if (conversation.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "削除",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            Text(
                text = conversation.question,
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 回答
            Text(
                text = "回答:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            
            Text(
                text = conversation.answer,
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // 日時
            val dateTime = Instant.fromEpochMilliseconds(conversation.timestamp)
                .toLocalDateTime(TimeZone.currentSystemDefault())
            Text(
                text = "${dateTime.year}/${dateTime.monthNumber.toString().padStart(2, '0')}/${dateTime.dayOfMonth.toString().padStart(2, '0')} ${dateTime.hour.toString().padStart(2, '0')}:${dateTime.minute.toString().padStart(2, '0')}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}