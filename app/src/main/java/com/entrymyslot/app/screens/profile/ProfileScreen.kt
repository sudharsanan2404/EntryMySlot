package com.entrymyslot.app.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Payment
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.entrymyslot.app.EntryMySlotApp

private val BgDark = Color(0xFF080B1A)
private val CardBg = Color(0xFF0D1025)
private val AccentOrange = Color(0xFFFF7A00)
private val White = Color.White
private val Gray = Color(0xFF8A8FA8)

data class ProfileMenuItem(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val onClick: () -> Unit
)

@Composable
fun ProfileScreen(
    showListYourVenue: Boolean = false,
    onNavigateToMyBookings: () -> Unit = {},
    onLogout: () -> Unit = {},
    onBackClick: () -> Unit = {},
    onListYourVenueClick: () -> Unit = {},
    onDebugClick: () -> Unit = {}
) {
    val viewModel = remember { ProfileViewModel(EntryMySlotApp.instance.appContainer.authRepository) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (showListYourVenue) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = White,
                            modifier = Modifier.size(28.dp).clickable { onBackClick() }
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("List Your Venue", color = White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = White,
                            modifier = Modifier.size(28.dp).clickable { onBackClick() }
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Profile", color = White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { onDebugClick() })
                    }
                }
            }
            if (!showListYourVenue) {
                item { ProfileHeader(name = uiState.userName, email = uiState.userEmail) }
                item { Spacer(modifier = Modifier.height(16.dp)) }
                item { ProfileMenuSection(onMyBookings = onNavigateToMyBookings, onListVenue = onListYourVenueClick) }
                item {
                    Button(
                        onClick = { viewModel.logout(onComplete = onLogout) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4444)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Logout", color = White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                item { ListYourVenueContent(onBack = onBackClick) }
            }
            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

@Composable
private fun ProfileHeader(name: String, email: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(CardBg)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(AccentOrange),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name.take(2).uppercase(),
                color = White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(name, color = White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Text(email, color = Gray, fontSize = 13.sp)
        }
    }
}

@Composable
private fun ProfileMenuSection(
    onMyBookings: () -> Unit,
    onListVenue: () -> Unit
) {
    val items = listOf(
        ProfileMenuItem("My Bookings", Icons.Outlined.ConfirmationNumber) { onMyBookings() },
        ProfileMenuItem("Payment Methods", Icons.Outlined.Payment) {},
        ProfileMenuItem("Notifications", Icons.Outlined.Notifications) {},
        ProfileMenuItem("Help & Support", Icons.Outlined.HelpOutline) {},
        ProfileMenuItem("About", Icons.Outlined.Info) {},
        ProfileMenuItem("List Your Venue", Icons.Outlined.Storefront) { onListVenue() }
    )
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardBg)
                    .clickable { item.onClick() }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(item.icon, contentDescription = null, tint = AccentOrange, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(14.dp))
                Text(item.title, color = White, fontSize = 15.sp, modifier = Modifier.weight(1f))
                Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = Gray, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun ListYourVenueContent(onBack: () -> Unit) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Register your venue on EntryMySlot", color = White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text("Reach more customers and manage bookings online.", color = Gray, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardBg)
                    .padding(vertical = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.SportsSoccer, contentDescription = null, tint = AccentOrange, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Turf", color = White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardBg)
                    .padding(vertical = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.Event, contentDescription = null, tint = AccentOrange, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Event Hall", color = White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        Button(
            onClick = { },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentOrange),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Get Started", color = White, fontWeight = FontWeight.Bold)
        }
    }
}
