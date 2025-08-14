package com.nokopi.kmpaiassistantapp.data.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

class SecureStorageImpl(private val context: Context) : SecureStorage {
    
    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    
    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        "secure_prefs",
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    override suspend fun saveApiKey(apiKey: String) {
        sharedPreferences.edit()
            .putString(API_KEY_KEY, apiKey)
            .apply()
    }
    
    override suspend fun getApiKey(): String? {
        return sharedPreferences.getString(API_KEY_KEY, null)
    }
    
    override suspend fun clearApiKey() {
        sharedPreferences.edit()
            .remove(API_KEY_KEY)
            .apply()
    }
    
    override suspend fun hasApiKey(): Boolean {
        return sharedPreferences.contains(API_KEY_KEY)
    }
    
    companion object {
        private const val API_KEY_KEY = "mcp_api_key"
    }
}