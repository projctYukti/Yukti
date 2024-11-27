package com.example.yukti.subscription

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State

class SubscriptionViewModel : ViewModel() {
    private val _isSubscribed = mutableStateOf<Boolean?>(null)
    val isSubscribed: State<Boolean?> = _isSubscribed

    private val _businessName = mutableStateOf<String?>(null)
    val businessName: State<String?> = _businessName

    // Methods to update the subscription status and business name
    fun setSubscriptionStatus(isSubscribed: Boolean) {
        _isSubscribed.value = isSubscribed
    }

    fun setBusinessName(name: String) {
        _businessName.value = name
    }
}
