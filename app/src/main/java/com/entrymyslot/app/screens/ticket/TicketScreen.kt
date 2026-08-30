package com.entrymyslot.app.screens.ticket

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.entrymyslot.app.R
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import kotlin.math.min
import kotlin.math.sqrt

private val TicketNight = Color(0xFF030A1C)
private val TicketBlue = Color(0xFF0126A5)
private val TicketOrange = Color(0xFFFA580B)
private val TicketPaleBlue = Color(0xFFDCE8FF)
private val TicketWhite60 = Color.White.copy(alpha = .60f)
private val TicketSans = FontFamily.SansSerif
private val TicketMono = FontFamily.Monospace

data class TicketDetails(
    val bookingId: String,
    val title: String,
    val category: String,
    val venue: String,
    val date: String,
    val time: String,
    val admission: String,
    val attendee: String = "Guest User",
    val amount: String = "₹0"
) {
    val slots: List<String>
        get() = time.split(" - ", ",").map(String::trim).filter(String::isNotEmpty)
    val qrPayload: String
        get() = "$bookingId|$title|$venue|$date|$time|$admission"
}

@Composable
fun TicketScreen(
    ticket: TicketDetails,
    onBackClick: () -> Unit,
    onDoneClick: () -> Unit,
    onDownloadClick: () -> Unit = {},
    onShareClick: () -> Unit = {}
) {
    ProvideTextStyle(TextStyle(fontFamily = TicketSans)) {
        Box(Modifier.fillMaxSize().background(TicketNight)) {
            TicketFixedBackground()
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)
            ) {
                TicketNavigation(onBackClick)
                Column(
                    Modifier.fillMaxWidth().padding(vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TicketBody(ticket)
                    Spacer(Modifier.height(24.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        TicketSaveButton(onDownloadClick)
//                        Text(
//                            "DONE",
//                            color = TicketWhite60,
//                            fontSize = 13.sp,
//                            fontWeight = FontWeight.Bold,
//                            letterSpacing = 1.sp,
//                            modifier = Modifier.clickable(onClick = onDoneClick).padding(14.dp)
//                        )
                    }
                    Spacer(Modifier.navigationBarsPadding())
                }
            }
        }
    }
}

@Composable
private fun TicketFixedBackground() {
    Canvas(Modifier.fillMaxSize()) {
        fun radius(cx: Float, cy: Float, stop: Float): Float {
            val farthestX = maxOf(cx, size.width - cx)
            val farthestY = maxOf(cy, size.height - cy)
            return sqrt(farthestX * farthestX + farthestY * farthestY) * stop
        }
        val bottom = androidx.compose.ui.geometry.Offset(size.width * .50f, size.height)
        val blue = androidx.compose.ui.geometry.Offset(size.width * .85f, size.height * .30f)
        val orange = androidx.compose.ui.geometry.Offset(size.width * .15f, size.height * .50f)
        drawRect(Brush.radialGradient(listOf(TicketOrange.copy(alpha = .15f), Color.Transparent), bottom, radius(bottom.x, bottom.y, .60f)))
        drawRect(Brush.radialGradient(listOf(TicketBlue.copy(alpha = .80f), Color.Transparent), blue, radius(blue.x, blue.y, .50f)))
        drawRect(Brush.radialGradient(listOf(TicketOrange.copy(alpha = .25f), Color.Transparent), orange, radius(orange.x, orange.y, .40f)))
    }
}

@Composable
private fun TicketNavigation(onBack: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().statusBarsPadding().padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(Modifier.clickable(onClick = onBack), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(40.dp).clip(CircleShape).background(Color.White.copy(alpha = .05f))
                    .border(1.dp, Color.White.copy(alpha = .20f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(12.dp))
            Text("BACK", color = TicketWhite60, fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = .6.sp)
        }
    }
}

@Composable
private fun TicketBody(ticket: TicketDetails) {
    val shape = RoundedCornerShape(24.dp)
    Box(
        Modifier.fillMaxWidth().height(640.dp)
            .graphicsLayer {
                compositingStrategy = CompositingStrategy.Offscreen
                this.shape = shape
                clip = true
            }
            .drawWithContent {
                drawRoundRect(Color.White.copy(alpha = .045f), cornerRadius = androidx.compose.ui.geometry.CornerRadius(24.dp.toPx()))
                drawContent()
                drawRoundRect(
                    Color.White.copy(alpha = .25f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(24.dp.toPx()),
                    style = Stroke(1.dp.toPx())
                )
                val cutout = 24.dp.toPx()
                val split = size.height * .56f
                drawCircle(Color.Transparent, cutout, androidx.compose.ui.geometry.Offset(0f, split), blendMode = BlendMode.Clear)
                drawCircle(Color.Transparent, cutout, androidx.compose.ui.geometry.Offset(size.width, split), blendMode = BlendMode.Clear)
                drawCircle(Color.Transparent, cutout, androidx.compose.ui.geometry.Offset(size.width / 2, 0f), blendMode = BlendMode.Clear)
                drawCircle(Color.Transparent, cutout, androidx.compose.ui.geometry.Offset(size.width / 2, size.height), blendMode = BlendMode.Clear)
            }
    ) {
        Column(Modifier.fillMaxSize()) {
            TicketPrimary(ticket, Modifier.weight(.56f).fillMaxWidth())
            TicketPerforation()
            TicketQrPanel(ticket, Modifier.weight(.44f).fillMaxWidth())
        }
    }
}

@Composable
private fun TicketPrimary(ticket: TicketDetails, modifier: Modifier) {
    Column(modifier.padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            TicketBrandLogo()
            TicketAdmitOne()
        }
        Column {
            Text(
                ticket.title,
                color = Color.White,
                fontSize = 28.sp,
                lineHeight = 32.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-1.2).sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, null, tint = TicketOrange, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(7.dp))
                Text(ticket.venue, color = TicketPaleBlue, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 1)
            }
        }
        TicketInformation(ticket)
    }
}

@Composable
private fun TicketInformation(ticket: TicketDetails) {
    Column(Modifier.fillMaxWidth().ticketTopRule(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.padding(top = 12.dp)) {
            Box(Modifier.weight(1f)) { TicketInfoCell("DATE") { TicketValue(ticket.date) } }
            Box(Modifier.weight(1f)) { TicketInfoCell("TIME SLOT(S)") { TicketSlots(ticket.slots) } }
        }
        Row {
            Box(Modifier.weight(1f)) { TicketInfoCell("ATTENDEE") { TicketValue(ticket.attendee, true) } }
            Box(Modifier.weight(1f)) { TicketInfoCell("ACCESS") { TicketValue(ticket.admission) } }
        }
    }
}

@Composable
private fun TicketInfoCell(label: String, value: @Composable () -> Unit) {
    Column { TicketMicroText(label); Spacer(Modifier.height(6.dp)); value() }
}

@Composable
private fun TicketValue(value: String, ellipsize: Boolean = false) {
    Text(
        value,
        color = Color.White,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        overflow = if (ellipsize) TextOverflow.Ellipsis else TextOverflow.Clip
    )
}

@Composable
private fun TicketSlots(slots: List<String>) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        slots.take(2).forEach { slot ->
            Text(
                slot,
                color = Color.White,
                fontFamily = TicketMono,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.background(Color.White.copy(alpha = .10f), RoundedCornerShape(5.dp))
                    .border(1.dp, Color.White.copy(alpha = .20f), RoundedCornerShape(5.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun TicketQrPanel(ticket: TicketDetails, modifier: Modifier) {
    var expanded by rememberSaveable(ticket.bookingId) { mutableStateOf(false) }
    Box(modifier.background(Color.Black.copy(alpha = .10f)).padding(24.dp), contentAlignment = Alignment.Center) {
        Row(Modifier.align(Alignment.TopEnd), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(3) { Box(Modifier.size(6.dp).background(Color.White.copy(alpha = .20f), CircleShape)) }
        }
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            TicketMicroText("TICKET REFERENCE")
            Spacer(Modifier.height(4.dp))
            Text(ticket.bookingId, color = TicketOrange, fontFamily = TicketMono, fontWeight = FontWeight.Bold, fontSize = 14.sp, letterSpacing = 1.2.sp)
            Spacer(Modifier.height(12.dp))
            Box(
                Modifier.size(100.dp).clip(RoundedCornerShape(16.dp)).background(Color.White)
                    .clickable { expanded = true }.padding(12.dp)
            ) { TicketQr(ticket.qrPayload, Modifier.fillMaxSize()) }
            Spacer(Modifier.height(12.dp))
            TicketMicroText("TOTAL PAID")
            Spacer(Modifier.height(4.dp))
            Text(ticket.amount, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black, letterSpacing = (-1).sp)
        }
    }
    if (expanded) ExpandedTicketQrDialog(ticket, onDismiss = { expanded = false })
}

@Composable
private fun ExpandedTicketQrDialog(ticket: TicketDetails, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            Modifier.fillMaxWidth().padding(horizontal = 24.dp).widthIn(max = 360.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Brush.linearGradient(listOf(Color(0xFF0A1222), Color(0xFF081B58), Color(0xFF271619))))
                .border(1.dp, Color.White.copy(alpha = .15f), RoundedCornerShape(24.dp))
                .padding(horizontal = 24.dp, vertical = 28.dp)
        ) {
            Box(
                Modifier.align(Alignment.TopEnd).size(36.dp).clip(CircleShape)
                    .background(Color.White.copy(alpha = .08f)).clickable(onClick = onDismiss),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Default.Close, "Close enlarged ticket QR code", tint = Color.White.copy(alpha = .80f)) }
            Column(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                TicketMicroText("TICKET REFERENCE")
                Spacer(Modifier.height(8.dp))
                Text(ticket.bookingId, color = TicketOrange, fontFamily = TicketMono, fontWeight = FontWeight.Bold, fontSize = 18.sp, letterSpacing = 1.5.sp)
                Spacer(Modifier.height(24.dp))
                Box(Modifier.size(260.dp).clip(RoundedCornerShape(20.dp)).background(Color.White).padding(18.dp)) {
                    TicketQr(ticket.qrPayload, Modifier.fillMaxSize())
                }
            }
        }
    }
}

@Composable
private fun TicketPerforation() {
    Canvas(Modifier.fillMaxWidth().height(2.dp).offset(y = (-1).dp)) {
        drawLine(
            color = Color.White.copy(alpha = .30f),
            start = androidx.compose.ui.geometry.Offset(24.dp.toPx(), size.height / 2),
            end = androidx.compose.ui.geometry.Offset(size.width - 24.dp.toPx(), size.height / 2),
            strokeWidth = 2.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(7.dp.toPx(), 7.dp.toPx()))
        )
    }
}

@Composable
private fun TicketAdmitOne() {
    Row(
        Modifier.background(TicketOrange.copy(alpha = .20f), RoundedCornerShape(50))
            .border(1.dp, TicketOrange.copy(alpha = .30f), RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(6.dp).background(TicketOrange, CircleShape))
        Spacer(Modifier.width(7.dp))
        Text("ADMIT ONE", color = TicketOrange, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun TicketQr(payload: String, modifier: Modifier) {
    val matrix = remember(payload) {
        QRCodeWriter().encode(
            payload,
            BarcodeFormat.QR_CODE,
            1,
            1,
            mapOf(EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M, EncodeHintType.MARGIN to 0)
        )
    }
    Canvas(modifier) {
        val cell = min(size.width, size.height) / matrix.width
        for (y in 0 until matrix.height) for (x in 0 until matrix.width) {
            if (matrix[x, y]) drawRect(
                TicketBlue,
                androidx.compose.ui.geometry.Offset(x * cell, y * cell),
                androidx.compose.ui.geometry.Size(cell + .5f, cell + .5f)
            )
        }
    }
}

@Composable
private fun TicketSaveButton(onSavePdf: () -> Unit) {
    Row(
        Modifier.background(TicketOrange, RoundedCornerShape(12.dp)).clickable(onClick = onSavePdf)
            .padding(horizontal = 28.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Download, null, tint = Color.White, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(12.dp))
        Text("SAVE PDF", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.2.sp)
    }
}

@Composable
private fun TicketMicroText(text: String) {
    Text(text, color = TicketWhite60, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.5.sp)
}

@Composable
private fun TicketBrandLogo() {
    Image(
        painterResource(R.drawable.entrymyslotlogopcg),
        "EntryMySlot",
        contentScale = ContentScale.Fit,
        modifier = Modifier.height(38.dp).width(121.dp)
    )
}

private fun Modifier.ticketTopRule(): Modifier = drawWithContent {
    drawContent()
    drawLine(
        Color.White.copy(alpha = .10f),
        androidx.compose.ui.geometry.Offset.Zero,
        androidx.compose.ui.geometry.Offset(size.width, 0f),
        1.dp.toPx(),
        StrokeCap.Butt
    )
}
