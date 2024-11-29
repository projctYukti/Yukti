package com.example.yukti.createbusiness.joinbusiness.businesschat

import android.R.attr.title
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun businessChatPage(username: String, uid : String ) {

    TopAppBar(
        modifier = Modifier.fillMaxWidth(),
    title = {
        Text(username,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth())
    })

    }

