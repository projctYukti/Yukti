package com.example.yukti.subscription

import android.content.Context
import androidx.compose.ui.platform.LocalContext
import com.google.gson.Gson

object SubscriptionCache {
    var isSubscribed: Boolean = false
    var businessName: String? = null
    var businessId: String? = null

    fun reset() {
        isSubscribed = false
        businessName = null
    }
    fun saveSubscriptionDetails(context: Context, isSubscribed: Boolean, businessName: String?,businessId:String?) {
        val sharedPreferences = context.getSharedPreferences("BusinessPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()


        // Save the subscription status and business name
        editor.putBoolean("isSubscribed", isSubscribed)
        editor.putString("businessName", businessName)
        editor.putString("businessId", businessId)
        // Save the subscription status and business name
        SubscriptionCache.isSubscribed = isSubscribed
        SubscriptionCache.businessName = businessName
        SubscriptionCache.businessId = businessId

        // Commit the changes
        editor.apply()
    }
    fun getSubscriptionDetails(context: Context): Triple<Boolean, String?, String?> {
        val sharedPreferences = context.getSharedPreferences("BusinessPrefs", Context.MODE_PRIVATE)

        // Retrieve the stored values
        val isSubscribed = sharedPreferences.getBoolean("isSubscribed", false)
        val businessName = sharedPreferences.getString("businessName", null)
        val businessId = sharedPreferences.getString("businessId", null)

        return Triple(isSubscribed, businessName,businessId)
    }
    fun clearSubscriptionDetails(context: Context) {
        val sharedPreferences = context.getSharedPreferences("BusinessPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Clear the saved subscription status and business name
        editor.remove("isSubscribed")
        editor.remove("businessName")
        editor.remove("businessId")

        // Commit the changes
        editor.apply()
    }



}
