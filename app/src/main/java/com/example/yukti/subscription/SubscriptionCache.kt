package com.example.yukti.subscription

object SubscriptionCache {
    var isSubscribed: Boolean = false
    var businessName: String? = null

    fun reset() {
        isSubscribed = false
        businessName = null
    }
}
