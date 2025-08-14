package com.nokopi.kmpaiassistantapp.speech

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

actual class SpeechRecognizerImpl : SpeechRecognizer {
    private val _isRecording = MutableStateFlow(false)
    override val isRecording: Flow<Boolean> = _isRecording.asStateFlow()
    
    private val _recognizedText = MutableStateFlow("")
    override val recognizedText: Flow<String> = _recognizedText.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    override val error: Flow<String?> = _error.asStateFlow()
    
    override suspend fun startRecording() {
        _isRecording.value = true
        _recognizedText.value = "音声認識の実装が必要です (Desktop)"
    }
    
    override suspend fun stopRecording() {
        _isRecording.value = false
    }
    
    override fun hasPermission(): Boolean = true
    
    override suspend fun requestPermission(): Boolean = true
}