package com.nokopi.kmpaiassistantapp.di

import com.nokopi.kmpaiassistantapp.data.database.DatabaseDriverFactory
import com.nokopi.kmpaiassistantapp.data.storage.SecureStorage
import com.nokopi.kmpaiassistantapp.data.storage.SecureStorageImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

actual val platformModule = module {
    single { DatabaseDriverFactory(androidContext()) }
    single<SecureStorage> { SecureStorageImpl(androidContext()) }
}