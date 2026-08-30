package com.entrymyslot.app.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.entrymyslot.app.screens.home.PopularEvent

enum class SearchResultType { MOVIE, SPORT, EVENT }

data class SearchResult(val item: PopularEvent, val type: SearchResultType)

@Composable
fun SearchScreen(
    movies: List<PopularEvent>,
    sports: List<PopularEvent>,
    events: List<PopularEvent>,
    onBackClick: () -> Unit,
    onResultClick: (SearchResult) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val allResults = remember(movies, sports, events) {
        movies.map { SearchResult(it, SearchResultType.MOVIE) } +
            sports.map { SearchResult(it, SearchResultType.SPORT) } +
            events.map { SearchResult(it, SearchResultType.EVENT) }
    }
    val results = remember(query, allResults) {
        val term = query.trim()
        if (term.isEmpty()) allResults.take(8) else allResults.filter { result ->
            result.item.title.contains(term, true) ||
                result.item.location.contains(term, true) ||
                result.type.name.contains(term, true)
        }
    }

    Box(
        Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF0126A5), Color(0xFF061A3D))))
            .statusBarsPadding().navigationBarsPadding().imePadding()
    ) {
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier.fillMaxWidth().padding(start = 6.dp, top = 8.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back", tint = Color.White)
                }
                Text("Search", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
            }

            TextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)
                    .height(54.dp).focusRequester(focusRequester),
                placeholder = { Text("Movies, events, sports or venues") },
                leadingIcon = { Icon(Icons.Outlined.Search, null) },
                trailingIcon = {
                    if (query.isNotEmpty()) IconButton(onClick = { query = "" }) {
                        Icon(Icons.Outlined.Clear, "Clear search")
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White.copy(alpha = .96f),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedLeadingIconColor = Color(0xFFFA580B),
                    cursorColor = Color(0xFF0126A5)
                ),
                keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
            )

            Text(
                if (query.isBlank()) "Popular near you" else "${results.size} results",
                color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
            )

            if (results.isEmpty()) EmptySearch(query) else LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(results, key = { "${it.type}-${it.item.id}" }) { result ->
                    SearchResultCard(result) { onResultClick(result) }
                }
            }
        }
    }
}

@Composable
private fun SearchResultCard(result: SearchResult, onClick: () -> Unit) {
    val (icon, label) = when (result.type) {
        SearchResultType.MOVIE -> Icons.Outlined.Movie to "Movie"
        SearchResultType.SPORT -> Icons.Outlined.SportsSoccer to "Sports"
        SearchResultType.EVENT -> Icons.Outlined.Event to "Event"
    }
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        color = Color(0xFF111D32), shape = RoundedCornerShape(16.dp), shadowElevation = 2.dp
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(48.dp).background(Color(0xFFFA580B).copy(alpha = .14f), CircleShape),
                contentAlignment = Alignment.Center
            ) { Icon(icon, null, tint = Color(0xFFFA580B), modifier = Modifier.size(24.dp)) }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(result.item.title, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(3.dp))
                Text("$label • ${result.item.location}", color = Color(0xFF98A2B3), fontSize = 12.sp, maxLines = 1)
            }
            Text(result.item.price, color = Color(0xFFFA580B), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun EmptySearch(query: String) {
    Column(
        Modifier.fillMaxWidth().padding(top = 64.dp, start = 24.dp, end = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Outlined.Search, null, tint = Color.White.copy(alpha = .32f), modifier = Modifier.size(56.dp))
        Spacer(Modifier.height(14.dp))
        Text("No matches for “$query”", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(Modifier.height(6.dp))
        Text("Try a movie, sport, event, or location.", color = Color(0xFF98A2B3), fontSize = 13.sp)
    }
}
