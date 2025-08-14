package com.nokopi.kmpaiassistantapp

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.nokopi.kmpaiassistantapp.di.appModule
import org.koin.core.context.startKoin

fun main() = application {
    // Koin DI初期化
    startKoin {
        modules(appModule)
    }
    
    Window(
        onCloseRequest = ::exitApplication,
        title = "KMPAIAssistantApp",
    ) {
        DesktopTestApp()
    }
}