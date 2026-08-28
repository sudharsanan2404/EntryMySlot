package com.entrymyslot.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.entrymyslot.app.screens.auth.AuthScreen
import com.entrymyslot.app.screens.home.HomeScreen

@Composable
fun AppNavigation() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "auth"
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