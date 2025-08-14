package com.nokopi.kmpaiassistantapp.data.storage

interface SecureStorage {
    suspend fun saveApiKey(apiKey: String)
    suspend fun getApiKey(): String?
    suspend fun clearApiKey()
    suspend fun hasApiKey(): Boolean
}