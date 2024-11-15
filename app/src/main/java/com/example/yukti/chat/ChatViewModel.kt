package com.example.yukti.chat

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.collection.mutableIntListOf
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yukti.gitignore.Constants
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {


    val messageList by lazy { mutableStateListOf<MessageModel>() }



    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = Constants.apiKey
    )


    fun sendMessage(question: String) {

        viewModelScope.launch{
            val chat = generativeModel.startChat(
                history = messageList.map{
                    content(it.role){
                        text(it.message)
                    }
                }.toList()
            )

            messageList.add(MessageModel(question, "user"))
            messageList.add(MessageModel("Typing...", "model"))
            val response = chat.sendMessage(question)
            messageList.removeAt(messageList.size-1)
            messageList.add(MessageModel(response.text.toString(), "model"))

        }
    }
}
