package com.nokopi.kmpaiassistantapp.speech

import kotlinx.coroutines.flow.Flow

interface SpeechRecognizer {
    val isRecording: Flow<Boolean>
    val recognizedText: Flow<String>
    val error: Flow<String?>
    
    suspend fun startRecording()
    suspend fun stopRecording()
    fun hasPermission(): Boolean
    suspend fun requestPermission(): Boolean
}

expect class SpeechRecognizerImpl() : SpeechRecognizer