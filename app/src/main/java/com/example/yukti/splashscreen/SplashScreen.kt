package com.example.yukti.splashscreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.yukti.R
import kotlinx.coroutines.delay


@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    // Launch effect to handle delay
    LaunchedEffect(Unit) {
        delay(1000) // Splash screen delay (2 seconds)
        onTimeout()
    }

    // UI for the splash screen
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black), // Set your background color
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo), // Your app logo
            contentDescription = "App Logo",
            modifier = Modifier.size(150.dp) // Adjust size as needed
        )
    }
}
