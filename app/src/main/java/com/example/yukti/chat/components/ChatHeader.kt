package com.example.yukti.chat.components

import android.R
import android.R.color.black
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatHeader(){
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
               modifier = Modifier.fillMaxWidth()) })
    }
}
