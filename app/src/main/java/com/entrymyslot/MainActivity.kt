package com.entrymyslot

import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import com.entrymyslot.ui.screens.AuthScreen
import com.entrymyslot.ui.theme.EntryMySlotTheme
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
            setContent {
                val systemUiController = rememberSystemUiController()
                SideEffect {
                    systemUiController.setStatusBarColor(
                        color = Color(0xFF262626), // EntryDark
                        darkIcons = false
                    )
                    systemUiController.setNavigationBarColor(
                        color = Color.White,
                        darkIcons = true
                    )
                }
            enableEdgeToEdge()
            setContent {
                EntryMySlotTheme {
                    Surface {
                        HomeScreen()
                    }
                }
            }
        }
    }
}