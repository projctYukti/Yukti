package com.example.yukti.createbusiness.joinbusiness.businesschat

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import com.example.yukti.subscription.SubscriptionCache
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ChatViewModel : ViewModel() {
    // LiveData or State to hold the list of messages
    private val _messages = mutableStateListOf<ChatMessage>()
    val messages: List<ChatMessage> get() = _messages

    val businessId = SubscriptionCache.businessId

    // Function to fetch messages from Firebase
    fun fetchMessages(userUid: String, receiverUid: String) {
        val database = FirebaseDatabase.getInstance().getReference("chats").child("businessChats").child(businessId.toString())
        val chatRef = database.child(userUid).child(receiverUid)

        chatRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                _messages.clear()  // Clear existing messages
                for (messageSnapshot in snapshot.children) {
                    val message = messageSnapshot.getValue(ChatMessage::class.java)
                    message?.let { _messages.add(it) }  // Add to the messages list
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatViewModel", "Failed to load messages: ${error.message}")
            }
        })

    }

    // Function to send a message to Firebase
    fun sendMessage(userUid: String, receiverUid: String, message: String) {
        val chatMessage = ChatMessage(
            sender = userUid,
            receiver = receiverUid,
            message = message,
            timestamp = System.currentTimeMillis()
        )

        val database = FirebaseDatabase.getInstance().getReference("chats").child("businessChats").child(businessId.toString())
        val userChatRef = database.child(userUid).child(receiverUid).push()
        val receiverChatRef = database.child(receiverUid).child(userUid).push()

        // Push message to both users' chat nodes
        userChatRef.setValue(chatMessage)
        receiverChatRef.setValue(chatMessage)
    }
}
