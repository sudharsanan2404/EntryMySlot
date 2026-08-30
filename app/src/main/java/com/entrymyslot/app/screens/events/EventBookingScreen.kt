package com.entrymyslot.app.screens.events

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val BgDark = Color(0xFF080B1A)
private val CardBg = Color(0xFF0D1025)
private val AccentOrange = Color(0xFFFF7A00)
private val White = Color.White
private val Gray = Color(0xFF8A8FA8)

@Composable
fun EventBookingScreen(
    eventId: Long,
    onBackClick: () -> Unit = {},
    onBookingSuccess: () -> Unit = {},
    eventTitle: String? = null
) {
    var quantity by remember { mutableStateOf("1") }
    var attendeeName by remember { mutableStateOf("") }
    var attendeePhone by remember { mutableStateOf("") }
    var couponCode by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = White,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { onBackClick() }
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Book Event",
                    color = White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (eventTitle != null) {
                    Text(
                        text = eventTitle,
                        color = White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Tickets", color = Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors()
                )

                OutlinedTextField(
                    value = attendeeName,
                    onValueChange = { attendeeName = it },
                    label = { Text("Attendee Name", color = Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors()
                )

                OutlinedTextField(
                    value = attendeePhone,
                    onValueChange = { attendeePhone = it },
                    label = { Text("Phone Number", color = Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Phone)
                )

                OutlinedTextField(
                    value = couponCode,
                    onValueChange = { couponCode = it },
                    label = { Text("Coupon Code (optional)", color = Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors()
                )

                if (errorMessage != null) {
                    Text(text = errorMessage!!, color = Color(0xFFFF4444), fontSize = 13.sp)
                }
                if (successMessage != null) {
                    Text(text = successMessage!!, color = Color(0xFF4CAF50), fontSize = 13.sp)
                }

                Button(
                    onClick = {
                        when {
                            quantity.isBlank() || quantity.toIntOrNull() == null || quantity.toInt() < 1 ->
                                errorMessage = "Enter valid ticket quantity"
                            attendeeName.isBlank() ->
                                errorMessage = "Enter attendee name"
                            attendeePhone.length !in 10..15 ->
                                errorMessage = "Enter valid phone number"
                            else -> {
                                isLoading = true
                                errorMessage = null
                                // TODO: call repository
                                isLoading = false
                                successMessage = "Booking confirmed!"
                                onBookingSuccess()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Confirm Booking", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun fieldColors() = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
    focusedTextColor = White,
    unfocusedTextColor = White,
    focusedBorderColor = AccentOrange,
    unfocusedBorderColor = Color(0xFF1E2244),
    cursorColor = AccentOrange,
    focusedLabelColor = Gray,
    unfocusedLabelColor = Gray
)
