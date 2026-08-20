package com.entrymyslot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.entrymyslot.AuthScreen
import com.entrymyslot.AuthCallbacks
import com.entrymyslot.User
import com.entrymyslot.AuthState
import com.entrymyslot.ui.theme.EntryMySlotTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val systemUiController = rememberSystemUiController()
            SideEffect {
                systemUiController.setStatusBarColor(
                    color = Color(0xFF262626),
                    darkIcons = false
                )
                systemUiController.setNavigationBarColor(
                    color = Color.White,
                    darkIcons = true
                )
            }

            EntryMySlotTheme {
                Surface {
                    AuthScreen(
                        callbacks = object : AuthCallbacks {
                            override val currentUser: User? = null

                            override suspend fun onSendOtp(phone: String): Result<Unit> {
                                return Result.success(Unit)
                            }

                            override suspend fun onVerifyOtp(phone: String, otp: String): Result<User> {
                                return Result.success(User(name = "User", phone = phone))
                            }

                            override suspend fun onRegister(
                                name: String,
                                phone: String,
                                email: String,
                                password: String
                            ): Result<Unit> {
                                return Result.success(Unit)
                            }

                            override suspend fun onLogout() {}
                        }
                    )
                }
            }
        }
    }
}