package com.example.yukti.createbusiness

import ManageBusiness
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberImagePainter
import com.example.yukti.animations.TripleOrbitLoadingAnimation
import com.example.yukti.navigation.Routes
import com.example.yukti.subscription.SubscriptionCache
import com.example.yukti.subscription.SubscriptionCache.businessId
import com.example.yukti.ui.theme.ColorUserMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun businessMembers(navController: NavHostController) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var members by remember { mutableStateOf<List<Members>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    var adminId by remember { mutableStateOf("") } // Mutable state for adminId
    // Fetch the adminId when the composable is launched
    LaunchedEffect(Unit) {
        ManageBusiness().fetchAdminId(
            businessId = businessId.toString(),
            onSuccess = { fetchedAdminId ->
                adminId = fetchedAdminId // Update adminId when fetched
                Log.d("businessMembers", "Fetched Admin ID: $adminId")
            },
            onFailure = { error ->
                Log.e("businessMembers", "Error fetching adminId: $error")
            }
        )
    }



    // Fetch members when the Composable is launched
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            isLoading = true
            val businessId = SubscriptionCache.getSubscriptionDetails(context).third
            members = ManageBusiness().fetchBusinessMembers(businessId.toString()) // Fetch members using business ID
            isLoading = false

        }
    }
    Column(modifier = Modifier.fillMaxSize().padding(start = 10.dp,end=10.dp)) {
        // Chat header (show receiver's name)
        TopAppBar(
            title = {
                Text(
                    "Business members", textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            navigationIcon = {
                IconButton(onClick = {
                    navController.navigate(Routes.chat) {
                        popUpTo(navController.graph.startDestinationId)
                        {
                            inclusive = true
                        }
                    }

                }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
        )

        // Display the UI
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                TripleOrbitLoadingAnimation(modifier = Modifier.size(100.dp))
            }
        } else {
            LazyColumn(

                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)

            ) {
                items(members) { member ->
                    MemberItem(member, navController, adminId.toString(),currentUserUid)
                    Log.d("adminId and currentUser", " adminId and currentUser is 1:$adminId $currentUserUid")


                }
            }
        }
    }
}

@Composable
fun MemberItem(member: Members, navController: NavHostController, adminId: String, currentUserUid: String) {
    Log.d("adminId and currentUser", " adminId and currentUser is2 :$adminId $currentUserUid")
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .clickable {
                navController.navigate("businessChat/${member.user.username}/${member.userId}/${Uri.encode(member.user.profilePictureUrl)}")
            },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically // Align items vertically in the center
        ) {
            // Profile Picture
            Box {
                Image(
                    painter = rememberImagePainter(
                        data = member.user.profilePictureUrl.toString(),
                        builder = {
                            crossfade(true) // Optional: smooth transition effect
                        }
                    ),
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(50.dp) // Set size for the profile image
                        .clip(CircleShape) // Make it circular
                        .background(Color.Gray) // Placeholder background
                )

                // "Owner" or "Me" badge
                if (member.userId == adminId || member.userId == currentUserUid) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd) // Align badge to the bottom-right corner of the profile picture
                            .offset(x = 10.dp, y = 10.dp) // Offset to ensure proper positioning
                            .background(ColorUserMessage, CircleShape) // Circular badge
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (member.userId == adminId) "Owner" else "Me",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp)) // Add spacing between the image and text

            // Member details
            Column {
                Text(
                    text = "Name: ${member.user.username}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Email: ${member.user.email}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
