package com.example.yukti.chat.components


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.window.PopupProperties
import kotlinx.coroutines.Job


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatHeader(onSignOut: () -> Job){
    var expanded by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxWidth(),


    ) {
        // Top Bar
       TopAppBar(
           modifier = Modifier.fillMaxWidth(),
//           colors = TopAppBarDefaults.topAppBarColors(
//               containerColor = Color(0xFF6200EE), // Replace with your desired color
//               titleContentColor = Color.White, // Title text color
//               navigationIconContentColor = Color.White // Navigation icon color
//           ),
           title = { Text("Chat",
               textAlign = TextAlign.Center,
               modifier = Modifier.fillMaxWidth()) },
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
                       }
                   )
               }
           })
    }
}
