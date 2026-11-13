package com.example.lottcamp.ui.screens.owner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cabin
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lottcamp.ui.components.AppChip
import com.example.lottcamp.ui.theme.LottCampTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.lottcamp.domain.Campsite
import com.example.lottcamp.ui.components.AppButton
import com.example.lottcamp.ui.components.AppNavBar
import com.example.lottcamp.ui.components.AppNavItem


@Composable
fun MyCampsiteScreen(
    campsites: List<Campsite>,
) {
    var selectedRole by remember { mutableStateOf("Owner") }
    var roleMenuOpen by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf("Explore") }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            AppNavBar {
                AppNavItem(
                    selected = selectedTab == "Explore",
                    onClick = { selectedTab = "Explore" },
                    label = "Explore",
                    icon = Icons.Outlined.Home,
                )
                AppNavItem(
                    selected = selectedTab == "Bookings",
                    onClick = { selectedTab = "Bookings" },
                    label = "Bookings",
                    icon = Icons.Outlined.CalendarMonth,
                )
                AppNavItem(
                    selected = selectedTab == "Messages",
                    onClick = { selectedTab = "Messages" },
                    label = "Messages",
                    icon = Icons.Outlined.ChatBubbleOutline,
                )
                AppNavItem(
                    selected = selectedTab == "Profile",
                    onClick = { selectedTab = "Profile" },
                    label = "Profile",
                    icon = Icons.Outlined.Person,
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(start = 16.dp, end = 16.dp, top = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.primary)
                )
                Text(
                    text = "Campsite App",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )
                Box {
                    AppChip(
                        text = "$selectedRole ▾",
                        selected = false,
                        onClick = { roleMenuOpen = true },
                    )
                    DropdownMenu(
                        expanded = roleMenuOpen,
                        onDismissRequest = { roleMenuOpen = false }
                    ) {
                        listOf("Owner", "Admin", "Entrant").forEach { role ->
                            DropdownMenuItem(
                                text = { Text(role) },
                                onClick = {
                                    selectedRole = role
                                    roleMenuOpen = false
                                }
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "My campsites",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${campsites.size} listings",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(20.dp))

            AppButton(
                text = "Register campsite",
                onClick = { /* TODO */ },
                modifier = Modifier.fillMaxWidth()
            )


        }
    }

}

@Preview(showBackground = true)
@Composable
fun MyCampsiteScreenPreview() {
    LottCampTheme {
        MyCampsiteScreen(campsites = emptyList())
    }
}

