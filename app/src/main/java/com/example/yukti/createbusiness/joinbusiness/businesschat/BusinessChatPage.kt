package com.example.yukti.createbusiness.joinbusiness.businesschat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.yukti.subscription.SubscriptionCache.businessId
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun businessChatPage( receiverUsername: String, receiverUid: String) {
    // Get the ViewModel instance
    val chatViewModel: ChatViewModel = viewModel()

    val userUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val username = FirebaseAuth.getInstance().currentUser?.displayName ?: ""



    // Fetch messages when the screen is loaded
    LaunchedEffect(key1 = true) {
        chatViewModel.fetchMessages(userUid, receiverUid)
    }
    // Get the list of messages from the ViewModel
    val messages = chatViewModel.messages

    var message by remember { mutableStateOf("") }
    Column(modifier = Modifier.fillMaxSize()) {
        // Chat header (show receiver's name)
        TopAppBar(title = { Text(receiverUsername) })

        // Display chat messages
        LazyColumn(modifier = Modifier.weight(1f).padding(16.dp), reverseLayout = true) {
            itemsIndexed(messages) { index, chatMessage ->
                ChatBubble(chatMessage = chatMessage, isCurrentUser = chatMessage.sender == username)
            }
        }

        // Message input field and send button
        Row(modifier = Modifier.padding(16.dp)) {
            TextField(
                value = message,
                onValueChange = { message = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type your message...") }
            )
            Button(
                onClick = {
                    if (message.isNotEmpty()) {
                       chatViewModel.sendMessage(userUid, receiverUid, message)
                        message = ""  // Clear the input field
                    }
                },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("Send")
            }
        }
    }
}

@Composable
fun ChatBubble(chatMessage: ChatMessage, isCurrentUser: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .background(
                    if (isCurrentUser) Color.Blue else Color.Gray,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(8.dp)
        ) {
            Column {
                Text(text = chatMessage.sender, color = Color.White)
                Text(text = chatMessage.message, color = Color.White)
            }
        }
    }
}





