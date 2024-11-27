package com.example.yukti.chat.components


import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.yukti.chat.components.menu.NavDrawerItems
import com.example.yukti.subscription.SubscriptionCache
import com.example.yukti.subscription.SubscriptionCache.businessName
import com.example.yukti.subscription.SubscriptionCache.isSubscribed
import com.example.yukti.subscription.SubscriptionChecker

import com.example.yukti.subscription.SubscriptionViewModel
import kotlinx.coroutines.Job


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatHeader(onSignOut: () -> Job,
               navItems: List<NavDrawerItems> = emptyList(), // Pass navigation items,
               onNavigationIconClick: () -> Unit={}){
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxWidth(),


    ) {

        var expanded by remember { mutableStateOf(false) }
        var headerText by remember { mutableStateOf("Chat") } // Default header text






       TopAppBar(
           modifier = Modifier.fillMaxWidth(),
//           colors = TopAppBarDefaults.topAppBarColors(
//               containerColor = Color(0xFF6200EE), // Replace with your desired color
//               titleContentColor = Color.White, // Title text color
//               navigationIconContentColor = Color.White // Navigation icon color
//           ),
           navigationIcon = {
               IconButton(onClick = onNavigationIconClick) {
                   Icon(imageVector = Icons.Default.Menu, contentDescription = "Menu")
               }
           },
           title = {

               if (isSubscribed){
                   headerText = businessName.toString()
               }else{

                   headerText = "Chat"
               }





                   // Show something else for non-subscribed users
                   Text(headerText,
                       textAlign = TextAlign.Center,
                       modifier = Modifier.fillMaxWidth())


                },
           actions = {
               IconButton(onClick = { expanded = !expanded }) {
                   Icon(Icons.Default.MoreVert, contentDescription = "More options")
               }
               DropdownMenu(
                   expanded = expanded,
                   onDismissRequest = { expanded = false },
                   properties = PopupProperties(focusable = true)
               ) {
                   DropdownMenuItem(
                       text = { Text("Sign Out") },
                       onClick = {
                           expanded = false
                           onSignOut()
                           fun reset() {
                               isSubscribed = false
                               businessName = null
                           }
                       }
                   )
               }
           })
    }
}
