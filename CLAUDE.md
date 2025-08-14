# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Kotlin Multiplatform (KMP) + Compose Multiplatform (CMP) AI Assistant application that integrates with Anthropic's API using MCP (Model Context Protocol) connectors. The app provides voice/text input for questions and retrieves answers from the Yumemi Open Handbook via MCP server integration.

**Target Platforms**: Android, iOS, Desktop (JVM)
**Note**: WASM target was removed due to SQLDelight compatibility issues.

## Build Commands

```bash
# Compile for Android (debug)
./gradlew compileDebugKotlinAndroid

# Build Android APK
./gradlew assembleDebug

# Build for all platforms
./gradlew build

# Run on desktop
./gradlew :composeApp:run

# Clean build
./gradlew clean
```

## Key Architecture Components

### MCP Integration
- **McpApiService** connects to Anthropic API with `mcp_servers` parameter
- Uses `https://api.anthropic.com/v1/messages` endpoint
- Required headers: `x-api-key`, `anthropic-version: 2023-06-01`, `anthropic-beta: mcp-client-2025-04-04`
- MCP server configuration for Yumemi OpenHandbook: `https://openhandbook.mcp.yumemi.jp/sse`
- Extended timeouts (2 minutes) for MCP processing
- Automatic retry with exponential backoff

### Database Layer
- **SQLDelight** for cross-platform database operations
- Schema: `composeApp/src/commonMain/sqldelight/com/nokopi/kmpaiassistantapp/database/Conversation.sq`
- **ConversationRepository** uses `MutableStateFlow` for reactive UI updates
- Must call `refreshConversations()` after database mutations for UI to update

### Dependency Injection
- **Koin** framework with expect/actual pattern for platform modules
- **McpApiService** is factory-scoped with dynamic API key injection: `factory { (apiKey: String) -> McpApiService(apiKey) }`
- Platform-specific modules defined in `di/PlatformModule.{platform}.kt`

### Secure Storage
- Cross-platform API key storage using expect/actual pattern
- Android: EncryptedSharedPreferences
- iOS: Keychain Services  
- JVM: Java Preferences
- Interface: `SecureStorage` with suspend functions

### Navigation Flow
- Setup screen → Main screen → Settings screen
- App checks API key availability to determine start destination
- Uses Navigation Compose with conditional routing

## Development Notes

### JSON Serialization
- Kotlinx Serialization with `encodeDefaults = true` to ensure all fields are serialized
- Anthropic API requires specific request structure with `mcp_servers` array

### Error Handling
- **ErrorHandler** utility categorizes exceptions into user-friendly messages
- **RetryHandler** implements exponential backoff for network failures
- Comprehensive error types: NetworkError, ApiKeyError, AuthenticationError, SerializationError

### Speech Integration
- expect/actual pattern for SpeechRecognizer and TextToSpeech
- Platform-specific implementations handle permissions and native APIs

### Platform-Specific Considerations
- Use `MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)` for Android EncryptedSharedPreferences
- Navigation Compose version should be `2.8.0-alpha10` for Compose Multiplatform compatibility
- SQLDelight driver factory requires platform-specific implementations

## Testing MCP Integration

When testing API calls, look for these log patterns:
```
[MCP] Sending request to: https://api.anthropic.com/v1/messages  
[MCP] Raw response: {"id":"msg_...", "content":[...]}
[MCP] Final response text length: {number}
[Repository] Refreshed conversations: {count} items
```

The app extracts multiple text content blocks from MCP responses and combines them for display.