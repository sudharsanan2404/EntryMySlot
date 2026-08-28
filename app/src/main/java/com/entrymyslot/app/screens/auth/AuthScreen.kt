package com.entrymyslot.app.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import com.entrymyslot.app.EntryMySlotApp

import com.entrymyslot.app.R
import kotlin.math.roundToInt

private val EntryBlue = Color(0xFF123BBD)
private val EntryOrange = Color(0xFFFF6500)
private val LabelGrey = Color(0xFF7D8597)
private val InactiveTabGrey = Color(0xFFF1F4F9)
private val BorderColor = Color(0xFFE2E8F0)
private val FieldErrorColor = Color(0xFFBA1A1A)

private data class AuthFormErrors(
    val fullName: String? = null,
    val email: String? = null,
    val password: String? = null,
    val confirmPassword: String? = null,
    val otp: String? = null
)

private fun validateLogin(email: String, password: String) = AuthFormErrors(
    email = email.validateEmail(),
    password = password.validatePassword()
)

private fun validateRegistration(
    fullName: String,
    email: String,
    password: String,
    confirmPassword: String
) = AuthFormErrors(
    fullName = when {
        fullName.isBlank() -> "Full name is required"
        fullName.trim().length < 2 -> "Enter at least 2 characters"
        else -> null
    },
    email = email.validateEmail(),
    password = password.validatePassword(),
    confirmPassword = when {
        confirmPassword.isBlank() -> "Please confirm your password"
        password != confirmPassword -> "Passwords do not match"
        else -> null
    }
)

private fun String.validateEmail(): String? = when {
    isBlank() -> "Email address is required"
    !matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) -> "Enter a valid email address"
    else -> null
}

private fun String.validatePassword(): String? = when {
    isBlank() -> "Password is required"
    length < 8 -> "Password must be at least 8 characters"
    !contains(Regex("[A-Z]")) -> "Must contain at least one uppercase letter"
    !contains(Regex("[a-z]")) -> "Must contain at least one lowercase letter"
    !contains(Regex("[0-9]")) -> "Must contain at least one number"
    !contains(Regex("[!@#\$%^&*(),.?\":{}|<>]")) -> "Must contain at least one special character"
    else -> null
}

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit = {}
) {

    var isLogin by remember { mutableStateOf(true) }
    var isOtpMode by remember { mutableStateOf(false) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    
    var otpValue by remember { mutableStateOf("") }
    var fieldErrors by remember { mutableStateOf(AuthFormErrors()) }
    val focusManager = LocalFocusManager.current

    val context = LocalContext.current
    val app = context.applicationContext as EntryMySlotApp

    val viewModel: AuthScreenViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {

            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(
                modelClass: Class<T>
            ): T {
                return AuthScreenViewModel(
                    repository = app.appContainer.authRepository
                ) as T
            }
        }
    )

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) {
            onAuthSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .navigationBarsPadding()
    ) {

        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            // ---------------------------------------------------------
            // TOP BAR
            // ---------------------------------------------------------

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(EntryBlue)
                    .statusBarsPadding()
                    .height(58.dp)
            ) {
                // Back Button (Only visible in OTP mode)
                if (uiState.isOtpMode) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 16.dp)
                            .clickable { 
                                viewModel.clearOtpMode()
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Back",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Logo
                Image(
                    painter = painterResource(id = R.drawable.entrymyslotlogo),
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // ---------------------------------------------------------
            // CONTENT
            // ---------------------------------------------------------

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .imePadding()
                        .padding(top = 30.dp, bottom = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // -----------------------------------------------------
                    // AUTH CARD
                    // -----------------------------------------------------

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 2.dp
                        )
                    ) {

                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {

                            if (uiState.isOtpMode) {
                                // -------------------------------------------------
                                // OTP VERIFICATION VIEW
                                // -------------------------------------------------
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = EntryOrange
                                    )
                                    
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    Text(
                                        text = "OTP VERIFICATION",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    Text(
                                        text = "Enter the OTP sent to your mobile number",
                                        fontSize = 13.sp,
                                        color = LabelGrey,
                                        textAlign = TextAlign.Center,
                                        fontWeight = FontWeight.Bold
                                    )
                                    
                                    Spacer(modifier = Modifier.height(24.dp))
                                    
                                    OtpInputField(
                                        otpText = otpValue,
                                        onOtpTextChange = { 
                                            otpValue = it
                                            fieldErrors = fieldErrors.copy(otp = null)
                                            viewModel.clearMessages()
                                            if (it.length == 6) {
                                                focusManager.clearFocus()
                                                viewModel.verifyOtp(email = email, otp = it)
                                            }
                                        },
                                        isError = fieldErrors.otp != null || uiState.errorMessage != null
                                    )

                                    FieldError(fieldErrors.otp ?: uiState.errorMessage)
                                    
                                    Spacer(modifier = Modifier.height(24.dp))
                                    
                                    Row(
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Didn't receive code? ",
                                            fontSize = 13.sp,
                                            color = LabelGrey,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Resend",
                                            fontSize = 13.sp,
                                            color = EntryOrange,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.clickable {
                                                viewModel.resendOtp(email)
                                            }
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.height(24.dp))
                                    
                                    Button(
                                        onClick = {
                                            val otpError = if (otpValue.length != 6) "Enter the 6-digit OTP" else null
                                            fieldErrors = fieldErrors.copy(otp = otpError)
                                            if (otpError == null) viewModel.verifyOtp(email = email, otp = otpValue)
                                        },
                                        enabled = !uiState.isLoading,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = EntryOrange
                                        )
                                    ) {
                                        Text(
                                            text = "SUBMIT",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            } else {

                                // -------------------------------------------------
                                // LOGIN / REGISTER TABS
                                // -------------------------------------------------

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                ) {

                                    AuthTab(
                                        text = "Login",
                                        icon = Icons.AutoMirrored.Filled.Login,
                                        selected = isLogin,
                                        isFirst = true,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        if (!isLogin) {
                                            isLogin = true
                                            email = ""
                                            password = ""
                                            confirmPassword = ""
                                            fullName = ""
                                            fieldErrors = AuthFormErrors()
                                            viewModel.clearMessages()
                                        }
                                    }

                                    AuthTab(
                                        text = "Register",
                                        icon = Icons.Default.PersonAdd,
                                        selected = !isLogin,
                                        isFirst = false,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        if (isLogin) {
                                            isLogin = false
                                            email = ""
                                            password = ""
                                            confirmPassword = ""
                                            fullName = ""
                                            fieldErrors = AuthFormErrors()
                                            viewModel.clearMessages()
                                        }
                                    }
                                }

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp, vertical = 16.dp)
                                ) {

                                    if (isLogin) {
                                        // -------------------------------------------------
                                        // LOGIN FORM
                                        // -------------------------------------------------

                                        AuthLabel("EMAIL ADDRESS")
                                        Spacer(modifier = Modifier.height(6.dp))
                                        AuthTextField(
                                            value = email,
                                            onValueChange = {
                                                email = it
                                                fieldErrors = fieldErrors.copy(email = null)
                                                viewModel.clearMessages()
                                            },
                                            placeholder = "you@example.com",
                                            leadingIcon = Icons.Default.Email,
                                            keyboardType = KeyboardType.Email,
                                            error = fieldErrors.email
                                        )

                                        Spacer(modifier = Modifier.height(14.dp))

                                        AuthLabel("PASSWORD")
                                        Spacer(modifier = Modifier.height(6.dp))
                                        AuthTextField(
                                            value = password,
                                            onValueChange = {
                                                password = it
                                                fieldErrors = fieldErrors.copy(password = null)
                                                viewModel.clearMessages()
                                            },
                                            placeholder = "Enter your password",
                                            leadingIcon = Icons.Default.Lock,
                                            isPassword = true,
                                            passwordVisible = passwordVisible,
                                            onPasswordVisibilityChange = { passwordVisible = !passwordVisible },
                                            error = fieldErrors.password
                                        )

                                        FieldError(uiState.errorMessage)

                                    } else {
                                        // -------------------------------------------------
                                        // REGISTER FORM
                                        // -------------------------------------------------

                                        AuthLabel("FULL NAME")
                                        Spacer(modifier = Modifier.height(6.dp))
                                        AuthTextField(
                                            value = fullName,
                                            onValueChange = {
                                                fullName = it
                                                fieldErrors = fieldErrors.copy(fullName = null)
                                                viewModel.clearMessages()
                                            },
                                            placeholder = "Name",
                                            leadingIcon = Icons.Default.Person,
                                            error = fieldErrors.fullName
                                        )

                                        Spacer(modifier = Modifier.height(14.dp))

                                        AuthLabel("EMAIL ADDRESS")
                                        Spacer(modifier = Modifier.height(6.dp))
                                        AuthTextField(
                                            value = email,
                                            onValueChange = {
                                                email = it
                                                fieldErrors = fieldErrors.copy(email = null)
                                                viewModel.clearMessages()
                                            },
                                            placeholder = "you@example.com",
                                            leadingIcon = Icons.Default.Email,
                                            keyboardType = KeyboardType.Email,
                                            error = fieldErrors.email
                                        )

                                        Spacer(modifier = Modifier.height(14.dp))

                                        AuthLabel("PASSWORD")
                                        Spacer(modifier = Modifier.height(6.dp))
                                        AuthTextField(
                                            value = password,
                                            onValueChange = {
                                                password = it
                                                fieldErrors = fieldErrors.copy(password = null, confirmPassword = null)
                                                viewModel.clearMessages()
                                            },
                                            placeholder = "Create a password",
                                            leadingIcon = Icons.Default.Lock,
                                            isPassword = true,
                                            passwordVisible = passwordVisible,
                                            onPasswordVisibilityChange = { passwordVisible = !passwordVisible },
                                            error = fieldErrors.password
                                        )

                                        Spacer(modifier = Modifier.height(14.dp))

                                        AuthLabel("CONFIRM PASSWORD")
                                        Spacer(modifier = Modifier.height(6.dp))
                                        AuthTextField(
                                            value = confirmPassword,
                                            onValueChange = {
                                                confirmPassword = it
                                                fieldErrors = fieldErrors.copy(confirmPassword = null)
                                                viewModel.clearMessages()
                                            },
                                            placeholder = "Confirm your password",
                                            leadingIcon = Icons.Default.Lock,
                                            isPassword = true,
                                            passwordVisible = confirmPasswordVisible,
                                            onPasswordVisibilityChange = { confirmPasswordVisible = !confirmPasswordVisible },
                                            error = fieldErrors.confirmPassword
                                        )

                                        FieldError(uiState.errorMessage)
                                    }

                                    Spacer(modifier = Modifier.height(20.dp))

                                    // -------------------------------------------------
                                    // ACTION BUTTON
                                    // -------------------------------------------------

                                    Button(
                                        onClick = {
                                            if (isLogin) {
                                                fieldErrors = validateLogin(email, password)
                                                if (fieldErrors == AuthFormErrors()) viewModel.login(email = email, password = password)
                                            } else {
                                                fieldErrors = validateRegistration(fullName, email, password, confirmPassword)
                                                if (fieldErrors == AuthFormErrors()) {
                                                    viewModel.register(email = email, fullName = fullName, password = password, confirmPassword = confirmPassword)
                                                }
                                            }
                                        },
                                        enabled = !uiState.isLoading,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = EntryOrange
                                        )
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = if (isLogin) Icons.AutoMirrored.Filled.Login else Icons.Default.PersonAdd,
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = if (uiState.isLoading) "PLEASE WAIT..." else if (isLogin) "LOGIN" else "CREATE ACCOUNT",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // -------------------------------------------------
                                    // SWITCH TEXT
                                    // -------------------------------------------------

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = if (isLogin) "Don't have an account? " else "Already have an account? ",
                                            color = LabelGrey,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = if (isLogin) "Sign up" else "Login",
                                            color = EntryOrange,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.clickable { isLogin = !isLogin }
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(1.dp)
                                            .background(BorderColor.copy(alpha = 0.5f))
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // -------------------------------------------------
                                    // FOOTER
                                    // -------------------------------------------------

                                    Text(
                                        text = buildAnnotatedString {
                                            append("By signing up, you agree to our ")
                                            withStyle(style = SpanStyle(color = EntryOrange, fontWeight = FontWeight.Bold)) {
                                                append("Terms")
                                            }
                                            append(" and ")
                                            withStyle(style = SpanStyle(color = EntryOrange, fontWeight = FontWeight.Bold)) {
                                                append("Privacy Policy")
                                            }
                                            append(".")
                                        },
                                        fontSize = 12.sp,
                                        color = LabelGrey,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OtpInputField(
    otpText: String,
    onOtpTextChange: (String) -> Unit,
    isError: Boolean
) {
    val focusRequester = remember { FocusRequester() }
    val shakeOffset = remember { Animatable(0f) }

    LaunchedEffect(isError) {
        if (isError) {
            shakeOffset.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 400
                    (-10f) at 50
                    10f at 100
                    (-10f) at 150
                    10f at 200
                    (-10f) at 250
                    10f at 300
                    (-5f) at 350
                }
            )
        } else {
            shakeOffset.snapTo(0f)
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    BasicTextField(
        value = otpText,
        onValueChange = {
            if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                onOtpTextChange(it)
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .offset { IntOffset(shakeOffset.value.roundToInt(), 0) }
            .focusRequester(focusRequester),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        cursorBrush = SolidColor(EntryOrange),
        decorationBox = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                repeat(6) { index ->
                    val char = when {
                        index >= otpText.length -> ""
                        else -> otpText[index].toString()
                    }
                    val isFocused = index == otpText.length
                    
                    Box(
                        modifier = Modifier
                            .size(45.dp)
                            .border(
                                width = 1.dp,
                                color = if (isError) FieldErrorColor else if (isFocused) EntryOrange else BorderColor,
                                shape = RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = char,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                textAlign = TextAlign.Center
                            )
                            if (isFocused) {
                                // Blinking Cursor
                                val cursorAlpha = remember { Animatable(1f) }
                                LaunchedEffect(Unit) {
                                    cursorAlpha.animateTo(
                                        targetValue = 0f,
                                        animationSpec = infiniteRepeatable(
                                            animation = tween(600),
                                            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
                                        )
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .width(2.dp)
                                        .height(20.dp)
                                        .alpha(cursorAlpha.value)
                                        .background(EntryOrange)
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun AuthTab(
    text: String,
    icon: ImageVector,
    selected: Boolean,
    isFirst: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(
                color = if (selected) EntryOrange else InactiveTabGrey,
                shape = RoundedCornerShape(
                    topStart = if (isFirst) 14.dp else 0.dp,
                    topEnd = if (!isFirst) 14.dp else 0.dp
                )
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = if (selected) Color.White else Color.Gray
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                color = if (selected) Color.White else Color.Gray,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun AuthLabel(text: String) {
    Text(
        text = text.uppercase(),
        color = LabelGrey,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.5.sp
    )
}

@Composable
private fun FieldError(message: String?) {
    AnimatedVisibility(
        visible = message != null,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = FieldErrorColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = message ?: "",
                color = FieldErrorColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordVisibilityChange: () -> Unit = {},
    phonePrefix: String? = null,
    error: String? = null
) {
    if (phonePrefix != null) {
        // Custom field for Phone Number to get the prefix flush to the left
        val interactionSource = remember { MutableInteractionSource() }
        val isFocused by interactionSource.collectIsFocusedAsState()
        
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp) // Fixed height to match others
                    .border(
                        width = 1.dp,
                        color = if (error != null) FieldErrorColor else if (isFocused) EntryOrange else BorderColor,
                        shape = RoundedCornerShape(10.dp)
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Prefix Box
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .background(
                            color = InactiveTabGrey,
                            shape = RoundedCornerShape(topStart = 9.dp, bottomStart = 9.dp)
                        )
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = phonePrefix,
                        color = Color.Black,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(BorderColor)
                )
                
                // Input Area
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp),
                    interactionSource = interactionSource,
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                    cursorBrush = SolidColor(EntryOrange),
                    decorationBox = { innerTextField ->
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                color = Color.LightGray,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        innerTextField()
                    }
                )
            }
            FieldError(error)
        }
    } else {
        Column {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp), // Controlled height
                singleLine = true,
                isError = error != null,
                textStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = 14.sp),
                placeholder = {
                    Text(
                        text = placeholder,
                        color = Color.LightGray,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = LabelGrey
                    )
                },
                trailingIcon = if (isPassword && value.isNotEmpty()) {
                    {
                        IconButton(onClick = onPasswordVisibilityChange) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null,
                                tint = LabelGrey
                            )
                        }
                    }
                } else null,
                visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
                shape = RoundedCornerShape(10.dp),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EntryOrange,
                    unfocusedBorderColor = BorderColor,
                    errorBorderColor = FieldErrorColor,
                    cursorColor = EntryOrange
                )
            )
            FieldError(error)
        }
    }
}
