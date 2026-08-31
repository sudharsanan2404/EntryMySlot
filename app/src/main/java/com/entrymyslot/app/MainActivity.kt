package com.entrymyslot.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.entrymyslot.app.navigation.AppNavigation
import com.entrymyslot.app.core.theme.EntryMySlotTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val app = applicationContext as EntryMySlotApp
        enableEdgeToEdge()
        setContent {
            EntryMySlotTheme {
                val mainViewModel: MainViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T =
                            MainViewModel(
                                authRepository = app.appContainer.authRepository,
                                networkMonitor = app.appContainer.networkMonitor
                            ) as T
                    }
                )
                val authState = mainViewModel.authState.collectAsStateWithLifecycle().value

                AppNavigation(
                    authState = authState,
                    onAuthenticated = mainViewModel::onAuthenticated,
                    onLoggedOut = mainViewModel::onLoggedOut
                )
            }
        }
    }
}
