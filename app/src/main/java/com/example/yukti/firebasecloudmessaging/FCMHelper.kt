package com.example.yukti.firebasecloudmessaging

import android.content.Context
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.auth.FirebaseAuth


class FCMHelper(private val context: Context) {

    // Get the current FCM token


   fun getFCMToken(): String? {
        return try {
            val token = FirebaseMessaging.getInstance().token
            token // Return the token if successful
        } catch (e: Exception) {
            e.printStackTrace()
            null // Return null if there's an error
        }.toString()
    }


    // Save FCM token to the Firebase Realtime Database
    fun saveTokenToDatabase(fcmToken: String?) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null && fcmToken != null) {
            val databaseReference = FirebaseDatabase.getInstance().getReference("users")
            val userId = currentUser.uid
            databaseReference.child(userId).child("fcmToken").setValue(fcmToken)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        println("FCM token saved successfully")
                    } else {
                        println("Failed to save FCM token: ${task.exception?.message}")
                    }
                }
        }
    }

    // Update the FCM token in case of token refresh
    fun updateFCMTokenOnTokenRefresh() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val refreshedToken = task.result
                // You can save the refreshed token in the database
                saveTokenToDatabase(refreshedToken)
            } else {
                println("Error refreshing FCM token: ${task.exception?.message}")
            }
        }
    }
}
