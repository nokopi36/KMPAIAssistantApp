package com.nokopi.kmpaiassistantapp.util

import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

object RetryHandler {
    
    suspend fun <T> retryWithExponentialBackoff(
        maxRetries: Int = 3,
        initialDelay: Duration = 1.seconds,
        maxDelay: Duration = 10.seconds,
        factor: Double = 2.0,
        retryCondition: (Throwable) -> Boolean = { shouldRetry(it) },
        block: suspend () -> T
    ): T {
        var currentDelay = initialDelay
        var lastException: Throwable? = null
        
        repeat(maxRetries) { attempt ->
            try {
                return block()
            } catch (e: Throwable) {
                lastException = e
                if (attempt == maxRetries - 1 || !retryCondition(e)) {
                    throw e
                }
                
                delay(currentDelay)
                currentDelay = (currentDelay * factor).coerceAtMost(maxDelay)
            }
        }
        
        throw lastException!!
    }
    
    private fun shouldRetry(throwable: Throwable): Boolean {
        return when (throwable) {
            // Retry on network errors
            is io.ktor.client.network.sockets.SocketTimeoutException -> true
            is io.ktor.client.network.sockets.ConnectTimeoutException -> true
            is io.ktor.client.plugins.ServerResponseException -> {
                // Retry on 5xx server errors and 429 rate limit
                throwable.response.status.value in listOf(500, 502, 503, 504, 429)
            }
            // Don't retry on client errors (4xx except 429)
            is io.ktor.client.plugins.ClientRequestException -> {
                throwable.response.status.value == 429 // Only retry on rate limit
            }
            // Don't retry on serialization errors
            is kotlinx.serialization.SerializationException -> false
            // Retry on other network-related errors
            else -> throwable.message?.contains("network", ignoreCase = true) == true ||
                    throwable.message?.contains("connection", ignoreCase = true) == true
        }
    }
}