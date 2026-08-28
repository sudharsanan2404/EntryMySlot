package com.entrymyslot.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.entrymyslot.app.core.storage.AuthTokenStore
import com.entrymyslot.app.screens.auth.AuthScreen
import com.entrymyslot.app.screens.home.HomeScreen

@Composable
fun AppNavigation(
    authTokenStore: AuthTokenStore
) {

    val navController = rememberNavController()
    val accessToken by authTokenStore.accessToken.collectAsState(initial = "LOADING")

    if (accessToken == "LOADING") {
        // You could show a splash screen here
        return
    }

    NavHost(
        navController = navController,
        startDestination = if (accessToken != null) "home" else "auth"
    ) {

        composable("auth") {
            AuthScreen(
                onAuthSuccess = {
                    navController.navigate("home") {
                        popUpTo("auth") {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable("home") {
            HomeScreen()
        }
    }
}