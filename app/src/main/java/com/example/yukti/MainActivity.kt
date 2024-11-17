package com.example.yukti

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.learningcompose.update.ShowUpdateDialog
import com.example.learningcompose.update.UpdateChecker

import com.example.yukti.chat.ChatPage

import com.example.yukti.chat.MessageModel
import com.example.yukti.sign_in.GoogleAuthUiClient
import com.example.yukti.sign_in.SignInScreen
import com.example.yukti.sign_in.SignInViewModel
import com.example.yukti.ui.theme.YuktiTheme
import com.google.android.gms.auth.api.identity.Identity
import kotlinx.coroutines.launch

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
        checkForUpdates()
        enableEdgeToEdge()

        // Create ChatViewModel


        // Set content for the activity
        setContent {
            YuktiTheme {
                val chatViewModel = ViewModelProvider(this)[ChatViewModel::class.java]
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // The navigation controller
                    val navController = rememberNavController()

                    // Navigation between screens
                    NavHost(navController = navController, startDestination = "sign_in") {
                        // Sign-in screen
                        composable("sign_in") {
                            val viewModel = viewModel<SignInViewModel>()
                            val state by viewModel.state.collectAsStateWithLifecycle()

                            LaunchedEffect(key1 = Unit) {
                                if (googleAuthUiClient.getSignedInUser() != null){
                                    navController.navigate("chat"){
                                    popUpTo("sign_in") { inclusive = true }}

                                }
                            }

                            // Handle Google sign-in result
                            val launcher = rememberLauncherForActivityResult(
                                contract = ActivityResultContracts.StartIntentSenderForResult(),
                                onResult = { result ->
                                    if (result.resultCode == RESULT_OK) {
                                        lifecycleScope.launch {
                                            val signInResult = googleAuthUiClient.signInWithIntent(
                                                intent = result.data ?: return@launch
                                            )
                                            viewModel.onSignInResult(signInResult)
                                        }
                                    }
                                }
                            )

                            LaunchedEffect(key1 = state.isSignInSuccessful) {
                                if (state.isSignInSuccessful) {
                                    Toast.makeText(
                                        applicationContext,
                                        "Sign in successful",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    // Navigate to chat after successful sign-in
                                    navController.navigate("chat") {
                                        popUpTo("sign_in") { inclusive = true }
                                    }
                                }
                            }

                            val context = LocalContext.current

                            // Sign-in screen UI
                            SignInScreen(
                                state = state,
                                onSignInClick = {
                                    lifecycleScope.launch {
                                        val signInIntentSender = googleAuthUiClient.signIn()

                                        // Check if the sign-in intent sender is null
                                        if (signInIntentSender == null) {
                                            println("Failed to get Sign-In Intent: Ensure configuration is correct.")
                                            Toast.makeText(context, "Failed to get Sign-In Intent", Toast.LENGTH_SHORT).show()
                                        } else {
                                            // Launch the sign-in intent sender
                                            launcher.launch(IntentSenderRequest.Builder(signInIntentSender).build())
                                        }
                                    }
                                },
                                navController = navController
                            )
                        }

                        // Chat screen
                        composable("chat") {
                            // Pass ChatViewModel to ChatPage
                            ChatPage(chatViewModel = chatViewModel)
                        }
                    }

                    // Show the update dialog if needed
                    if (showDialog && apkUrl != null) {
                        ShowUpdateDialog(
                            onDismiss = { showDialog = false },
                            onUpdate = {
                                apkUrl?.let { downloadAndInstallApk(it) }
                            }
                        )
                    }
                }
            }
        }
    }

    // Function to check for updates
    private fun checkForUpdates() {
        val context = this

        UpdateChecker.checkForUpdates(context) { url ->
            apkUrl = url
            showDialog = true
        }
    }

    // Function to download and install the APK if there's an update
    private fun downloadAndInstallApk(apkUrl: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(apkUrl))
        startActivity(intent)
    }
}

