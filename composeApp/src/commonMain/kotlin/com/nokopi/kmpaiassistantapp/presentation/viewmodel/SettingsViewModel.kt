package com.nokopi.kmpaiassistantapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nokopi.kmpaiassistantapp.data.storage.SecureStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val apiKey: String = "",
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null,
    val hasApiKey: Boolean = false
)

class SettingsViewModel(
    private val secureStorage: SecureStorage
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    init {
        checkExistingApiKey()
    }
    
    private fun checkExistingApiKey() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val hasKey = secureStorage.hasApiKey()
                val apiKey = if (hasKey) secureStorage.getApiKey() ?: "" else ""
                _uiState.value = _uiState.value.copy(
                    hasApiKey = hasKey,
                    apiKey = if (hasKey) "••••••••••••••••" else "", // マスク表示
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "API Keyの取得に失敗しました: ${e.message}"
                )
            }
        }
    }
    
    fun updateApiKey(apiKey: String) {
        _uiState.value = _uiState.value.copy(
            apiKey = apiKey,
            isSaved = false,
            error = null
        )
    }
    
    fun saveApiKey() {
        val apiKey = _uiState.value.apiKey.trim()
        if (apiKey.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "API Keyを入力してください")
            return
        }
        
        if (apiKey == "••••••••••••••••") {
            _uiState.value = _uiState.value.copy(error = "新しいAPI Keyを入力してください")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                secureStorage.saveApiKey(apiKey)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSaved = true,
                    hasApiKey = true,
                    apiKey = "••••••••••••••••" // 保存後はマスク表示
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "API Keyの保存に失敗しました: ${e.message}"
                )
            }
        }
    }
    
    fun clearApiKey() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                secureStorage.clearApiKey()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    hasApiKey = false,
                    apiKey = "",
                    isSaved = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "API Keyの削除に失敗しました: ${e.message}"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun clearSavedFlag() {
        _uiState.value = _uiState.value.copy(isSaved = false)
    }
}