package com.example.yukti.Home

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text

import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.yukti.subscription.SubscriptionCache
import com.example.yukti.subscription.SubscriptionCache.businessId
import com.example.yukti.subscription.SubscriptionCache.businessName
import com.example.yukti.subscription.SubscriptionCache.getSubscriptionDetails
import com.example.yukti.subscription.SubscriptionChecker
import com.example.yukti.subscription.SubscriptionViewModel


@Composable
fun HomePage(modifier: Modifier = Modifier) {
    val chatViewModel: ChatViewModel = viewModel()
    val context = LocalContext.current

    // Directly accessing the State in the ViewModel
    val inventoryResult = chatViewModel.inventoryResult.value
    // Collect isLoading state reactively
    val isLoadingState = chatViewModel.isLoading.collectAsState()
    val isLoading = isLoadingState.value
    val subscriptionViewModel= SubscriptionViewModel()
    val subscriptionChecker = SubscriptionChecker(context)
    LaunchedEffect(Unit) {
        val (isSubscribed, businessName , businessId ) = subscriptionChecker.checkSubscription()

        // Check if the values are being fetched correctly
        Log.d("ChatPage", "Fetched isSubscribed: $isSubscribed, businessName: $businessName")

        // Save to the singleton
        SubscriptionCache.isSubscribed = isSubscribed
        subscriptionViewModel.setSubscriptionStatus(isSubscribed) // Ensure this method is called
        Log.d("ChatPage", "Saved isSubscribed to Cache: ${SubscriptionCache.isSubscribed}")

        // Set business name
        SubscriptionCache.businessName = businessName
        subscriptionViewModel.setBusinessName(businessName.toString()) // Make sure it's set properly
        Log.d("ChatPage", "Saved businessName to Cache: ${SubscriptionCache.businessName}")

        SubscriptionCache.businessId = businessId
        subscriptionViewModel.setBusinessId(businessId.toString()) // Make sure it's set properly
        Log.d("ChatPage", "Saved businessId to Cache: ${SubscriptionCache.businessId}")

    }

    // Trigger loading when the page is first displayed
    LaunchedEffect(Unit) {
        chatViewModel.loadMessagesAndFetchInventory(getSubscriptionDetails(context).third, getSubscriptionDetails(context).second.toString(), context)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // Enable scrolling
            .padding(20.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Big Card at the Top
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp, top = 20.dp)
                .clickable { },
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "Inventory Status", style = MaterialTheme.typography.headlineMedium)

                Spacer(modifier = Modifier.height(16.dp))

                if (isLoading) {
                    Text(text = "Loading inventory details....")
                } else {
                    Text(
                        text = inventoryResult ?: "Your inventory is empty",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // Three Cards at the Bottom
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            repeat(3) {
                Card(
                    modifier = Modifier
                        .size(width = 110.dp, height = 125.dp)
                        .clickable { },
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Name: Yukti",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            repeat(3) {
                Card(
                    modifier = Modifier
                        .size(width = 110.dp, height = 125.dp)
                        .clickable { },
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Name: Yukti",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }

        // Big Card at the Top
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp, top = 20.dp)
                .clickable { },
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(6) {
                    Text(
                        text = "Your inventory is empty",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}



