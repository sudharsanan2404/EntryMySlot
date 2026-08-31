package com.entrymyslot.app.screens.profile

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Payment
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.entrymyslot.app.core.components.EntryBottomNavigation
import com.entrymyslot.app.screens.home.GlowBackground

private val ProfileBackground = Color(0xFF061A38)
private val ProfileSurface = Color(0xFF0B274F)
private val ProfileSurfaceRaised = Color(0xFF0D2D5A)
private val ProfileBorder = Color(0xFF24527D)
private val ProfileAccent = Color(0xFFFA580B)
private val ProfilePrimaryText = Color(0xFFF8FAFF)
private val ProfileSecondaryText = Color(0xFFA8B8CF)
private val ProfileMutedText = Color(0xFF7185A1)
private val ProfileDestructive = Color(0xFFE17272)

@Composable
fun ProfileScreen(
    onBottomNavigationClick: (String) -> Unit = {},
    onBookingClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
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
                contentPadding = PaddingValues(top = 4.dp, bottom = 18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item { ProfileHeaderSection() }
                item { StatisticsRow() }
                item { QuickActionsSection(onBookingClick = onBookingClick) }
                item { AccountSettingsSection() }
                item { PartnerSection() }
                item { LogoutButton(onClick = onLogoutClick) }
            }

            EntryBottomNavigation(
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
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(ProfileAccent.copy(alpha = 0.14f))
                .border(
                    BorderStroke(1.5.dp, ProfileAccent.copy(alpha = 0.86f)),
                    CircleShape
                )
                .padding(5.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(ProfileSurfaceRaised, ProfileSurface)
                        )
                    )
                    .border(
                        BorderStroke(1.dp, ProfileBorder.copy(alpha = 0.75f)),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = "Profile",
                    tint = ProfilePrimaryText.copy(alpha = 0.88f),
                    modifier = Modifier.size(35.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Navaneethan",
            color = ProfilePrimaryText,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "navaneethan@email.com",
            color = ProfileSecondaryText,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun StatisticsRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(ProfileSurface.copy(alpha = 0.86f))
            .border(
                BorderStroke(1.dp, ProfileBorder.copy(alpha = 0.78f)),
                RoundedCornerShape(15.dp)
            )
            .padding(vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatItem("Bookings", "12", Modifier.weight(1f))
        StatDivider()
        StatItem("Upcoming", "3", Modifier.weight(1f), isHighlight = true)
        StatDivider()
        StatItem("Completed", "9", Modifier.weight(1f))
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    isHighlight: Boolean = false
) {
    Column(
        modifier = modifier.semantics {
            contentDescription = "$value $label"
        },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            color = if (isHighlight) ProfileAccent else ProfilePrimaryText,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.height(1.dp))
        Text(
            text = label,
            color = ProfileSecondaryText,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}

@Composable
private fun StatDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(30.dp)
            .background(ProfileBorder.copy(alpha = 0.62f))
    )
}

@Composable
private fun QuickActionsSection(onBookingClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Quick access",
            color = ProfileMutedText,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp,
            modifier = Modifier.padding(start = 2.dp, bottom = 7.dp)
        )
        QuickActionCard(
            title = "My Bookings",
            icon = Icons.Outlined.ConfirmationNumber,
            onClick = onBookingClick
        )
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.988f else 1f,
        animationSpec = tween(durationMillis = 110),
        label = "quickActionScale"
    )
    val color by animateColorAsState(
        targetValue = if (isPressed) ProfileSurfaceRaised else ProfileSurface,
        animationSpec = tween(durationMillis = 110),
        label = "quickActionColor"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(14.dp),
                ambientColor = Color.Black.copy(alpha = 0.16f),
                spotColor = Color.Black.copy(alpha = 0.22f)
            )
            .clip(RoundedCornerShape(14.dp))
            .background(color)
            .border(
                BorderStroke(1.dp, ProfileBorder.copy(alpha = 0.78f)),
                RoundedCornerShape(14.dp)
            )
            .semantics {
                contentDescription = title
                role = Role.Button
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClickLabel = title,
                onClick = onClick
            )
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = ProfileAccent,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(11.dp))
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            color = ProfilePrimaryText,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
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
            color = ProfilePrimaryText,
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(start = 2.dp, bottom = 8.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(15.dp))
                .background(ProfileSurface.copy(alpha = 0.88f))
                .border(
                    BorderStroke(1.dp, ProfileBorder.copy(alpha = 0.76f)),
                    RoundedCornerShape(15.dp)
                )
        ) {
            SettingsItem(
                icon = Icons.Outlined.Person,
                title = "Personal Information",
                onClick = {}
            )
            SettingsDivider()
            PersonalInformationDetails()
        }
    }
}

@Composable
private fun PersonalInformationDetails() {
    val details = listOf(
        "Full Name" to "Navaneethan",
        "Email" to "navaneethan@email.com",
        "Phone" to "+91 98765 43210",
        "City" to "Chennai",
        "Member Since" to "August 2026"
    )
    Column(Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
        details.forEach { (label, value) ->
            Row(Modifier.fillMaxWidth().padding(vertical = 7.dp)) {
                Text(label, color = ProfileMutedText, fontSize = 12.sp, modifier = Modifier.weight(1f))
                Text(value, color = ProfilePrimaryText, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun PartnerSection() {
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(18.dp)).background(ProfileSurface)
            .border(1.dp, ProfileBorder, RoundedCornerShape(18.dp)).padding(16.dp)
    ) {
        Text("List Your Turf or Venue", color = ProfilePrimaryText, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        Text(
            "Reach more customers by partnering with EntryMySlot.",
            color = ProfileSecondaryText,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
        )
        Button(
            onClick = { },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = ProfileAccent)
        ) { Text("Partner With Us", color = Color.White, fontWeight = FontWeight.Bold) }
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val backgroundColor by animateColorAsState(
        targetValue = if (isPressed) {
            ProfileSurfaceRaised.copy(alpha = 0.92f)
        } else {
            Color.Transparent
        },
        animationSpec = tween(durationMillis = 100),
        label = "settingsRowColor"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(backgroundColor)
            .semantics {
                contentDescription = title
                role = Role.Button
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClickLabel = title,
                onClick = onClick
            )
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = ProfileAccent,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(11.dp))
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            color = ProfilePrimaryText,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SettingsDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 55.dp, end = 14.dp)
            .height(1.dp)
            .background(ProfileBorder.copy(alpha = 0.34f))
    )
}

@Composable
private fun LogoutButton(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.985f else 1f,
        animationSpec = tween(durationMillis = 110),
        label = "logoutScale"
    )
    val backgroundColor by animateColorAsState(
        targetValue = if (isPressed) {
            ProfileDestructive.copy(alpha = 0.12f)
        } else {
            ProfileSurface.copy(alpha = 0.54f)
        },
        animationSpec = tween(durationMillis = 110),
        label = "logoutColor"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(48.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(
                BorderStroke(1.dp, ProfileDestructive.copy(alpha = 0.42f)),
                RoundedCornerShape(12.dp)
            )
            .semantics {
                contentDescription = "Log Out"
                role = Role.Button
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClickLabel = "Log Out",
                onClick = onClick
            ),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Logout,
            contentDescription = null,
            tint = ProfileDestructive,
            modifier = Modifier.size(17.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Log Out",
            color = ProfileDestructive,
            fontSize = 13.sp,
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
                .background(ProfileSurface.copy(alpha = 0.96f))
                .border(
                    BorderStroke(1.dp, ProfileBorder.copy(alpha = 0.78f)),
                    RoundedCornerShape(18.dp)
                )
                .padding(horizontal = 5.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                ProfileNavigationItem(
                    label = item.first,
                    unselectedIcon = item.second,
                    selectedIcon = item.third,
                    isSelected = selectedItem == item.first,
                    onClick = { onItemSelected(item.first) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ProfileNavigationItem(
    label: String,
    unselectedIcon: ImageVector,
    selectedIcon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "profileNavScale"
    )
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) ProfileAccent.copy(alpha = 0.15f) else Color.Transparent,
        animationSpec = tween(durationMillis = 160),
        label = "profileNavColor"
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
                contentDescription = label
                selected = isSelected
                stateDescription = if (isSelected) "Selected" else "Not selected"
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
            imageVector = if (isSelected) selectedIcon else unselectedIcon,
            contentDescription = null,
            tint = if (isSelected) ProfileAccent else ProfileMutedText,
            modifier = Modifier.size(19.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = if (isSelected) ProfileAccent else ProfileMutedText,
            fontSize = 8.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
