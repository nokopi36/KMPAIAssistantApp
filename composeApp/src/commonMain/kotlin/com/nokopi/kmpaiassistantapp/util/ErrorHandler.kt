package com.nokopi.kmpaiassistantapp.util

import kotlinx.serialization.SerializationException
import kotlin.Exception

sealed class AppError(override val message: String) : Exception(message) {
    data class NetworkError(override val message: String) : AppError(message)
    data class ApiKeyError(override val message: String) : AppError(message)
    data class AuthenticationError(override val message: String) : AppError(message)
    data class SerializationError(override val message: String) : AppError(message)
    data class UnknownError(override val message: String) : AppError(message)
}

object ErrorHandler {
    
    fun handleException(exception: Throwable): AppError {
        return when (exception) {
            is io.ktor.client.network.sockets.SocketTimeoutException -> {
                AppError.NetworkError("接続がタイムアウトしました。インターネット接続を確認してください。")
            }
            is io.ktor.client.network.sockets.ConnectTimeoutException -> {
                AppError.NetworkError("サーバーに接続できませんでした。インターネット接続を確認してください。")
            }
            is io.ktor.client.plugins.ClientRequestException -> {
                when (exception.response.status.value) {
                    400 -> AppError.NetworkError("リクエストが無効です。")
                    401 -> AppError.AuthenticationError("APIキーが無効です。設定を確認してください。")
                    403 -> AppError.AuthenticationError("APIキーの権限が不足しています。")
                    404 -> AppError.NetworkError("APIエンドポイントが見つかりません。")
                    429 -> AppError.NetworkError("リクエストが多すぎます。しばらく待ってからお試しください。")
                    else -> AppError.NetworkError("サーバーエラーが発生しました (${exception.response.status.value})")
                }
            }
            is io.ktor.client.plugins.ServerResponseException -> {
                when (exception.response.status.value) {
                    500 -> AppError.NetworkError("サーバー内部エラーが発生しました。しばらく待ってからお試しください。")
                    502 -> AppError.NetworkError("サーバーが利用できません。しばらく待ってからお試しください。")
                    503 -> AppError.NetworkError("サービスが一時的に利用できません。しばらく待ってからお試しください。")
                    else -> AppError.NetworkError("サーバーエラーが発生しました (${exception.response.status.value})")
                }
            }
            is SerializationException -> {
                AppError.SerializationError("データの解析に失敗しました。")
            }
            is AppError -> exception
            else -> AppError.UnknownError(exception.message ?: "予期しないエラーが発生しました")
        }
    }
    
    fun getUserFriendlyMessage(error: AppError): String {
        return error.message
    }
}