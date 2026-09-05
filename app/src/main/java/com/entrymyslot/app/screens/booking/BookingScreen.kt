package com.entrymyslot.app.screens.booking

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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SentimentDissatisfied
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.entrymyslot.app.EntryMySlotApp
import com.entrymyslot.app.core.components.PremiumLoadingState
import com.entrymyslot.app.core.components.PremiumErrorState
import com.entrymyslot.app.core.components.PremiumEmptyState
import com.entrymyslot.app.screens.home.GlowBackground
import com.entrymyslot.app.data.model.Booking
import com.entrymyslot.app.data.model.BookingStatus
import com.entrymyslot.app.data.model.BookingType

private val BookingBackground = Color(0xFF061A38)
private val BookingSurface = Color(0xFF0B274F)
private val BookingSurfaceRaised = Color(0xFF0D2D5A)
private val BookingBorder = Color(0xFF24527D)
private val BookingAccent = Color(0xFFFA580B)
private val BookingPrimaryText = Color(0xFFF8FAFF)
private val BookingSecondaryText = Color(0xFFA8B8CF)
private val BookingMutedText = Color(0xFF7185A1)
private val StatusCompletedColor = Color(0xFF52A77A)
private val StatusCancelledColor = Color(0xFFD46B6B)

typealias BookingItem = Booking

@Composable
fun BookingScreen(
    onBackClick: () -> Unit = {},
    onBottomNavigationClick: (String) -> Unit = {},
    onViewTicketClick: (BookingItem) -> Unit = {}
) {
    val bookingViewModel: BookingViewModel = viewModel()
    val state by bookingViewModel.uiState.collectAsStateWithLifecycle()
    val tabs = listOf("Upcoming", "Past")
    val filters = listOf("All", "Movies", "Turf", "Events")

    LaunchedEffect(Unit) {
        bookingViewModel.loadBookings()
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
            BookingHeader(onBackClick = onBackClick)

            BookingTabs(
                tabs = tabs,
                selectedTab = state.selectedTab,
                onTabSelected = bookingViewModel::selectTab
            )

            if (state.selectedTab < 2) {
                LazyRow(
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        top = 10.dp,
                        end = 16.dp,
                        bottom = 10.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    items(filters, key = { it }) { filter ->
                        BookingFilterChip(
                            label = filter,
                            isSelected = state.selectedFilter == filter,
                            onClick = { bookingViewModel.selectFilter(filter) }
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(
                    top = 2.dp,
                    bottom = 92.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when {
                    state.isLoading && state.bookings.isEmpty() -> {
                        item { BookingLoadingState() }
                    }
                    state.errorMessage != null && state.bookings.isEmpty() -> {
                        item {
                            BookingErrorState(
                                message = state.errorMessage.orEmpty(),
                                onRetry = bookingViewModel::retry
                            )
                        }
                    }
                    state.bookings.isEmpty() -> {
                        item {
                            EmptyState(
                                title = if (state.selectedTab == 0) {
                                    "No Upcoming Bookings"
                                } else {
                                    "No Past Bookings"
                                },
                                subtitle = if (state.selectedTab == 0) {
                                    "Your next experience is waiting for you."
                                } else {
                                    "Go book something amazing!"
                                }
                            )
                        }
                    }
                    else -> {
                        if (state.isLoading) {
                            item(key = "booking-refreshing") { BookingRefreshingState() }
                        }
                        state.errorMessage?.let { message ->
                            item(key = "booking-inline-error") {
                                BookingInlineError(message, bookingViewModel::retry)
                            }
                        }
                        items(state.bookings, key = { it.id }) { item ->
                            BookingCard(
                                item = item,
                                isPast = state.selectedTab == 1,
                                onViewTicketClick = onViewTicketClick
                            )
                        }
                    }
                }
            }

        }
    }
}

@Composable
private fun BookingHeader(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BookingBackButton(onClick = onBackClick)
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "My Bookings",
            color = BookingPrimaryText,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun BookingBackButton(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = tween(durationMillis = 110),
        label = "bookingBackScale"
    )

    Box(
        modifier = Modifier
            .size(44.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
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
            tint = BookingPrimaryText,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun BookingTabs(
    tabs: List<String>,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(BookingSurface.copy(alpha = 0.90f))
            .border(
                BorderStroke(1.dp, BookingBorder.copy(alpha = 0.76f)),
                RoundedCornerShape(13.dp)
            )
            .padding(4.dp)
    ) {
        tabs.forEachIndexed { index, title ->
            val isSelected = selectedTab == index
            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val scale by animateFloatAsState(
                targetValue = if (isPressed) 0.97f else 1f,
                animationSpec = tween(durationMillis = 100),
                label = "bookingTabScale"
            )
            val containerColor by animateColorAsState(
                targetValue = if (isSelected) BookingAccent else Color.Transparent,
                animationSpec = tween(durationMillis = 170),
                label = "bookingTabColor"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .clip(RoundedCornerShape(10.dp))
                    .background(containerColor)
                    .semantics {
                        selected = isSelected
                        stateDescription = if (isSelected) "Selected" else "Not selected"
                        role = Role.Button
                    }
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        role = Role.Button,
                        onClickLabel = title,
                        onClick = { onTabSelected(index) }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = title,
                    color = if (isSelected) Color.White else BookingSecondaryText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun BookingFilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "bookingFilterScale"
    )
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) BookingAccent else BookingSurfaceRaised,
        animationSpec = tween(durationMillis = 150),
        label = "bookingFilterColor"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) BookingAccent else BookingBorder.copy(alpha = 0.72f),
        animationSpec = tween(durationMillis = 150),
        label = "bookingFilterBorder"
    )

    Box(
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(9.dp))
            .background(containerColor)
            .border(BorderStroke(1.dp, borderColor), RoundedCornerShape(9.dp))
            .semantics {
                selected = isSelected
                stateDescription = if (isSelected) "Selected filter" else "Not selected"
                role = Role.Button
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClickLabel = "$label filter",
                onClick = onClick
            )
            .padding(horizontal = 13.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.White else BookingSecondaryText,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun BookingCard(
    item: BookingItem,
    isPast: Boolean,
    onViewTicketClick: (BookingItem) -> Unit
) {
    val actionInteractionSource = remember { MutableInteractionSource() }
    val isActionPressed by actionInteractionSource.collectIsPressedAsState()
    val cardScale by animateFloatAsState(
        targetValue = if (isActionPressed) 0.988f else 1f,
        animationSpec = tween(durationMillis = 110),
        label = "bookingCardScale"
    )
    val cardElevation by animateDpAsState(
        targetValue = if (isActionPressed) 2.dp else 6.dp,
        animationSpec = tween(durationMillis = 110),
        label = "bookingCardElevation"
    )
    val cardShape = RoundedCornerShape(17.dp)
    val actionLabel = if (item.type == BookingType.TURF) "View Booking" else "View Ticket"
    val itemTitle = item.title
    val itemLocation = item.location

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .graphicsLayer {
                scaleX = cardScale
                scaleY = cardScale
                alpha = if (isPast) 0.90f else 1f
            }
            .shadow(
                elevation = cardElevation,
                shape = cardShape,
                ambientColor = Color.Black.copy(alpha = 0.18f),
                spotColor = Color.Black.copy(alpha = 0.25f)
            )
            .clip(cardShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        BookingSurface,
                        BookingSurfaceRaised.copy(alpha = if (isPast) 0.68f else 0.90f)
                    )
                )
            )
            .border(
                BorderStroke(1.dp, BookingBorder.copy(alpha = if (isPast) 0.58f else 0.80f)),
                cardShape
            )
            .semantics(mergeDescendants = true) {
                contentDescription = buildString {
                    append(item.type.name)
                    append(", ")
                    append(item.status.name)
                    append(", ")
                    append(itemTitle)
                    append(", ")
                    append(itemLocation)
                    append(", ")
                    append(item.dateTime)
                    append(", ")
                    append(item.details)
                    append(", ")
                    append(item.price)
                }
            }
            .padding(15.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BookingTypeLabel(type = item.type)
            StatusBadge(status = item.status)
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = itemTitle,
            color = BookingPrimaryText,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 20.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(3.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Outlined.LocationOn,
                contentDescription = null,
                tint = BookingMutedText,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = itemLocation,
                modifier = Modifier.weight(1f),
                color = BookingSecondaryText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        BookingMetadataRow(
            icon = Icons.Outlined.CalendarToday,
            text = item.dateTime,
            accentIcon = true
        )
        Spacer(modifier = Modifier.height(6.dp))
        BookingMetadataRow(
            icon = Icons.Outlined.Info,
            text = item.details,
            accentIcon = false
        )

        Spacer(modifier = Modifier.height(13.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(BookingBorder.copy(alpha = 0.42f))
        )

        Spacer(modifier = Modifier.height(11.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.price,
                color = if (item.status == BookingStatus.UPCOMING) {
                    BookingAccent
                } else {
                    BookingPrimaryText
                },
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(BookingAccent)
                    .clickable(
                        interactionSource = actionInteractionSource,
                        indication = null,
                        role = Role.Button,
                        onClickLabel = actionLabel,
                        onClick = { onViewTicketClick(item) }
                    )
                    .padding(horizontal = 15.dp, vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = actionLabel,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun BookingTypeLabel(type: BookingType) {
    val icon = when (type) {
        BookingType.MOVIE -> Icons.Outlined.Movie
        BookingType.TURF -> Icons.Outlined.SportsSoccer
        BookingType.EVENT -> Icons.Outlined.ConfirmationNumber
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(BookingAccent.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = BookingAccent,
                modifier = Modifier.size(15.dp)
            )
        }
        Spacer(modifier = Modifier.width(7.dp))
        Text(
            text = type.name,
            color = BookingSecondaryText,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.7.sp
        )
    }
}

@Composable
private fun BookingMetadataRow(
    icon: ImageVector,
    text: String,
    accentIcon: Boolean
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (accentIcon) BookingAccent else BookingMutedText,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(7.dp))
        Text(
            text = text,
            modifier = Modifier.weight(1f),
            color = BookingSecondaryText,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun StatusBadge(status: BookingStatus) {
    val (color, text) = when (status) {
        BookingStatus.UPCOMING -> BookingAccent to "UPCOMING"
        BookingStatus.COMPLETED -> StatusCompletedColor to "COMPLETED"
        BookingStatus.CANCELLED -> StatusCancelledColor to "CANCELLED"
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(7.dp))
            .background(color.copy(alpha = 0.13f))
            .border(
                BorderStroke(1.dp, color.copy(alpha = 0.28f)),
                RoundedCornerShape(7.dp)
            )
            .semantics { stateDescription = text }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 8.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.6.sp
        )
    }
}

@Composable
private fun BookingLoadingState() {
    PremiumLoadingState(
        modifier = Modifier.fillMaxWidth().padding(vertical = 72.dp),
        message = "Loading your bookings..."
    )
}

@Composable
private fun BookingRefreshingState() {
    PremiumLoadingState(
        modifier = Modifier.fillMaxWidth().height(100.dp),
        message = "Updating bookings..."
    )
}

@Composable
private fun BookingErrorState(message: String, onRetry: () -> Unit) {
    PremiumErrorState(
        modifier = Modifier.fillMaxWidth().padding(vertical = 58.dp),
        title = "Booking Load Failed",
        message = message,
        onRetry = onRetry
    )
}

@Composable
private fun BookingInlineError(message: String, onRetry: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(StatusCancelledColor.copy(alpha = 0.18f))
            .border(1.dp, StatusCancelledColor.copy(alpha = 0.45f), RoundedCornerShape(13.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = message,
            modifier = Modifier.weight(1f),
            color = BookingPrimaryText,
            fontSize = 10.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Retry",
            color = BookingAccent,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable(role = Role.Button, onClick = onRetry)
        )
    }
}

@Composable
private fun BookingRetryButton(onRetry: () -> Unit) {
    Text(
        text = "RETRY",
        color = Color.White,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .clip(RoundedCornerShape(11.dp))
            .background(BookingAccent)
            .clickable(role = Role.Button, onClick = onRetry)
            .padding(horizontal = 22.dp, vertical = 10.dp)
    )
}

@Composable
private fun EmptyState(
    title: String,
    subtitle: String
) {
    PremiumEmptyState(
        modifier = Modifier.fillMaxWidth().padding(vertical = 52.dp),
        title = title,
        message = subtitle
    )
}

@Composable
private fun BookingBottomNavigation(
    selectedItem: String,
    onItemSelected: (String) -> Unit
) {
    val items = listOf(
        Triple("Home", Icons.Outlined.Home, Icons.Rounded.Home),
        Triple("Search", Icons.Outlined.Search, Icons.Outlined.Search),
        Triple("My Bookings", Icons.Outlined.ConfirmationNumber, Icons.Outlined.ConfirmationNumber),
        Triple("Profile", Icons.Outlined.AccountCircle, Icons.Outlined.AccountCircle)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(18.dp),
                    ambientColor = Color.Black.copy(alpha = 0.18f),
                    spotColor = Color.Black.copy(alpha = 0.24f)
                )
                .clip(RoundedCornerShape(18.dp))
                .background(BookingSurface.copy(alpha = 0.96f))
                .border(
                    BorderStroke(1.dp, BookingBorder.copy(alpha = 0.78f)),
                    RoundedCornerShape(18.dp)
                )
                .padding(horizontal = 5.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                BookingNavigationItem(
                    label = item.first,
                    unselectedIcon = item.second,
                    selectedIcon = item.third,
                    selected = selectedItem == item.first,
                    onClick = { onItemSelected(item.first) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun BookingNavigationItem(
    label: String,
    unselectedIcon: ImageVector,
    selectedIcon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) {
            0.93f
        } else {
            1f
        },
        animationSpec = tween(durationMillis = 100),
        label = "bookingNavScale"
    )
    val containerColor by animateColorAsState(
        targetValue = if (selected) BookingAccent.copy(alpha = 0.15f) else Color.Transparent,
        animationSpec = tween(durationMillis = 160),
        label = "bookingNavColor"
    )

    Column(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(12.dp))
            .background(containerColor)
            .semantics {
                this.selected = selected
                stateDescription = if (selected) "Selected" else "Not selected"
                role = Role.Button
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClickLabel = label,
                onClick = onClick
            )
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = if (selected) {
                selectedIcon
            } else {
                unselectedIcon
            },
            contentDescription = label,
            tint = if (selected) BookingAccent else BookingMutedText,
            modifier = Modifier.size(19.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = if (selected) BookingAccent else BookingMutedText,
            fontSize = 8.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
