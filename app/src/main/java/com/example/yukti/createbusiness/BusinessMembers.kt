package com.example.yukti.createbusiness

import ManageBusiness
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.yukti.navigation.Routes
import com.example.yukti.subscription.SubscriptionCache
import kotlinx.coroutines.launch

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
                MemberItem(member,navController)
            }
        }
    }
}

@Composable
fun MemberItem(member: Members,navController: NavHostController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 30.dp)
            .clickable{navController.navigate("businessChat/${member.user.username}/${member.uid}")},
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
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
