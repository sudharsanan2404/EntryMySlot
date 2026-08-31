package com.entrymyslot.app.screens.movies

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Theaters
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.entrymyslot.app.screens.home.GlowBackground
import com.entrymyslot.app.data.FakeData
import com.entrymyslot.app.data.model.Cinema
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private val MovieOrange = Color(0xFFFA580B)
private val MovieBackground = Color(0xFF061A38)
private val MovieBlue = Color(0xFF0A2D62)
private val MovieBlueRaised = Color(0xFF0B274F)
private val MovieBlueEdge = Color(0xFF3976A8)
private val MovieWhite = Color(0xFFF8FAFF)
private val MovieSecondary = Color(0xFFA8B8CF)
private val MovieMuted = Color(0xFF7185A1)
private val MovieDivider = Color(0xFF24476F)

@Composable
fun CinemaSelectionScreen(
    movieId: String,
    onBackClick: () -> Unit,
    onTimeSelected: (Cinema, String, Calendar) -> Unit
) {
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
    var searchQuery by remember { mutableStateOf("") }
    val filteredCinemas = remember(searchQuery) {
        FakeData.cinemas.filter { cinema ->
            cinema.name.contains(searchQuery.trim(), ignoreCase = true)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        GlowBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            CinemaSelectionTopBar(onBackClick = onBackClick)

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 28.dp)
            ) {
                item(key = "date_selector") {
                    Column(modifier = Modifier.padding(top = 10.dp, bottom = 20.dp)) {
                        SectionHeader(
                            title = "Select Date",
                            icon = {
                                Icon(
                                    imageVector = Icons.Rounded.CalendarMonth,
                                    contentDescription = null,
                                    tint = MovieOrange,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        DateSelector(
                            selectedDate = selectedDate,
                            onDateSelected = { selectedDate = it }
                        )
                    }
                }

                item(key = "cinema_header") {
                    Text(
                        text = "Available Cinemas",
                        modifier = Modifier
                            .padding(horizontal = 20.dp, vertical = 2.dp)
                            .semantics { heading() },
                        color = MovieWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                item(key = "cinema_search") {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp),
                        placeholder = { Text("Search cinema...", color = MovieMuted) },
                        leadingIcon = {
                            Icon(Icons.Outlined.Search, contentDescription = null, tint = MovieSecondary)
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MovieWhite,
                            unfocusedTextColor = MovieWhite,
                            cursorColor = MovieOrange,
                            focusedBorderColor = MovieOrange,
                            unfocusedBorderColor = MovieBlueEdge.copy(alpha = 0.55f),
                            focusedContainerColor = MovieBlueRaised,
                            unfocusedContainerColor = MovieBlueRaised
                        )
                    )
                    Spacer(Modifier.height(10.dp))
                }

                items(
                    items = filteredCinemas,
                    key = { cinema -> cinema.id }
                ) { cinema ->
                    CinemaCard(
                        cinema = cinema,
                        showTimes = FakeData.getCinemaShowTimes(cinema.id, movieId),
                        onTimeClick = { time ->
                            onTimeSelected(cinema, time, selectedDate)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CinemaSelectionTopBar(onBackClick: () -> Unit) {
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
            text = "Select Cinema & Time",
            color = MovieWhite,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.1.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
    icon: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .semantics { heading() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MovieBlue.copy(alpha = 0.86f))
                .border(
                    width = 1.dp,
                    color = MovieBlueEdge.copy(alpha = 0.28f),
                    shape = RoundedCornerShape(10.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title,
            color = MovieWhite,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.1.sp
        )
    }
}

@Composable
private fun DateSelector(
    selectedDate: Calendar,
    onDateSelected: (Calendar) -> Unit
) {
    val locale = LocalConfiguration.current.locales[0]

    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(count = 7, key = { index -> index }) { index ->
            val date = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, index)
            }
            val isSelected =
                date.get(Calendar.DAY_OF_YEAR) == selectedDate.get(Calendar.DAY_OF_YEAR)
            val day = SimpleDateFormat("EEE", locale).format(date.time)
            val number = SimpleDateFormat("dd", locale).format(date.time)
            val accessibilityDate = SimpleDateFormat("EEEE, MMMM d", locale).format(date.time)

            DateCard(
                day = day,
                number = number,
                accessibilityDate = accessibilityDate,
                isSelected = isSelected,
                onClick = { onDateSelected(date) }
            )
        }
    }
}

@Composable
private fun DateCard(
    day: String,
    number: String,
    accessibilityDate: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val shape = RoundedCornerShape(16.dp)
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) MovieOrange else MovieBlue.copy(alpha = 0.92f),
        animationSpec = tween(170),
        label = "dateBackground"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) {
            Color.White.copy(alpha = 0.14f)
        } else {
            MovieBlueEdge.copy(alpha = 0.34f)
        },
        animationSpec = tween(170),
        label = "dateBorder"
    )
    val elevation by animateDpAsState(
        targetValue = if (isSelected && !isPressed) 8.dp else 0.dp,
        animationSpec = tween(150),
        label = "dateElevation"
    )
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else if (isSelected) 1f else 0.98f,
        animationSpec = tween(120),
        label = "dateScale"
    )

    Column(
        modifier = Modifier
            .width(58.dp)
            .height(70.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = MovieOrange.copy(alpha = 0.24f),
                spotColor = MovieOrange.copy(alpha = 0.24f)
            )
            .clip(shape)
            .background(backgroundColor)
            .border(1.dp, borderColor, shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.RadioButton,
                onClick = onClick
            )
            .semantics {
                contentDescription = accessibilityDate
                selected = isSelected
                role = Role.RadioButton
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val locale = LocalConfiguration.current.locales[0]
        Text(
            text = day.uppercase(locale),
            color = if (isSelected) MovieWhite.copy(alpha = 0.86f) else MovieSecondary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.4.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = number,
            color = MovieWhite,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun CinemaCard(
    cinema: Cinema,
    showTimes: List<String>,
    onTimeClick: (String) -> Unit
) {
    val shape = RoundedCornerShape(20.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 7.dp)
            .shadow(
                elevation = 6.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.18f),
                spotColor = Color.Black.copy(alpha = 0.22f)
            )
            .clip(shape)
            .background(MovieBlueRaised.copy(alpha = 0.97f))
            .border(
                width = 1.dp,
                color = MovieBlueEdge.copy(alpha = 0.25f),
                shape = shape
            )
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(MovieBlue.copy(alpha = 0.96f))
                        .border(
                            1.dp,
                            MovieBlueEdge.copy(alpha = 0.3f),
                            RoundedCornerShape(13.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Theaters,
                        contentDescription = null,
                        tint = MovieOrange,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = cinema.name,
                        color = MovieWhite,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = null,
                            tint = MovieMuted,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = cinema.location,
                            color = MovieSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(15.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MovieDivider.copy(alpha = 0.55f))
            )
            Spacer(modifier = Modifier.height(13.dp))

            Text(
                text = "SHOWTIMES",
                color = MovieMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(9.dp))

            CinemaShowTimes(
                cinema = cinema,
                showTimes = showTimes,
                onTimeClick = onTimeClick
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CinemaShowTimes(
    cinema: Cinema,
    showTimes: List<String>,
    onTimeClick: (String) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        showTimes.forEach { time ->
            ShowtimeChip(
                cinemaName = cinema.name,
                time = time,
                onClick = { onTimeClick(time) }
            )
        }
    }
}

@Composable
private fun ShowtimeChip(
    cinemaName: String,
    time: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val shape = RoundedCornerShape(12.dp)
    val backgroundColor by animateColorAsState(
        targetValue = if (isPressed) MovieOrange else MovieBlue.copy(alpha = 0.92f),
        animationSpec = tween(110),
        label = "showtimeBackground"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isPressed) {
            Color.White.copy(alpha = 0.14f)
        } else {
            MovieBlueEdge.copy(alpha = 0.34f)
        },
        animationSpec = tween(110),
        label = "showtimeBorder"
    )
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = tween(100),
        label = "showtimeScale"
    )

    Box(
        modifier = Modifier
            .widthIn(min = 84.dp)
            .height(40.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .background(backgroundColor)
            .border(1.dp, borderColor, shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick
            )
            .semantics {
                contentDescription = "$time show time at $cinemaName"
                role = Role.Button
            }
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = time,
            color = MovieWhite,
            fontSize = 12.sp,
            fontWeight = if (isPressed) FontWeight.Bold else FontWeight.Medium
        )
    }
}
