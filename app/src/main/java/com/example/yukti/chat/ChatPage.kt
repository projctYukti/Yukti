package com.example.yukti.chat

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.yukti.MainActivity


import com.example.yukti.chat.components.ChatHeader
import com.example.yukti.sign_in.GoogleAuthUiClient
import com.example.yukti.sign_in.SignInScreen
import com.example.yukti.ui.theme.ColorModelMessage
import com.example.yukti.ui.theme.ColorUserMessage
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun ChatPage(chatViewModel: ChatViewModel,googleAuthUiClient : GoogleAuthUiClient
             ) {

    val errorState by chatViewModel.errorState.collectAsState()
    val context = LocalContext.current

    val chatId = FirebaseAuth.getInstance().currentUser?.uid ?: "default_chat"
    val onSignOut = rememberCoroutineScope() // Move the rememberCoroutineScope here

    val signOutAction = {
        // Use the coroutine scope here
        onSignOut.launch {
            try {
                googleAuthUiClient.signOut() // Call your suspend sign-out function


                // Show the success message
                Toast.makeText(context, "Signed Out", Toast.LENGTH_SHORT).show()

                // Kill the current activity and navigate to GoogleAuthUiClient (login activity)
                val intent = Intent(context, MainActivity::class.java)
                context.startActivity(intent)

                // Finish the current activity to prevent going back to it after sign-out
                if (context is Activity) {
                    context.finish()  // Close the current activity
                }

            } catch (e: Exception) {
                Toast.makeText(context, "Sign out failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(chatId) {
        chatViewModel.onChatScreenOpened(chatId)
    }

    LaunchedEffect(errorState) {
        errorState?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            chatViewModel._errorState.value = null
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ChatHeader(onSignOut = signOutAction)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing
                    .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom))
                .padding(start = 10.dp, end = 10.dp, bottom = 10.dp)// Respect system bars
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()

            ) {
                // Message list adjusts dynamically to keyboard
                MessageList(
                    modifier = Modifier
                        .weight(1f),
                    messageList = chatViewModel.messageList
                )

                // Message input stays above the keyboard
                MessageInput(
                    onMessageSend = {
                        chatViewModel.sendMessage(chatId, it)
                    }
                )
            }
        }
    }
}

@Composable
fun MessageList(modifier: Modifier = Modifier, messageList: List<MessageModel>) {
    LazyColumn(
        modifier = modifier,
        reverseLayout = true // Start from the bottom like chat apps
    ) {
        items(messageList.reversed()) { message ->
            MessaageRow(messageModel = message)
        }
    }
}

@Composable
fun MessaageRow(messageModel: MessageModel) {
    val isModel = messageModel.role == "model"

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .align(if (isModel) Alignment.BottomStart else Alignment.BottomEnd)
                    .padding(
                        start = if (isModel) 8.dp else 70.dp,
                        end = if (isModel) 70.dp else 8.dp,
                        top = 8.dp,
                        bottom = 8.dp
                    )
                    .clip(RoundedCornerShape(48f))
                    .background(if (isModel) ColorModelMessage else ColorUserMessage)
                    .padding(16.dp)
            ) {
                Text(
                    text = messageModel.message,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun MessageInput(onMessageSend: (String) -> Unit) {
    var message by remember { mutableStateOf("") }

    Row(
        modifier = Modifier
            .padding(8.dp)
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
        IconButton(onClick = {
            if (message.isNotBlank()) {
                onMessageSend(message)
                message = "" // Clear the message after sending
            }
        }) {
            Icon(imageVector = Icons.Default.Send, contentDescription = "Send Message")
        }
    }
}
