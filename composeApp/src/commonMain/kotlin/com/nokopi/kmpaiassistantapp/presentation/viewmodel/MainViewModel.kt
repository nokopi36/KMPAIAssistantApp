package com.nokopi.kmpaiassistantapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nokopi.kmpaiassistantapp.data.model.ConversationItem
import com.nokopi.kmpaiassistantapp.data.network.McpApiService
import com.nokopi.kmpaiassistantapp.data.repository.ConversationRepository
import com.nokopi.kmpaiassistantapp.data.storage.SecureStorage
import com.nokopi.kmpaiassistantapp.speech.SpeechRecognizer
import com.nokopi.kmpaiassistantapp.speech.TextToSpeech
import com.nokopi.kmpaiassistantapp.util.ErrorHandler
import com.nokopi.kmpaiassistantapp.util.AppError
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class MainUiState(
    val conversations: List<ConversationItem> = emptyList(),
    val currentQuestion: String = "",
    val isLoading: Boolean = false,
    val isRecording: Boolean = false,
    val isSpeaking: Boolean = false,
    val error: String? = null,
    val showFavoritesOnly: Boolean = false
)

class MainViewModel(
    private val conversationRepository: ConversationRepository,
    private val secureStorage: SecureStorage,
    private val speechRecognizer: SpeechRecognizer,
    private val textToSpeech: TextToSpeech
) : ViewModel(), KoinComponent {
    
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
    
    init {
        observeConversations()
        observeSpeechRecognizer()
        observeTextToSpeech()
    }
    
    private fun observeConversations() {
        viewModelScope.launch {
            conversationRepository.getAllConversations().collectLatest { conversations ->
                _uiState.value = _uiState.value.copy(conversations = conversations)
            }
        }
    }
    
    private fun observeSpeechRecognizer() {
        viewModelScope.launch {
            speechRecognizer.isRecording.collectLatest { isRecording ->
                _uiState.value = _uiState.value.copy(isRecording = isRecording)
            }
        }
        
        viewModelScope.launch {
            speechRecognizer.recognizedText.collectLatest { text ->
                if (text.isNotEmpty()) {
                    _uiState.value = _uiState.value.copy(currentQuestion = text)
                }
            }
        }
    }
    
    private fun observeTextToSpeech() {
        viewModelScope.launch {
            textToSpeech.isSpeaking.collectLatest { isSpeaking ->
                _uiState.value = _uiState.value.copy(isSpeaking = isSpeaking)
            }
        }
    }
    
    fun updateQuestion(question: String) {
        _uiState.value = _uiState.value.copy(currentQuestion = question)
    }
    
    fun sendQuestion() {
        val question = _uiState.value.currentQuestion.trim()
        if (question.isEmpty()) return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val apiKey = secureStorage.getApiKey()
                if (apiKey == null) {
                    val error = AppError.ApiKeyError("APIキーが設定されていません。設定画面で設定してください。")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = ErrorHandler.getUserFriendlyMessage(error)
                    )
                    return@launch
                }
                
                val mcpApiService: McpApiService by inject { parametersOf(apiKey) }
                
                mcpApiService.sendMessage(question).fold(
                    onSuccess = { answer ->
                        println("[ViewModel] Received answer (${answer.length} chars): ${answer.take(100)}...")
                        conversationRepository.insertConversation(question, answer)
                        println("[ViewModel] Conversation saved to database")
                        _uiState.value = _uiState.value.copy(
                            currentQuestion = "",
                            isLoading = false
                        )
                        mcpApiService.close()
                    },
                    onFailure = { exception ->
                        val appError = ErrorHandler.handleException(exception)
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = ErrorHandler.getUserFriendlyMessage(appError)
                        )
                        mcpApiService.close()
                    }
                )
            } catch (e: Exception) {
                val appError = ErrorHandler.handleException(e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = ErrorHandler.getUserFriendlyMessage(appError)
                )
            }
        }
    }
    
    fun startVoiceRecording() {
        if (!speechRecognizer.hasPermission()) {
            viewModelScope.launch {
                speechRecognizer.requestPermission()
            }
            return
        }
        
        viewModelScope.launch {
            speechRecognizer.startRecording()
        }
    }
    
    fun stopVoiceRecording() {
        viewModelScope.launch {
            speechRecognizer.stopRecording()
        }
    }
    
    fun speakAnswer(text: String) {
        viewModelScope.launch {
            textToSpeech.speak(text)
        }
    }
    
    fun stopSpeaking() {
        viewModelScope.launch {
            textToSpeech.stop()
        }
    }
    
    fun toggleFavorite(conversationId: String, isFavorite: Boolean) {
        viewModelScope.launch {
            conversationRepository.toggleFavorite(conversationId, isFavorite)
        }
    }
    
    fun deleteConversation(conversationId: String) {
        viewModelScope.launch {
            conversationRepository.deleteConversation(conversationId)
        }
    }
    
    fun toggleFavoritesFilter() {
        val showFavorites = !_uiState.value.showFavoritesOnly
        _uiState.value = _uiState.value.copy(showFavoritesOnly = showFavorites)
        
        if (showFavorites) {
            viewModelScope.launch {
                conversationRepository.getFavoriteConversations().collectLatest { conversations ->
                    _uiState.value = _uiState.value.copy(conversations = conversations)
                }
            }
        } else {
            observeConversations()
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}