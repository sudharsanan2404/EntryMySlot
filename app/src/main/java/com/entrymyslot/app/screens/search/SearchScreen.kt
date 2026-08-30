package com.entrymyslot.app.screens.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
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
import com.entrymyslot.app.screens.home.GlowBackground
import com.entrymyslot.app.core.components.ElevatedCardSubtitle
import com.entrymyslot.app.core.components.ElevatedCardTitle
import com.entrymyslot.app.core.components.ElevatedContrastCard
import com.entrymyslot.app.core.components.EntryCardAccent

enum class SearchResultType { MOVIE, SPORT, EVENT }
data class SearchResult(val item: PopularEvent, val type: SearchResultType)
private enum class PriceFilter(val label: String) { ANY("Any price"), FREE("Free"), UNDER_500("Under ₹500"), ABOVE_500("₹500+") }
private enum class SearchSort(val label: String) { RELEVANCE("Relevance"), PRICE_LOW("Price: Low to high"), PRICE_HIGH("Price: High to low") }

private val SearchTop = Color(0xFF0B3A82)
private val SearchBottom = Color(0xFF061A33)
private val SearchAccent = EntryCardAccent
private val SearchCard = Color(0xFF0E0B38).copy(alpha = .68f)
private val SearchMuted = Color(0xFF98A2B3)
private val SearchText = Color.White
private val SearchField = Color(0xFF0E0B38).copy(alpha = .78f)

@Composable
fun SearchScreen(
    movies: List<PopularEvent>, sports: List<PopularEvent>, events: List<PopularEvent>,
    onBackClick: () -> Unit, onResultClick: (SearchResult) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var showFilters by remember { mutableStateOf(false) }
    var selectedTypes by remember { mutableStateOf(SearchResultType.entries.toSet()) }
    var priceFilter by remember { mutableStateOf(PriceFilter.ANY) }
    var sort by remember { mutableStateOf(SearchSort.RELEVANCE) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val allResults = remember(movies, sports, events) {
        movies.map { SearchResult(it, SearchResultType.MOVIE) } +
            sports.map { SearchResult(it, SearchResultType.SPORT) } +
            events.map { SearchResult(it, SearchResultType.EVENT) }
    }
    val results = remember(query, selectedTypes, priceFilter, sort, allResults) {
        val term = query.trim()
        allResults.asSequence()
            .filter { it.type in selectedTypes }
            .filter { term.isEmpty() || it.item.title.contains(term, true) || it.item.location.contains(term, true) || it.type.name.contains(term, true) }
            .filter {
                val price = it.item.price.numericPrice()
                when (priceFilter) {
                    PriceFilter.ANY -> true
                    PriceFilter.FREE -> price == 0
                    PriceFilter.UNDER_500 -> price in 1..499
                    PriceFilter.ABOVE_500 -> price >= 500
                }
            }
            .let { sequence ->
                when (sort) {
                    SearchSort.RELEVANCE -> sequence
                    SearchSort.PRICE_LOW -> sequence.sortedBy { it.item.price.numericPrice() }
                    SearchSort.PRICE_HIGH -> sequence.sortedByDescending { it.item.price.numericPrice() }
                }
            }.toList().let { if (term.isEmpty() && !showFilters) it.take(8) else it }
    }
    val activeFilterCount = (if (selectedTypes.size < SearchResultType.entries.size) 1 else 0) +
        (if (priceFilter != PriceFilter.ANY) 1 else 0) + (if (sort != SearchSort.RELEVANCE) 1 else 0)

    Box(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().imePadding()) {
        GlowBackground()
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth().padding(start = 6.dp, top = 8.dp, end = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = SearchText) }
                Column {
                    Text("Discover", color = SearchText, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                    Text("Find your next experience", color = SearchMuted, fontSize = 11.sp)
                }
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TextField(
                    value = query, onValueChange = { query = it },
                    modifier = Modifier.weight(1f).height(54.dp).focusRequester(focusRequester),
                    placeholder = { Text("Movies, sports or venues") }, leadingIcon = { Icon(Icons.Rounded.Search, null) },
                    trailingIcon = { if (query.isNotEmpty()) IconButton(onClick = { query = "" }) { Icon(Icons.Rounded.Clear, "Clear") } },
                    singleLine = true, shape = RoundedCornerShape(16.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = SearchField, unfocusedContainerColor = SearchField,
                        focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = SearchText, unfocusedTextColor = SearchText,
                        focusedPlaceholderColor = SearchMuted, unfocusedPlaceholderColor = SearchMuted,
                        focusedLeadingIconColor = SearchAccent, unfocusedLeadingIconColor = SearchMuted,
                        focusedTrailingIconColor = SearchMuted, unfocusedTrailingIconColor = SearchMuted,
                        cursorColor = SearchAccent
                    ), keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
                )
                BadgedBox(badge = { if (activeFilterCount > 0) Badge(containerColor = SearchAccent) { Text(activeFilterCount.toString()) } }) {
                    FilledIconButton(onClick = { showFilters = !showFilters }, colors = IconButtonDefaults.filledIconButtonColors(containerColor = SearchAccent), modifier = Modifier.size(50.dp)) {
                        Icon(Icons.Rounded.Tune, "Filters", tint = Color.White)
                    }
                }
            }
            AnimatedVisibility(showFilters) {
                SearchFilters(selectedTypes, priceFilter, sort,
                    onTypeToggle = { selectedTypes = if (it in selectedTypes) selectedTypes - it else selectedTypes + it },
                    onPriceChange = { priceFilter = it }, onSortChange = { sort = it },
                    onReset = { selectedTypes = SearchResultType.entries.toSet(); priceFilter = PriceFilter.ANY; sort = SearchSort.RELEVANCE })
            }
            Text(
                if (query.isBlank() && activeFilterCount == 0) "Popular near you" else "${results.size} results",
                color = SearchText, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
            )
            if (results.isEmpty()) EmptySearch(query) else LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)
            ) { items(results, key = { "${it.type}-${it.item.id}" }) { result -> SearchResultCard(result) { onResultClick(result) } } }
        }
    }
}

@Composable
private fun SearchFilters(
    selectedTypes: Set<SearchResultType>, priceFilter: PriceFilter, sort: SearchSort,
    onTypeToggle: (SearchResultType) -> Unit, onPriceChange: (PriceFilter) -> Unit,
    onSortChange: (SearchSort) -> Unit, onReset: () -> Unit
) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp).background(SearchCard, RoundedCornerShape(18.dp)).padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Filter results", color = SearchText, fontWeight = FontWeight.Bold)
            TextButton(onClick = onReset) { Text("Reset", color = SearchAccent) }
        }
        Text("Category", color = SearchMuted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SearchResultType.entries.forEach { type ->
                val label = when (type) { SearchResultType.MOVIE -> "Movies"; SearchResultType.SPORT -> "Sports"; SearchResultType.EVENT -> "Events" }
                FilterChip(selected = type in selectedTypes, onClick = { onTypeToggle(type) }, label = { Text(label) }, colors = searchFilterChipColors())
            }
        }
        Text("Price", color = SearchMuted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PriceFilter.entries.forEach { filter -> FilterChip(selected = priceFilter == filter, onClick = { onPriceChange(filter) }, label = { Text(filter.label) }, colors = searchFilterChipColors()) }
        }
        var sortMenuOpen by remember { mutableStateOf(false) }
        Box {
            OutlinedButton(onClick = { sortMenuOpen = true }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.AutoMirrored.Rounded.Sort, null, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text(sort.label); Spacer(Modifier.weight(1f)); Icon(Icons.Rounded.ExpandMore, null)
            }
            DropdownMenu(expanded = sortMenuOpen, onDismissRequest = { sortMenuOpen = false }) {
                SearchSort.entries.forEach { option -> DropdownMenuItem(text = { Text(option.label) }, onClick = { onSortChange(option); sortMenuOpen = false }) }
            }
        }
    }
}

@Composable private fun searchFilterChipColors() = FilterChipDefaults.filterChipColors(labelColor = SearchMuted, selectedContainerColor = SearchAccent, selectedLabelColor = Color.White)

private fun String.numericPrice(): Int {
    if (contains("free", true)) return 0
    return Regex("\\d[\\d,]*").find(this)?.value?.replace(",", "")?.toIntOrNull() ?: Int.MAX_VALUE
}

@Composable
private fun SearchResultCard(result: SearchResult, onClick: () -> Unit) {
    val (icon, label) = when (result.type) {
        SearchResultType.MOVIE -> Icons.Rounded.Movie to "Movie"
        SearchResultType.SPORT -> Icons.Rounded.SportsSoccer to "Sports"
        SearchResultType.EVENT -> Icons.Rounded.Event to "Event"
    }
    ElevatedContrastCard(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp).background(SearchAccent.copy(alpha = .14f), CircleShape), contentAlignment = Alignment.Center) { Icon(icon, null, tint = SearchAccent, modifier = Modifier.size(24.dp)) }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                ElevatedCardTitle(result.item.title)
                Spacer(Modifier.height(2.dp))
                ElevatedCardSubtitle("$label • ${result.item.location}")
                Spacer(Modifier.height(10.dp))
            }
            Text(result.item.price, color = SearchAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun EmptySearch(query: String) {
    Column(Modifier.fillMaxWidth().padding(top = 64.dp, start = 24.dp, end = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Rounded.SearchOff, null, tint = SearchText.copy(alpha = .32f), modifier = Modifier.size(56.dp)); Spacer(Modifier.height(14.dp))
        Text(if (query.isBlank()) "No results match these filters" else "No matches for “$query”", color = SearchText, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(Modifier.height(6.dp)); Text("Try changing your search or filters.", color = SearchMuted, fontSize = 13.sp)
    }
}
