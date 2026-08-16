package com.entrymyslot

import android.util.Patterns
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

// --- Ultra Clean Premium Theme Colors ---
val SolidDarkGrey = Color(0xFF121418) // Clean, deep grey background
val BorderGrey = Color(0xFF333842) // Subtle border for unfocused states
val PrimaryOrangeStart = Color(0xFFFF8A00)
val PrimaryOrangeEnd = Color(0xFFFF5200)
val TextWhite = Color(0xFFF3F4F6)
val TextMuted = Color(0xFFA0AEC0)

enum class AuthState { INPUT_SCREEN, OTP_SCREEN }

data class Country(val name: String, val code: String, val digitCount: Int)

@Composable
fun LoginScreen() {
    var currentScreen by remember { mutableStateOf(AuthState.INPUT_SCREEN) }
    var inputValue by remember { mutableStateOf("") }
    val uriHandler = LocalUriHandler.current

    Box(modifier = Modifier.fillMaxSize().background(SolidDarkGrey)) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                val termsText = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = TextMuted)) { append("By clicking continue you are accepting our\n") }
                    pushStringAnnotation(tag = "TNC", annotation = "https://google.com")
                    withStyle(style = SpanStyle(color = PrimaryOrangeStart, textDecoration = TextDecoration.Underline)) {
                        append("Terms and Conditions")
                    }
                    pop()
                    withStyle(style = SpanStyle(color = TextMuted)) { append(" and ") }
                    pushStringAnnotation(tag = "PP", annotation = "https://google.com")
                    withStyle(style = SpanStyle(color = PrimaryOrangeStart, textDecoration = TextDecoration.Underline)) {
                        append("Privacy Policy")
                    }
                    pop()
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .imePadding() // Pushes text above keyboard
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ClickableText(
                        text = termsText,
                        style = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Center, fontSize = 11.sp, lineHeight = 16.sp),
                        onClick = { offset ->
                            termsText.getStringAnnotations(tag = "TNC", start = offset, end = offset).firstOrNull()?.let {
                                uriHandler.openUri(it.item)
                            }
                            termsText.getStringAnnotations(tag = "PP", start = offset, end = offset).firstOrNull()?.let {
                                uriHandler.openUri(it.item)
                            }
                        }
                    )
                }
            }
        ) { paddingValues ->
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    slideInHorizontally(animationSpec = tween(400)) { it } + fadeIn(tween(400)) togetherWith
                            slideOutHorizontally(animationSpec = tween(400)) { -it } + fadeOut(tween(400))
                },
                label = "login_transition",
                modifier = Modifier.fillMaxSize().padding(paddingValues)
            ) { targetScreen ->
                when (targetScreen) {
                    AuthState.INPUT_SCREEN -> {
                        InputScreen(
                            inputValue = inputValue,
                            onValueChange = { inputValue = it },
                            onContinue = { currentScreen = AuthState.OTP_SCREEN }
                        )
                    }
                    AuthState.OTP_SCREEN -> {
                        OtpScreen(
                            targetContact = inputValue,
                            onBack = { currentScreen = AuthState.INPUT_SCREEN }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InputScreen(
    inputValue: String,
    onValueChange: (String) -> Unit,
    onContinue: () -> Unit
) {
    val context = LocalContext.current
    val countries = listOf(
        Country("India", "+91", 10),
        Country("USA", "+1", 10),
        Country("UK", "+44", 10),
        Country("UAE", "+971", 9)
    )
    var expandedDropdown by remember { mutableStateOf(false) }
    var selectedCountry by remember { mutableStateOf(countries[0]) }

    val isPhoneMode = inputValue.isEmpty() || inputValue.first().isDigit()
    val isEmailMode = inputValue.isNotEmpty() && !inputValue.first().isDigit()

    val isValid = when {
        isPhoneMode && inputValue.isNotEmpty() -> inputValue.length == selectedCountry.digitCount
        isEmailMode -> Patterns.EMAIL_ADDRESS.matcher(inputValue).matches()
        else -> false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Icon
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .border(1.dp, PrimaryOrangeStart, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "EMS", color = PrimaryOrangeStart, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "EntryMySlot", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = TextWhite)
        Text(
            text = "your game your slot",
            fontSize = 13.sp,
            color = PrimaryOrangeStart,
            fontStyle = FontStyle.Italic
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Fixed TextBox (Transparent Background)
        OutlinedTextField(
            value = inputValue,
            onValueChange = { newValue ->
                val filtered = newValue.replace(" ", "")
                if (isPhoneMode && filtered.length > selectedCountry.digitCount) return@OutlinedTextField
                onValueChange(filtered)
            },
            label = { Text("Phone Number or Email", color = TextMuted, fontSize = 14.sp) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryOrangeStart,
                unfocusedBorderColor = BorderGrey,
                focusedTextColor = TextWhite,
                unfocusedTextColor = TextWhite,
                cursorColor = PrimaryOrangeStart,
                unfocusedContainerColor = Color.Transparent, // FIX: Removed the ugly background block
                focusedContainerColor = Color.Transparent  // FIX: Transparent background
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = if (isPhoneMode) KeyboardType.Number else KeyboardType.Email
            ),
            singleLine = true,
            leadingIcon = if (isPhoneMode) {
                {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { expandedDropdown = true }
                            .padding(start = 16.dp, end = 8.dp)
                    ) {
                        Text(text = selectedCountry.code, color = TextWhite, fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown", tint = TextMuted)

                        DropdownMenu(
                            expanded = expandedDropdown,
                            onDismissRequest = { expandedDropdown = false },
                            modifier = Modifier.background(SolidDarkGrey)
                        ) {
                            countries.forEach { country ->
                                DropdownMenuItem(
                                    text = { Text("${country.name} (${country.code})", color = TextWhite) },
                                    onClick = {
                                        selectedCountry = country
                                        expandedDropdown = false
                                        onValueChange("")
                                    }
                                )
                            }
                        }
                    }
                }
            } else null
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (isValid) {
                    onContinue()
                } else {
                    val msg = if (isPhoneMode) "Enter valid ${selectedCountry.digitCount}-digit number" else "Enter valid email ID"
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            contentPadding = PaddingValues(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = if (isValid) Brush.horizontalGradient(listOf(PrimaryOrangeStart, PrimaryOrangeEnd))
                        else Brush.horizontalGradient(listOf(Color.Gray, Color.DarkGray)),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Continue", fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun OtpScreen(targetContact: String, onBack: () -> Unit) {
    var otpValue by remember { mutableStateOf("") }
    val context = LocalContext.current

    // Keyboard Focus Logic
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        delay(300) // Small delay to let the screen transition finish smoothly
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            IconButton(onClick = onBack, modifier = Modifier.offset(x = (-12).dp)) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextWhite)
            }
            Text(text = "Verify OTP", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextWhite)
        }

        Text(
            text = "Code sent to $targetContact",
            fontSize = 13.sp,
            color = TextMuted,
            modifier = Modifier.padding(bottom = 40.dp)
        )

        // Perfect Square OTP Fields
        BasicTextField(
            value = otpValue,
            onValueChange = { if (it.length <= 6) otpValue = it },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier.focusRequester(focusRequester), // Attached FocusRequester here
            decorationBox = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    repeat(6) { index ->
                        val char = if (index >= otpValue.length) "" else otpValue[index].toString()
                        val isFocused = otpValue.length == index

                        Box(
                            modifier = Modifier
                                .size(50.dp) // FIX: Perfect Square size
                                .clip(RoundedCornerShape(10.dp))
                                .border(
                                    width = if (isFocused) 2.dp else 1.dp,
                                    color = if (isFocused) PrimaryOrangeStart else BorderGrey,
                                    shape = RoundedCornerShape(10.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = char, fontSize = 22.sp, color = TextWhite, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = {
                if (otpValue.length == 6) {
                    Toast.makeText(context, "Welcome to EntryMySlot!", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Enter 6-digit OTP", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            contentPadding = PaddingValues(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = if (otpValue.length == 6) Brush.horizontalGradient(listOf(PrimaryOrangeStart, PrimaryOrangeEnd))
                        else Brush.horizontalGradient(listOf(Color.Gray, Color.DarkGray)),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Verify & Login", fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Resend OTP",
            color = PrimaryOrangeStart,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .clickable { Toast.makeText(context, "OTP Resent!", Toast.LENGTH_SHORT).show() }
                .padding(8.dp)
        )
    }
}