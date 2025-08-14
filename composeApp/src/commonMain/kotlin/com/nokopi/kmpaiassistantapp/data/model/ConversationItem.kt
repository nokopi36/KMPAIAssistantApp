package com.nokopi.kmpaiassistantapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ConversationItem(
    val id: String,
    val question: String,
    val answer: String,
    val timestamp: Long,
    val isFavorite: Boolean = false
)