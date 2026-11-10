package com.example.lottcamp.ui.screens.admin

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lottcamp.controller.AdminHomeController
import com.example.lottcamp.domain.Campsite
import com.example.lottcamp.ui.components.AppChip
import com.example.lottcamp.ui.components.AppChipVariant
import com.example.lottcamp.ui.components.AppSearchBar
import com.example.lottcamp.ui.theme.LottCampTheme

@Composable
fun AdminHomeScreen(
    modifier: Modifier = Modifier,
    controller: AdminHomeController = remember { AdminHomeController() },
) {
    var query by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Header()

        AppSearchBar(
            query = query,
            onQueryChange = {
                query = it
                controller.onSearch(it)
            },
            placeholder = "Search users, campsites, images...",
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CampsitesTile(
                count = controller.campsiteCount,
                onClick = controller::onBrowseCampsitesClick,
                modifier = Modifier.weight(2f),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                StatTile(
                    number = controller.userCount,
                    label = "Users",
                    icon = Icons.Outlined.Group,
                    onClick = controller::onUsersClick,
                )
                StatTile(
                    number = controller.imageCount,
                    label = "Images",
                    icon = Icons.Outlined.Image,
                    onClick = controller::onImagesClick,
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SectionTitle("Latest listings", Modifier.weight(1f))
            LinkText("View all", controller::onViewAllListingsClick)
        }

        // listings
        Column {
            controller.latestCampsites.forEachIndexed { index, campsite ->
                if (index > 0) RowDivider(spacing = 12.dp)
                ListingRow(campsite, onClick = { controller.onCampsiteClick(campsite) })
            }
        }

        SectionTitle("Logs")
        Column {
            LogRow(Icons.Outlined.Notifications, "Notifications", controller::onNotificationsClick)
            RowDivider(spacing = 16.dp)
            LogRow(Icons.Outlined.ChatBubbleOutline, "Owner messages", controller::onOwnerMessagesClick)
        }
    }
}

@Composable
private fun Header() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.primary)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = "Campsite App",
            fontSize = 28.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
        )
        AppChip(
            text = "Admin ▾",
            selected = false,
            onClick = {},
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
            variant = AppChipVariant.Secondary,
        )
    }
}

@Composable
private fun CampsitesTile(count: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(200.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(Color(0xFF1F2A33))
            .clickable(onClick = onClick)
            .padding(16.dp),
    ) {
        Column(modifier = Modifier.align(Alignment.TopStart)) {
            Text("Campsites", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("$count listings", color = Color.White)
        }
        Row(
            modifier = Modifier.align(Alignment.BottomStart),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Browse", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(4.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White)
        }
    }
}

@Composable
private fun StatTile(number: Int, label: String, icon: ImageVector, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(94.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.secondary)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = number.toString(),
            fontSize = 32.sp,
            color = MaterialTheme.colorScheme.primary,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(6.dp))
            Text(label, maxLines = 1, softWrap = false)
        }
    }
}

@Composable
private fun ListingRow(campsite: Campsite, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            // Image needs to go here
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(campsite.name, style = MaterialTheme.typography.titleMedium)
            Text(campsite.locationName, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** Divider between list rows. [spacing] is the gap above and below the line. */
@Composable
private fun RowDivider(spacing: Dp) {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = spacing),
        color = MaterialTheme.colorScheme.outline,
    )
}

@Composable
private fun LogRow(icon: ImageVector, title: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null)
        Spacer(Modifier.width(16.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        LinkText("View", onClick, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(text, style = MaterialTheme.typography.titleLarge, modifier = modifier)
}

@Composable
private fun LinkText(
    text: String,
    onClick: () -> Unit,
    color: Color = MaterialTheme.colorScheme.onSurface,
) {
    Row(
        modifier = Modifier.clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text, color = color)
        Spacer(Modifier.width(4.dp))
        Icon(
            Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp),
        )
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
fun AdminHomeScreenPreview() {
    LottCampTheme {
        AdminHomeScreen()
    }
}
