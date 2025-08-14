package com.nokopi.kmpaiassistantapp.di

import com.nokopi.kmpaiassistantapp.data.database.DatabaseDriverFactory
import com.nokopi.kmpaiassistantapp.data.network.McpApiService
import com.nokopi.kmpaiassistantapp.data.repository.ConversationRepository
import com.nokopi.kmpaiassistantapp.data.storage.SecureStorage
import com.nokopi.kmpaiassistantapp.presentation.viewmodel.MainViewModel
import com.nokopi.kmpaiassistantapp.presentation.viewmodel.SettingsViewModel
import com.nokopi.kmpaiassistantapp.speech.SpeechRecognizer
import com.nokopi.kmpaiassistantapp.speech.SpeechRecognizerImpl
import com.nokopi.kmpaiassistantapp.speech.TextToSpeech
import com.nokopi.kmpaiassistantapp.speech.TextToSpeechImpl
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

expect val platformModule: org.koin.core.module.Module

val appModule = module {
    includes(platformModule)
    
    single { ConversationRepository(get()) }
    
    factory { (apiKey: String) ->
        McpApiService(apiKey = apiKey)
    }
    
    single<SpeechRecognizer> { SpeechRecognizerImpl() }
    
    single<TextToSpeech> { TextToSpeechImpl() }
    
    viewModel {
        MainViewModel(
            conversationRepository = get<ConversationRepository>(),
            secureStorage = get<SecureStorage>(),
            speechRecognizer = get<SpeechRecognizer>(),
            textToSpeech = get<TextToSpeech>()
        )
    }
    
    viewModel {
        SettingsViewModel(
            secureStorage = get<SecureStorage>()
        )
    }
}