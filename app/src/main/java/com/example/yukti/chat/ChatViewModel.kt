package com.example.yukti.chat

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yukti.gitignore.Constants
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    private val _messageList = MutableStateFlow<List<MessageModel>>(emptyList())
    val messageList: StateFlow<List<MessageModel>> = _messageList

    // Simulated sendMessage function to add user and response messages
    fun sendMessage(question: String) {
        viewModelScope.launch {
            // Update messageList with the user's message
            val updatedMessages = _messageList.value.toMutableList().apply {
                add(MessageModel(question, "user"))
            }
            _messageList.value = updatedMessages

            // Simulate receiving a response from the model
            val response = "Response to: $question"
            _messageList.value = updatedMessages + MessageModel(response, "model")
        }
    }
}