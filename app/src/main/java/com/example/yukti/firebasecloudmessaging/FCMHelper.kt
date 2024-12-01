package com.example.yukti.firebasecloudmessaging

import android.content.Context
import android.util.Log
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
    fun saveFcmTokenToDatabase(userId: String) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                // Handle error
                Log.w("FCM", "Fetching FCM token failed", task.exception)
                return@addOnCompleteListener
            }
            // Get FCM Token
            val token = task.result
            Log.d("FCM", "FCM Token: $token")

            // Save token to Firebase Realtime Database
            val database = FirebaseDatabase.getInstance()
            val userTokenRef = database.reference.child("users").child(userId).child("fcmToken")
            userTokenRef.setValue(token)
        }
    }


    // Update the FCM token in case of token refresh
    fun deleteFcmTokenFromDatabase(userId: String) {
        val database = FirebaseDatabase.getInstance()
        val userTokenRef = database.reference.child("users").child(userId).child("fcmToken")
        userTokenRef.removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("FCM", "Token successfully removed from database")
            } else {
                Log.w("FCM", "Failed to remove token", task.exception)
            }
        }
    }

}
