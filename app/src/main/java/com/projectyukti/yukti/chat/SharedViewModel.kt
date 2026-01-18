package com.projectyukti.yukti.chat

import androidx.lifecycle.ViewModel
import androidx.compose.material3.DrawerState
import kotlinx.coroutines.CoroutineScope

class SharedViewModel : ViewModel() {
    var drawerState: DrawerState? = null
    var scope: CoroutineScope? = null
}
