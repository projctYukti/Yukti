package com.projectyukti.yukti.splashscreen



import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

import com.projectyukti.yukti.MainActivity
import com.projectyukti.yukti.ui.theme.YuktiTheme
import kotlinx.coroutines.delay

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Use the native splash screen API for Android 12+ (API 31 and above

            // Install the splash screen
            installSplashScreen()


            startActivity(Intent(this, MainActivity::class.java))
                        finish() // Close SplashActivity




        } else {

            // For devices below Android 12, use the manual splash screen
            enableEdgeToEdge()
            setContent {
                YuktiTheme {
                    SplashScreen{
                        startActivity(Intent(this, MainActivity::class.java))
                        finish() // Close SplashActivity
                    }
                }
            }

        }
    }
}
