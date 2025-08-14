package com.nokopi.kmpaiassistantapp.data.repository

import com.nokopi.kmpaiassistantapp.data.database.DatabaseDriverFactory
import com.nokopi.kmpaiassistantapp.data.model.ConversationItem
import com.nokopi.kmpaiassistantapp.database.AppDatabase
import com.benasher44.uuid.uuid4
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock

class ConversationRepository(databaseDriverFactory: DatabaseDriverFactory) {
    private val database = AppDatabase(databaseDriverFactory.createDriver())
    
    private val _conversationsFlow = MutableStateFlow<List<ConversationItem>>(emptyList())
    
    init {
        refreshConversations()
    }
    
    fun getAllConversations(): Flow<List<ConversationItem>> {
        refreshConversations()
        return _conversationsFlow.asStateFlow()
    }
    
    private fun refreshConversations() {
        val conversations = database.conversationQueries.selectAll().executeAsList()
        _conversationsFlow.value = conversations.map { conversation ->
            ConversationItem(
                id = conversation.id,
                question = conversation.question,
                answer = conversation.answer,
                timestamp = conversation.timestamp,
                isFavorite = conversation.is_favorite == 1L
            )
        }
        println("[Repository] Refreshed conversations: ${conversations.size} items")
    }
    
    fun getFavoriteConversations(): Flow<List<ConversationItem>> {
        val conversations = database.conversationQueries.selectFavorites().executeAsList()
        return flowOf(conversations.map { conversation ->
            ConversationItem(
                id = conversation.id,
                question = conversation.question,
                answer = conversation.answer,
                timestamp = conversation.timestamp,
                isFavorite = true
            )
        })
    }
    
    suspend fun insertConversation(question: String, answer: String): String {
        val id = uuid4().toString()
        val timestamp = Clock.System.now().toEpochMilliseconds()
        println("[Repository] Inserting conversation: Q='${question.take(50)}...' A='${answer.take(50)}...'")
        database.conversationQueries.insertConversation(
            id = id,
            question = question,
            answer = answer,
            timestamp = timestamp,
            is_favorite = 0L
        )
        refreshConversations() // データベース更新後にFlowを更新
        return id
    }
    
    suspend fun toggleFavorite(id: String, isFavorite: Boolean) {
        database.conversationQueries.updateFavoriteStatus(
            is_favorite = if (isFavorite) 1L else 0L,
            id = id
        )
    }
    
    suspend fun deleteConversation(id: String) {
        database.conversationQueries.deleteById(id)
    }
    
    suspend fun deleteAllConversations() {
        database.conversationQueries.deleteAll()
    }
}