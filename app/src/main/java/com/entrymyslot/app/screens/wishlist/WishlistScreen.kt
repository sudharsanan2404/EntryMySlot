package com.entrymyslot.app.screens.wishlist

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.entrymyslot.app.core.components.EntryBottomNavigation
import com.entrymyslot.app.screens.home.GlowBackground
import com.entrymyslot.app.screens.home.PopularEvent

private val WishlistSurface = Color(0xFF0B274F)
private val WishlistBorder = Color(0xFF24527D)
private val WishlistAccent = Color(0xFFFA580B)
private val WishlistText = Color(0xFFF8FAFF)
private val WishlistSecondary = Color(0xFFA8B8CF)

data class WishlistEntry(
    val event: PopularEvent,
    val category: String,
    val icon: ImageVector
)

object WishlistStore {
    val items = mutableStateListOf<WishlistEntry>()

    fun contains(id: String): Boolean = items.any { it.event.id == id }

    fun toggle(event: PopularEvent, category: String, icon: ImageVector) {
        val existing = items.indexOfFirst { it.event.id == event.id }
        if (existing >= 0) items.removeAt(existing) else items.add(WishlistEntry(event, category, icon))
    }
}

@Composable
fun WishlistScreen(
    onBackClick: () -> Unit,
    onItemClick: (PopularEvent, String) -> Unit,
    onBottomNavigationClick: (String) -> Unit = {}
) {
    Box(Modifier.fillMaxSize()) {
        GlowBackground()
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            Row(
                Modifier.fillMaxWidth().height(68.dp).padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = WishlistText,
                    modifier = Modifier.size(40.dp).padding(9.dp).clickable(onClick = onBackClick)
                )
                Column(Modifier.padding(start = 10.dp)) {
                    Text("Wishlist", color = WishlistText, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text("Your interested picks", color = WishlistSecondary, fontSize = 11.sp)
                }
            }

            if (WishlistStore.items.isEmpty()) {
                Column(
                    Modifier.weight(1f).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Outlined.FavoriteBorder, null, tint = WishlistSecondary, modifier = Modifier.size(42.dp))
                    Text("No interested items yet", color = WishlistText, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 14.dp))
                    Text("Tap Interested on a movie, event, or venue.", color = WishlistSecondary, fontSize = 11.sp, modifier = Modifier.padding(top = 5.dp))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(start = 16.dp, top = 10.dp, end = 16.dp, bottom = 92.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(WishlistStore.items, key = { "${it.category}-${it.event.id}" }) { entry ->
                        WishlistCard(entry = entry, onClick = { onItemClick(entry.event, entry.category) })
                    }
                }
            }

        }
        EntryBottomNavigation(
            selectedItem = "",
            onItemSelected = onBottomNavigationClick,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun WishlistCard(entry: WishlistEntry, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().height(132.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.horizontalGradient(listOf(WishlistSurface, Color(0xFF0D315F))))
            .border(1.dp, WishlistBorder.copy(alpha = 0.75f), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.width(102.dp).height(112.dp).clip(RoundedCornerShape(15.dp))
                .background(Color(0xFF071D3C)),
            contentAlignment = Alignment.Center
        ) {
            if (entry.event.imageUrl != null) {
                AsyncImage(
                    model = entry.event.imageUrl,
                    contentDescription = entry.event.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(entry.icon, null, tint = WishlistAccent, modifier = Modifier.size(34.dp))
            }
            Box(
                Modifier.align(Alignment.TopStart).padding(7.dp).clip(RoundedCornerShape(50))
                    .background(Color.Black.copy(alpha = 0.62f)).padding(horizontal = 7.dp, vertical = 4.dp)
            ) {
                Text(entry.category, color = WishlistText, fontSize = 8.sp, fontWeight = FontWeight.Bold)
            }
        }
        Column(Modifier.weight(1f).padding(start = 13.dp)) {
            Text(entry.event.title, color = WishlistText, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Row(Modifier.padding(top = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.LocationOn, null, tint = WishlistSecondary, modifier = Modifier.size(14.dp))
                Text(entry.event.location, color = WishlistSecondary, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(start = 4.dp))
            }
            Text(entry.event.price, color = WishlistAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
        }
        Box(Modifier.size(34.dp).clip(CircleShape).background(WishlistAccent.copy(alpha = 0.14f)), contentAlignment = Alignment.Center) {
            Icon(Icons.Rounded.Favorite, contentDescription = "Interested", tint = WishlistAccent, modifier = Modifier.size(18.dp))
        }
    }
}
