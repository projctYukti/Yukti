import com.google.firebase.firestore.auth.User

data class UserInfo(
    val username: String = "", // Default value to avoid issues with Firebase deserialization
    val email: String = "", // Default value to avoid issues with Firebase deserialization
    val profilePictureUrl: String = ""
)

data class Members(
    val userId: String,
    val user: UserInfo
)
