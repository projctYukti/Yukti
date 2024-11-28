import com.google.firebase.firestore.auth.User

data class UserInfo(
    val username: String = "", // Default value to avoid issues with Firebase deserialization
    val email: String = "" // Default value to avoid issues with Firebase deserialization
)

data class Members(
    val uid: String,
    val user: UserInfo
)
