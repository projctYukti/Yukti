package com.example.yukti.subscription

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SubscriptionViewModel : ViewModel() {
    private val _isSubscribed = MutableStateFlow(false)
    val isSubscribed: StateFlow<Boolean> get() = _isSubscribed

    private val _businessName = mutableStateOf<String?>(null)
    val businessName: State<String?> = _businessName


    // Methods to update the subscription status and business name
    fun setSubscriptionStatus(isSubscribed: Boolean) {
        _isSubscribed.value = isSubscribed
    }

    fun setBusinessName(businessName: String) {
        _businessName.value = businessName
    }
}
