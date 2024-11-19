import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yukti.chat.MessageModel
import com.example.yukti.gitignore.Constants
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.ServerException
import com.google.ai.client.generativeai.type.content
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.net.UnknownHostException

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
        apiKey = Constants.apiKey
    )

    // Send message to generative AI model
    fun sendMessage(chatId: String, userMessage: String) {
        viewModelScope.launch {
            try {
                // Add the user's message to the local list and save to Firebase
                val userMessageModel = MessageModel(message = userMessage, role = "user")
                messageList.add(userMessageModel)
                saveMessageToFirebase(chatId, userMessageModel)

                // Prepare chat history (include loaded Firebase messages)
                val chatHistory = messageList.joinToString("\n") {
                    "${it.role}: ${it.message}"
                }



                // Send chat history to the generative AI model
                val modelResponse = generativeModel.generateContent(chatHistory)

                // Add the model's response to the local list and save to Firebase
                val modelMessageModel = MessageModel(message = modelResponse.text.toString(), role = "model")
                messageList.add(modelMessageModel)
                saveMessageToFirebase(chatId, modelMessageModel)

            } catch (e: Exception) {
                _errorState.value = "Something went wrong. Please try again."
                Log.d("Gemini", e.toString())
                e.printStackTrace()
            }catch (e : ServerException){
                _errorState.value = "The model is overloaded. Please try again later."

                e.printStackTrace()
            }
        }
    }


    private fun saveMessageToFirebase(chatId: String, message: MessageModel) {
        val messagesRef = database.child("chats").child(chatId).child("messages")
        messagesRef.push().setValue(message)
            .addOnSuccessListener {
                Log.d("ChatViewModel", "Message saved successfully.")
            }
            .addOnFailureListener { e ->
                Log.e("ChatViewModel", "Failed to save message: ${e.message}")
            }
    }

    fun loadChatMessages(chatId: String) {
        database.child("chats").child(chatId).child("messages")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val newMessageList = mutableListOf<MessageModel>()

                    // Fetch messages from Firebase
                    for (messageSnapshot in snapshot.children) {
                        val message = messageSnapshot.child("message").value as? String
                        val role = messageSnapshot.child("role").value as? String
                        if (message != null && role != null) {
                            newMessageList.add(MessageModel(message = message, role = role))
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
            })
    }

    fun onChatScreenOpened(chatId: String) {
        loadChatMessages(chatId)
    }


}
