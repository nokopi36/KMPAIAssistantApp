package com.nokopi.kmpaiassistantapp.data.storage

import java.util.prefs.Preferences

class SecureStorageImpl : SecureStorage {
    
    private val preferences = Preferences.userNodeForPackage(SecureStorageImpl::class.java)
    
    override suspend fun saveApiKey(apiKey: String) {
        preferences.put(API_KEY_KEY, apiKey)
        preferences.flush()
    }
    
    override suspend fun getApiKey(): String? {
        return preferences.get(API_KEY_KEY, null)
    }
    
    override suspend fun clearApiKey() {
        preferences.remove(API_KEY_KEY)
        preferences.flush()
    }
    
    override suspend fun hasApiKey(): Boolean {
        return preferences.get(API_KEY_KEY, null) != null
    }
    
    companion object {
        private const val API_KEY_KEY = "mcp_api_key"
    }
}