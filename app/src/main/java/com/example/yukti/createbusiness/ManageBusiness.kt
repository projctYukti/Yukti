import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class ManageBusiness {

    suspend fun fetchBusinessMembers(businessId: String): List<Members> {
        val db = FirebaseDatabase.getInstance()
        val businessRef = db.getReference("businesses").child(businessId).child("members")
        val usersRef = db.getReference("users")
        val members = mutableListOf<Members>()

        try {
            // Fetch member UIDs from the business
            val memberSnapshot: DataSnapshot = businessRef.get().await()
            Log.d("FetchBusinessMembers", "Member Snapshot: ${memberSnapshot.value}")

            for (child in memberSnapshot.children) {
                val uid = child.key
                if (uid == null) {
                    Log.e("FetchBusinessMembers", "UID is null for child: $child")
                    continue
                }
                Log.d("FetchBusinessMembers", "Fetching user details for UID: $uid")

                // Fetch user details for each UID
                val userSnapshot: DataSnapshot = usersRef.child(uid).get().await()
                Log.d("FetchBusinessMembers", "User Snapshot for UID $uid: ${userSnapshot.value}")

                // Deserialize the user data into UserInfo
                val user = userSnapshot.getValue(UserInfo::class.java)
                if (user == null) {
                    Log.e("FetchBusinessMembers", "UserInfo is null for UID: $uid")
                } else {
                    Log.d("FetchBusinessMembers", "Fetched UserInfo for UID $uid: $user")
                    members.add(Members(uid, user))
                }
            }
        } catch (e: Exception) {
            Log.e("FetchBusinessMembers", "Error fetching business members", e)
        }
        return members
    }
}
