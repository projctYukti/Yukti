package com.example.yukti.createbusiness

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.yukti.chat.SharedViewModel
import com.example.yukti.ui.theme.YuktiTheme

class SubscriptionActivity : ComponentActivity() {
    private lateinit var sharedViewModel: SharedViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedViewModel = ViewModelProvider(this).get(SharedViewModel::class.java)

        // You can now access the drawerState and scope from the ViewModel
        val drawerState = sharedViewModel.drawerState
        val scope = sharedViewModel.scope
        enableEdgeToEdge()
        setContent {
            YuktiTheme {}

        }
    }
}