package com.entrymyslot.app.screens.ticket

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class TicketDetails(
    val bookingId: String,
    val title: String,
    val category: String,
    val venue: String,
    val date: String,
    val time: String,
    val admission: String
)

@Composable
fun TicketScreen(
    ticket: TicketDetails,
    onBackClick: () -> Unit,
    onDoneClick: () -> Unit,
    onDownloadClick: () -> Unit = {},
    onShareClick: () -> Unit = {}
) {
    Column(
        Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF0126A5), Color(0xFF061A3D))))
            .statusBarsPadding().navigationBarsPadding()
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back", tint = Color.White) }
            Text("Your Digital Pass", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 19.sp)
        }

        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Outlined.CheckCircle, null, tint = Color(0xFF4ADE80), modifier = Modifier.size(32.dp))
            Spacer(Modifier.height(6.dp))
            Text("Booking confirmed", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(18.dp))

            Column(Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(24.dp)).padding(22.dp)) {
                Text(ticket.category.uppercase(), color = Color(0xFFFA580B), fontSize = 11.sp, fontWeight = FontWeight.Black, letterSpacing = 1.4.sp)
                Spacer(Modifier.height(7.dp))
                Text(ticket.title, color = Color(0xFF0A1D4D), fontSize = 27.sp, lineHeight = 30.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.height(18.dp))
                TicketDetailRow("DATE", ticket.date, "TIME", ticket.time)
                Spacer(Modifier.height(14.dp))
                TicketDetailRow("VENUE", ticket.venue, "ADMISSION", ticket.admission)
                Spacer(Modifier.height(22.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.weight(1f).height(1.dp).background(Color(0xFFD6DCE7)))
                    Box(Modifier.padding(horizontal = 10.dp).size(8.dp).background(Color(0xFFFA580B), CircleShape))
                    Box(Modifier.weight(1f).height(1.dp).background(Color(0xFFD6DCE7)))
                }
                Spacer(Modifier.height(20.dp))
                TicketQrCode(Modifier.size(142.dp).align(Alignment.CenterHorizontally))
                Spacer(Modifier.height(12.dp))
                Text(ticket.bookingId, color = Color(0xFF667085), fontSize = 11.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
                Text("Show this pass at the entrance", color = Color(0xFF0A1D4D), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))
            }

            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onDownloadClick, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                    Icon(Icons.Outlined.Download, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(7.dp)); Text("Save", color = Color.White)
                }
                OutlinedButton(onClick = onShareClick, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                    Icon(Icons.Outlined.Share, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(7.dp)); Text("Share", color = Color.White)
                }
            }
        }

        Button(
            onClick = onDoneClick,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp).height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFA580B))
        ) { Text("Done", fontWeight = FontWeight.ExtraBold) }
    }
}

@Composable
private fun TicketDetailRow(leftLabel: String, leftValue: String, rightLabel: String, rightValue: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        TicketValue(leftLabel, leftValue, Modifier.weight(1f))
        TicketValue(rightLabel, rightValue, Modifier.weight(1f))
    }
}

@Composable
private fun TicketValue(label: String, value: String, modifier: Modifier) {
    Column(modifier) {
        Text(label, color = Color(0xFF98A2B3), fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = .8.sp)
        Spacer(Modifier.height(3.dp))
        Text(value, color = Color(0xFF0A1D4D), fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, maxLines = 2)
    }
}

@Composable
private fun TicketQrCode(modifier: Modifier = Modifier) {
    Canvas(modifier.background(Color.White)) {
        val cells = 21
        val cell = size.minDimension / cells
        for (row in 0 until cells) for (column in 0 until cells) {
            val finder = (row < 7 && column < 7) || (row < 7 && column >= 14) || (row >= 14 && column < 7)
            val module = finder || ((row * 17 + column * 31 + row * column) % 5 < 2)
            if (module) drawRect(Color(0xFF0A1D4D), Offset(column * cell, row * cell), Size(cell, cell))
        }
    }
}
