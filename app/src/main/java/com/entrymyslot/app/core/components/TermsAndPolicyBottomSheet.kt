package com.entrymyslot.app.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Gavel
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsAndPolicyBottomSheet(
    category: String, // "MOVIE", "TURF", "EVENT"
    onDismiss: () -> Unit,
    onAccept: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0E0B38),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.2f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Terms & Policies",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Please review our booking rules and privacy policy before proceeding.",
                color = Color(0xFF98A2B3),
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp)
            ) {
                PolicyItem(
                    icon = Icons.Rounded.Gavel,
                    title = "Booking Terms",
                    content = "Tickets once booked cannot be cancelled or refunded. Please verify your selection before payment."
                )
                Spacer(modifier = Modifier.height(16.dp))
                PolicyItem(
                    icon = Icons.Rounded.Security,
                    title = "Privacy Policy",
                    content = "We value your privacy. Your payment information is encrypted and never stored on our servers."
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                when (category.uppercase()) {
                    "MOVIE" -> {
                        PolicyItem(
                            icon = Icons.Rounded.Gavel,
                            title = "Cinema Rules",
                            content = "Please arrive 15 minutes early. Outside food and beverages are not allowed inside the theatre."
                        )
                    }
                    "TURF" -> {
                        PolicyItem(
                            icon = Icons.Rounded.Gavel,
                            title = "Turf Rules",
                            content = "Professional artificial grass shoes or flat-soled shoes only. Please vacate the turf promptly when your slot ends."
                        )
                    }
                    "EVENT" -> {
                        PolicyItem(
                            icon = Icons.Rounded.Gavel,
                            title = "Event Rules",
                            content = "Entry allowed only with valid digital pass. Age restrictions may apply based on the event category."
                        )
                    }
                    else -> {
                        PolicyItem(
                            icon = Icons.Rounded.Gavel,
                            title = "Venue Rules",
                            content = "Please follow the instructions provided at the venue for a safe and enjoyable experience."
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onAccept,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8A3D))
            ) {
                Text(
                    text = "Accept & Continue",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun PolicyItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    content: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFFFF8A3D),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = content,
                color = Color(0xFF98A2B3),
                fontSize = 13.sp,
                lineHeight = 18.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
