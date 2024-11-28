package com.example.yukti.subscription


import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext

import androidx.navigation.NavHostController

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class SubscriptionChecker(private val context: Context) {
    private val userId: String = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    suspend fun checkSubscription(): Triple<Boolean, String?, String?> {
        val tag = "SubscriptionChecker" // Tag for logs
        return try {
            val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)
            Log.d(tag, "Fetching user data for userId: $userId")

            val snapshot = userRef.get().await()
            Log.d(tag, "User data snapshot fetched: ${snapshot.value}")

            if (snapshot.exists()) {
                val isSubscribed = snapshot.child("isSubscribed").getValue(Boolean::class.java) ?: false
                Log.d(tag, "Subscription status: $isSubscribed")


                if (snapshot.child("businessId").value is Map<*, *>) {
                    val businessIdMap = snapshot.child("businessId").value as Map<*, *>
                    val businessId = businessIdMap.keys.firstOrNull()?.toString() // Extract the first key as the businessId
                    Log.d(tag, "Extracted Business ID: $businessId")

                    if (!businessId.isNullOrBlank()) {
                        val businessRef = FirebaseDatabase.getInstance().getReference("businesses").child(businessId)
                        Log.d(tag, "Fetching business data for businessId: $businessId")

                        val businessSnapshot = businessRef.get().await()
                        Log.d(tag, "Business data snapshot fetched: ${businessSnapshot.value}")

                        val businessName = businessSnapshot.child("businessName").getValue(String::class.java)
                        Log.d(tag, "Business name: $businessName")

                        // Save subscription details to the cache for both subscribed and unsubscribed users
                        SubscriptionCache.saveSubscriptionDetails(context, isSubscribed, businessName, businessId)

                        return Triple(isSubscribed, businessName,businessId)
                    } else {
                        Log.d(tag, "No business ID found for user.")
                        Toast.makeText(context, "User is not associated with any business.", Toast.LENGTH_SHORT).show()
                        return Triple(isSubscribed, null,null)
                    }
                } else {
                    Log.d(tag, "No business ID found or unexpected format.")
                    Toast.makeText(context, "User is not associated with any business.", Toast.LENGTH_SHORT).show()
                    return Triple(isSubscribed, null,null)
                }

            } else {
                Log.d(tag, "User not found in the database.")
                Toast.makeText(context, "User not found in the database.", Toast.LENGTH_SHORT).show()
                return Triple(false, null,null)
            }
        } catch (e: Exception) {
            Log.e(tag, "Error occurred while checking subscription: ${e.message}", e)
            Toast.makeText(context, "Failed to check subscription: ${e.message}", Toast.LENGTH_SHORT).show()
            return Triple(false, null,null)
        }
    }


}


