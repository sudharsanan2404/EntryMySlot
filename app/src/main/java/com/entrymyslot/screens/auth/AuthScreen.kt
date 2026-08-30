package com.entrymyslot.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.entrymyslot.app.R

private val EntryBlue = Color(0xFF123BBD)
private val EntryOrange = Color(0xFFFF6500)
private val LabelGrey = Color(0xFF7D8597)
private val InactiveTabGrey = Color(0xFFF1F4F9)
private val BorderColor = Color(0xFFE2E8F0)

@Composable
fun AuthScreen(
    onBackClick: () -> Unit = {}
) {

    var isLogin by remember { mutableStateOf(true) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
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
                // Back Button
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 16.dp)
                        .clickable { onBackClick() },
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

                // Logo
                Image(
                    painter = painterResource(id = R.drawable.entrymyslot),
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
                        .padding(top = 80.dp, bottom = 20.dp),
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
                                    isLogin = true
                                }

                                AuthTab(
                                    text = "Register",
                                    icon = Icons.Default.PersonAdd,
                                    selected = !isLogin,
                                    isFirst = false,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    isLogin = false
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
                                        onValueChange = { email = it },
                                        placeholder = "you@example.com",
                                        leadingIcon = Icons.Default.Email,
                                        keyboardType = KeyboardType.Email
                                    )

                                    Spacer(modifier = Modifier.height(14.dp))

                                    AuthLabel("PASSWORD")
                                    Spacer(modifier = Modifier.height(6.dp))
                                    AuthTextField(
                                        value = password,
                                        onValueChange = { password = it },
                                        placeholder = "Enter your password",
                                        leadingIcon = Icons.Default.Lock,
                                        isPassword = true,
                                        passwordVisible = passwordVisible,
                                        onPasswordVisibilityChange = { passwordVisible = !passwordVisible }
                                    )

                                } else {
                                    // -------------------------------------------------
                                    // REGISTER FORM
                                    // -------------------------------------------------

                                    AuthLabel("FULL NAME")
                                    Spacer(modifier = Modifier.height(6.dp))
                                    AuthTextField(
                                        value = fullName,
                                        onValueChange = { fullName = it },
                                        placeholder = "name",
                                        leadingIcon = Icons.Default.Person
                                    )

                                    Spacer(modifier = Modifier.height(14.dp))

                                    AuthLabel("EMAIL ADDRESS")
                                    Spacer(modifier = Modifier.height(6.dp))
                                    AuthTextField(
                                        value = email,
                                        onValueChange = { email = it },
                                        placeholder = "you@example.com",
                                        leadingIcon = Icons.Default.Email,
                                        keyboardType = KeyboardType.Email
                                    )

                                    Spacer(modifier = Modifier.height(14.dp))

                                    AuthLabel("PHONE NUMBER")
                                    Spacer(modifier = Modifier.height(6.dp))
                                    AuthTextField(
                                        value = phoneNumber,
                                        onValueChange = { phoneNumber = it },
                                        placeholder = "Enter 10-digit number",
                                        leadingIcon = Icons.Default.Phone,
                                        keyboardType = KeyboardType.Phone,
                                        phonePrefix = "+91"
                                    )

                                    Spacer(modifier = Modifier.height(14.dp))

                                    AuthLabel("PASSWORD")
                                    Spacer(modifier = Modifier.height(6.dp))
                                    AuthTextField(
                                        value = password,
                                        onValueChange = { password = it },
                                        placeholder = "Create a password",
                                        leadingIcon = Icons.Default.Lock,
                                        isPassword = true,
                                        passwordVisible = passwordVisible,
                                        onPasswordVisibilityChange = { passwordVisible = !passwordVisible }
                                    )
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                // -------------------------------------------------
                                // ACTION BUTTON
                                // -------------------------------------------------

                                Button(
                                    onClick = {
                                        // Authentication will be added later
                                    },
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
                                            text = if (isLogin) "LOGIN" else "CREATE ACCOUNT",
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

                                Spacer(modifier = Modifier.height(10.dp))

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
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordVisibilityChange: () -> Unit = {},
    phonePrefix: String? = null
) {
    if (phonePrefix != null) {
        // Custom field for Phone Number to get the prefix flush to the left
        val isFocused = remember { mutableStateOf(false) }
        
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp) // Fixed height to match others
                    .border(
                        width = 1.dp,
                        color = if (isFocused.value) EntryOrange else BorderColor,
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
        }
    } else {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp), // Controlled height
            singleLine = true,
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
                cursorColor = EntryOrange
            )
        )
    }
}
