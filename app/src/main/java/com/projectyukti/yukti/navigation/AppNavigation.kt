package com.projectyukti.yukti.navigation

import ChatViewModel
import android.app.Activity.RESULT_OK
import android.content.Context
import android.graphics.Rect
import android.net.Uri
import android.util.Log
import android.view.ViewTreeObserver

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.projectyukti.yukti.Home.HomePage
import com.projectyukti.yukti.Insights.Insights
import com.projectyukti.yukti.NavItems
import com.projectyukti.yukti.chat.ChatPage
import com.projectyukti.yukti.createbusiness.BusinessSetupPage
import com.projectyukti.yukti.createbusiness.SubscriptionPage
import com.projectyukti.yukti.createbusiness.businessMembers
import com.projectyukti.yukti.createbusiness.joinbusiness.JoinBusinessPage
import com.projectyukti.yukti.createbusiness.joinbusiness.businesschat.businessChatPage
import com.projectyukti.yukti.sign_in.GoogleAuthUiClient
import com.projectyukti.yukti.sign_in.SignInScreen
import com.projectyukti.yukti.sign_in.SignInViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch


@Composable
fun AppNavigation(
    chatViewModel: ChatViewModel,
    googleAuthUiClient: GoogleAuthUiClient,
    applicationContext: Context
) {
    val navItemsList = listOf(
        NavItems(label = "Home", icon = Icons.Default.Home, route = Routes.home),
        NavItems(label = "Chat Box", icon = Icons.Default.Chat, route = Routes.chat),
        NavItems(label = "Insights", icon = Icons.Default.Insights, route = Routes.insights),
    )

    val navController: NavHostController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Determine the start destination based on the sign-in state
    val startDestination = if (googleAuthUiClient.getSignedInUser() != null) {
        Routes.home
    } else {
        Routes.signIn
    }

    // Track current route to highlight the correct bottom navigation item
    val currentDestination by navController.currentBackStackEntryAsState()
    val currentRoute = currentDestination?.destination?.route

    // Check if bottom navigation should be shown
    val showBottomNavigation = currentRoute != Routes.signIn



    var isKeyboardVisible by remember { mutableStateOf(false) }
    isKeyboardVisible = isKeyboardOpen()
    Log.d("Keyboard open?", isKeyboardVisible.toString())


    var defaultPadding: PaddingValues = PaddingValues( bottom = 0.dp)
    // Adjust padding based on keyboard visibility


    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomNavigation) {
                NavigationBar {
                    navItemsList.forEach { navItem ->
                        NavigationBarItem(
                            selected = currentRoute == navItem.route,
                            onClick = {
                                if (currentRoute != navItem.route) {
                                    navController.navigate(navItem.route) {
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = navItem.icon,
                                    contentDescription = navItem.label
                                )
                            }
                        )
                    }
                }
            }
        },
        contentWindowInsets = WindowInsets.statusBars.only(WindowInsetsSides.Bottom)
    ) { innerPadding ->


        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier
        ) {
            // Sign-in screen
            composable(Routes.signIn) {
                val signInViewModel: SignInViewModel = viewModel()
                val state by signInViewModel.state.collectAsStateWithLifecycle()

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

                LaunchedEffect(state.isSignInSuccessful) {
                    if (state.isSignInSuccessful) {
                        Toast.makeText(context, "Sign-in successful", Toast.LENGTH_LONG).show()
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
                                launcher.launch(IntentSenderRequest.Builder(signInIntentSender).build())
                            } else {
                                Toast.makeText(context, "Failed to get Sign-In Intent", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    navController = navController
                )
            }

            composable(Routes.home) {
                HomePage(modifier = Modifier.fillMaxSize().padding(innerPadding))
            }

            composable(Routes.chat) {
                ChatPage(
                    chatViewModel = chatViewModel,
                    googleAuthUiClient = googleAuthUiClient,
                    navController = navController,
                    innerPadding
                    )

            }

            composable(Routes.insights) {
                Insights(modifier = Modifier.fillMaxSize().padding(innerPadding))
            }

            composable(Routes.subscriptionPage) {
                BusinessSetupPage(navController = navController, userId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty())
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
                route = "businessChat/{username}/{uid}/{profilePictureUrl}",
                arguments = listOf(
                    navArgument("username") { type = NavType.StringType },
                    navArgument("uid") { type = NavType.StringType },
                    navArgument("profilePictureUrl") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val username = backStackEntry.arguments?.getString("username") ?: ""
                val uid = backStackEntry.arguments?.getString("uid") ?: ""
                val profilePictureUrl = Uri.decode(backStackEntry.arguments?.getString("profilePictureUrl") ?: "")
                businessChatPage(
                    receiverUsername = username,
                    receiverUid = uid,
                    navController = navController,
                    innerPadding,
                    profilePictureUrl
                )
            }
        }
    }
}

@Composable
fun isKeyboardOpen(): Boolean {
    val view = LocalView.current
    var isKeyboardVisible by remember { mutableStateOf(false) }

    DisposableEffect(view) {
        val listener = ViewTreeObserver.OnGlobalLayoutListener {
            val rect = Rect()
            view.getWindowVisibleDisplayFrame(rect)
            val screenHeight = view.rootView.height
            val keypadHeight = screenHeight - rect.bottom
            isKeyboardVisible = keypadHeight > screenHeight * 0.15
        }
        view.viewTreeObserver.addOnGlobalLayoutListener(listener)
        onDispose { view.viewTreeObserver.removeOnGlobalLayoutListener(listener) }
    }

    return isKeyboardVisible
}
