package com.entrymyslot.app.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Gavel
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val SheetBackground = Color(0xFF061A38)
private val SheetSurface = Color(0xFF0B274F)
private val SheetBorder = Color(0xFF24527D)
private val SheetAccent = Color(0xFFFA580B)
private val SheetText = Color(0xFFF8FAFF)
private val SheetSecondary = Color(0xFFA8B8CF)

private data class BookingRule(val title: String, val content: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsAndPolicyBottomSheet(
    category: String,
    onDismiss: () -> Unit,
    onAccept: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val normalizedCategory = category.uppercase()
    val bookingName = when (normalizedCategory) {
        "MOVIE" -> "movie booking"
        "TURF" -> "turf booking"
        "EVENT" -> "event booking"
        else -> "booking"
    }
    val rules = bookingRulesFor(normalizedCategory)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SheetBackground,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .size(width = 38.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(SheetBorder)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 28.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(44.dp).background(SheetAccent.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Gavel, null, tint = SheetAccent, modifier = Modifier.size(22.dp))
                }
                Column(modifier = Modifier.padding(start = 13.dp)) {
                    Text("Before you continue", color = SheetText, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "Important rules for this $bookingName",
                        color = SheetSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Spacer(Modifier.height(18.dp))
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                rules.forEachIndexed { index, rule -> BookingRuleCard(number = index + 1, rule = rule) }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .background(SheetAccent.copy(alpha = 0.09f), RoundedCornerShape(13.dp))
                    .padding(horizontal = 13.dp, vertical = 11.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(Icons.Outlined.Info, null, tint = SheetAccent, modifier = Modifier.size(17.dp))
                Text(
                    "By continuing, you confirm that the booking details are correct and that you agree to follow these rules.",
                    color = SheetSecondary,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(start = 9.dp)
                )
            }

            Button(
                onClick = onAccept,
                modifier = Modifier.fillMaxWidth().padding(top = 17.dp).height(54.dp),
                shape = RoundedCornerShape(15.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SheetAccent)
            ) {
                Icon(Icons.Rounded.Check, null, tint = Color.White, modifier = Modifier.size(19.dp))
                Text(
                    "I Understand · Continue",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

private fun bookingRulesFor(category: String): List<BookingRule> = when (category) {
    "MOVIE" -> listOf(
        BookingRule("No alcohol", "Alcoholic drinks are not permitted anywhere inside the cinema."),
        BookingRule("No outside food", "Food and drinks purchased outside the cinema cannot be taken into the auditorium."),
        BookingRule("No smoking", "Smoking and vaping are prohibited throughout the cinema premises.")
    )
    "TURF" -> listOf(
        BookingRule("Use your booked slot", "Enter at the selected start time and clear the playing area when your reserved slot ends."),
        BookingRule("Play safely", "Use suitable footwear and follow staff instructions, capacity limits and equipment rules."),
        BookingRule("Keep the venue smoke-free", "Alcohol, smoking and vaping are not permitted within the turf premises.")
    )
    "EVENT" -> listOf(
        BookingRule("Keep your ticket ready", "Present the valid digital ticket at entry and keep its QR code private."),
        BookingRule("Follow entry conditions", "Age limits, identification checks and re-entry rules are decided by the venue."),
        BookingRule("Respect venue guidance", "Follow staff directions and use only designated areas for restricted activities.")
    )
    else -> listOf(
        BookingRule("Check your booking", "Confirm the venue, date, time and quantity before continuing."),
        BookingRule("Follow venue rules", "Observe the safety and entry guidance provided at the venue.")
    )
}

@Composable
private fun BookingRuleCard(number: Int, rule: BookingRule) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SheetSurface, RoundedCornerShape(14.dp))
            .border(1.dp, SheetBorder.copy(alpha = 0.48f), RoundedCornerShape(14.dp))
            .padding(13.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier.size(26.dp).background(SheetAccent.copy(alpha = 0.14f), CircleShape),
            contentAlignment = Alignment.Center
        ) { Text(number.toString(), color = SheetAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
        Column(modifier = Modifier.padding(start = 11.dp)) {
            Text(rule.title, color = SheetText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(rule.content, color = SheetSecondary, fontSize = 11.sp, lineHeight = 16.sp, modifier = Modifier.padding(top = 3.dp))
        }
    }
}
