package com.entrymyslot.app.core.components
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.rounded.Gavel
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Policy
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.selection.toggleable

private val SheetBackground = Color(0xFF061A38)
private val SheetSurface = Color(0xFF0B274F)
private val SheetBorder = Color(0xFF24527D)
private val SheetAccent = Color(0xFFFF8A00)
private val SheetText = Color(0xFFF8FAFF)
private val SheetSecondary = Color(0xFFA8B8CF)

private data class BookingRule(
    val title: String,
    val content: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsAndPolicyBottomSheet(
    category: String,
    onDismiss: () -> Unit,
    onAccept: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val normalizedCategory = category.uppercase()
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
            Text(
                text = "Booking Terms & Conditions",
                color = SheetText,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Please review the important conditions before continuing.",
                color = SheetSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                modifier = Modifier.padding(top = 5.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp)
                    .heightIn(max = 390.dp)
            ) {
                itemsIndexed(rules) { index, rule ->
                    BookingRuleCard(rule = rule)
                    if (index != rules.lastIndex) Spacer(Modifier.height(14.dp))
                }
            }

            Text("By continuing, you agree to the above conditions.", color = SheetSecondary, fontSize = 13.sp)

            Button(
                onClick = onAccept,
                modifier = Modifier.fillMaxWidth().padding(top = 17.dp).height(54.dp),
                shape = RoundedCornerShape(15.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SheetAccent,
                    contentColor = Color.White,
                    disabledContainerColor = SheetAccent.copy(alpha = 0.22f),
                    disabledContentColor = Color.White.copy(alpha = 0.45f)
                )
            ) {
                Text(
                    "Continue Booking",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun bookingRulesFor(category: String): List<BookingRule> = listOf(
    BookingRule("Booking Terms", "Verify your selection and review the venue's cancellation and refund terms before payment.", Icons.Outlined.ConfirmationNumber),
    BookingRule("Privacy Policy", "Review the payment provider's privacy policy in checkout before entering payment information.", Icons.Outlined.Security),
    BookingRule("Venue Rules", when (category) {
        "MOVIE" -> "Check the cinema's arrival, admission, and food policies before your visit."
        "TURF" -> "Check the venue's footwear requirements and your selected slot's start and end times."
        "EVENT" -> "Check the event organizer's admission and age requirements before booking."
        else -> "Please follow the instructions provided at the venue for a safe and enjoyable experience."
    }, Icons.Outlined.Info)
)

@Composable
private fun BookingRuleCard(rule: BookingRule) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {

            Icon(rule.icon, null, tint = SheetAccent, modifier = Modifier.size(17.dp))

            Text(
                text = rule.content,
                color = SheetSecondary,
                fontSize = 12.sp,
                lineHeight = 19.sp,
                modifier = Modifier.padding(start = 10.dp, end = 4.dp)
            )
        }
    }
}