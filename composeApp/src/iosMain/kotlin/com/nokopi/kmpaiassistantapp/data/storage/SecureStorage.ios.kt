package com.nokopi.kmpaiassistantapp.data.storage

import kotlinx.cinterop.*
import platform.Foundation.*
import platform.Security.*

class SecureStorageImpl : SecureStorage {
    
    override suspend fun saveApiKey(apiKey: String) {
        val query = mutableMapOf<CFStringRef?, Any>().apply {
            put(kSecClass, kSecClassGenericPassword)
            put(kSecAttrService, SERVICE_NAME)
            put(kSecAttrAccount, API_KEY_ACCOUNT)
            put(kSecValueData, apiKey.encodeToByteArray().toNSData())
            put(kSecAttrAccessible, kSecAttrAccessibleWhenUnlockedThisDeviceOnly)
        }
        
        // 既存のキーを削除
        SecItemDelete(query as CFDictionaryRef)
        
        // 新しいキーを保存
        SecItemAdd(query as CFDictionaryRef, null)
    }
    
    override suspend fun getApiKey(): String? {
        val query = mutableMapOf<CFStringRef?, Any>().apply {
            put(kSecClass, kSecClassGenericPassword)
            put(kSecAttrService, SERVICE_NAME)
            put(kSecAttrAccount, API_KEY_ACCOUNT)
            put(kSecReturnData, kCFBooleanTrue)
            put(kSecMatchLimit, kSecMatchLimitOne)
        }
        
        val result = memScoped {
            val resultRef = alloc<CFTypeRefVar>()
            val status = SecItemCopyMatching(query as CFDictionaryRef, resultRef.ptr)
            
            if (status == errSecSuccess) {
                val data = resultRef.value as NSData
                return data.toByteArray().decodeToString()
            }
            null
        }
        
        return result
    }
    
    override suspend fun clearApiKey() {
        val query = mutableMapOf<CFStringRef?, Any>().apply {
            put(kSecClass, kSecClassGenericPassword)
            put(kSecAttrService, SERVICE_NAME)
            put(kSecAttrAccount, API_KEY_ACCOUNT)
        }
        
        SecItemDelete(query as CFDictionaryRef)
    }
    
    override suspend fun hasApiKey(): Boolean {
        return getApiKey() != null
    }
    
    companion object {
        private const val SERVICE_NAME = "KMPAIAssistantApp"
        private const val API_KEY_ACCOUNT = "mcp_api_key"
    }
}

private fun ByteArray.toNSData(): NSData {
    return NSMutableData().apply {
        if (isNotEmpty()) {
            this@toNSData.usePinned { pinned ->
                appendBytes(pinned.addressOf(0), this@toNSData.size.toULong())
            }
        }
    }
}

private fun NSData.toByteArray(): ByteArray {
    return ByteArray(length.toInt()).apply {
        usePinned { pinned ->
            getBytes(pinned.addressOf(0), length)
        }
    }
}