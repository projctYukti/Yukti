package com.projectyukti.yukti.chat.components.menu

import android.widget.ImageButton
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

@Composable
fun DrawerHeader(){

    val userId = Firebase.auth.currentUser?.uid
    val profilePicUrl = remember { mutableStateOf<String?>(null) }  // Remember profile picture URL

    val database = FirebaseDatabase.getInstance()
    val userRef = database.getReference("users").child(userId.toString())


    // Fetch data asynchronously and update the state
    userRef.get().addOnSuccessListener { snapshot ->
        profilePicUrl.value = snapshot.child("profilePictureUrl").getValue(String::class.java)
    }
    Box(modifier = Modifier.fillMaxWidth()
        .padding(vertical = 60.dp),
        contentAlignment = Alignment.Center
        ){
        // Profile Picture Image
        Image(
            painter = rememberImagePainter(
                data = profilePicUrl.value,
                builder = {
                    crossfade(true) // Optional: smooth transition effect
                     }
            ),
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(100.dp) // Set size for the profile image
                .clip(CircleShape) // Make it circular
        )

    }
}
@Composable
fun DrawerBody(
    items: List<NavDrawerItems>,
    modifier: Modifier = Modifier,
    itemTextStyle : TextStyle = TextStyle(fontSize =  18.sp),
    onItemClick:   (NavDrawerItems) -> Unit

){
    LazyColumn(modifier){
        items(items){ item ->
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(16.dp)
                    .clickable{
                        onItemClick(item)

                    }.padding(16.dp)

            ){
                Icon(imageVector = item.icon, contentDescription = item.contentDescription)
                Spacer(modifier = Modifier.padding(16.dp))
                Text(text = item.title,
                    style = itemTextStyle,
                    modifier = Modifier.weight(1f))
            }

        }}

}