package com.example.yukti.sign_in

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.BeginSignInRequest.GoogleIdTokenRequestOptions
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.example.yukti.R
import com.google.firebase.database.ktx.database
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

class GoogleAuthUiClient(
    private val context: Context,
    private val oneTapClient: SignInClient
) {
    private val auth = Firebase.auth
    private val database = Firebase.database.reference // Firebase Database reference

    suspend fun signIn(): IntentSender? {
        return try {
            val result = oneTapClient.beginSignIn(buildSignInRequest()).await()
            result?.pendingIntent?.intentSender
        } catch (e: Exception) {
            // Log specific exceptions
            when (e) {
                is CancellationException -> throw e
                is com.google.android.gms.common.api.ApiException -> {
                    println("API Exception: ${e.statusCode} - ${e.message}")
                }

                else -> {
                    println("Error in signIn(): ${e.localizedMessage}")
                }
            }
            null
        }
    }

    suspend fun signInWithIntent(intent: Intent): SignInResult {
        return try {
            val credential = oneTapClient.getSignInCredentialFromIntent(intent)
            val googleIdToken = credential.googleIdToken
            println("Google ID Token: $googleIdToken")

            val googleCredentials = GoogleAuthProvider.getCredential(googleIdToken, null)
            val user = auth.signInWithCredential(googleCredentials).await().user

            val userData = user?.run {
                UserData(uid, displayName, photoUrl?.toString())
            }

            if (userData != null) {
                saveUserToDatabase(userData) // Save user data to Realtime Database
            }

            SignInResult(data = userData, errorMessage = null)

        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
            println("Error in signInWithIntent: ${e.localizedMessage}")
            SignInResult(data = null, errorMessage = e.message)
        }
    }


    suspend fun signOut() {
        try {
            oneTapClient.signOut().await()
            auth.signOut()
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
        }
    }

    fun getSignedInUser(): UserData? = auth.currentUser?.run {
        UserData(
            userId = uid,
            username = displayName,
            profilePictureUrl = photoUrl?.toString()
        )
    }

    private fun buildSignInRequest(): BeginSignInRequest {
        println("Building sign-in request with client ID: ${context.getString(R.string.web_client_id)}")
        return BeginSignInRequest.Builder()
            .setGoogleIdTokenRequestOptions(
                GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(context.getString(R.string.web_client_id))
                    .build()
            )
            .setAutoSelectEnabled(true)
            .build()
    }
    suspend fun saveUserToDatabase(userData: UserData) {
        try {
            val userRef = database.child("users").child(userData.userId)
            userRef.setValue(userData).await()
            println("User data saved to database: ${userData.username}")
        } catch (e: Exception) {
            e.printStackTrace()
            println("Error saving user data to database: ${e.message}")
        }
    }
}