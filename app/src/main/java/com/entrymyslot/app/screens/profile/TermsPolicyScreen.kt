package com.entrymyslot.app.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.entrymyslot.app.screens.home.GlowBackground

private val TermsBackground = Color(0xFF061A38)
private val TermsSurface = Color(0xFF0B274F)
private val TermsBorder = Color(0xFF24527D)
private val TermsAccent = Color(0xFFFA580B)
private val TermsText = Color(0xFFF8FAFF)
private val TermsSecondary = Color(0xFFA8B8CF)

private data class TermsSection(val title: String, val content: String)

private val termsSections = listOf(
    TermsSection("Using EntryMySlot", "Use accurate account and contact details when making a booking. You are responsible for reviewing the venue, date, time, quantity and customer details before confirming payment."),
    TermsSection("Bookings and payments", "A booking is confirmed only after the payment flow is completed and a ticket or booking ID is generated. Prices, taxes and any applicable charges are shown before you continue."),
    TermsSection("Changes, cancellations and refunds", "Cancellation or rescheduling eligibility can differ by venue and booking type. Review the conditions shown for your selection before paying. Approved refunds may require processing time from the payment provider."),
    TermsSection("Entry and venue rules", "Carry the generated ticket and any identification requested by the venue. Follow the venue's safety, age, timing and prohibited-item rules. Entry may be refused when a ticket is invalid or venue rules are not followed."),
    TermsSection("Location and notifications", "Location is used only when permission is granted or when you choose a city manually. Notifications may be used for booking reminders and relevant updates, and can be changed from device settings."),
    TermsSection("Privacy and app data", "Only provide information required to use the app and complete a booking. You can clear locally stored app data through your device settings. Avoid sharing your ticket QR code or booking ID publicly."),
    TermsSection("Help and support", "If booking information looks incorrect or you need assistance, use Help & Support from the menu and include your booking ID. Do not include card PINs, UPI PINs or passwords in a support message.")
)

@Composable
fun TermsPolicyScreen(onBackClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(TermsBackground)) {
        GlowBackground()
        LazyColumn(
            modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = TermsText)
                    }
                    Text("Terms & Policy", color = TermsText, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth().border(1.dp, TermsBorder.copy(alpha = 0.55f), RoundedCornerShape(22.dp)),
                    color = Color.Transparent,
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Box(modifier = Modifier.background(Brush.horizontalGradient(listOf(Color(0xFF123F77), TermsSurface)))) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Box(
                                modifier = Modifier.size(42.dp).background(TermsAccent.copy(alpha = 0.16f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) { Icon(Icons.Outlined.Description, null, tint = TermsAccent) }
                            Spacer(Modifier.height(16.dp))
                            Text("Simple terms for every booking", color = TermsText, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            Text(
                                "This page explains the general conditions for using EntryMySlot. Booking-specific rules are shown again before payment.",
                                color = TermsSecondary,
                                fontSize = 13.sp,
                                lineHeight = 19.sp,
                                modifier = Modifier.padding(top = 7.dp)
                            )
                        }
                    }
                }
            }
            item {
                Row(modifier = Modifier.padding(top = 10.dp, bottom = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Info, null, tint = TermsAccent, modifier = Modifier.size(18.dp))
                    Text(
                        "GENERAL CONDITIONS",
                        color = TermsSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
            itemsIndexed(termsSections) { index, section -> TermsSectionCard(index + 1, section) }
        }
    }
}

@Composable
private fun TermsSectionCard(number: Int, section: TermsSection) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(TermsSurface.copy(alpha = 0.86f), RoundedCornerShape(17.dp))
            .border(1.dp, TermsBorder.copy(alpha = 0.4f), RoundedCornerShape(17.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier.size(28.dp).background(TermsAccent.copy(alpha = 0.14f), CircleShape),
            contentAlignment = Alignment.Center
        ) { Text(number.toString(), color = TermsAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
        Column(modifier = Modifier.padding(start = 13.dp)) {
            Text(section.title, color = TermsText, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text(section.content, color = TermsSecondary, fontSize = 12.sp, lineHeight = 18.sp, modifier = Modifier.padding(top = 5.dp))
        }
    }
}
