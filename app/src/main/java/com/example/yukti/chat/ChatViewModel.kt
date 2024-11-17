import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yukti.chat.MessageModel
import com.example.yukti.gitignore.Constants
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.net.UnknownHostException

class ChatViewModel : ViewModel() {

    // List of messages
    val messageList = mutableStateListOf<MessageModel>()

    // Flow to handle errors (e.g., network or other issues)
     val _errorState = MutableStateFlow<String?>(null)
    val errorState: StateFlow<String?> = _errorState

    // Generative model client
    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = Constants.apiKey
    )

    // Send message to generative AI model
    fun sendMessage(question: String) {
        viewModelScope.launch {
            try {
                // Start chat history from the message list
                val chat = generativeModel.startChat(
                    history = messageList.map {
                        content(it.role) {
                            text(it.message)
                        }
                    }.toList()
                )

                // Add user's question to the message list
                messageList.add(MessageModel(question, "user"))

                // Show "typing..." while waiting for the response
                messageList.add(MessageModel("Typing...", "model"))

                // Send the message and get a response
                val response = chat.sendMessage(question)

                // Update the message list with the model's response
                messageList.removeAt(messageList.size - 1)
                messageList.add(MessageModel(response.text.toString(), "model"))
            } catch (e: UnknownHostException) {
                // Handle network error (no internet connection)
                _errorState.value = "No internet connection. Please try again."
            } catch (e: Exception) {
                // Catch other exceptions
                _errorState.value = "Something went wrong. Please try again."
                e.printStackTrace()
                Log.e("ChatViewModel", "Error: ${e.message}")
            }
        }
    }
}
