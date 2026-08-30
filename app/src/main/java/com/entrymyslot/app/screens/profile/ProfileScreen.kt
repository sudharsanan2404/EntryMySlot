package com.entrymyslot.app.screens.profile

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.entrymyslot.app.R
import com.entrymyslot.app.screens.home.GlowBackground

// ------------------------------------------------------------
// COLORS & STYLES (Matching EntryMySlot Identity)
// ------------------------------------------------------------
private val EntryBlueTop = Color(0xFF0B3A82)
private val EntryBlueBottom = Color(0xFF061A33)
private val EntryDarkBlue = Color(0xFF0E0B38).copy(alpha = .68f)
private val EntryOrange = Color(0xFFFF8A3D)
private val EntryWhite = Color.White
private val EntryGray = Color(0xFF98A2B3)
private val EntryCardBg = Color(0xFF0E0B38).copy(alpha = .68f)
private val EntryBorder = Color(0xFF1648D5).copy(alpha = .38f)

// ------------------------------------------------------------
// PROFILE SCREEN
// ------------------------------------------------------------
@Composable
fun ProfileScreen(
    onBottomNavigationClick: (String) -> Unit = {},
    onBookingClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    Box(modifier = Modifier.fillMaxSize()) {
        GlowBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // 1. Profile Header
                item {
                    ProfileHeaderSection()
                }

                // 2. Statistics Row
                item {
                    StatisticsRow()
                }

                // 3. Quick Actions (My Bookings)
                item {
                    QuickActionsSection(onBookingClick)
                }

                // 4. Account Settings
                item {
                    AccountSettingsSection()
                }

                // 5. Logout Button
                item {
                    LogoutButton(onLogoutClick)
                }
            }

            // Bottom Navigation
            ProfileBottomNavigation(
                selectedItem = "Profile",
                onItemSelected = onBottomNavigationClick
            )
        }
    }
}

@Composable
private fun ProfileHeaderSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile Picture Placeholder
        Box(
            modifier = Modifier
                .size(78.dp)
                .clip(CircleShape)
                .background(EntryDarkBlue)
                .border(2.dp, EntryOrange, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = EntryWhite.copy(alpha = 0.6f),
                modifier = Modifier.size(44.dp)
            )
        }

        Spacer(modifier = Modifier.height(9.dp))

        Text(
            text = "Navaneethan",
            color = EntryWhite,
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "navaneethan@email.com",
            color = EntryGray,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = { /* Edit Profile */ },
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = EntryOrange),
            modifier = Modifier.height(36.dp),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 0.dp)
        ) {
            Text("Edit Profile", color = EntryWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun StatisticsRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(EntryCardBg.copy(alpha = 0.5f))
            .border(1.dp, EntryBorder, RoundedCornerShape(16.dp))
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatItem("Bookings", "12")
        StatDivider()
        StatItem("Upcoming", "3", isHighlight = true)
        StatDivider()
        StatItem("Completed", "9")
    }
}

@Composable
private fun StatItem(label: String, value: String, isHighlight: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = if (isHighlight) EntryOrange else EntryWhite,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = EntryGray,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun StatDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(30.dp)
            .background(EntryBorder)
    )
}

@Composable
private fun QuickActionsSection(onBookingClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickActionCard(
            title = "My Bookings",
            icon = Icons.Outlined.ConfirmationNumber,
            modifier = Modifier.fillMaxWidth(),
            onClick = onBookingClick
        )
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(82.dp),
        shape = RoundedCornerShape(16.dp),
        color = EntryCardBg,
        border = BorderStroke(1.dp, EntryBorder)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = EntryOrange,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = title,
                color = EntryWhite,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun AccountSettingsSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Account",
            color = EntryWhite,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = EntryCardBg),
            border = BorderStroke(1.dp, EntryBorder)
        ) {
            Column {
                SettingsItem(Icons.Outlined.Person, "Personal Information")
                SettingsDivider()
                SettingsItem(Icons.Outlined.Payment, "Saved Payments")
                SettingsDivider()
                SettingsItem(Icons.Outlined.Notifications, "Notifications")
                SettingsDivider()
                SettingsItem(Icons.AutoMirrored.Outlined.HelpOutline, "Help & Support")
                SettingsDivider()
                SettingsItem(Icons.Outlined.Description, "Terms & Conditions")
                SettingsDivider()
                SettingsItem(Icons.Outlined.PrivacyTip, "Privacy Policy")
            }
        }
    }
}

@Composable
private fun SettingsItem(icon: ImageVector, title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = EntryOrange, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(title, color = EntryWhite, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, null, tint = EntryGray, modifier = Modifier.size(14.dp))
    }
}

@Composable
private fun SettingsDivider() {
    Box(modifier = Modifier.fillMaxWidth().height(1.dp).padding(horizontal = 16.dp).background(EntryBorder.copy(alpha = 0.2f)))
}

@Composable
private fun LogoutButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .height(48.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = EntryCardBg),
        border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.3f))
    ) {
        Text(
            text = "Log Out",
            color = Color.Red.copy(alpha = 0.8f),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ProfileBottomNavigation(
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
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .navigationBarsPadding()
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF0E0B38).copy(alpha = .86f),
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, Color(0xFF1648D5).copy(alpha = .38f)),
            shadowElevation = 10.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 5.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                items.forEach { item ->
                    val selected = selectedItem == item.first
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selected) EntryOrange.copy(alpha = .16f) else Color.Transparent)
                            .clickable { onItemSelected(item.first) }
                            .padding(vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = if (selected) item.third else item.second,
                            contentDescription = item.first,
                            tint = if (selected) EntryOrange else EntryGray,
                            modifier = Modifier.size(19.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.first,
                            color = if (selected) EntryOrange else EntryGray,
                            fontSize = 8.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
