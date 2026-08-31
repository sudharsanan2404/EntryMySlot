package com.entrymyslot.app.screens.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.entrymyslot.app.EntryMySlotApp
import com.entrymyslot.app.R
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

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
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return AuthScreenViewModel(repository = app.appContainer.authRepository) as T
            }
        }
    )

    val uiState by viewModel.uiState.collectAsState()
    var showSuccessOverlay by remember { mutableStateOf(false) }
    var successMessageTitle by remember { mutableStateOf("") }
    var successMessageSub by remember { mutableStateOf("") }

    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) {
            if (isLogin) {
                successMessageTitle = "Login Successful"
                successMessageSub = "Welcome back to EntryMySlot"
            } else {
                successMessageTitle = "Account Verified"
                successMessageSub = "Your account has been successfully registered.\nWelcome to EntryMySlot"
            }
            showSuccessOverlay = true
            kotlinx.coroutines.delay(2200.milliseconds)
            onAuthSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(EntryBlue)
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        // Main Form Content
        if (!showSuccessOverlay) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Absolute Top Back Button
                if (uiState.isOtpMode) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .statusBarsPadding()
                            .padding(16.dp)
                            .clickable { viewModel.clearOtpMode() },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center)
                        .padding(horizontal = 18.dp)
                        .verticalScroll(rememberScrollState())
                        .imePadding()
                        .padding(top = 0.dp, bottom = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Logo
                    Image(
                        painter = painterResource(id = R.drawable.entrymyslotlogo),
                        contentDescription = null,
                        modifier = Modifier
                            .width(240.dp)
                            .height(80.dp)
                            .padding(bottom = 24.dp)
                    )

                    // Auth Card with Transition
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        AnimatedContent(
                            targetState = uiState.isOtpMode,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(400)) + slideInHorizontally { it } togetherWith
                                fadeOut(animationSpec = tween(400)) + slideOutHorizontally { -it }
                            },
                            label = "AuthModeTransition"
                        ) { isOtp ->
                            if (isOtp) {
                                OtpVerificationView(
                                    otpValue = otpValue,
                                    onOtpChange = {
                                        otpValue = it
                                        fieldErrors = fieldErrors.copy(otp = null)
                                        viewModel.clearMessages()
                                        if (it.length == 6) {
                                            focusManager.clearFocus()
                                            viewModel.verifyOtp(email = email, otp = it)
                                        }
                                    },
                                    error = fieldErrors.otp ?: uiState.errorMessage,
                                    isLoading = uiState.isLoading,
                                    onResend = { viewModel.resendOtp(email) },
                                    onSubmit = {
                                        val otpError = if (otpValue.length != 6) "Enter the 6-digit OTP" else null
                                        fieldErrors = fieldErrors.copy(otp = otpError)
                                        if (otpError == null) viewModel.verifyOtp(email = email, otp = otpValue)
                                    }
                                )
                            } else {
                                LoginRegisterView(
                                    isLogin = isLogin,
                                    email = email,
                                    onEmailChange = { email = it; fieldErrors = fieldErrors.copy(email = null); viewModel.clearMessages() },
                                    password = password,
                                    onPasswordChange = { password = it; fieldErrors = fieldErrors.copy(password = null); viewModel.clearMessages() },
                                    confirmPassword = confirmPassword,
                                    onConfirmPasswordChange = { confirmPassword = it; fieldErrors = fieldErrors.copy(confirmPassword = null); viewModel.clearMessages() },
                                    fullName = fullName,
                                    onFullNameChange = { fullName = it; fieldErrors = fieldErrors.copy(fullName = null); viewModel.clearMessages() },
                                    passwordVisible = passwordVisible,
                                    onPasswordVisibilityChange = { passwordVisible = !passwordVisible },
                                    confirmPasswordVisible = confirmPasswordVisible,
                                    onConfirmPasswordVisibilityChange = { confirmPasswordVisible = !confirmPasswordVisible },
                                    fieldErrors = fieldErrors,
                                    serverError = uiState.errorMessage,
                                    isLoading = uiState.isLoading,
                                    onTabSwitch = {
                                        isLogin = it
                                        email = ""
                                        password = ""
                                        confirmPassword = ""
                                        fullName = ""
                                        fieldErrors = AuthFormErrors()
                                        viewModel.clearMessages()
                                    },
                                    onSubmit = {
                                        if (isLogin) {
                                            fieldErrors = validateLogin(email, password)
                                            if (fieldErrors == AuthFormErrors()) viewModel.login(email = email, password = password)
                                        } else {
                                            fieldErrors = validateRegistration(fullName, email, password, confirmPassword)
                                            if (fieldErrors == AuthFormErrors()) viewModel.register(email = email, fullName = fullName, password = password, confirmPassword = confirmPassword)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Success Overlay
        SuccessOverlay(visible = showSuccessOverlay, title = successMessageTitle, subtitle = successMessageSub)
    }
}

@Composable
private fun SuccessOverlay(visible: Boolean, title: String, subtitle: String) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(300)) + scaleIn(tween(300), initialScale = 0.96f),
        exit = fadeOut(tween(200))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(EntryBlue),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Success",
                    modifier = Modifier.size(82.dp),
                    tint = EntryOrange
                )
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = subtitle,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun OtpVerificationView(
    otpValue: String,
    onOtpChange: (String) -> Unit,
    error: String?,
    isLoading: Boolean,
    onResend: () -> Unit,
    onSubmit: () -> Unit
) {
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
            text = "Enter the OTP sent to your email address",
            fontSize = 13.sp,
            color = LabelGrey,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        OtpInputField(
            otpText = otpValue,
            onOtpTextChange = onOtpChange,
            isError = error != null
        )

        FieldError(error)
        
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
                modifier = Modifier.clickable { onResend() }
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onSubmit,
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = EntryOrange)
        ) {
            Text(
                text = if (isLoading) "VERIFYING..." else "SUBMIT",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun LoginRegisterView(
    isLogin: Boolean,
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    confirmPassword: String,
    onConfirmPasswordChange: (String) -> Unit,
    fullName: String,
    onFullNameChange: (String) -> Unit,
    passwordVisible: Boolean,
    onPasswordVisibilityChange: () -> Unit,
    confirmPasswordVisible: Boolean,
    onConfirmPasswordVisibilityChange: () -> Unit,
    fieldErrors: AuthFormErrors,
    serverError: String?,
    isLoading: Boolean,
    onTabSwitch: (Boolean) -> Unit,
    onSubmit: () -> Unit
) {
    Column {
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
                modifier = Modifier.weight(1f),
                onClick = { if (!isLogin) onTabSwitch(true) }
            )
            AuthTab(
                text = "Register",
                icon = Icons.Default.PersonAdd,
                selected = !isLogin,
                isFirst = false,
                modifier = Modifier.weight(1f),
                onClick = { if (isLogin) onTabSwitch(false) }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            if (isLogin) {
                AuthLabel("EMAIL ADDRESS")
                Spacer(modifier = Modifier.height(6.dp))
                AuthTextField(
                    value = email,
                    onValueChange = onEmailChange,
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
                    onValueChange = onPasswordChange,
                    placeholder = "Enter your password",
                    leadingIcon = Icons.Default.Lock,
                    isPassword = true,
                    passwordVisible = passwordVisible,
                    onPasswordVisibilityChange = onPasswordVisibilityChange,
                    error = fieldErrors.password
                )
                FieldError(serverError)
            } else {
                AuthLabel("FULL NAME")
                Spacer(modifier = Modifier.height(6.dp))
                AuthTextField(
                    value = fullName,
                    onValueChange = onFullNameChange,
                    placeholder = "Name",
                    leadingIcon = Icons.Default.Person,
                    error = fieldErrors.fullName
                )

                Spacer(modifier = Modifier.height(14.dp))

                AuthLabel("EMAIL ADDRESS")
                Spacer(modifier = Modifier.height(6.dp))
                AuthTextField(
                    value = email,
                    onValueChange = onEmailChange,
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
                    onValueChange = onPasswordChange,
                    placeholder = "Create a password",
                    leadingIcon = Icons.Default.Lock,
                    isPassword = true,
                    passwordVisible = passwordVisible,
                    onPasswordVisibilityChange = onPasswordVisibilityChange,
                    error = fieldErrors.password
                )

                Spacer(modifier = Modifier.height(14.dp))

                AuthLabel("CONFIRM PASSWORD")
                Spacer(modifier = Modifier.height(6.dp))
                AuthTextField(
                    value = confirmPassword,
                    onValueChange = onConfirmPasswordChange,
                    placeholder = "Confirm your password",
                    leadingIcon = Icons.Default.Lock,
                    isPassword = true,
                    passwordVisible = confirmPasswordVisible,
                    onPasswordVisibilityChange = onConfirmPasswordVisibilityChange,
                    error = fieldErrors.confirmPassword
                )
                FieldError(serverError)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onSubmit,
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EntryOrange)
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
                        text = if (isLoading) "PLEASE WAIT..." else if (isLogin) "LOGIN" else "CREATE ACCOUNT",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                    modifier = Modifier.clickable { onTabSwitch(!isLogin) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(BorderColor.copy(alpha = 0.5f)))
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = buildAnnotatedString {
                    append("By signing up, you agree to our ")
                    withStyle(style = SpanStyle(color = EntryOrange, fontWeight = FontWeight.Bold)) { append("Terms") }
                    append(" and ")
                    withStyle(style = SpanStyle(color = EntryOrange, fontWeight = FontWeight.Bold)) { append("Privacy Policy") }
                    append(".")
                },
                fontSize = 12.sp,
                color = LabelGrey,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
            )
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
                    
                    val charScale = remember { Animatable(0.6f) }
                    LaunchedEffect(char) {
                        if (char.isNotEmpty()) {
                            charScale.snapTo(0.6f)
                            charScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                        }
                    }

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
                                textAlign = TextAlign.Center,
                                modifier = Modifier.graphicsLayer(scaleX = charScale.value, scaleY = charScale.value)
                            )
                            if (isFocused) {
                                val cursorAlpha = remember { Animatable(1f) }
                                LaunchedEffect(Unit) {
                                    cursorAlpha.animateTo(
                                        targetValue = 0f,
                                        animationSpec = infiniteRepeatable(
                                            animation = tween(600),
                                            repeatMode = RepeatMode.Reverse
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
    error: String? = null
) {
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
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
