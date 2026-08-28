package com.entrymyslot.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.entrymyslot.app.navigation.AppNavigation
import com.entrymyslot.app.screens.auth.AuthScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val app = applicationContext as EntryMySlotApp
        val authTokenStore = app.appContainer.authTokenStore
        
        enableEdgeToEdge()
        setContent {
            AppNavigation(authTokenStore = authTokenStore)
        }
    }
}