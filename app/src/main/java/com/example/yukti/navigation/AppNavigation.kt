package com.example.yukti.navigation

import android.app.Activity.RESULT_OK
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

import com.example.yukti.Home.HomePage
import com.example.yukti.Insights.Insights

import com.example.yukti.NavItems
import com.example.yukti.chat.ChatPage
import com.example.yukti.createbusiness.BusinessSetupPage
import com.example.yukti.createbusiness.SubscriptionPage
import com.example.yukti.createbusiness.businessMembers

import com.example.yukti.createbusiness.joinbusiness.JoinBusinessPage
import com.example.yukti.createbusiness.joinbusiness.businesschat.businessChatPage
import com.example.yukti.sign_in.GoogleAuthUiClient
import com.example.yukti.sign_in.SignInScreen
import com.example.yukti.sign_in.SignInViewModel

import com.example.yukti.subscription.SubscriptionViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch


@Composable
fun AppNavigation(
    chatViewModel: ChatViewModel,
    googleAuthUiClient: GoogleAuthUiClient,
    applicationContext: Context
) {

    val navItemsList = listOf(
        NavItems(label = "Home", icon = Icons.Default.Home),
        NavItems(label = "Chat Box", icon = Icons.Default.Chat),
        NavItems(label = "Insights", icon = Icons.Default.Insights),
    )

    // Remember the selected index for the bottom navigation
    var selectedIndex by remember { mutableIntStateOf(0) }
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Determine the start destination based on the sign-in state
    val startDestination = if (googleAuthUiClient.getSignedInUser() != null) {
        Routes.home
    } else {
        Routes.signIn
    }

    // Scaffold for bottom navigation and content
    Scaffold(
        modifier = Modifier.fillMaxSize(),

        bottomBar = {
            NavigationBar {
                navItemsList.forEachIndexed { index, navItem ->
                    NavigationBarItem(
                        selected = selectedIndex == index,
                        onClick = {
                            selectedIndex = index
                            when (index) {
                                0 -> navController.navigate(Routes.home) {
                                    popUpTo(Routes.home) { inclusive = true }
                                }
                                1 -> navController.navigate(Routes.chat)
                                2 -> navController.navigate(Routes.insights)
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = navItem.icon,
                                contentDescription = navItem.label
                            )
                        },

                    )
                }
            }
        }, contentWindowInsets = WindowInsets.statusBars.only(WindowInsetsSides.Horizontal)
    ) { innerPadding ->
        // Navigation Host
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Sign-in screen
            composable(Routes.signIn) {
                val signInViewModel = viewModel<SignInViewModel>()
                val state by signInViewModel.state.collectAsStateWithLifecycle()

                // Google Sign-In launcher
                val launcher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartIntentSenderForResult(),
                    onResult = { result ->
                        if (result.resultCode == RESULT_OK) {
                            coroutineScope.launch {
                                val signInResult = googleAuthUiClient.signInWithIntent(
                                    intent = result.data ?: return@launch
                                )
                                signInViewModel.onSignInResult(signInResult)
                            }
                        }
                    }
                )

                // Sign-in success handling
                LaunchedEffect(state.isSignInSuccessful) {
                    if (state.isSignInSuccessful) {
                        Toast.makeText(
                            context,
                            "Sign-in successful",
                            Toast.LENGTH_LONG
                        ).show()
                        navController.navigate(Routes.home) {
                            popUpTo(Routes.signIn) { inclusive = true }
                        }
                    }
                }

                SignInScreen(
                    state = state,
                    onSignInClick = {
                        coroutineScope.launch {
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

            // Home screen
            composable(Routes.home) {
                HomePage(modifier = Modifier.fillMaxSize())
            }

            // Chat screen
            composable(Routes.chat) {
                ChatPage(
                    chatViewModel = chatViewModel,
                    googleAuthUiClient = googleAuthUiClient,
                    navController = navController,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Insights screen
            composable(Routes.insights) {
                Insights(modifier = Modifier.fillMaxSize())
            }

            // Other routes
            composable(Routes.subscriptionPage) {
                SubscriptionPage(navController = navController)
            }
            composable(Routes.businessSetup) {
                BusinessSetupPage(navController = navController, userId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty())
            }
            composable(Routes.joinBusiness) {
                JoinBusinessPage(navController = navController, userId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty())
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
                businessChatPage(
                    receiverUsername = username,
                    receiverUid = uid,
                    navController = navController
                )
            }
        }
    }
}
