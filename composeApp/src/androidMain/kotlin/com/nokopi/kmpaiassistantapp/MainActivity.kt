package com.nokopi.kmpaiassistantapp

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.nokopi.kmpaiassistantapp.debug.DebugInfo
import com.nokopi.kmpaiassistantapp.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class AIAssistantApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@AIAssistantApplication)
            modules(appModule)
        }
        
        // デバッグ情報を出力
        DebugInfo.logKoinModules()
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}