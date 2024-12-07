package com.example.yukti

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import com.example.learningcompose.update.ShowUpdateDialog
import com.example.learningcompose.update.UpdateChecker

import com.example.yukti.navigation.AppNavigation

import com.example.yukti.navigation.Routes
import com.example.yukti.permission.RequestNotificationPermission
import com.example.yukti.sign_in.GoogleAuthUiClient
import com.example.yukti.ui.theme.YuktiTheme
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.database.FirebaseDatabase

class MainActivity : ComponentActivity() {
    private val googleAuthUiClient by lazy {
        GoogleAuthUiClient(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext)
        )
    }

    private var showDialog by mutableStateOf(false)
    private var apkUrl: String? by mutableStateOf(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)




        enableEdgeToEdge()




// Perform your update check here
        checkForUpdates()







// Determine the start destination based on login status


        // Set the content for the activity
        setContent {
            YuktiTheme {
                val chatViewModel = ViewModelProvider(this)[ChatViewModel::class.java]
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation( chatViewModel, googleAuthUiClient,applicationContext)

                    // Show update dialog if needed
                    if (showDialog && apkUrl != null) {
                        ShowUpdateDialog(
                            onDismiss = { showDialog = false },
                            onUpdate = { apkUrl?.let { downloadAndInstallApk(it) } }
                        )
                    }
                }
            }
        }
    }

    private fun checkForUpdates() {
        UpdateChecker.checkForUpdates(this) { url ->
            apkUrl = url
            showDialog = true
        }
    }

    private fun downloadAndInstallApk(apkUrl: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(apkUrl))
        startActivity(intent)
    }
}


