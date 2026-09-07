package com.entrymyslot.app.screens.ticket

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ButtonDefaults
import com.entrymyslot.app.core.components.PremiumLoadingState
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.entrymyslot.app.R
import com.entrymyslot.app.EntryMySlotApp
import com.entrymyslot.app.data.model.TicketDetails
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import kotlin.math.min
import kotlin.math.sqrt
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

private val TicketNight = Color(0xFF030A1C)
private val TicketBlue = Color(0xFF0126A5)
private val TicketOrange = Color(0xFFFA580B)
private val TicketPaleBlue = Color(0xFFDCE8FF)
private val TicketWhite60 = Color.White.copy(alpha = .60f)
private val TicketSans = FontFamily.SansSerif
private val TicketMono = FontFamily.Monospace

@Composable
fun TicketScreen(
    type: String,
    itemId: String,
    bookingKey: String,
    ticketUuid: String,
    onBackClick: () -> Unit,
    onDoneClick: () -> Unit,
    onDownloadClick: () -> Unit = {},
    onShareClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val app = context.applicationContext as EntryMySlotApp
    val viewModel: TicketViewModel = viewModel(
        key = "$type:$bookingKey:$ticketUuid",
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                TicketViewModel(
                    type = type,
                    itemId = itemId,
                    bookingKey = bookingKey,
                    ticketUuid = ticketUuid,
                    backend = app.appContainer.backend
                ) as T
        }
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val savePdf = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri -> if (uri != null) viewModel.savePdf(context, uri) }
    androidx.compose.runtime.LaunchedEffect(uiState.errorMessage) {
        if (uiState.ticket != null && uiState.errorMessage != null) android.widget.Toast.makeText(context, uiState.errorMessage, android.widget.Toast.LENGTH_LONG).show()
    }

    when {
        uiState.isLoading -> Box(
            Modifier.fillMaxSize().background(TicketNight),
            contentAlignment = Alignment.Center
        ) {
            PremiumLoadingState(modifier = Modifier.fillMaxSize())
        }
        uiState.ticket == null -> Box(
            Modifier.fillMaxSize().background(TicketNight).padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(uiState.errorMessage ?: "Unable to load ticket.", color = Color.White)
                Spacer(Modifier.height(18.dp))
                Text(
                    "RETRY",
                    color = TicketOrange,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { viewModel.loadTicket() }
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    "BACK",
                    color = TicketWhite60,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable(onClick = onBackClick)
                )
            }
        }
        else -> TicketContent(
            ticket = requireNotNull(uiState.ticket),
            selectedTicketIndex = uiState.selectedTicketIndex,
            ticketCount = uiState.tickets.size,
            onPreviousTicket = viewModel::previousTicket,
            onNextTicket = viewModel::nextTicket,
            onBackClick = onBackClick,
            onDownloadClick = { savePdf.launch("EntryMySlot-ticket.pdf") },
            onShareClick = {
                val ticket = requireNotNull(uiState.ticket)
                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                    this.type = "text/plain"
                    putExtra(android.content.Intent.EXTRA_TEXT, listOf(ticket.title, ticket.venue, ticket.date, ticket.time, ticket.bookingId, ticket.ticketUuid).joinToString("\n"))
                }
                runCatching { context.startActivity(android.content.Intent.createChooser(intent, "Share ticket")) }
                    .onFailure { android.widget.Toast.makeText(context, "No app is available to share this ticket.", android.widget.Toast.LENGTH_SHORT).show() }
            }
        )
    }
}

@Composable
private fun TicketContent(
    ticket: TicketDetails,
    selectedTicketIndex: Int,
    ticketCount: Int,
    onPreviousTicket: () -> Unit,
    onNextTicket: () -> Unit,
    onBackClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onShareClick: () -> Unit
) {
    BackHandler(onBack = onBackClick)
    val ticketTilt = rememberTicketTilt()
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
                    TicketBody(ticket, ticketTilt)
                    if (ticketCount > 1) {
                        Spacer(Modifier.height(16.dp))
                        TicketSelectionControls(selectedTicketIndex, ticketCount, onPreviousTicket, onNextTicket)
                    }
                    Spacer(Modifier.height(24.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        TicketSaveButton(onDownloadClick)
                        TicketShareButton(onShareClick)
                    }
                    Spacer(Modifier.navigationBarsPadding())
                }
            }
        }
    }
}

@Composable
private fun TicketSelectionControls(selectedIndex: Int, count: Int, onPrevious: () -> Unit, onNext: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color.White.copy(alpha = .08f))
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val colors = ButtonDefaults.textButtonColors(contentColor = TicketOrange, disabledContentColor = TicketWhite60.copy(alpha = .35f))
        TextButton(onClick = onPrevious, enabled = selectedIndex > 0, colors = colors) { Text("Previous") }
        Text("Ticket ${selectedIndex + 1} of $count", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        TextButton(onClick = onNext, enabled = selectedIndex < count - 1, colors = colors) { Text("Next") }
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
        val bottom = Offset(size.width * .50f, size.height)
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
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(12.dp))
            Text("BACK", color = TicketWhite60, fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = .6.sp)
        }
    }
}

@Composable
private fun TicketBody(ticket: TicketDetails, tilt: TicketTilt) {
    val shape = RoundedCornerShape(24.dp)
    val density = LocalDensity.current.density
    val isMovieTicket = ticket.category.equals("MOVIE", ignoreCase = true)
    val seatRows = if (isMovieTicket) {
        ((ticket.admission.split(',').filter(String::isNotBlank).size.coerceAtLeast(1) + 2) / 3)
    } else {
        1
    }
    val extraSeatHeight = ((seatRows - 1) * 20).coerceAtMost(80)
    val ticketHeight = if (isMovieTicket) 625 + extraSeatHeight else 600
    val primaryHeight = if (isMovieTicket) 360 + extraSeatHeight else 335
    val primaryWeight = primaryHeight.toFloat() / ticketHeight.toFloat()
    Box(
        Modifier.fillMaxWidth().height(ticketHeight.dp)
            .graphicsLayer {
                rotationX = tilt.x
                rotationY = tilt.y
                cameraDistance = 18f * density
                compositingStrategy = CompositingStrategy.Offscreen
                this.shape = shape
                clip = true
            }
            .drawWithContent {
                drawRoundRect(Color.White.copy(alpha = .045f), cornerRadius = CornerRadius(24.dp.toPx()))
                drawContent()
                drawRoundRect(
                    Color.White.copy(alpha = .25f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(24.dp.toPx()),
                    style = Stroke(1.dp.toPx())
                )
                val cutout = 24.dp.toPx()
                val split = size.height * primaryWeight
                drawCircle(Color.Transparent, cutout, androidx.compose.ui.geometry.Offset(0f, split), blendMode = BlendMode.Clear)
                drawCircle(Color.Transparent, cutout, androidx.compose.ui.geometry.Offset(size.width, split), blendMode = BlendMode.Clear)
                drawCircle(Color.Transparent, cutout, androidx.compose.ui.geometry.Offset(size.width / 2, 0f), blendMode = BlendMode.Clear)
                drawCircle(Color.Transparent, cutout, androidx.compose.ui.geometry.Offset(size.width / 2, size.height), blendMode = BlendMode.Clear)
            }
    ) {
        Column(Modifier.fillMaxSize()) {
            TicketPrimary(ticket, Modifier.weight(primaryWeight).fillMaxWidth())
            TicketPerforation()
            TicketQrPanel(ticket, Modifier.weight(1f - primaryWeight).fillMaxWidth())
        }
    }
}

private data class TicketTilt(val x: Float, val y: Float)

@Composable
private fun rememberTicketTilt(): TicketTilt {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    var targetX by remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    var targetY by remember { androidx.compose.runtime.mutableFloatStateOf(0f) }

    DisposableEffect(context, lifecycleOwner) {
        val manager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor = manager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)
            ?: manager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        var basePitch: Float? = null
        var baseRoll: Float? = null
        val rotationMatrix = FloatArray(9)
        val orientation = FloatArray(3)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                SensorManager.getOrientation(rotationMatrix, orientation)
                val pitch = orientation[1]
                val roll = orientation[2]
                val initialPitch = basePitch
                val initialRoll = baseRoll
                if (initialPitch == null || initialRoll == null) {
                    basePitch = pitch
                    baseRoll = roll
                    return
                }
                targetX = (-Math.toDegrees(angleDelta(pitch, initialPitch).toDouble()).toFloat() * .55f)
                    .coerceIn(-8f, 8f)
                targetY = (Math.toDegrees(angleDelta(roll, initialRoll).toDouble()).toFloat() * .55f)
                    .coerceIn(-8f, 8f)
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            when (event) {
                androidx.lifecycle.Lifecycle.Event.ON_START -> {
                    basePitch = null
                    baseRoll = null
                    if (sensor != null) manager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
                }
                androidx.lifecycle.Lifecycle.Event.ON_STOP -> {
                    manager.unregisterListener(listener)
                    targetX = 0f
                    targetY = 0f
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            manager.unregisterListener(listener)
            targetX = 0f
            targetY = 0f
        }
    }

    val smoothX by animateFloatAsState(targetX, spring(dampingRatio = .72f, stiffness = 180f), label = "ticketTiltX")
    val smoothY by animateFloatAsState(targetY, spring(dampingRatio = .72f, stiffness = 180f), label = "ticketTiltY")
    return TicketTilt(smoothX, smoothY)
}

private fun angleDelta(value: Float, baseline: Float): Float =
    atan2(sin(value - baseline), cos(value - baseline))

@Composable
private fun TicketPrimary(ticket: TicketDetails, modifier: Modifier) {
    Column(modifier.padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            TicketBrandLogo()
            TicketCategoryBadge(ticket.category)
        }
        Spacer(Modifier.height(16.dp))
        Text(
            ticket.title,
            color = Color.White,
            fontSize = 24.sp,
            lineHeight = 28.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = (-0.5).sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        if (ticket.venue.isNotBlank()) {
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, null, tint = TicketOrange, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Column(Modifier.weight(1f)) {
                    if (ticket.venue.isNotBlank()) {
                        Text(ticket.venue, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        TicketInformation(ticket)
    }
}

@Composable
private fun TicketCategoryBadge(category: String) {
    Box(
        Modifier.background(TicketOrange.copy(alpha = .15f), RoundedCornerShape(8.dp))
            .border(1.dp, TicketOrange.copy(alpha = .40f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        val label = when (category.uppercase()) {
            "MOVIE" -> "Movie"
            "EVENT" -> "Event"
            "TURF" -> "Turf"
            else -> category
        }
        Text(label, color = TicketOrange, fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
    }
}

@Composable
private fun TicketInformation(ticket: TicketDetails) {
    Column(Modifier.fillMaxWidth().ticketTopRule().padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(Modifier.fillMaxWidth()) {
            Box(Modifier.weight(1f)) { TicketInfoCell("DATE") { TicketValue(ticket.date) } }
            Box(Modifier.weight(1f)) { TicketInfoCell("TIME") { TicketValue(ticket.time, ellipsize = true, maxLines = 2) } }
        }
        Row(Modifier.fillMaxWidth()) {
            Box(Modifier.weight(1f)) { TicketInfoCell("ATTENDEE") { TicketValue(ticket.attendee, ellipsize = true) } }
            Box(Modifier.weight(1f)) {
                TicketInfoCell(if (ticket.category == "MOVIE") "SEATS" else "ACCESS") {
                    if (ticket.category == "MOVIE") TicketSeatValue(ticket.admission)
                    else TicketValue(ticket.admission, ellipsize = true, maxLines = 2)
                }
            }
        }
    }
}

@Composable
private fun TicketSeatValue(value: String) {
    val seats = value.split(',').map(String::trim).filter(String::isNotBlank)
    val formattedSeats = if (seats.size > 1) {
        seats.chunked(3).joinToString("\n") { row -> row.joinToString(", ") }
    } else {
        value.trim()
    }
    Text(
        text = formattedSeats,
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        fontSize = 14.sp,
        lineHeight = 19.sp,
        fontWeight = FontWeight.Bold,
        maxLines = 4,
        softWrap = true,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun TicketInfoCell(label: String, value: @Composable () -> Unit) {
    Column { TicketMicroText(label); Spacer(Modifier.height(6.dp)); value() }
}

@Composable
private fun TicketValue(value: String, ellipsize: Boolean = false, maxLines: Int = 1) {
    Text(
        value,
        color = Color.White,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        maxLines = maxLines,
        softWrap = maxLines > 1,
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
    var expanded by rememberSaveable(ticket.ticketUuid) { mutableStateOf(false) }
    Box(modifier = modifier.background(Color.Black.copy(alpha = .10f)).padding(horizontal = 24.dp, vertical = 14.dp), contentAlignment = Alignment.Center) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            TicketMicroText("BOOKING REFERENCE")
            Spacer(Modifier.height(4.dp))
            Text(ticket.bookingId, color = TicketOrange, fontFamily = TicketMono, fontWeight = FontWeight.Bold, fontSize = 14.sp, letterSpacing = 1.2.sp)
            Spacer(Modifier.height(10.dp))
            Box(
                Modifier.size(104.dp).clip(RoundedCornerShape(16.dp)).background(Color.White)
                    .clickable { expanded = true }.padding(12.dp)
            ) { TicketQr(ticket.qrPayload, Modifier.fillMaxSize()) }
            Spacer(Modifier.height(8.dp))
            TicketMicroText("TOTAL PAID")
            Spacer(Modifier.height(2.dp))
            Text(ticket.amount.ifBlank { "Amount unavailable" }, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black, letterSpacing = (-1).sp)
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
                TicketMicroText("BOOKING REFERENCE")
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
private fun TicketQr(payload: String, modifier: Modifier) {
    val matrix = remember(payload) {
        runCatching { QRCodeWriter().encode(
            payload,
            BarcodeFormat.QR_CODE,
            1,
            1,
            mapOf(EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M, EncodeHintType.MARGIN to 0)
        ) }.getOrNull()
    }
    if (matrix == null) {
        Box(modifier, contentAlignment = Alignment.Center) {
            Text("QR code unavailable", color = TicketBlue, fontSize = 12.sp)
        }
        return
    }
    Canvas(modifier) {
        val cell = min(size.width, size.height) / matrix.width
        for (y in 0 until matrix.height) for (x in 0 until matrix.width) {
            if (matrix[x, y]) drawRect(
                TicketBlue,
                androidx.compose.ui.geometry.Offset(x * cell, y * cell),
                Size(cell + .5f, cell + .5f)
            )
        }
    }
}

@Composable
private fun TicketSaveButton(onSavePdf: () -> Unit) {
    Row(
        Modifier.background(TicketOrange, RoundedCornerShape(12.dp)).clickable(onClick = onSavePdf)
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Download, null, tint = Color.White, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Text("SAVE PDF", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
    }
}

@Composable
private fun TicketShareButton(onShare: () -> Unit) {
    Box(
        Modifier.size(48.dp).clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = .10f))
            .border(1.dp, Color.White.copy(alpha = .20f), RoundedCornerShape(12.dp))
            .clickable(onClick = onShare),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Default.Share, "Share ticket", tint = Color.White, modifier = Modifier.size(20.dp))
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
        Offset.Zero,
        androidx.compose.ui.geometry.Offset(size.width, 0f),
        1.dp.toPx(),
        StrokeCap.Butt
    )
}
