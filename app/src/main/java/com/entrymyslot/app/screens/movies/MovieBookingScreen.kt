package com.entrymyslot.app.screens.movies

import androidx.activity.compose.BackHandler

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.entrymyslot.app.EntryMySlotApp
import com.entrymyslot.app.core.components.TermsAndPolicyBottomSheet
import com.entrymyslot.app.data.booking.MovieSeatRowDto
import com.entrymyslot.app.data.booking.ShowtimeDto
import com.entrymyslot.app.data.model.Cinema
import com.entrymyslot.app.screens.home.GlowBackground
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

private val MovieOrange = Color(0xFFFA580B)
private val MovieBackground = Color(0xFF061A38)
private val MovieBlue = Color(0xFF0A2D62)
private val MovieBlueRaised = Color(0xFF103C73)
private val MovieBlueEdge = Color(0xFF3976A8)
private val MovieWhite = Color(0xFFF8FAFF)
private val MovieSecondary = Color(0xFFA8B8CF)
private val MovieMuted = Color(0xFF7185A1)
private val MovieDivider = Color(0xFF24476F)
private val SeatAvailable = Color(0xFF164A79)
private val SeatAvailableEdge = Color(0xFF4384B5)
private val SeatSelected = MovieOrange
private val SeatBooked = Color(0xFF4A586B)
private val SeatBookedEdge = Color(0xFF657184)

private val SeatMapWidth = 360.dp
private val SeatMapHeight = 360.dp

@Composable
fun MovieBookingScreen(
    movieId: String,
    showtimeId: Int,
    onBackClick: () -> Unit = {},
    onContinueClick: () -> Unit = {}
) {
    val app = LocalContext.current.applicationContext as EntryMySlotApp
    val movieBookingViewModel: MovieBookingViewModel = viewModel(
        key = "movie_booking_${movieId}_$showtimeId",
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                MovieBookingViewModel(
                    bookingApi = app.appContainer.bookingApi,
                    detailsApi = app.appContainer.detailsApi,
                    networkMonitor = app.appContainer.networkMonitor,
                    pendingCheckoutStore = app.appContainer.pendingCheckoutStore
                ) as T
        }
    )
    BackHandler { movieBookingViewModel.releaseAndGoBack(onBackClick) }
    val state by movieBookingViewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(movieId, showtimeId) {
        movieBookingViewModel.loadSeatBooking(movieId, showtimeId)
    }

    var showTerms by remember { mutableStateOf(false) }
    val cinema = state.cinema?.let { Cinema(it.id.toString(), it.name, listOf(it.address, it.city).filter(String::isNotBlank).joinToString(", ")) }
    val currentTime = state.showtime?.displayTime().orEmpty()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        GlowBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            PremiumTopBar(onBackClick = { movieBookingViewModel.releaseAndGoBack(onBackClick) })
            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MovieOrange)
                }
                state.errorMessage != null && state.seatLayout == null -> BookingErrorState(
                    message = state.errorMessage.orEmpty(),
                    onRetry = movieBookingViewModel::retry
                )
                cinema != null && state.seatLayout != null -> {
                    CinemaMetadata(cinema = cinema)
                    ShowTimeSelector(
                        showTimes = listOf(currentTime),
                        selectedTime = currentTime,
                        onTimeSelected = {}
                    )
                    SeatCountSelector(
                        selectedCount = state.desiredSeatCount,
                        onCountSelected = movieBookingViewModel::setDesiredSeatCount
                    )
                    if (state.holdSecondsRemaining > 0) {
                        Text(
                            text = "Seats held for ${state.holdSecondsRemaining / 60}:${(state.holdSecondsRemaining % 60).toString().padStart(2, '0')}",
                            color = MovieOrange,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 2.dp)
                        )
                    }
                    state.errorMessage?.let { message ->
                        Text(message, color = Color(0xFFFFC4B0), fontSize = 12.sp, modifier = Modifier.padding(horizontal = 20.dp))
                    }
                    SeatLegend(modifier = Modifier.padding(top = 8.dp, bottom = 6.dp))
                    SeatMapViewport(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        rows = state.seatLayout!!.rows,
                        selectedSeatIds = state.selectedSeatIds,
                        isHolding = state.isHolding,
                        onSeatClick = movieBookingViewModel::onSeatClicked
                    )
                    MovieBottomBar(
                        count = state.selectedSeatIds.size,
                        total = state.totalPaise / 100,
                        onContinueClick = {
                            if (movieBookingViewModel.validateSelection()) showTerms = true
                        }
                    )
                }
            }
        }

        if (showTerms) {
            TermsAndPolicyBottomSheet(
                category = "MOVIE",
                onDismiss = { showTerms = false },
                onAccept = {
                    showTerms = false
                    movieBookingViewModel.createHoldAndPrepareCheckout(onContinueClick)
                }
            )
        }
    }
}

@Composable
private fun BookingErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(message, color = MovieSecondary)
        Spacer(Modifier.height(12.dp))
        Button(onClick = onRetry) { Text("Retry") }
    }
}

@Composable
private fun PremiumTopBar(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
            contentDescription = "Back",
            tint = MovieWhite,
            modifier = Modifier.size(40.dp).padding(9.dp).clickable(onClick = onBackClick)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "Select Seats",
            color = MovieWhite,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.1.sp
        )
    }
}

@Composable
private fun CinemaMetadata(cinema: Cinema) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
    ) {
        Text(
            text = cinema.name,
            color = MovieWhite,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = cinema.location,
            color = MovieSecondary,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ShowTimeSelector(
    showTimes: List<String>,
    selectedTime: String,
    onTimeSelected: (String) -> Unit
) {
    Column(modifier = Modifier.padding(top = 12.dp)) {
        SectionLabel(text = "Show times")
        Spacer(modifier = Modifier.height(9.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(items = showTimes, key = { it }) { time ->
                val isSelected = time == selectedTime
                val backgroundColor by animateColorAsState(
                    targetValue = if (isSelected) MovieOrange else MovieBlue.copy(alpha = 0.9f),
                    animationSpec = tween(170),
                    label = "showTimeColor"
                )
                val elevation by animateDpAsState(
                    targetValue = if (isSelected) 8.dp else 0.dp,
                    animationSpec = tween(170),
                    label = "showTimeElevation"
                )

                Box(
                    modifier = Modifier
                        .widthIn(min = 92.dp)
                        .height(44.dp)
                        .shadow(
                            elevation = elevation,
                            shape = RoundedCornerShape(13.dp),
                            ambientColor = MovieOrange.copy(alpha = 0.28f),
                            spotColor = MovieOrange.copy(alpha = 0.28f)
                        )
                        .clip(RoundedCornerShape(13.dp))
                        .background(backgroundColor)
                        .border(
                            1.dp,
                            if (isSelected) Color.White.copy(alpha = 0.12f)
                            else MovieBlueEdge.copy(alpha = 0.34f),
                            RoundedCornerShape(13.dp)
                        )
                        .semantics {
                            contentDescription = "$time show time"
                            selected = isSelected
                            role = Role.RadioButton
                        }
                        .selectable(
                            selected = isSelected,
                            role = Role.RadioButton,
                            onClick = { onTimeSelected(time) }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = time,
                        color = MovieWhite,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun SeatCountSelector(
    selectedCount: Int,
    onCountSelected: (Int) -> Unit
) {
    Column(modifier = Modifier.padding(top = 15.dp)) {
        SectionLabel(text = "How many seats?")
        Spacer(modifier = Modifier.height(9.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(count = 8, key = { it }) { index ->
                val count = index + 1
                val isSelected = count == selectedCount
                val backgroundColor by animateColorAsState(
                    targetValue = if (isSelected) MovieOrange else MovieBlue,
                    animationSpec = tween(160),
                    label = "seatCountColor"
                )
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1f else 0.96f,
                    animationSpec = tween(160),
                    label = "seatCountScale"
                )

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .graphicsLayer { scaleX = scale; scaleY = scale }
                        .clip(RoundedCornerShape(12.dp))
                        .background(backgroundColor)
                        .border(
                            1.dp,
                            if (isSelected) Color.White.copy(alpha = 0.12f)
                            else MovieBlueEdge.copy(alpha = 0.3f),
                            RoundedCornerShape(12.dp)
                        )
                        .semantics {
                            contentDescription = "$count ${if (count == 1) "seat" else "seats"}"
                            selected = isSelected
                            role = Role.RadioButton
                        }
                        .selectable(
                            selected = isSelected,
                            role = Role.RadioButton,
                            onClick = { onCountSelected(count) }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = count.toString(),
                        color = MovieWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(horizontal = 20.dp),
        color = MovieWhite,
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.1.sp
    )
}

@Composable
private fun SeatLegend(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        LegendItem(SeatVisualState.Available, "Available")
        Spacer(modifier = Modifier.width(18.dp))
        LegendItem(SeatVisualState.Selected, "Selected")
        Spacer(modifier = Modifier.width(18.dp))
        LegendItem(SeatVisualState.Booked, "Booked")
    }
}

@Composable
private fun LegendItem(state: SeatVisualState, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        SeatSilhouette(
            state = state,
            modifier = Modifier.size(width = 22.dp, height = 19.dp),
            showStateIcon = false
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text, color = MovieSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun SeatMapViewport(
    rows: List<MovieSeatRowDto>,
    selectedSeatIds: Set<Int>,
    isHolding: Boolean,
    onSeatClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val contentWidthPx = with(density) { SeatMapWidth.toPx() }
    val contentHeightPx = with(density) { SeatMapHeight.toPx() }
    var viewportSize by remember { mutableStateOf(IntSize.Zero) }
    var zoom by remember { mutableFloatStateOf(1f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }
    var transformJob by remember { mutableStateOf<Job?>(null) }

    val fitScale = if (viewportSize == IntSize.Zero) 1f else min(
        viewportSize.width / contentWidthPx,
        viewportSize.height / contentHeightPx
    ).coerceAtMost(1f)

    fun clampOffset(candidate: Offset, targetZoom: Float): Offset {
        if (viewportSize == IntSize.Zero) return Offset.Zero
        val totalScale = fitScale * targetZoom
        val maxX = max((contentWidthPx * totalScale - viewportSize.width) / 2f, 0f)
        val maxY = max((contentHeightPx * totalScale - viewportSize.height) / 2f, 0f)
        return Offset(
            candidate.x.coerceIn(-maxX, maxX),
            candidate.y.coerceIn(-maxY, maxY)
        )
    }

    fun animateTransform(targetZoom: Float, focalPoint: Offset?) {
        transformJob?.cancel()
        transformJob = scope.launch {
            val startZoom = zoom
            val startOffset = panOffset
            val viewportCenter = Offset(viewportSize.width / 2f, viewportSize.height / 2f)
            val requestedOffset = if (focalPoint == null || targetZoom == 1f) {
                Offset.Zero
            } else {
                startOffset + (focalPoint - viewportCenter - startOffset) *
                    (1f - targetZoom / startZoom)
            }
            val targetOffset = clampOffset(requestedOffset, targetZoom)

            animate(0f, 1f, animationSpec = tween(220)) { progress, _ ->
                zoom = startZoom + (targetZoom - startZoom) * progress
                panOffset = Offset(
                    startOffset.x + (targetOffset.x - startOffset.x) * progress,
                    startOffset.y + (targetOffset.y - startOffset.y) * progress
                )
            }
        }
    }

    Box(
        modifier = modifier
            .padding(horizontal = 10.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(MovieBlue.copy(alpha = 0.2f))
            .border(1.dp, MovieDivider.copy(alpha = 0.38f), RoundedCornerShape(22.dp))
            .onSizeChanged { size ->
                viewportSize = size
                panOffset = clampOffset(panOffset, zoom)
            }
            .pointerInput(viewportSize, fitScale) {
                detectTransformGestures(panZoomLock = true) { centroid, pan, zoomChange, _ ->
                    transformJob?.cancel()
                    val oldZoom = zoom
                    val newZoom = (zoom * zoomChange).coerceIn(1f, 2.8f)
                    val viewportCenter = Offset(viewportSize.width / 2f, viewportSize.height / 2f)
                    val focalAdjustment =
                        (centroid - viewportCenter - panOffset) * (1f - newZoom / oldZoom)
                    zoom = newZoom
                    panOffset = clampOffset(panOffset + pan + focalAdjustment, newZoom)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .requiredSize(SeatMapWidth, SeatMapHeight)
                .graphicsLayer {
                    val totalScale = fitScale * zoom
                    scaleX = totalScale
                    scaleY = totalScale
                    translationX = panOffset.x
                    translationY = panOffset.y
                    transformOrigin = TransformOrigin.Center
                }
        ) {
            SeatMapContent(rows, selectedSeatIds, isHolding, onSeatClick)
        }

        AnimatedVisibility(
            visible = zoom > 1.05f,
            modifier = Modifier.align(Alignment.TopEnd).padding(10.dp),
            enter = fadeIn(tween(140)) + scaleIn(tween(140), initialScale = 0.9f),
            exit = fadeOut(tween(120)) + scaleOut(tween(120), targetScale = 0.9f)
        ) {
            Surface(
                onClick = { animateTransform(1f, null) },
                shape = CircleShape,
                color = MovieBlueRaised.copy(alpha = 0.96f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    Color.White.copy(alpha = 0.12f)
                ),
                shadowElevation = 5.dp
            ) {
                Box(modifier = Modifier.size(34.dp), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = "Fit seating map to screen",
                        tint = MovieWhite,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SeatMapContent(
    rows: List<MovieSeatRowDto>,
    selectedSeatIds: Set<Int>,
    isHolding: Boolean,
    onSeatClick: (Int) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CinemaScreen()
        Spacer(modifier = Modifier.height(18.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            rows.forEach { row ->
                Row(modifier = Modifier.height(32.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = row.rowLabel,
                        color = MovieMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.width(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    SeatGroup(row, selectedSeatIds, isHolding, onSeatClick)
                }
            }
        }
    }
}

@Composable
private fun CinemaScreen() {
    Column(
        modifier = Modifier.fillMaxWidth().height(54.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Canvas(modifier = Modifier.fillMaxWidth(0.78f).height(25.dp)) {
            val screenPath = Path().apply {
                moveTo(size.width * 0.07f, size.height * 0.72f)
                quadraticBezierTo(
                    size.width * 0.5f,
                    size.height * 0.08f,
                    size.width * 0.93f,
                    size.height * 0.72f
                )
            }
            drawPath(
                screenPath,
                Color(0xFF8CC8FF).copy(alpha = 0.09f),
                style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
            )
            drawPath(
                path = screenPath,
                brush = Brush.horizontalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.48f),
                        Color.White.copy(alpha = 0.94f),
                        Color.White.copy(alpha = 0.48f)
                    )
                ),
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = "S C R E E N",
            color = MovieSecondary,
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 1.5.sp
        )
    }
}

@Composable
private fun SeatGroup(
    row: MovieSeatRowDto,
    selectedSeatIds: Set<Int>,
    isHolding: Boolean,
    onSeatClick: (Int) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        row.seats.sortedBy { it.seatNumber }.forEachIndexed { index, seat ->
            val previous = row.seats.sortedBy { it.seatNumber }.getOrNull(index - 1)
            if (previous != null) {
                val coordinateGap = if (seat.xPosition != null && previous.xPosition != null) {
                    seat.xPosition - previous.xPosition
                } else 0.0
                if (seat.seatNumber - previous.seatNumber > 1 || coordinateGap > 1.5) {
                    Spacer(Modifier.width(12.dp))
                }
            }
            val isSelected = seat.seatId in selectedSeatIds
            val isUnavailable = seat.status != "available" && !isSelected
            SeatItem("${row.rowLabel}${seat.seatNumber}", isSelected, isUnavailable) {
                if (!isHolding) onSeatClick(seat.seatId)
            }
        }
    }
}

@Composable
private fun SeatItem(
    seatId: String,
    isSelected: Boolean,
    isBooked: Boolean,
    onClick: () -> Unit
) {
    val state = when {
        isBooked -> SeatVisualState.Booked
        isSelected -> SeatVisualState.Selected
        else -> SeatVisualState.Available
    }
    val scale by animateFloatAsState(
        if (isSelected) 1f else 0.94f,
        tween(170),
        label = "seatScale"
    )

    Box(
        modifier = Modifier
            .size(width = 32.dp, height = 29.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .semantics {
                contentDescription = "Seat $seatId"
                stateDescription = when (state) {
                    SeatVisualState.Available -> "Available"
                    SeatVisualState.Selected -> "Selected"
                    SeatVisualState.Booked -> "Booked"
                }
                selected = isSelected
                role = Role.Button
                if (isBooked) disabled()
            }
            .selectable(
                selected = isSelected,
                enabled = !isBooked,
                role = Role.Button,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        SeatSilhouette(state, Modifier.fillMaxSize(), showStateIcon = true)
    }
}

@Composable
private fun SeatSilhouette(
    state: SeatVisualState,
    modifier: Modifier = Modifier,
    showStateIcon: Boolean
) {
    val targetFill = when (state) {
        SeatVisualState.Available -> SeatAvailable
        SeatVisualState.Selected -> SeatSelected
        SeatVisualState.Booked -> SeatBooked
    }
    val targetEdge = when (state) {
        SeatVisualState.Available -> SeatAvailableEdge
        SeatVisualState.Selected -> MovieOrange
        SeatVisualState.Booked -> SeatBookedEdge
    }
    val fill by animateColorAsState(targetFill, tween(170), label = "seatFill")
    val edge by animateColorAsState(targetEdge, tween(170), label = "seatEdge")

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            if (state == SeatVisualState.Selected) {
                drawRoundRect(
                    MovieOrange.copy(alpha = 0.16f),
                    Offset(w * 0.02f, h * 0.02f),
                    Size(w * 0.96f, h * 0.96f),
                    CornerRadius(w * 0.24f)
                )
            }
            drawRoundRect(
                fill,
                Offset(w * 0.17f, h * 0.04f),
                Size(w * 0.66f, h * 0.61f),
                CornerRadius(w * 0.18f)
            )
            drawRoundRect(
                edge.copy(alpha = 0.72f),
                Offset(w * 0.17f, h * 0.04f),
                Size(w * 0.66f, h * 0.61f),
                CornerRadius(w * 0.18f),
                style = Stroke(1.dp.toPx())
            )
            drawRoundRect(
                fill,
                Offset(w * 0.12f, h * 0.5f),
                Size(w * 0.76f, h * 0.34f),
                CornerRadius(w * 0.12f)
            )
            drawRoundRect(
                edge.copy(alpha = 0.78f),
                Offset(w * 0.12f, h * 0.5f),
                Size(w * 0.76f, h * 0.34f),
                CornerRadius(w * 0.12f),
                style = Stroke(1.dp.toPx())
            )
            drawRoundRect(
                edge.copy(alpha = 0.88f),
                Offset(w * 0.02f, h * 0.44f),
                Size(w * 0.14f, h * 0.41f),
                CornerRadius(w * 0.07f)
            )
            drawRoundRect(
                edge.copy(alpha = 0.88f),
                Offset(w * 0.84f, h * 0.44f),
                Size(w * 0.14f, h * 0.41f),
                CornerRadius(w * 0.07f)
            )
            drawRoundRect(
                edge.copy(alpha = 0.72f),
                Offset(w * 0.2f, h * 0.82f),
                Size(w * 0.09f, h * 0.14f),
                CornerRadius(w * 0.04f)
            )
            drawRoundRect(
                edge.copy(alpha = 0.72f),
                Offset(w * 0.71f, h * 0.82f),
                Size(w * 0.09f, h * 0.14f),
                CornerRadius(w * 0.04f)
            )
        }

        if (showStateIcon) {
            AnimatedVisibility(
                visible = state == SeatVisualState.Selected,
                enter = fadeIn(tween(120)) + scaleIn(tween(150), initialScale = 0.65f),
                exit = fadeOut(tween(90)) + scaleOut(tween(110), targetScale = 0.7f)
            ) {
                Icon(
                    Icons.Rounded.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(13.dp).offset(y = (-2).dp)
                )
            }
            if (state == SeatVisualState.Booked) {
                Icon(
                    Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.64f),
                    modifier = Modifier.size(9.dp).offset(y = (-2).dp)
                )
            }
        }
    }
}

@Composable
private fun MovieBottomBar(count: Int, total: Int, onContinueClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF071A35).copy(alpha = 0.98f))
            .drawBehind {
                drawLine(
                    MovieDivider.copy(alpha = 0.72f),
                    Offset.Zero,
                    Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx()
                )
            }
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                if (count == 0) "Select seats"
                else "$count ${if (count == 1) "seat" else "seats"} selected",
                color = MovieSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text("₹$total", color = MovieWhite, fontSize = 21.sp, fontWeight = FontWeight.Bold)
        }
        Button(
            onClick = onContinueClick,
            enabled = count > 0,
            modifier = Modifier.width(132.dp).height(48.dp),
            shape = RoundedCornerShape(15.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MovieOrange,
                contentColor = MovieWhite,
                disabledContainerColor = MovieBlueRaised.copy(alpha = 0.72f),
                disabledContentColor = MovieSecondary.copy(alpha = 0.7f)
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 5.dp,
                pressedElevation = 1.dp,
                disabledElevation = 0.dp
            )
        ) {
            Text("Continue", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

private enum class SeatVisualState {
    Available,
    Selected,
    Booked
}

private fun ShowtimeDto.displayTime(): String = runCatching {
    DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)
        .format(Instant.parse(showDatetime).atZone(ZoneId.systemDefault()))
}.getOrDefault(showDatetime)
