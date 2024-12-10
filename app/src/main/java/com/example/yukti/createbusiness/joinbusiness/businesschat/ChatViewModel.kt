package com.example.yukti.createbusiness.joinbusiness.businesschat

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.yukti.notifications.SendMessageNotification
import com.example.yukti.subscription.SubscriptionCache
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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

    fun sendMessage(senderUid: String, receiverUid: String, message: String) {
        val chatMessage = ChatMessage(
            sender = senderUid,
            receiver = receiverUid,
            message = message + "\n" + getCurrentDateTime(),
            timestamp = getCurrentDateTime(),

        )

        val chatPath = generateChatPath(businessId.toString(), senderUid, receiverUid)
        val messageRef = FirebaseDatabase.getInstance().getReference("chats/$chatPath/messages").push()
        val currentUserName = FirebaseAuth.getInstance().currentUser?.displayName ?: "Unknown User"

        // Send the message
        messageRef.setValue(chatMessage)
            .addOnSuccessListener {
                // After message is successfully saved, fetch the FCM token of the receiver
                FirebaseDatabase.getInstance().getReference("users").child(receiverUid)
                    .child("fcmToken").get()
                    .addOnSuccessListener { snapshot ->
                        if (snapshot.exists()) {
                            val fcmToken = snapshot.getValue(String::class.java)
                            if (!fcmToken.isNullOrEmpty()) {
                                // Send the notification with the fetched FCM token
                                Log.e("FCM", "Got Fcm token for receiver: $fcmToken")
                                SendMessageNotification().sendNotificationToUser(fcmToken, currentUserName, message)
                            } else {
                                Log.e("FCM", "FCM token is null or empty for receiver: $receiverUid")
                            }
                        } else {
                            Log.e("FCM", "FCM token not found for receiver: $receiverUid")
                        }
                    }
                    .addOnFailureListener { exception ->
                        // Handle failure if the FCM token retrieval fails
                        Log.e("FCM", "Failed to retrieve FCM token for receiver: $receiverUid, ${exception.message}")
                    }

                Log.d("ChatViewModel", "Message sent successfully to $receiverUid")
            }
            .addOnFailureListener {
                Log.e("ChatViewModel", "Failed to send message to $receiverUid: ${it.message}")
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
    fun getCurrentDateTime(): String {
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss") // You can customize the format
        return currentDateTime.format(formatter)
    }
}
