package com.example.yukti.subscription

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SubscriptionViewModel : ViewModel() {
    private val _isSubscribed = MutableStateFlow(false)
    val isSubscribed: StateFlow<Boolean> get() = _isSubscribed

    private val _businessName = mutableStateOf<String?>(null)
    val businessName: State<String?> = _businessName

    private val _businessId = mutableStateOf<String?>(null)
    val businessId: State<String?> = _businessId


    // Methods to update the subscription status and business name
    fun setSubscriptionStatus(isSubscribed: Boolean) {
        _isSubscribed.value = isSubscribed
    }

    fun setBusinessName(businessName: String) {
        _businessName.value = businessName
    }
    fun setBusinessId(businessId: String) {
        _businessId.value = businessId
    }
    fun fetchSubscriptionStatus(subscriptionChecker: SubscriptionChecker) {
        viewModelScope.launch {
            val (isSubscribed, businessName) = subscriptionChecker.checkSubscription()
            setSubscriptionStatus(isSubscribed)
            setBusinessName(businessName.toString())
        }
    }

}
