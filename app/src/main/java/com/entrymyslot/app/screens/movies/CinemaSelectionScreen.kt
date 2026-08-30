package com.entrymyslot.app.screens.movies

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Theaters
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*
import com.entrymyslot.app.screens.home.GlowBackground
import com.entrymyslot.app.core.components.ElevatedCardSubtitle
import com.entrymyslot.app.core.components.ElevatedCardTitle
import com.entrymyslot.app.core.components.ElevatedContrastCard
import com.entrymyslot.app.core.components.EntryCardAccent

private val MovieBlueTop = Color(0xFF0B3A82)
private val MovieBlueBottom = Color(0xFF061A33)
private val MovieOrange = EntryCardAccent
private val MovieWhite = Color.White
private val MovieGray = Color(0xFF98A2B3)
private val MovieCardLight = Color(0xFF0E0B38).copy(alpha = .68f)

data class Cinema(
    val id: String,
    val name: String,
    val location: String,
    val showTimes: List<String>
)

val sampleCinemas = listOf(
    Cinema("c1", "PVR Cinemas", "Phoenix Marketcity, Chennai", listOf("10:30 AM", "01:45 PM", "04:30 PM", "07:15 PM", "10:30 PM")),
    Cinema("c2", "INOX", "VR Mall, Chennai", listOf("11:00 AM", "02:30 PM", "05:00 PM", "08:15 PM", "11:00 PM")),
    Cinema("c3", "AGS Cinemas", "T. Nagar, Chennai", listOf("10:00 AM", "01:00 PM", "04:00 PM", "07:00 PM", "10:00 PM")),
    Cinema("c4", "Sathyam Cinemas", "Royapettah, Chennai", listOf("10:45 AM", "02:00 PM", "05:15 PM", "08:30 PM", "11:15 PM"))
)

@Composable
fun CinemaSelectionScreen(
    onBackClick: () -> Unit,
    onTimeSelected: (Cinema, String, Calendar) -> Unit
) {
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        GlowBackground()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = MovieWhite,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { onBackClick() }
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Select Cinema & Time",
                    color = MovieWhite,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // Date Selector
                item {
                    Column(modifier = Modifier.padding(vertical = 16.dp)) {
                        Row(
                            modifier = Modifier.padding(horizontal = 18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.CalendarMonth, null, tint = MovieOrange, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Select Date", color = MovieWhite, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        DateSelector(selectedDate) { selectedDate = it }
                    }
                }

                // Cinema List
                items(sampleCinemas) { cinema ->
                    CinemaRow(cinema) { time ->
                        onTimeSelected(cinema, time, selectedDate)
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
private fun CinemaRow(cinema: Cinema, onTimeClick: (String) -> Unit) {
    ElevatedContrastCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column(Modifier.fillMaxWidth()) {
            ElevatedCardTitle(cinema.name)
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.LocationOn, null, tint = MovieGray, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                ElevatedCardSubtitle(cinema.location, Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                cinema.showTimes.forEach { time ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, MovieOrange, RoundedCornerShape(8.dp))
                            .clickable { onTimeClick(time) }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(time, color = MovieOrange, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
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
