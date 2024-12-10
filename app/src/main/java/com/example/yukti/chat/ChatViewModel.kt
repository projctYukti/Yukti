import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
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
import androidx.compose.runtime.State
import java.util.Locale

class ChatViewModel : ViewModel() {


    private val database: DatabaseReference = FirebaseDatabase.getInstance().getReference("chats")

    // List of messages
    val messageList = mutableStateListOf<MessageModel>()
    val chatId = FirebaseAuth.getInstance().currentUser?.uid ?: "default_chat"

    // Flow to handle errors (e.g., network or other issues)
     val _errorState = MutableStateFlow<String?>(null)
    val errorState: StateFlow<String?> = _errorState
    private val _inventoryResult = mutableStateOf<String?>(null)
    val inventoryResult: State<String?> = _inventoryResult

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    // Define a function to simulate loading
    fun setLoadingState(loading: Boolean) {
        _isLoading.value = loading
    }
    // Generative model client
    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = Constants().apiKey,
    )

    private var textToSpeech: TextToSpeech? = null
    // State to manage TTS status
    val isTTSActive = mutableStateOf(false)

    // Initialize TextToSpeech here if needed
    fun initTTS(context: Context) {
        textToSpeech = TextToSpeech(context, OnInitListener { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.setLanguage(Locale.US)
            } else {
                Log.e("TTS", "Initialization failed")
            }
        })
    }

    // Send message to generative AI model
    fun sendMessage(chatId: String, userMessage: String,businessId: String?,businessName: String,context: Context) {

        viewModelScope.launch {
            try {
                // Add the user's message to the local list and save to Firebase
                val userMessageModel = MessageModel(message = userMessage + "\n" + getCurrentDateTime(), role = "user", timestamp = getCurrentDateTime())
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
                // Regex to match seconds (e.g., :45, 45s)
                val secondsRegex = """\b(:\d{1,2}|\d{1,2}s)\b""".toRegex()
                val timeRegex = """\b\d{1,2}:\d{2}(:\d{2})?\b""".toRegex()
                val dateRegex ="""\b(\d{1,2}[/\-]\d{1,2}[/\-]\d{2,4}|\d{4}[/\-]\d{1,2}[/\-]\d{1,2}|\b(?:Today|Tomorrow|Yesterday)\b)\b""".toRegex(RegexOption.IGNORE_CASE)
                ttsHelper.speak(
                    modelResponse.text?.replace(dateRegex, "")
                        ?.replace(timeRegex, "")?.replace(secondsRegex, "")?.replace("\\s+".toRegex(), " ")?.trim().toString()
                )



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


     fun saveMessageToFirebase(
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
    fun loadMessagesAndFetchInventory(businessId: String?, businessName: String, context: Context) {
        _isLoading.value = true

        database.child("businessChats").child(businessId ?: "").child(businessName).child(chatId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    messageList.clear()
                    for (messageSnapshot in snapshot.children) {
                        val message = messageSnapshot.child("message").value as? String
                        val role = messageSnapshot.child("role").value as? String
                        if (message != null && role != null) {
                            messageList.add(MessageModel(message, role, getCurrentDateTime()))
                        }
                    }

                    // After loading messages, call the Gemini API
                    viewModelScope.launch {
                        val chatHistory = messageList.joinToString("\n") { "${it.role}: ${it.message}" }
                        val prompt = "Provide an inventory summary based on the following conversation and don't uses notes: \n$chatHistory"

                        try {
                            val generativeModel = GenerativeModel(
                                modelName = "gemini-1.5-flash",
                                apiKey = Constants().apiKey
                            )
                            val response = generativeModel.generateContent(prompt)
                            _inventoryResult.value = response.text.toString()
                        } catch (e: Exception) {
                            Log.e("ChatViewModel", "Error: ${e.message}")
                            _inventoryResult.value = "Error fetching inventory details."
                        } finally {
                            _isLoading.value = false
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ChatViewModel", "Error loading messages: ${error.message}")
                    _isLoading.value = false
                }
            })
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
    // Start Text-to-Speech
    fun startTextToSpeech(text: String) {
        if (isTTSActive.value) return // TTS is already active
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        isTTSActive.value = true
    }

    // Stop Text-to-Speech
    fun stopTextToSpeech() {
        textToSpeech?.stop()
        isTTSActive.value = false
    }

    // Release resources when the ViewModel is cleared
    override fun onCleared() {
        super.onCleared()
        textToSpeech?.shutdown()
    }




}
@Composable
fun geminiImagePrompt(
    photoBitmap: Bitmap,
    chatViewModel1: ChatViewModel,
    businessId: String,
    businessName: String,
    chatId: String
) {
    val context = LocalContext.current
    val resultText = remember { mutableStateOf<String?>(null) }
    val ttsHelper =  TTSHelper(context)
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
                            ttsHelper.speak(resultText.value.toString())


                            val userMessage = MessageModel(
                                message = "Image",
                                role = "user",
                                timestamp = chatViewModel1.getCurrentDateTime()
                            )
                            chatViewModel1.messageList.add(userMessage)
                            chatViewModel1.saveMessageToFirebase(chatId, userMessage,businessId,businessName)

                            // Add "Typing..." message to the local list (but do NOT save it to Firebase)
                           val typingMessage = MessageModel(message = "Typing...", role = "model", timestamp = chatViewModel1.getCurrentDateTime())
                            chatViewModel1.messageList.add(typingMessage)


                            val modelMessage = MessageModel(
                                message = result?.text.toString(),
                                role = "model",
                                timestamp = chatViewModel1.getCurrentDateTime()
                            )
                            chatViewModel1.messageList.add(modelMessage)
                            chatViewModel1.messageList.remove(typingMessage)

                            chatViewModel1.saveMessageToFirebase(chatId, modelMessage,businessId,businessName)


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

