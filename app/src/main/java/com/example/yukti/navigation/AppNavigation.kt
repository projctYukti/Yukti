package com.example.yukti.navigation

import android.app.Activity.RESULT_OK
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.yukti.chat.ChatPage
import com.example.yukti.createbusiness.BusinessSetupPage
import com.example.yukti.createbusiness.SubscriptionPage
import com.example.yukti.createbusiness.businessMembers

import com.example.yukti.createbusiness.joinbusiness.JoinBusinessPage
import com.example.yukti.createbusiness.joinbusiness.businesschat.businessChatPage
import com.example.yukti.navigation.Routes.businessMembers
import com.example.yukti.sign_in.GoogleAuthUiClient
import com.example.yukti.sign_in.SignInScreen
import com.example.yukti.sign_in.SignInViewModel

import com.example.yukti.subscription.SubscriptionViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun AppNavigation(
    startDestination: String,
    chatViewModel: ChatViewModel,
    googleAuthUiClient: GoogleAuthUiClient,
    applicationContext: Context
) {

    val corutineScope = rememberCoroutineScope()
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""


    // Navigation controller
    val navController = rememberNavController()

    // Navigation host
    NavHost(navController = navController, startDestination = startDestination) {
        // Sign-in screen
        composable(Routes.signIn) {
            val viewModel = viewModel<SignInViewModel>()
            val state by viewModel.state.collectAsStateWithLifecycle()

            // Handle Google sign-in result
            val launcher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartIntentSenderForResult(),
                onResult = { result ->
                    if (result.resultCode == RESULT_OK) {
                        corutineScope.launch {
                            val signInResult = googleAuthUiClient.signInWithIntent(
                                intent = result.data ?: return@launch
                            )
                            viewModel.onSignInResult(signInResult)
                        }
                    }
                }
            )

            LaunchedEffect(state.isSignInSuccessful) {
                if (state.isSignInSuccessful) {
                    Toast.makeText(
                        applicationContext,
                        "Sign in successful",
                        Toast.LENGTH_LONG
                    ).show()
                    navController.navigate(Routes.chat) {
                        popUpTo("sign_in") { inclusive = true }
                    }
                }
            }

            val context = LocalContext.current

            SignInScreen(
                state = state,
                onSignInClick = {
                    corutineScope.launch {
                        val signInIntentSender = googleAuthUiClient.signIn()
                        if (signInIntentSender != null) {
                            launcher.launch(
                                IntentSenderRequest.Builder(signInIntentSender).build()
                            )
                        } else {
                            Toast.makeText(
                                context,
                                "Failed to get Sign-In Intent",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                },
                navController = navController
            )
        }

        // Chat screen
        composable(Routes.chat) {
            ChatPage(
                chatViewModel = chatViewModel, googleAuthUiClient = googleAuthUiClient,navController,
                subscriptionViewModel = SubscriptionViewModel())
        }
        composable(Routes.subscriptionPage) {
            SubscriptionPage(navController)
        }
        composable(Routes.businessSetup) {
            BusinessSetupPage(navController, userId.toString())
        }
        composable(Routes.joinBusiness) {
            JoinBusinessPage(navController = navController, userId = userId.toString())
        }
        composable(Routes.businessMembers) {
            businessMembers(navController = navController)
        }
        composable(
            route = "businessChat/{username}/{uid}",
            arguments = listOf(
                navArgument("username") { type = NavType.StringType },
                navArgument("uid") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: ""
            val uid = backStackEntry.arguments?.getString("uid") ?: ""
            businessChatPage(username = username, uid = uid)
        }


    }

}