package com.example.yukti.createbusiness.joinbusiness.businesschat

import android.provider.CalendarContract
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.yukti.subscription.SubscriptionCache.businessId
import com.example.yukti.ui.theme.ColorModelMessage
import com.example.yukti.ui.theme.ColorUserMessage
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun businessChatPage( receiverUsername: String, receiverUid: String) {
    // Get the ViewModel instance
    val chatViewModel: ChatViewModel = viewModel()

    val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val currentUsername = FirebaseAuth.getInstance().currentUser?.displayName ?: ""



    // Fetch messages when the screen is loaded
    LaunchedEffect(key1 = true) {
        chatViewModel.fetchMessages(currentUserUid, receiverUid)
    }
    // Get the list of messages from the ViewModel
    val messages = chatViewModel.messages

    var message by remember { mutableStateOf("") }
    Column(modifier = Modifier.fillMaxSize().padding(start = 10.dp,end=10.dp)) {
        // Chat header (show receiver's name)
        TopAppBar(title = {
            Text(
                receiverUsername, textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        })

        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(
                    WindowInsets.safeDrawing
                        .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
                )

        ) {


            Column(
                modifier = Modifier
                    .fillMaxSize()

            ) {

                // Display chat messages
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxSize(), reverseLayout = true,

                ) {
                    itemsIndexed(messages.reversed()) { index, chatMessage ->
                        ChatBubble(
                            chatMessage = chatMessage,
                            isCurrentUser = chatMessage.sender == currentUserUid,
                            currentUsername,
                            receiverUsername
                        )
                    }
                }

                // Message input field and send button
                Row(
                    modifier = Modifier.padding(8.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        modifier = Modifier.weight(1f),
                        value = message,
                        onValueChange = { message = it },
                        label = { Text("Type a message") },
                        shape = RoundedCornerShape(20.dp) // Border radius

                    )
                    IconButton(
                        onClick = {
                            if (message.isNotEmpty()) {
                                chatViewModel.sendMessage(currentUserUid, receiverUid, message)
                                message = ""  // Clear the input field
                            }
                        },
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = "Send Message")
                    }
                }
            }

        }

    }
}

@Composable
fun ChatBubble(chatMessage: ChatMessage, isCurrentUser: Boolean, currentUsername: String, receiverUsername: String) {
    Log.d("ChatMessage", chatMessage.toString()+" "+isCurrentUser+" "+currentUsername+" "+receiverUsername)
    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,

    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .align(if (isCurrentUser) Alignment.BottomEnd else Alignment.BottomStart)
                    .padding(
                        start = if (isCurrentUser) 4.dp else 10.dp,
                        end = if (isCurrentUser) 10.dp else 4.dp,
                        top = 8.dp,
                        bottom = 8.dp
                    )
                    .clip(RoundedCornerShape(48f))
                    .background(if (isCurrentUser) ColorUserMessage else ColorModelMessage)
                    .padding(16.dp)
            ) {
                Column {

                    Text(
                        if (isCurrentUser) {
                            currentUsername
                        } else {
                            receiverUsername
                        },
                        color = Color.White,


                    ) // Different text color based on user)


                    Text(
                        text = chatMessage.message,
                        color = Color.White,


                    ) // Different text color based on user)
                }
            }
        }
    }
}






