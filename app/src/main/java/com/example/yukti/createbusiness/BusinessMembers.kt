package com.example.yukti.createbusiness

import ManageBusiness
import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberImagePainter
import com.example.yukti.navigation.Routes
import com.example.yukti.subscription.SubscriptionCache
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun businessMembers(navController: NavHostController) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var members by remember { mutableStateOf<List<Members>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

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
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(

                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)

            ) {
                items(members) { member ->
                    MemberItem(member, navController)
                    Log.d("profilePictureUrl",member.user.profilePictureUrl.toString())
                }
            }
        }
    }
}

@Composable
fun MemberItem(member: Members,navController: NavHostController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .clickable{navController.navigate("businessChat/${member.user.username}/${member.uid}")},

        elevation = CardDefaults.cardElevation(4.dp)
    ) {

        Box(modifier = Modifier.fillMaxWidth()
            .padding(vertical = 6.dp),
            contentAlignment = Alignment.Center
        ){
            // Profile Picture Image
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
            )

        }
        Row(modifier = Modifier.padding(16.dp)) {
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
