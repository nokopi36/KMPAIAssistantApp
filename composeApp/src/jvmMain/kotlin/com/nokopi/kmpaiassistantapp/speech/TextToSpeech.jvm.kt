package com.nokopi.kmpaiassistantapp.speech

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.delay

actual class TextToSpeechImpl : TextToSpeech {
    private val _isSpeaking = MutableStateFlow(false)
    override val isSpeaking: Flow<Boolean> = _isSpeaking.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    override val error: Flow<String?> = _error.asStateFlow()
    
    override suspend fun speak(text: String) {
        _isSpeaking.value = true
        delay(2000) // シミュレート
        _isSpeaking.value = false
    }
    
    override suspend fun stop() {
        _isSpeaking.value = false
    }
    
    override suspend fun pause() {
        _isSpeaking.value = false
    }
    
    override suspend fun resume() {
        _isSpeaking.value = true
    }
    
    override fun setSpeed(speed: Float) {}
    
    override fun setPitch(pitch: Float) {}
}