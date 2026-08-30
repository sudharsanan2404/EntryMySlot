package com.entrymyslot.app.screens.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material.icons.rounded.SportsSoccer
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.entrymyslot.app.screens.home.PopularEvent

enum class SearchResultType { MOVIE, SPORT, EVENT }

data class SearchResult(
    val item: PopularEvent,
    val type: SearchResultType
)

private enum class PriceFilter(val label: String) {
    ANY("Any price"),
    FREE("Free"),
    UNDER_500("Under ₹500"),
    ABOVE_500("₹500+")
}

private enum class SearchSort(val label: String) {
    RELEVANCE("Relevance"),
    PRICE_LOW("Price: Low to high"),
    PRICE_HIGH("Price: High to low")
}

private val SearchBackground = Color(0xFF061A38)
private val SearchSurface = Color(0xFF0B274F)
private val SearchSurfaceRaised = Color(0xFF0D2D5A)
private val SearchBorder = Color(0xFF24527D)
private val SearchAccent = Color(0xFFFA580B)
private val SearchPrimaryText = Color(0xFFF8FAFF)
private val SearchSecondaryText = Color(0xFFA8B8CF)
private val SearchMutedText = Color(0xFF7185A1)

@Composable
fun SearchScreen(
    movies: List<PopularEvent>,
    sports: List<PopularEvent>,
    events: List<PopularEvent>,
    onBackClick: () -> Unit,
    onResultClick: (SearchResult) -> Unit
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
            .filter {
                term.isEmpty() ||
                    it.item.title.contains(term, true) ||
                    it.item.location.contains(term, true) ||
                    it.type.name.contains(term, true)
            }
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
            }
            .toList()
            .let { if (term.isEmpty() && !showFilters) it.take(8) else it }
    }
    val activeFilterCount =
        (if (selectedTypes.size < SearchResultType.entries.size) 1 else 0) +
            (if (priceFilter != PriceFilter.ANY) 1 else 0) +
            (if (sort != SearchSort.RELEVANCE) 1 else 0)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SearchBackground)
            .navigationBarsPadding()
            .imePadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(248.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0C3B78),
                            SearchBackground.copy(alpha = 0.22f),
                            Color.Transparent
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            SearchHeader(onBackClick = onBackClick)

            SearchControls(
                query = query,
                onQueryChange = { query = it },
                onClearQuery = { query = "" },
                showFilters = showFilters,
                activeFilterCount = activeFilterCount,
                onFilterClick = { showFilters = !showFilters },
                focusRequester = focusRequester,
                onSearchAction = { focusManager.clearFocus() }
            )

            AnimatedVisibility(
                visible = showFilters,
                enter = expandVertically(
                    animationSpec = tween(durationMillis = 190),
                    expandFrom = Alignment.Top
                ) + fadeIn(tween(durationMillis = 150)),
                exit = shrinkVertically(
                    animationSpec = tween(durationMillis = 170),
                    shrinkTowards = Alignment.Top
                ) + fadeOut(tween(durationMillis = 120))
            ) {
                SearchFilters(
                    selectedTypes = selectedTypes,
                    priceFilter = priceFilter,
                    sort = sort,
                    onTypeToggle = {
                        selectedTypes = if (it in selectedTypes) {
                            selectedTypes - it
                        } else {
                            selectedTypes + it
                        }
                    },
                    onPriceChange = { priceFilter = it },
                    onSortChange = { sort = it },
                    onReset = {
                        selectedTypes = SearchResultType.entries.toSet()
                        priceFilter = PriceFilter.ANY
                        sort = SearchSort.RELEVANCE
                    }
                )
            }

            ResultsHeader(
                title = if (query.isBlank() && activeFilterCount == 0) {
                    "Popular near you"
                } else {
                    "${results.size} results"
                }
            )

            if (results.isEmpty()) {
                EmptySearch(
                    query = query,
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 24.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = results,
                        key = { "${it.type}-${it.item.id}" }
                    ) { result ->
                        SearchResultCard(
                            result = result,
                            onClick = { onResultClick(result) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchHeader(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SearchBackButton(onClick = onBackClick)
        Spacer(modifier = Modifier.width(11.dp))
        Column {
            Text(
                text = "Discover",
                color = SearchPrimaryText,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = "Find your next experience",
                color = SearchSecondaryText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun SearchBackButton(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = tween(durationMillis = 110),
        label = "searchBackScale"
    )

    Box(
        modifier = Modifier
            .size(44.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(CircleShape)
            .background(SearchSurface.copy(alpha = 0.94f))
            .border(BorderStroke(1.dp, SearchBorder), CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClickLabel = "Go back",
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
            contentDescription = "Back",
            tint = SearchPrimaryText,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun SearchControls(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    showFilters: Boolean,
    activeFilterCount: Int,
    onFilterClick: () -> Unit,
    focusRequester: FocusRequester,
    onSearchAction: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 11.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PremiumSearchField(
            query = query,
            onQueryChange = onQueryChange,
            onClearQuery = onClearQuery,
            focusRequester = focusRequester,
            onSearchAction = onSearchAction,
            modifier = Modifier.weight(1f)
        )
        FilterButton(
            expanded = showFilters,
            activeFilterCount = activeFilterCount,
            onClick = onFilterClick
        )
    }
}

@Composable
private fun PremiumSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    focusRequester: FocusRequester,
    onSearchAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    val borderColor by animateColorAsState(
        targetValue = if (isFocused) SearchAccent else SearchBorder.copy(alpha = 0.76f),
        animationSpec = tween(durationMillis = 150),
        label = "searchFieldBorder"
    )
    val searchIconColor by animateColorAsState(
        targetValue = if (isFocused) SearchAccent else SearchSecondaryText,
        animationSpec = tween(durationMillis = 150),
        label = "searchIconColor"
    )

    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .height(52.dp)
            .focusRequester(focusRequester)
            .onFocusChanged { isFocused = it.isFocused }
            .semantics {
                contentDescription = "Search movies, sports or venues"
            },
        textStyle = TextStyle(
            color = SearchPrimaryText,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        ),
        singleLine = true,
        cursorBrush = SolidColor(SearchAccent),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearchAction() }),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(15.dp))
                    .background(SearchSurface.copy(alpha = 0.96f))
                    .border(
                        BorderStroke(if (isFocused) 1.5.dp else 1.dp, borderColor),
                        RoundedCornerShape(15.dp)
                    )
                    .padding(start = 14.dp, end = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = null,
                    tint = searchIconColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (query.isEmpty()) {
                        Text(
                            text = "Movies, sports or venues",
                            color = SearchMutedText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    innerTextField()
                }
                AnimatedVisibility(
                    visible = query.isNotEmpty(),
                    enter = fadeIn(tween(100)) + scaleIn(tween(120), initialScale = 0.82f),
                    exit = fadeOut(tween(80)) + scaleOut(tween(90), targetScale = 0.82f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .clickable(
                                role = Role.Button,
                                onClickLabel = "Clear search",
                                onClick = onClearQuery
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Clear,
                            contentDescription = "Clear search",
                            tint = SearchSecondaryText,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun FilterButton(
    expanded: Boolean,
    activeFilterCount: Int,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1f,
        animationSpec = tween(durationMillis = 110),
        label = "filterButtonScale"
    )
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 90f else 0f,
        animationSpec = tween(durationMillis = 170),
        label = "filterIconRotation"
    )
    val containerColor by animateColorAsState(
        targetValue = if (expanded) SearchAccent else SearchSurfaceRaised,
        animationSpec = tween(durationMillis = 150),
        label = "filterButtonColor"
    )

    Box(
        modifier = Modifier.size(52.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clip(RoundedCornerShape(14.dp))
                .background(containerColor)
                .border(
                    BorderStroke(
                        1.dp,
                        if (expanded) SearchAccent else SearchBorder.copy(alpha = 0.80f)
                    ),
                    RoundedCornerShape(14.dp)
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    role = Role.Button,
                    onClickLabel = "Filters",
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Tune,
                contentDescription = "Filters",
                tint = if (expanded) Color.White else SearchAccent,
                modifier = Modifier
                    .size(21.dp)
                    .graphicsLayer { rotationZ = rotation }
            )
        }

        AnimatedVisibility(
            visible = activeFilterCount > 0,
            modifier = Modifier.align(Alignment.TopEnd),
            enter = fadeIn(tween(100)) + scaleIn(tween(130), initialScale = 0.7f),
            exit = fadeOut(tween(80)) + scaleOut(tween(90), targetScale = 0.7f)
        ) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(SearchAccent)
                    .border(2.dp, SearchBackground, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = activeFilterCount.toString(),
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun SearchFilters(
    selectedTypes: Set<SearchResultType>,
    priceFilter: PriceFilter,
    sort: SearchSort,
    onTypeToggle: (SearchResultType) -> Unit,
    onPriceChange: (PriceFilter) -> Unit,
    onSortChange: (SearchSort) -> Unit,
    onReset: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(17.dp))
            .background(SearchSurface.copy(alpha = 0.96f))
            .border(
                BorderStroke(1.dp, SearchBorder.copy(alpha = 0.80f)),
                RoundedCornerShape(17.dp)
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "FILTER RESULTS",
                modifier = Modifier.weight(1f),
                color = SearchPrimaryText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
            Text(
                text = "Reset",
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(
                        role = Role.Button,
                        onClickLabel = "Reset filters",
                        onClick = onReset
                    )
                    .padding(horizontal = 9.dp, vertical = 6.dp),
                color = SearchAccent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        FilterSectionLabel(text = "Category")
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            SearchResultType.entries.forEach { type ->
                val label = when (type) {
                    SearchResultType.MOVIE -> "Movies"
                    SearchResultType.SPORT -> "Sports"
                    SearchResultType.EVENT -> "Events"
                }
                PremiumFilterChip(
                    text = label,
                    selected = type in selectedTypes,
                    onClick = { onTypeToggle(type) }
                )
            }
        }

        FilterSectionLabel(text = "Price")
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            PriceFilter.entries.forEach { filter ->
                PremiumFilterChip(
                    text = filter.label,
                    selected = priceFilter == filter,
                    onClick = { onPriceChange(filter) }
                )
            }
        }

        FilterSectionLabel(text = "Sort by")
        SortSelector(
            selectedSort = sort,
            onSortChange = onSortChange
        )
    }
}

@Composable
private fun FilterSectionLabel(text: String) {
    Text(
        text = text,
        color = SearchMutedText,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.6.sp
    )
}

@Composable
private fun PremiumFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "filterChipScale"
    )
    val containerColor by animateColorAsState(
        targetValue = if (selected) SearchAccent else SearchSurfaceRaised,
        animationSpec = tween(durationMillis = 140),
        label = "filterChipColor"
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) SearchAccent else SearchBorder.copy(alpha = 0.72f),
        animationSpec = tween(durationMillis = 140),
        label = "filterChipBorder"
    )

    Row(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(9.dp))
            .background(containerColor)
            .border(BorderStroke(1.dp, borderColor), RoundedCornerShape(9.dp))
            .semantics {
                this.selected = selected
                stateDescription = if (selected) "Selected" else "Not selected"
                role = Role.Button
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClickLabel = text,
                onClick = onClick
            )
            .padding(horizontal = 11.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AnimatedVisibility(
            visible = selected,
            enter = fadeIn(tween(100)) + scaleIn(tween(120), initialScale = 0.75f),
            exit = fadeOut(tween(80)) + scaleOut(tween(90), targetScale = 0.75f)
        ) {
            Row {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
        }
        Text(
            text = text,
            color = if (selected) Color.White else SearchSecondaryText,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun SortSelector(
    selectedSort: SearchSort,
    onSortChange: (SearchSort) -> Unit
) {
    var sortMenuOpen by remember { mutableStateOf(false) }
    val chevronRotation by animateFloatAsState(
        targetValue = if (sortMenuOpen) 180f else 0f,
        animationSpec = tween(durationMillis = 160),
        label = "sortChevronRotation"
    )

    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(11.dp))
                .background(SearchSurfaceRaised)
                .border(
                    BorderStroke(1.dp, SearchBorder.copy(alpha = 0.78f)),
                    RoundedCornerShape(11.dp)
                )
                .clickable(
                    role = Role.Button,
                    onClickLabel = "Sort results",
                    onClick = { sortMenuOpen = true }
                )
                .padding(horizontal = 13.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.Sort,
                contentDescription = null,
                tint = SearchAccent,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(9.dp))
            Text(
                text = selectedSort.label,
                modifier = Modifier.weight(1f),
                color = SearchPrimaryText,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Icon(
                imageVector = Icons.Rounded.ExpandMore,
                contentDescription = null,
                tint = SearchSecondaryText,
                modifier = Modifier
                    .size(18.dp)
                    .graphicsLayer { rotationZ = chevronRotation }
            )
        }

        DropdownMenu(
            expanded = sortMenuOpen,
            onDismissRequest = { sortMenuOpen = false },
            modifier = Modifier
                .background(SearchSurfaceRaised)
                .border(
                    BorderStroke(1.dp, SearchBorder.copy(alpha = 0.82f)),
                    RoundedCornerShape(10.dp)
                )
        ) {
            SearchSort.entries.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option.label,
                            color = if (selectedSort == option) {
                                SearchAccent
                            } else {
                                SearchPrimaryText
                            },
                            fontSize = 12.sp,
                            fontWeight = if (selectedSort == option) {
                                FontWeight.Bold
                            } else {
                                FontWeight.Medium
                            }
                        )
                    },
                    onClick = {
                        onSortChange(option)
                        sortMenuOpen = false
                    },
                    leadingIcon = {
                        if (selectedSort == option) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = SearchAccent,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ResultsHeader(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(
            start = 18.dp,
            top = 12.dp,
            end = 18.dp,
            bottom = 9.dp
        ),
        color = SearchPrimaryText,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold
    )
}

private fun String.numericPrice(): Int {
    if (contains("free", true)) return 0
    return Regex("\\d[\\d,]*")
        .find(this)
        ?.value
        ?.replace(",", "")
        ?.toIntOrNull()
        ?: Int.MAX_VALUE
}

@Composable
private fun SearchResultCard(
    result: SearchResult,
    onClick: () -> Unit
) {
    val (icon, label) = when (result.type) {
        SearchResultType.MOVIE -> Icons.Rounded.Movie to "Movie"
        SearchResultType.SPORT -> Icons.Rounded.SportsSoccer to "Sports"
        SearchResultType.EVENT -> Icons.Rounded.Event to "Event"
    }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.985f else 1f,
        animationSpec = tween(durationMillis = 110),
        label = "resultCardScale"
    )
    val elevation by animateDpAsState(
        targetValue = if (isPressed) 1.dp else 4.dp,
        animationSpec = tween(durationMillis = 110),
        label = "resultCardElevation"
    )
    val cardShape = RoundedCornerShape(15.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = elevation,
                shape = cardShape,
                ambientColor = Color.Black.copy(alpha = 0.14f),
                spotColor = Color.Black.copy(alpha = 0.20f)
            )
            .clip(cardShape)
            .background(SearchSurface)
            .border(
                BorderStroke(1.dp, SearchBorder.copy(alpha = 0.76f)),
                cardShape
            )
            .semantics(mergeDescendants = true) {
                contentDescription = "$label, ${result.item.title}, ${result.item.location}, ${result.item.price}"
                role = Role.Button
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClickLabel = "Open ${result.item.title}",
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(SearchAccent.copy(alpha = 0.13f))
                .border(
                    BorderStroke(1.dp, SearchAccent.copy(alpha = 0.24f)),
                    RoundedCornerShape(13.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = SearchAccent,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = result.item.title,
                color = SearchPrimaryText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$label • ${result.item.location}",
                color = SearchSecondaryText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = result.item.price,
            color = SearchAccent,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun EmptySearch(
    query: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(SearchSurfaceRaised.copy(alpha = 0.74f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.SearchOff,
                contentDescription = null,
                tint = SearchSecondaryText,
                modifier = Modifier.size(27.dp)
            )
        }
        Spacer(modifier = Modifier.height(13.dp))
        Text(
            text = if (query.isBlank()) {
                "No results match these filters"
            } else {
                "No matches for “$query”"
            },
            color = SearchPrimaryText,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = "Try changing your search or filters.",
            color = SearchSecondaryText,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
