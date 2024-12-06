import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.yukti.chat.MessageModel
import com.example.yukti.createbusiness.ExportChatData
import com.example.yukti.gitignore.Constants
import com.example.yukti.texttospeach.TTSHelper
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.java.GenerativeModelFutures
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.GenerateContentResponse
import com.google.ai.client.generativeai.type.ServerException
import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ChatViewModel : ViewModel() {


    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("chats")

    // List of messages
    val messageList = mutableStateListOf<MessageModel>()
    val chatId = FirebaseAuth.getInstance().currentUser?.uid ?: "default_chat"

    // Flow to handle errors (e.g., network or other issues)
     val _errorState = MutableStateFlow<String?>(null)
    val errorState: StateFlow<String?> = _errorState

    // Generative model client
    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = Constants().apiKey,
    )

    // Send message to generative AI model
    fun sendMessage(chatId: String, userMessage: String,businessId: String?,businessName: String,context: Context) {

        viewModelScope.launch {
            try {
                // Add the user's message to the local list and save to Firebase
                val userMessageModel = MessageModel(message = userMessage + getCurrentDateTime(), role = "user", timestamp = getCurrentDateTime())
                val ttsHelper =  TTSHelper(context)
                val keywords = listOf("generateBill","generate", "bill", "createInvoice", "generateReport", "makeBill","invoice")
                var chatHistory: String
                var typingMessage: MessageModel

                messageList.add(userMessageModel)
                Log.d("userMessageModel",userMessageModel.toString())
                saveMessageToFirebase(chatId, userMessageModel, businessId, businessName)

                if (keywords.any { keyword -> userMessage.contains(keyword, ignoreCase = true)}){
                    // Add "Typing..." message to the local list (but do NOT save it to Firebase)
                    typingMessage = MessageModel(message = "Generating a bill ...", role = "model", timestamp = getCurrentDateTime())
                    messageList.add(typingMessage)

                    chatHistory = messageList.filter { it.message != "Generating a bill ..." }.joinToString("\n") {
                        "${it.role}: ${it.message}"
                    }

                    }else{

                    // Add "Typing..." message to the local list (but do NOT save it to Firebase)
                    typingMessage = MessageModel(message = "Typing...", role = "model", timestamp = getCurrentDateTime())
                    messageList.add(typingMessage)


                    // Prepare chat history (include loaded Firebase messages)

                    chatHistory = messageList.filter { it.message != "Typing..." }.joinToString("\n") {
                        "${it.role}: ${it.message}"
                    }

                    }







                // Send chat history to the generative AI model
                val modelResponse = generativeModel.generateContent(chatHistory)
                ttsHelper.speak(modelResponse.text.toString())



                // Remove "Typing..." message from the local list
                messageList.remove(typingMessage)

                // Add the model's response to the local list and save to Firebase
                val modelMessageModel = MessageModel(message = modelResponse.text.toString(), role = "model", timestamp = getCurrentDateTime())
                messageList.add(modelMessageModel)
                saveMessageToFirebase(chatId, modelMessageModel,businessId,businessName)
                if (keywords.any { keyword -> userMessage.contains(keyword, ignoreCase = true) } && modelResponse.text.toString() != null){
                    ExportChatData().exportChatData(context, modelResponse.text.toString())
                }

//            } catch (e: Exception) {
//                _errorState.value = "Something went wrong. Please try again."
//                Log.d("Gemini", e.toString())
//                e.printStackTrace()
            }catch (e : ServerException){
                _errorState.value = "The model is overloaded. Please try again later."
                Log.d("Gemini", e.toString())
                e.printStackTrace()
            }catch (e : SocketTimeoutException){
                _errorState.value = "Connection timed out. Please check your internet connection and try again."
                Log.d("Gemini", e.toString())
                e.printStackTrace()
            }
        }
    }


    private fun saveMessageToFirebase(
        chatId: String,
        message: MessageModel,

        businessId: String?,
        businessName: String
    ) {
        if (businessId!=null){val messagesRef = database.child("businessChats").child(businessId).child(businessName).child(chatId)
            messagesRef.push().setValue(message)
                .addOnSuccessListener {
                    Log.d("ChatViewModel", "Message saved successfully.")
                }
                .addOnFailureListener { e ->
                    Log.e("ChatViewModel", "Failed to save message: ${e.message}")
                }}else{
        val messagesRef = database.child("chats").child(chatId).child("messages")
        messagesRef.push().setValue(message)
            .addOnSuccessListener {
                Log.d("ChatViewModel", "Message saved successfully.")
            }
            .addOnFailureListener { e ->
                Log.e("ChatViewModel", "Failed to save message: ${e.message}")
            }}

    }

    fun loadChatMessages(chatId: String, businessId: String?, businessName: String) {
        Log.d("Business name and Id" , "$businessId + $businessName")
        if (businessId!=null){database.child("businessChats").child(businessId).child(businessName).child(chatId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val newMessageList = mutableListOf<MessageModel>()

                    // Fetch messages from Firebase
                    for (messageSnapshot in snapshot.children) {
                        val message = messageSnapshot.child("message").value as? String
                        val role = messageSnapshot.child("role").value as? String
                        if (message != null && role != null) {
                            newMessageList.add(MessageModel(message = message, role = role, timestamp = getCurrentDateTime()))
                        }
                    }

                    // Update the UI with the loaded messages (preserve order)
                    // Reverse if messages in Firebase are stored in reverse order
                    messageList.clear()
                    messageList.addAll(newMessageList)

                    Log.d("ChatViewModel", "Chat messages loaded: ${messageList.size}")
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ChatViewModel", "Error loading messages: ${error.message}")
                    _errorState.value = "Failed to load messages. Please try again."
                }
            })}else{
        database.child("chats").child(chatId).child("messages")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val newMessageList = mutableListOf<MessageModel>()

                    // Fetch messages from Firebase
                    for (messageSnapshot in snapshot.children) {
                        val message = messageSnapshot.child("message").value as? String
                        val role = messageSnapshot.child("role").value as? String
                        if (message != null && role != null) {
                            newMessageList.add(MessageModel(message = message, role = role, timestamp = getCurrentDateTime()))
                        }
                    }

                    // Update the UI with the loaded messages (preserve order)
                    // Reverse if messages in Firebase are stored in reverse order
                    messageList.clear()
                    messageList.addAll(newMessageList)

                    Log.d("ChatViewModel", "Chat messages loaded: ${messageList.size}")
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ChatViewModel", "Error loading messages: ${error.message}")
                    _errorState.value = "Failed to load messages. Please try again."
                }
            })}
    }

    fun onChatScreenOpened(chatId: String,  businessId: String?,businessName: String) {
        loadChatMessages(chatId,businessId,businessName)
    }


    fun getCurrentDateTime(): String {
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss") // You can customize the format
        return currentDateTime.format(formatter)
    }




}
@Composable
fun geminiImagePrompt(photoBitmap: Bitmap) {
    val context = LocalContext.current
    val resultText = remember { mutableStateOf<String?>(null) }
    val isError = remember { mutableStateOf(false) }

    LaunchedEffect(photoBitmap) {
        try {
            val apiKey = Constants().apiKey // Replace with your actual API key
            val modelName = "gemini-1.5-flash"
            val gm = GenerativeModel(modelName, apiKey)
            val model = GenerativeModelFutures.from(gm)

            val content = Content.Builder()
                .text("What is this?")
                .image(photoBitmap)
                .build()

            val response: ListenableFuture<GenerateContentResponse> = model.generateContent(content)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                Futures.addCallback(
                    response,
                    object : FutureCallback<GenerateContentResponse> {
                        override fun onSuccess(result: GenerateContentResponse?) {
                            resultText.value = result?.text
                            Toast.makeText(context, result?.text.toString(), Toast.LENGTH_LONG).show()

                        }

                        override fun onFailure(t: Throwable) {
                            t.printStackTrace()
                            isError.value = true
                        }
                    },
                    context.mainExecutor
                )
            } else {
                Toast.makeText(context, "Requires API 28 or higher", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            isError.value = true
        }
    }

    if (isError.value) {
        Toast.makeText(context, "Error in processing request", Toast.LENGTH_SHORT).show()
    }

    resultText.value?.let { result ->
        // Display result text in the UI
        ResultDisplay(result)
    }


}
@Composable
fun ResultDisplay(result: String) {
    val chatViewModel: ChatViewModel = viewModel()
//    Update the messageList state
            LaunchedEffect(result) {
                chatViewModel.messageList.add(
                    MessageModel(result, "model", chatViewModel.getCurrentDateTime())
                )
            }


    Column {
        Text(text = "Gemini Result:")
        Text(text = result)
    }
}

