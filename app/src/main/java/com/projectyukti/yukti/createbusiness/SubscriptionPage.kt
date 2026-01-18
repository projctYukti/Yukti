package com.projectyukti.yukti.createbusiness

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.projectyukti.yukti.MainActivity
import com.projectyukti.yukti.navigation.Routes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionPage(navController: NavHostController) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(modifier = Modifier.padding(10.dp),
                title = { Text("ChatGPT Plus") },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate(Routes.chat){
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
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()

                    .padding(padding)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Description
                Text(
                    text = "Access our most powerful model and advanced features",

                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Features List
                val features = listOf(
                    "Up to 5x more messages on GPT-4o and access to GPT-4",
                    "Higher limits for photo and file uploads, web browsing, image generation, and data analysis",
                    "Access to Advanced Voice Mode",
                    "Use, create, and share custom GPTs",
                    "Early access to new features"
                )

                features.forEach { feature ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Check",
                            tint = Color.Green,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = feature,

                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Restore Subscription Text
                Text(
                    text = "Restore subscription",

                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.clickable {
                        Toast.makeText(context, "Restore Subscription Clicked", Toast.LENGTH_SHORT).show()
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Pricing Info
                Text(
                    text = "Auto-renews for ₹1,950.00/month until canceled",

                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Subscribe Button
                Button(
                    onClick = {
                        navController.navigate(Routes.businessSetup){

                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Text("Subscribe")
                }
            }
        }
    )
}
