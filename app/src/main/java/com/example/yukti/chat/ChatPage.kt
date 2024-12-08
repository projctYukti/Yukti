package com.example.yukti.chat

import android.app.Activity

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.provider.MediaStore
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AdfScanner
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.yukti.MainActivity


import com.example.yukti.chat.components.ChatHeader
import com.example.yukti.chat.components.menu.DrawerBody
import com.example.yukti.chat.components.menu.DrawerHeader
import com.example.yukti.chat.components.menu.NavDrawerItems
import com.example.yukti.navigation.Routes
import com.example.yukti.permission.CameraPermission
import com.example.yukti.permission.MicrophonePermission
import com.example.yukti.permission.RequestNotificationPermission
import com.example.yukti.sign_in.GoogleAuthUiClient
import com.example.yukti.subscription.SubscriptionCache
import com.example.yukti.subscription.SubscriptionCache.clearSubscriptionDetails
import com.example.yukti.subscription.SubscriptionCache.getSubscriptionDetails
import com.example.yukti.subscription.SubscriptionChecker
import com.example.yukti.subscription.SubscriptionViewModel
import com.example.yukti.texttospeach.TTSHelper
import com.example.yukti.ui.theme.ColorModelMessage
import com.example.yukti.ui.theme.ColorUserMessage
import com.google.firebase.auth.FirebaseAuth
import geminiImagePrompt
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun ChatPage(
    chatViewModel: ChatViewModel,
    googleAuthUiClient: GoogleAuthUiClient
    ,
    navController: NavHostController,

    modifier: Modifier

) {

    val subscriptionViewModel= SubscriptionViewModel()
    RequestNotificationPermission()
    val context = LocalContext.current

    val isSubscribed by subscriptionViewModel.isSubscribed.collectAsState()
    var businessName = subscriptionViewModel.businessName.value
    var businessId by remember { mutableStateOf("") }
    val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val subscriptionChecker = SubscriptionChecker(context)



    LaunchedEffect(Unit) {
        val (isSubscribed, businessName , businessId ) = subscriptionChecker.checkSubscription()

        // Check if the values are being fetched correctly
        Log.d("ChatPage", "Fetched isSubscribed: $isSubscribed, businessName: $businessName")

        // Save to the singleton
        SubscriptionCache.isSubscribed = isSubscribed
        subscriptionViewModel.setSubscriptionStatus(isSubscribed) // Ensure this method is called
        Log.d("ChatPage", "Saved isSubscribed to Cache: ${SubscriptionCache.isSubscribed}")

        // Set business name
        SubscriptionCache.businessName = businessName
        subscriptionViewModel.setBusinessName(businessName.toString()) // Make sure it's set properly
        Log.d("ChatPage", "Saved businessName to Cache: ${SubscriptionCache.businessName}")

        SubscriptionCache.businessId = businessId
        subscriptionViewModel.setBusinessId(businessId.toString()) // Make sure it's set properly
        Log.d("ChatPage", "Saved businessId to Cache: ${SubscriptionCache.businessId}")

    }





    val sharedViewModel: SharedViewModel = viewModel() // Using ViewModelProvider
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
//
//// Store the drawerState and scope in the ViewModel
//    sharedViewModel.drawerState = drawerState
//    sharedViewModel.scope = scope


    val errorState by chatViewModel.errorState.collectAsState()


    val chatId = FirebaseAuth.getInstance().currentUser?.uid ?: "default_chat"
    val onSignOut = rememberCoroutineScope() // Move the rememberCoroutineScope here
    val ttsHelper = remember { TTSHelper(context) }

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
                (context as? Activity)?.finish()
                clearSubscriptionDetails(context)

                // Finish the current activity to prevent going back to it after sign-out

            } catch (e: Exception) {
                Toast.makeText(context, "Sign out failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(chatId) {
        chatViewModel.onChatScreenOpened(chatId,getSubscriptionDetails(context).third,getSubscriptionDetails(context).second.toString())
        Log.d("premiumUsers", getSubscriptionDetails(context).first.toString())
    }

    LaunchedEffect(errorState) {
        errorState?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            chatViewModel._errorState.value = null
        }
    }

    DisposableEffect(context) {
        onDispose {
            ttsHelper.shutdown()  // Release resources
        }
    }
    LaunchedEffect(drawerState.isOpen) {
        if (drawerState.isOpen) {



            // Trigger specific actions when the drawer opens
            Log.d("Drawer", "Drawer opened")
            // You can add any specific actions here, like refreshing data
        } else {
            // Trigger actions when the drawer is closed
            Log.d("Drawer", "Drawer closed")
        }
    }


    // Retaining navItems based on subscription status
    val navItems = remember(getSubscriptionDetails(context)

    ) {
        Log.d("items",getSubscriptionDetails(context).toString())
        if (getSubscriptionDetails(context).first) {
            listOf(
                NavDrawerItems(
                    getSubscriptionDetails(context).second.toString(), // Use `orEmpty` to avoid null value
                    getSubscriptionDetails(context).second.toString(),
                    "Go to Manage business page",
                    icon = Icons.Default.Business
                ),
                NavDrawerItems(
                    "Business Members",
                    "Business Members",
                    "View Member List",
                    icon = Icons.Default.AccountCircle
                ),
                NavDrawerItems(
                    "Generate a bill",
                    "Generate a bill",
                    "Generate a bill",
                    icon = Icons.Default.AdfScanner
                ),

            )
        }
        else if (getSubscriptionDetails(context).first == false && getSubscriptionDetails(context).second != null){

            listOf(
                NavDrawerItems(
                    getSubscriptionDetails(context).second.toString(), // Use `orEmpty` to avoid null value
                    getSubscriptionDetails(context).second.toString(),
                    "Go to Manage business page",
                    icon = Icons.Default.Business
                ),
                NavDrawerItems(
                    "Business Members",
                    "Business Members",
                    "View Member List",
                    icon = Icons.Default.AccountCircle
                ),
                NavDrawerItems(
                    "Generate a bill",
                    "Generate a bill",
                    "Generate a bill",
                    icon = Icons.Default.AdfScanner
                ),

                )
        }

        else {
            listOf(
                NavDrawerItems(
                    "Create a Business",
                    "Create a Business",
                    "Go to Create a Business page",
                    icon = Icons.Default.Create
                ),
                NavDrawerItems(
                    "Join a Business",
                    "Join a Business",
                    "Go to Join a Business page",
                    icon = Icons.Default.AddCircle
                )
            )
        }
    }



    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            Column(
                modifier
                .fillMaxHeight()
                .background(Color.Gray) // Optional semi-transparent background

            ) {
            DrawerHeader()

            DrawerBody(
                items = navItems,
                onItemClick = { item ->

                    when (item.title) {

                        "Generate a bill"->{

//                            ExportChatData().exportChatData(context,
//                                getSubscriptionDetails(context).third.toString(),getSubscriptionDetails(context).second.toString(),
//                                currentUserUid)

                            }

                        "Business Members"->{
                            navController.navigate(Routes.businessMembers){
                                popUpTo(navController.graph.startDestinationId)
                            }}
                        "Create a Business" -> {
                            navController.navigate(Routes.subscriptionPage){
                                popUpTo(navController.graph.startDestinationId)
                            }

                        }"Join a Business" -> {
                        navController.navigate(Routes.joinBusiness)

                    }
                        else -> {
                            Log.d("items",item.title)
                            Toast.makeText(context, "Clicked: ${item.title}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    scope.launch{
                        drawerState.close()
                    }
                }
            )
        }}
    ) {

        Column(modifier) {
            ChatHeader(onSignOut = signOutAction,
                navItems = navItems,
                onNavigationIconClick = {
                    scope.launch {
                        if (drawerState.isClosed) drawerState.open() else drawerState.close()
                    }
                }
            )

            Box(
                modifier

                    .padding(start = 10.dp, end = 10.dp, bottom = 10.dp)// Respect system bars
            ) {
                Column(
                    modifier



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
                            chatViewModel.sendMessage(chatId, it,getSubscriptionDetails(context).third,getSubscriptionDetails(context).second.toString(),context)
                        },
                        context,
                        getSubscriptionDetails(context).third.toString(),getSubscriptionDetails(context).second.toString(),currentUserUid
                    )
                }
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
fun MessageInput(onMessageSend: (String) -> Unit,context: Context,businessId: String,businessName: String,currentUserUid: String) {
    var photoBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showGeminiPrompt by remember { mutableStateOf(false) }
    // Gemini Prompt Trigger
    if (showGeminiPrompt && photoBitmap != null) {
        geminiImagePrompt(photoBitmap!!)
        
        showGeminiPrompt = false // Reset the trigger after invoking Gemini
    }
    // Launcher for the camera intent
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            photoBitmap = result.data?.extras?.get("data") as? Bitmap
            showGeminiPrompt = true // Trigger Gemini prompt in a composable-safe way
            Toast.makeText(context, "Photo captured successfully", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Camera action canceled", Toast.LENGTH_SHORT).show()
        }
    }
    var message by remember { mutableStateOf("") }
    // Intent to capture speech
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0)
            if (!spokenText.isNullOrEmpty()) {
                message = spokenText

                onMessageSend(message)// Send the message after speech-to-text
                message = ""
            }
        }
    }

    Row(
        modifier = Modifier
            .padding(8.dp)
            .statusBarsPadding()


            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically

    ) {
        OutlinedTextField(
            modifier = Modifier.weight(1f).imePadding(),
            value = message,
            onValueChange = { message = it },
            label = { Text("Type a message") },
            shape = RoundedCornerShape(20.dp) ,
            leadingIcon = {
                IconButton(onClick = {
                    if (CameraPermission().checkAndRequestPermission(context as Activity)) {

                        // Handle camera button click (e.g., open camera)
                        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        try {
                            cameraLauncher.launch(cameraIntent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error starting camera: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "Permission not granted", Toast.LENGTH_SHORT).show()
                    }

                }) { }
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Camera"
                    )


            },
            trailingIcon = {
                IconButton(onClick = {
                    if (MicrophonePermission().checkAndRequestPermission(context as Activity)){
                        // Handle microphone button click (e.g., start voice input)
                        if (SpeechRecognizer.isRecognitionAvailable(context)) {
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...")
                            }
                            speechRecognizerLauncher.launch(intent)
                        } else {
                            Toast.makeText(context, "Speech Recognition not available", Toast.LENGTH_SHORT).show()}
                    }else{
                        Toast.makeText(context, "Permission not granted", Toast.LENGTH_SHORT).show()
                    }


                }) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Microphone"
                    )
                }
            },
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




