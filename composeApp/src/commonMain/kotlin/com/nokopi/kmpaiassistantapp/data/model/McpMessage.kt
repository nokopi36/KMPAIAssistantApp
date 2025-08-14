package com.nokopi.kmpaiassistantapp.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Anthropic API with MCP servers
@Serializable
data class AnthropicRequest(
    @SerialName("model")
    val model: String = "claude-3-5-sonnet-20241022",
    @SerialName("max_tokens")
    val maxTokens: Int = 1024,
    @SerialName("messages")
    val messages: List<AnthropicMessage>,
    @SerialName("mcp_servers")
    val mcpServers: List<McpServerConfig>
)

@Serializable
data class AnthropicMessage(
    @SerialName("role")
    val role: String,
    @SerialName("content")
    val content: String
)

@Serializable
data class McpServerConfig(
    @SerialName("type")
    val type: String,
    @SerialName("url")
    val url: String,
    @SerialName("name")
    val name: String
)

@Serializable
data class AnthropicResponse(
    @SerialName("id")
    val id: String? = null,
    @SerialName("type")
    val type: String? = null,
    @SerialName("role")
    val role: String? = null,
    @SerialName("content")
    val content: List<AnthropicContent> = emptyList(),
    @SerialName("model")
    val model: String? = null,
    @SerialName("stop_reason")
    val stopReason: String? = null,
    @SerialName("usage")
    val usage: AnthropicUsage? = null,
    @SerialName("error")
    val error: AnthropicError? = null
)

@Serializable
data class AnthropicError(
    @SerialName("type")
    val type: String,
    @SerialName("message")
    val message: String
)

@Serializable
data class AnthropicContent(
    @SerialName("type")
    val type: String,
    @SerialName("text")
    val text: String? = null,
    @SerialName("tool_use")
    val toolUse: ToolUse? = null,
    @SerialName("tool_result")
    val toolResult: ToolResult? = null
)

@Serializable
data class ToolUse(
    @SerialName("id")
    val id: String,
    @SerialName("name")
    val name: String,
    @SerialName("input")
    val input: Map<String, String>
)

@Serializable
data class ToolResult(
    @SerialName("tool_use_id")
    val toolUseId: String,
    @SerialName("content")
    val content: String
)

@Serializable
data class AnthropicUsage(
    @SerialName("input_tokens")
    val inputTokens: Int,
    @SerialName("output_tokens")
    val outputTokens: Int
)