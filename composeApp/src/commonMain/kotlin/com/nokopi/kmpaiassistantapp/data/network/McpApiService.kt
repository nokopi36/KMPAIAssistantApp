package com.nokopi.kmpaiassistantapp.data.network

import com.nokopi.kmpaiassistantapp.data.model.AnthropicRequest
import com.nokopi.kmpaiassistantapp.data.model.AnthropicResponse
import com.nokopi.kmpaiassistantapp.data.model.AnthropicMessage
import com.nokopi.kmpaiassistantapp.data.model.McpServerConfig
import com.nokopi.kmpaiassistantapp.util.RetryHandler
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.seconds

class McpApiService(
    private val apiKey: String
) {
    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true  // デフォルト値も含めてエンコード
            })
        }
        install(Logging) {
            level = LogLevel.ALL
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 120000  // 2分
            connectTimeoutMillis = 30000   // 30秒
            socketTimeoutMillis = 120000   // 2分
        }
    }
    
    private val baseUrl = "https://api.anthropic.com/v1"
    
    suspend fun sendMessage(message: String): Result<String> {
        return try {
            val responseText = RetryHandler.retryWithExponentialBackoff(
                maxRetries = 2,  // MCPは処理が重いのでリトライ回数を減らす
                initialDelay = 2.seconds,
                maxDelay = 30.seconds
            ) {
                val request = AnthropicRequest(
                    model = "claude-3-5-sonnet-20241022",
                    maxTokens = 1024,
                    messages = listOf(
                        AnthropicMessage(
                            role = "user",
                            content = message
                        )
                    ),
                    mcpServers = listOf(
                        McpServerConfig(
                            type = "url",
                            url = "https://openhandbook.mcp.yumemi.jp/sse",
                            name = "yumemi-openhandbook"
                        )
                    )
                )
                
                println("[MCP] Sending request to: $baseUrl/messages")
                println("[MCP] Request body: $request")
                println("[MCP] MCP processing may take some time, please wait...")
                
                // リクエストをJSONとして出力して確認
                val json = Json { 
                    prettyPrint = true
                    encodeDefaults = true  // デフォルト値も含めてエンコード
                }
                val requestJson = json.encodeToString(AnthropicRequest.serializer(), request)
                println("[MCP] Request JSON: $requestJson")
                
                val httpResponse = httpClient.post("$baseUrl/messages") {
                    contentType(ContentType.Application.Json)
                    setBody(request)
                    headers {
                        append("x-api-key", apiKey)
                        append("anthropic-version", "2023-06-01")
                        append("anthropic-beta", "mcp-client-2025-04-04")
                    }
                }
                
                val responseString = httpResponse.body<String>()
                println("[MCP] Raw response: $responseString")
                
                // レスポンスをパースする前に構造を確認
                val response: AnthropicResponse = try {
                    httpResponse.body<AnthropicResponse>()
                } catch (e: Exception) {
                    println("[MCP] Failed to parse as AnthropicResponse: ${e.message}")
                    // 簡単なテキストレスポンスとして扱う
                    return@retryWithExponentialBackoff responseString
                }
                
                println("[MCP] Received response: $response")
                
                // エラーチェック
                if (response.error != null) {
                    val errorMessage = "API Error: ${response.error!!.type} - ${response.error!!.message}"
                    println("[MCP] $errorMessage")
                    return@retryWithExponentialBackoff errorMessage
                }
                
                println("[MCP] Response content count: ${response.content.size}")
                response.content.forEachIndexed { index, content ->
                    println("[MCP] Content[$index]: type=${content.type}, text=${content.text}")
                }
                
                // MCPレスポンスから全てのテキストコンテンツを結合して回答を構築
                val textContents = response.content
                    .filter { it.type == "text" && !it.text.isNullOrEmpty() }
                    .mapNotNull { it.text }
                
                println("[MCP] Found ${textContents.size} text contents")
                textContents.forEachIndexed { index, text ->
                    println("[MCP] Text content[$index]: ${text.take(100)}...")
                }
                
                val responseText = when {
                    textContents.isNotEmpty() -> {
                        // 全てのテキストコンテンツを結合
                        textContents.joinToString("\n\n")
                    }
                    // ツールの結果から回答を取得（フォールバック）
                    response.content.any { it.type == "tool_result" } -> {
                        response.content.first { it.type == "tool_result" }.toolResult?.content ?: "ツール実行結果が空です。"
                    }
                    else -> "回答を取得できませんでした。"
                }
                
                println("[MCP] Final response text length: ${responseText.length}")
                
                println("[MCP] Extracted text: $responseText")
                responseText
            }
            
            Result.success(responseText)
        } catch (e: Exception) {
            println("[MCP] Exception occurred: ${e::class.simpleName}: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    fun close() {
        httpClient.close()
    }
}