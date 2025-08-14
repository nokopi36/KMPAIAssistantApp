package com.nokopi.kmpaiassistantapp.speech

import kotlinx.coroutines.flow.Flow

interface TextToSpeech {
    val isSpeaking: Flow<Boolean>
    val error: Flow<String?>
    
    suspend fun speak(text: String)
    suspend fun stop()
    suspend fun pause()
    suspend fun resume()
    fun setSpeed(speed: Float)
    fun setPitch(pitch: Float)
}

expect class TextToSpeechImpl() : TextToSpeech