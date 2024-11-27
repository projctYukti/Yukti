package com.example.yukti.subscription

import android.content.Context
import com.google.gson.Gson

object SubscriptionCache {
    var isSubscribed: Boolean = false
    var businessName: String? = null

    fun reset() {
        isSubscribed = false
        businessName = null
    }
    fun saveSubscriptionDetails(context: Context, isSubscribed: Boolean, businessName: String?) {
        val sharedPreferences = context.getSharedPreferences("BusinessPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Save the subscription status and business name
        editor.putBoolean("isSubscribed", isSubscribed)
        editor.putString("businessName", businessName)

        // Commit the changes
        editor.apply()
    }
    fun getSubscriptionDetails(context: Context): Pair<Boolean, String?> {
        val sharedPreferences = context.getSharedPreferences("BusinessPrefs", Context.MODE_PRIVATE)

        // Retrieve the stored values
        val isSubscribed = sharedPreferences.getBoolean("isSubscribed", false)
        val businessName = sharedPreferences.getString("businessName", null)

        return Pair(isSubscribed, businessName)
    }
    fun clearSubscriptionDetails(context: Context) {
        val sharedPreferences = context.getSharedPreferences("BusinessPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Clear the saved subscription status and business name
        editor.remove("isSubscribed")
        editor.remove("businessName")

        // Commit the changes
        editor.apply()
    }



}
