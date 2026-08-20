package com.entrymyslot

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/* ──────────────────────────────────────────────────────────────
   Brand colours (mirrors the Tailwind config in auth.html)
   ────────────────────────────────────────────────────────────── */
object AuthColors {
    val Primary      = Color(0xFFF05A28)
    val PrimaryHover = Color(0xFFE04C1A)
    val Dark         = Color(0xFF262626)
    val DarkHover    = Color(0xFF1A1A1A)
    val Border       = Color(0xFFE5E7EB)
    val GrayBg       = Color(0xFFF9FAFB)
    val GrayText     = Color(0xFF6B7280)
    val LightText    = Color(0xFF9CA3AF)
    val SuccessGreen = Color(0xFF22C55E)
    val SuccessBg    = Color(0xFFDCFCE7)
}

/* ──────────────────────────────────────────────────────────────
   State types
   ────────────────────────────────────────────────────────────── */
sealed interface AuthState {
    data object Idle       : AuthState
    data object Loading    : AuthState
    data object OtpSent    : AuthState
    data object Verified   : AuthState
    data object LoggedIn   : AuthState
    data class  Error(val message: String) : AuthState
}

data class User(
    val name:  String = "",
    val email: String = "",
    val phone: String = ""
)

/* ──────────────────────────────────────────────────────────────
   Callbacks — implement in your ViewModel / presenter
   ────────────────────────────────────────────────────────────── */
interface AuthCallbacks {
    suspend fun onSendOtp(phone: String): Result<Unit>
    suspend fun onVerifyOtp(phone: String, otp: String): Result<User>
    suspend fun onRegister(name: String, phone: String, email: String, password: String): Result<Unit>
    suspend fun onLogout()
    val currentUser: User?
}

/* ═══════════════════════════════════════════════════════════════
   ROOT COMPOSABLE — drop this into your Activity / NavHost
   ═══════════════════════════════════════════════════════════════ */
@Composable
fun AuthScreen(
    callbacks: AuthCallbacks,
    onBackHome:    () -> Unit = {},
    onMyBookings:  () -> Unit = {}
) {
    val snackbarHost = remember { SnackbarHostState() }
    var authState     by remember { mutableStateOf<AuthState>(AuthState.Idle) }
    var activeTab     by remember { mutableStateOf(AuthTab.Login) }
    var loginStep     by remember { mutableStateOf(LoginStep.Phone) }
    var loginPhone    by remember { mutableStateOf("") }
    var snackMessage  by remember { mutableStateOf<String?>(null) }

    /* Show toast-like snackbar */
    LaunchedEffect(snackMessage) {
        snackMessage?.let {
            snackbarHost.showSnackbar(it)
            snackMessage = null
        }
    }

    /* If ViewModel already has a logged-in user */
    val user = callbacks.currentUser
    if (user != null && loginStep == LoginStep.Otp) {
        loginStep = LoginStep.Verified
    }

    when {
        user != null && loginStep == LoginStep.Verified -> {
            LoggedInScreen(
                userName     = user.name,
                onMyBookings = onMyBookings,
                onLogout = {
                    /* logout via ViewModel; in practice pass a lambda */
                }
            )
        }
        else -> {
            AuthContent(
                activeTab        = activeTab,
                loginStep        = loginStep,
                loginPhone       = loginPhone,
                authState        = authState,
                snackbarHost     = snackbarHost,
                onTabChange      = { activeTab = it },
                onPhoneChange    = { loginPhone = it },
                onStepChange     = { loginStep = it },
                onStateChange    = { authState = it },
                onSnackMessage   = { snackMessage = it },
                onBackHome       = onBackHome,
                callbacks        = callbacks
            )
        }
    }
}

/* ═══════════════════════════════════════════════════════════════
   Inner enums & helpers
   ═══════════════════════════════════════════════════════════════ */
enum class AuthTab  { Login, Register }
enum class LoginStep { Phone, Otp, Verified }

/* ═══════════════════════════════════════════════════════════════
   Full auth card layout
   ═══════════════════════════════════════════════════════════════ */
@Composable
private fun AuthContent(
    activeTab: AuthTab,
    loginStep: LoginStep,
    loginPhone: String,
    authState: AuthState,
    snackbarHost: SnackbarHostState,
    onTabChange: (AuthTab) -> Unit,
    onPhoneChange: (String) -> Unit,
    onStepChange: (LoginStep) -> Unit,
    onStateChange: (AuthState) -> Unit,
    onSnackMessage: (String?) -> Unit,
    onBackHome: () -> Unit,
    callbacks: AuthCallbacks
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F4F6))
    ) {
        /* Header */
        AuthHeader(onBackHome)

        /* Card */
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0xFFF3F4F6), RoundedCornerShape(16.dp))
            ) {
                /* Tab switcher */
                AuthTabRow(activeTab, onTabChange)

                /* Content */
                when (activeTab) {
                    AuthTab.Login -> LoginForm(
                        loginStep     = loginStep,
                        loginPhone    = loginPhone,
                        authState     = authState,
                        onPhoneChange = onPhoneChange,
                        onStepChange  = onStepChange,
                        onStateChange = onStateChange,
                        onSnack       = onSnackMessage,
                        callbacks     = callbacks
                    )
                    AuthTab.Register -> RegisterForm(
                        authState     = authState,
                        onStateChange = onStateChange,
                        onSnack       = onSnackMessage,
                        onDone        = {
                            onTabChange(AuthTab.Login)
                            onSnackMessage("Account created! Please log in.")
                        },
                        callbacks     = callbacks
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "By signing up, you agree to our Terms and Privacy Policy.",
                fontSize = 11.sp,
                color = AuthColors.LightText,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }

        /* Footer */
        AuthFooter()
    }

    SnackbarHost(hostState = snackbarHost)
}

/* ──────────────────────────────
   Header
   ────────────────────────────── */
@Composable
private fun AuthHeader(onBackHome: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(AuthColors.Dark)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        /* Replace with your actual logo drawable */
        Text("EntryMySlot", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onBackHome() }
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Back to Home", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
        }
    }
}

/* ──────────────────────────────
   Footer
   ────────────────────────────── */
@Composable
private fun AuthFooter() {
    Text(
        text = "Copyright 2026 © EntryMySlot Entertainment Pvt. Ltd. All Rights Reserved.",
        color = Color.White.copy(alpha = 0.35f),
        fontSize = 11.sp,
        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .background(AuthColors.Dark)
            .padding(vertical = 16.dp)
    )
}

/* ──────────────────────────────
   Tab Row
   ────────────────────────────── */
@Composable
private fun AuthTabRow(activeTab: AuthTab, onTabChange: (AuthTab) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth()) {
        TabBtn(
            label    = "Login",
            icon     = "🔑",
            active   = activeTab == AuthTab.Login,
            modifier = Modifier.weight(1f)
        ) { onTabChange(AuthTab.Login) }

        TabBtn(
            label    = "Register",
            icon     = "👤",
            active   = activeTab == AuthTab.Register,
            modifier = Modifier.weight(1f)
        ) { onTabChange(AuthTab.Register) }
    }
}

@Composable
private fun TabBtn(label: String, icon: String, active: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val bg       = if (active) AuthColors.Primary else Color(0xFFF3F4F6)
    val textClr  = if (active) Color.White else Color(0xFF9CA3AF)
    Button(
        onClick    = onClick,
        colors     = ButtonDefaults.buttonColors(containerColor = bg),
        shape      = RoundedCornerShape(0.dp),
        modifier   = modifier.height(52.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
        elevation  = ButtonDefaults.buttonElevation(0.dp)
    ) {
        Text("$icon  $label", color = textClr, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

/* ═══════════════════════════════════════════════════════════════
   LOGIN FORM
   ═══════════════════════════════════════════════════════════════ */
@Composable
private fun LoginForm(
    loginStep: LoginStep,
    loginPhone: String,
    authState: AuthState,
    onPhoneChange: (String) -> Unit,
    onStepChange: (LoginStep) -> Unit,
    onStateChange: (AuthState) -> Unit,
    onSnack: (String?) -> Unit,
    callbacks: AuthCallbacks
) {
    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)) {

        AnimatedVisibility(visible = loginStep == LoginStep.Phone) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Label("PHONE NUMBER")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CountryPrefix()
                    OutlinedTextField(
                        value       = loginPhone,
                        onValueChange = { v ->
                            if (v.length <= 10 && v.all { it.isDigit() }) onPhoneChange(v)
                        },
                        modifier    = Modifier.weight(1f),
                        placeholder = { Text("Enter 10-digit number", color = AuthColors.LightText, fontSize = 14.sp) },
                        singleLine  = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone
                        ),
                        shape       = RoundedCornerShape(10.dp),
                        colors      = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AuthColors.Primary,
                            unfocusedBorderColor = AuthColors.Border
                        ),
                        enabled     = authState != AuthState.Loading
                    )
                }
                Text(
                    "We will send you a one-time SMS verification code.",
                    fontSize = 12.sp,
                    color = AuthColors.LightText,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                ActionBtn(
                    text = "Get OTP",
                    loading = authState == AuthState.Loading,
                    enabled = loginPhone.length == 10
                ) {
                    onStateChange(AuthState.Loading)
                    /* ViewModel handles actual call; observer resets state */
                }
            }
        }

        AnimatedVisibility(visible = loginStep == LoginStep.Otp) {
            OtpSection(
                phone      = loginPhone,
                isLoading  = authState == AuthState.Loading,
                callbacks  = callbacks,
                onStateChange = onStateChange,
                onStepChange  = onStepChange,
                onSnack       = onSnack
            )
        }
    }
}

/* ═══════════════════════════════════════════════════════════════
   OTP SECTION — fully typed with focus management
   ═══════════════════════════════════════════════════════════════ */
@Composable
private fun OtpSection(
    phone: String,
    isLoading: Boolean,
    callbacks: AuthCallbacks,
    onStateChange: (AuthState) -> Unit,
    onStepChange: (LoginStep) -> Unit,
    onSnack: (String?) -> Unit
) {
    /* Each digit stored as a one-char string */
    var otp by remember { mutableStateOf(List(6) { "" }) }
    val focusReq = remember { List(6) { FocusRequester() } }
    var countdown by remember { mutableIntStateOf(30) }
    var canResend by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    /* Auto-focus first box when section appears */
    LaunchedEffect(Unit) {
        delay(150)
        focusReq[0].requestFocus()
    }

    /* Countdown timer */
    LaunchedEffect(canResend) {
        if (!canResend) {
            while (countdown > 0) {
                delay(1000L)
                countdown--
            }
            canResend = true
        }
    }

    Column(
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        /* ENTER OTP label */
        Text(
            "ENTER OTP",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = AuthColors.GrayText,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            "Sent to +91 $phone",
            fontSize = 12.sp,
            color = AuthColors.LightText,
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        )

        /* Six OTP boxes */
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            repeat(6) { idx ->
                OtpBox(
                    digit      = otp[idx],
                    onDigit    = { ch ->
                        val next = otp.toMutableList()
                        next[idx] = ch
                        otp = next
                        /* Advance focus */
                        if (ch.isNotEmpty() && idx < 5) focusReq[idx + 1].requestFocus()
                        /* Auto-submit when all 6 filled */
                        if (otp.all { it.isNotBlank() }) {
                            scope.launch {
                                onVerifyAndProceed(phone, otp.joinToString(""), callbacks, onStateChange, onStepChange, onSnack)
                            }
                        }
                    },
                    onDelete   = {
                        if (otp[idx].isEmpty() && idx > 0) {
                            val prev = otp.toMutableList()
                            prev[idx - 1] = ""
                            otp = prev
                            focusReq[idx - 1].requestFocus()
                        }
                    },
                    focusReq   = focusReq[idx],
                    enabled    = !isLoading
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        /* Resend row */
        Row(horizontalArrangement = Arrangement.Center) {
            Text("Did not receive? ", fontSize = 12.sp, color = AuthColors.LightText)
            if (canResend) {
                TextButton(onClick = {
                    canResend = false
                    countdown = 30
                    otp = List(6) { "" }
                    focusReq[0].requestFocus()
                    onSnack("OTP resent!")
                }) {
                    Text("Resend", color = AuthColors.Primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Text("(${countdown}s)", fontSize = 12.sp, color = AuthColors.LightText)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        /* Verify button */
        ActionBtn(
            text    = "Verify & Login",
            loading = isLoading,
            enabled = otp.all { it.isNotBlank() }
        ) {
            scope.launch {
                onVerifyAndProceed(phone, otp.joinToString(""), callbacks, onStateChange, onStepChange, onSnack)
            }
        }
    }
}

/* Triggered from both auto-submit and manual button click */
private suspend fun onVerifyAndProceed(
    phone: String,
    otp: String,
    callbacks: AuthCallbacks,
    onState: (AuthState) -> Unit,
    onStep: (LoginStep) -> Unit,
    onSnack: (String?) -> Unit
) {
    onState(AuthState.Loading)
    callbacks.onVerifyOtp(phone, otp)
        .onSuccess { user ->
            onState(AuthState.Verified)
            onStep(LoginStep.Verified)
            onSnack("Login successful!")
        }
        .onFailure { err ->
            onState(AuthState.Error(err.message ?: "Invalid OTP"))
            onSnack(err.message ?: "Invalid OTP. Please try again.")
        }
}

/* ──────────────────────────────
   Single OTP digit box
   Focus is managed externally via FocusRequester
   ────────────────────────────── */
@Composable
private fun OtpBox(
    digit: String,
    onDigit: (String) -> Unit,
    onDelete: () -> Unit,
    focusReq: FocusRequester,
    enabled: Boolean
) {
    var focused by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .size(width = 44.dp, height = 52.dp)
            .border(
                width = if (focused) 2.5.dp else 2.dp,
                color = if (focused) AuthColors.Primary else AuthColors.Border,
                shape = RoundedCornerShape(10.dp)
            )
            .background(Color.White, RoundedCornerShape(10.dp))
            .focusRequester(focusReq)
            .onFocusChanged { focused = it.isFocused }
            .onPreviewKeyEvent { e ->
                if (e.key == Key.Backspace && digit.isEmpty()) {
                    onDelete()
                    true
                } else false
            },
        contentAlignment = Alignment.Center
    ) {
        /* Invisible TextField — it captures IME input into the box */
        BasicTextField(
            value     = digit,
            onValueChange = { new ->
                if (new.length <= 1 && new.all { it.isDigit() }) onDigit(new)
            },
            enabled   = enabled,
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(
                fontSize       = 22.sp,
                fontWeight     = FontWeight.Bold,
                color          = AuthColors.Dark,
                textAlign      = androidx.compose.ui.text.style.TextAlign.Center
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            cursorBrush = SolidColor(AuthColors.Primary),
            modifier   = Modifier
                .fillMaxSize()
                .padding(horizontal = 4.dp),
            decorationBox = { inner -> inner() }
        )
    }
}

/* ═══════════════════════════════════════════════════════════════
   REGISTER FORM
   ═══════════════════════════════════════════════════════════════ */
@Composable
private fun RegisterForm(
    authState: AuthState,
    onStateChange: (AuthState) -> Unit,
    onSnack: (String?) -> Unit,
    onDone: () -> Unit,
    callbacks: AuthCallbacks
) {
    var name     by remember { mutableStateOf("") }
    var email    by remember { mutableStateOf("") }
    var phone    by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val loading  = authState == AuthState.Loading
    val canSubmit = name.isNotBlank()
            && phone.length == 10
            && password.length >= 6

    Column(
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        FormInput(label = "FULL NAME", value = name, onValueChange = { name = it },
            placeholder = "Your full name")
        FormInput(label = "EMAIL", value = email, onValueChange = { email = it },
            placeholder = "you@example.com",
            keyboardType = androidx.compose.ui.text.input.KeyboardType.Email)
        PhoneFormInput(label = "PHONE NUMBER", value = phone, onValueChange = {
            if (it.length <= 10 && it.all { c -> c.isDigit() }) phone = it
        }, placeholder = "Enter 10-digit number")
        FormInput(label = "PASSWORD", value = password, onValueChange = { password = it },
            placeholder = "Min 6 characters", isPassword = true)

        Spacer(modifier = Modifier.height(4.dp))
        ActionBtn(
            text = "Create Account",
            loading = loading,
            enabled = canSubmit
        ) {
            onStateChange(AuthState.Loading)
            /* ViewModel handles actual registration */
            onDone()
        }
    }
}

/* ═══════════════════════════════════════════════════════════════
   Logged-In Screen
   ═══════════════════════════════════════════════════════════════ */
@Composable
private fun LoggedInScreen(
    userName: String,
    onMyBookings: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F4F6)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AuthHeader(onBackHome = {})

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0xFFF3F4F6), RoundedCornerShape(16.dp))
                    .padding(vertical = 40.dp, horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(AuthColors.SuccessBg, RoundedCornerShape(50)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CheckCircle, null,
                        tint = AuthColors.SuccessGreen, modifier = Modifier.size(32.dp))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("You are logged in!", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AuthColors.Dark)
                Text(
                    "You can continue booking or manage your reservations.",
                    fontSize = 14.sp, color = AuthColors.GrayText,
                    modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ActionBtn("My Bookings", "🎫", onClick = onMyBookings)
                    ActionBtn("Logout", "↪", onClick = onLogout, containerColor = AuthColors.Dark)
                }
            }
        }
        AuthFooter()
    }
}

/* ═══════════════════════════════════════════════════════════════
   Small shared composables
   ═══════════════════════════════════════════════════════════════ */

@Composable
private fun Label(text: String) {
    Text(
        text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.5.sp,
        color = AuthColors.GrayText,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

@Composable
private fun CountryPrefix() {
    Box(
        modifier = Modifier
            .height(52.dp)
            .background(AuthColors.GrayBg, RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp))
            .border(1.5.dp, AuthColors.Border, RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp))
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("+91", color = AuthColors.GrayText, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun FormInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: (@Composable () -> Unit)? = null,
    isPassword: Boolean = false,
    keyboardType: androidx.compose.ui.text.input.KeyboardType = androidx.compose.ui.text.input.KeyboardType.Text
) {
    Column {
        Label(label)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value          = value,
            onValueChange  = onValueChange,
            modifier       = Modifier.fillMaxWidth(),
            placeholder    = { Text(placeholder, color = AuthColors.LightText, fontSize = 14.sp) },
            singleLine     = true,
            leadingIcon    = leadingIcon,
            visualTransformation = if (isPassword) PasswordVisualTransformation()
            else androidx.compose.ui.text.input.VisualTransformation.None,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType
            ),
            shape          = RoundedCornerShape(10.dp),
            colors         = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = AuthColors.Primary,
                unfocusedBorderColor = AuthColors.Border
            )
        )
    }
}

@Composable
private fun PhoneFormInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    Column {
        Label(label)
        Spacer(modifier = Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            CountryPrefix()
            OutlinedTextField(
                value       = value,
                onValueChange = onValueChange,
                modifier    = Modifier.weight(1f),
                placeholder = { Text(placeholder, color = AuthColors.LightText, fontSize = 14.sp) },
                singleLine  = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone
                ),
                shape       = RoundedCornerShape(10.dp),
                colors      = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = AuthColors.Primary,
                    unfocusedBorderColor = AuthColors.Border
                )
            )
        }
    }
}

@Composable
private fun ActionBtn(
    text: String = "",
    icon: String = "",
    loading: Boolean = false,
    enabled: Boolean = true,
    containerColor: Color = AuthColors.Primary,
    onClick: () -> Unit
) {
    Button(
        onClick           = onClick,
        enabled           = enabled && !loading,
        modifier          = Modifier.fillMaxWidth().height(52.dp),
        shape             = RoundedCornerShape(10.dp),
        colors            = ButtonDefaults.buttonColors(
            containerColor        = containerColor,
            disabledContainerColor = Color(0xFFD1D5DB),
            contentColor          = Color.White
        ),
        elevation         = ButtonDefaults.buttonElevation(0.dp)
    ) {
        if (loading) {
            CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
        } else {
            Text(text = if (icon.isNotBlank()) "$icon  $text" else text,
                fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
    }
}
