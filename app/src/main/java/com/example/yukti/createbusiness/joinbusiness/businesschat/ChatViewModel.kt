package com.example.yukti.createbusiness.joinbusiness.businesschat

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.yukti.subscription.SubscriptionCache
import com.google.firebase.database.*

class ChatViewModel : ViewModel() {

    // LiveData or State to hold the list of messages
    private val _messages = mutableStateListOf<ChatMessage>()
    val messages: List<ChatMessage> get() = _messages

    // Typing status
    private val _isTyping = mutableStateOf(false)
    val isTyping: Boolean get() = _isTyping.value

    private val businessId = SubscriptionCache.businessId
    private val database = FirebaseDatabase.getInstance()
    private val listeners = mutableMapOf<String, ValueEventListener>()

    // Function to fetch messages
    fun fetchMessages(senderUid: String, receiverUid: String) {
        val chatPath = generateChatPath(businessId.toString(), senderUid, receiverUid)
        val chatRef = database.getReference("chats/$chatPath/messages")

        val messageListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                _messages.clear() // Clear existing messages
                for (messageSnapshot in snapshot.children) {
                    val message = messageSnapshot.getValue(ChatMessage::class.java)
                    message?.let { _messages.add(it) }
                }
                // Sort messages by timestamp (optional if Firebase doesn't already do this)
                _messages.sortBy { it.timestamp }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatViewModel", "Failed to load messages: ${error.message}")
            }
        }
        chatRef.addValueEventListener(messageListener)
        listeners["messages"] = messageListener
    }

    // Function to send a message
    fun sendMessage(senderUid: String, receiverUid: String, message: String) {
        val chatMessage = ChatMessage(
            sender = senderUid,
            receiver = receiverUid,
            message = message,
            timestamp = System.currentTimeMillis()
        )

        val chatPath = generateChatPath(businessId.toString(), senderUid, receiverUid)
        val messageRef = database.getReference("chats/$chatPath/messages").push()

        messageRef.setValue(chatMessage)
            .addOnSuccessListener {
                Log.d("ChatViewModel", "Message sent successfully")
            }
            .addOnFailureListener {
                Log.e("ChatViewModel", "Failed to send message: ${it.message}")
            }
    }

    // Function to generate chat path
    private fun generateChatPath(businessId: String, senderUid: String, receiverUid: String): String {
        return if (senderUid < receiverUid) {
            "businessChats/$businessId/${senderUid}_$receiverUid"
        } else {
            "businessChats/$businessId/${receiverUid}_$senderUid"
        }
    }

    // Update typing status with debounce logic
    fun updateTypingStatus(senderUid: String, receiverUid: String, isTyping: Boolean) {
        val chatPath = generateChatPath(businessId.toString(), senderUid, receiverUid)
        val typingRef = database.getReference("chats/$chatPath/typing/$senderUid")

        typingRef.setValue(isTyping)
            .addOnSuccessListener {
                Log.d("ChatViewModel", "Typing status updated successfully")
            }
            .addOnFailureListener {
                Log.e("ChatViewModel", "Failed to update typing status: ${it.message}")
            }
    }

    // Listen for typing status
    fun listenForTypingStatus(senderUid: String, receiverUid: String) {
        val chatPath = generateChatPath(businessId.toString(), senderUid, receiverUid)
        val typingRef = database.getReference("chats/$chatPath/typing/$receiverUid")

        val typingListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                _isTyping.value = snapshot.getValue(Boolean::class.java) ?: false
                Log.e("ChatViewModel", "User is typing: ${_isTyping.value}")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatViewModel", "Failed to listen for typing status: ${error.message}")
            }
        }
        typingRef.addValueEventListener(typingListener)
        listeners["typing"] = typingListener
    }

    // Clear listeners when ViewModel is destroyed
    override fun onCleared() {
        super.onCleared()
        listeners.forEach { (key, listener) ->
            database.getReference("chats/$key").removeEventListener(listener)
        }
        listeners.clear()
    }
}
