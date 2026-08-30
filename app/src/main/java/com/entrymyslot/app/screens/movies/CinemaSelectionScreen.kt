package com.entrymyslot.app.screens.movies

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.entrymyslot.app.data.model.CinemaDto
import com.entrymyslot.app.data.model.ShowtimeDto
import java.text.SimpleDateFormat
import java.util.*

private val MovieBlueTop = Color(0xFF063DB5)
private val MovieBlueBottom = Color(0xFF041F5D)
private val MovieOrange = Color(0xFFFF8A00)
private val MovieWhite = Color.White
private val MovieGray = Color(0xFFB8C0D0)
private val MovieCardLight = Color(0xFF142B58)

@Composable
fun CinemaSelectionScreen(
    movieId: String,
    onCinemaClick: (String) -> Unit,
    onBackClick: () -> Unit
) {
    val viewModel = remember { CinemaSelectionViewModel() }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }

    val movieIdLong = movieId.toLongOrNull()

    if (uiState.cinemas.isEmpty() && !uiState.isLoading && uiState.error == null && movieIdLong != null) {
        viewModel.loadCinemasAndShowtimes(movieIdLong)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(MovieBlueTop, MovieBlueBottom)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
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
                    tint = MovieWhite,
                    modifier = Modifier.size(28.dp).clickable { onBackClick() }
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = "Select Cinema & Time", color = MovieWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            if (uiState.error != null) {
                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(text = uiState.error!!, color = Color(0xFFFF5252), fontSize = 13.sp)
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                item {
                    Column(modifier = Modifier.padding(vertical = 16.dp)) {
                        Row(
                            modifier = Modifier.padding(horizontal = 18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Outlined.CalendarMonth, null, tint = MovieOrange, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Select Date", color = MovieWhite, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        DateSelector(selectedDate) { selectedDate = it }
                    }
                }

                if (uiState.isLoading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                            androidx.compose.material3.CircularProgressIndicator(color = MovieOrange)
                        }
                    }
                } else {
                    val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate.time)

                    items(uiState.cinemas) { cinema ->
                        val cinemaShowtimes = uiState.showtimesByCinema[cinema.id]
                            ?.filter { it.showDate == dateStr } ?: emptyList()
                        CinemaRow(
                            cinema = cinema,
                            showtimes = cinemaShowtimes,
                            onTimeClick = { showtimeId ->
                                onCinemaClick(showtimeId.toString())
                            }
                        )
                    }

                    if (uiState.cinemas.isEmpty() && !uiState.isLoading) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                                Text("No cinemas found", color = MovieGray, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DateSelector(selectedDate: Calendar, onDateSelected: (Calendar) -> Unit) {
    val locale = Locale.getDefault()
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(7) { i ->
            val date = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, i) }
            val isSelected = date.get(Calendar.DAY_OF_YEAR) == selectedDate.get(Calendar.DAY_OF_YEAR)

            val day = SimpleDateFormat("EEE", locale).format(date.time)
            val num = SimpleDateFormat("dd", locale).format(date.time)

            Column(
                modifier = Modifier
                    .width(50.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) MovieOrange else MovieCardLight)
                    .clickable { onDateSelected(date) }
                    .padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(day, color = if (isSelected) MovieWhite else MovieGray, fontSize = 11.sp)
                Text(num, color = MovieWhite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun CinemaRow(
    cinema: CinemaDto,
    showtimes: List<ShowtimeDto>,
    onTimeClick: (Long) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF101A2C))
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(cinema.name, color = MovieWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        val location = listOfNotNull(cinema.address, cinema.city).filter { it.isNotBlank() }.joinToString(", ")
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.LocationOn, null, tint = MovieGray, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(location.ifBlank { "Location TBA" }, color = MovieGray, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            showtimes.forEach { st ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, MovieOrange.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .clickable { onTimeClick(st.id) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(st.showTime, color = MovieOrange, fontSize = 13.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement
    ) {
        content()
    }
}
