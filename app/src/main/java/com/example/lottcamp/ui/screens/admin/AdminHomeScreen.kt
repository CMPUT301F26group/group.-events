package com.example.lottcamp.ui.screens.admin

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Notifications
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lottcamp.controller.AdminController
import com.example.lottcamp.controller.AdminHomeController
import com.example.lottcamp.repository.SampleAdminRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import com.example.lottcamp.domain.UserRole
import com.example.lottcamp.ui.components.AppDivider
import com.example.lottcamp.ui.components.AppHeader
import com.example.lottcamp.ui.components.AppImagePlaceholder
import com.example.lottcamp.ui.components.AppListRow
import com.example.lottcamp.ui.components.AppSearchBar
import com.example.lottcamp.ui.components.AppSectionTitle
import com.example.lottcamp.ui.components.AppStoredImage
import com.example.lottcamp.ui.theme.LottCampTheme

/**
 * Admin landing screen: role switcher, search, counts, latest listings and log shortcuts.
 *
 * @param modifier Applied to the scrolling column.
 * @param controller Supplies the data and handles every tap.
 * @param roles Roles the admin can switch to from the header.
 * @param onRoleSelected Called with the role picked in the header.
 */
@Composable
fun AdminHomeScreen(
    modifier: Modifier = Modifier,
    controller: AdminHomeController,
    roles: List<UserRole> = emptyList(),
    onRoleSelected: (UserRole) -> Unit = {},
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
        AppHeader(UserRole.ADMIN, roles, onRoleSelected)

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
            AppSectionTitle("Latest listings", Modifier.weight(1f))
            LinkText("View all", controller::onViewAllListingsClick)
        }

        // listings
        Column {
            controller.latestCampsites.forEachIndexed { index, campsite ->
                if (index > 0) AppDivider(spacing = 12.dp)
                AppListRow(campsite.name, campsite.locationName, { controller.onCampsiteClick(campsite) }) {
                    Box {
                        AppImagePlaceholder()
                        AppStoredImage(
                            imageId = campsite.imageId,
                            imageRepo = controller.imageRepo,
                            contentDescription = campsite.name,
                            modifier = Modifier.matchParentSize().clip(MaterialTheme.shapes.medium),
                        )
                    }
                }
            }
        }

        AppSectionTitle("Logs")
        Column {
            LogRow(Icons.Outlined.Notifications, "Notifications", controller::onNotificationsClick)
            AppDivider(spacing = 16.dp)
            LogRow(Icons.Outlined.ChatBubbleOutline, "Owner messages", controller::onOwnerMessagesClick)
        }
    }
}

@Composable
private fun CampsitesTile(count: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val contentColor = MaterialTheme.colorScheme.onPrimary
    Box(
        modifier = modifier
            .height(200.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.primary)
            .clickable(onClick = onClick)
            .padding(16.dp),
    ) {
        Column(modifier = Modifier.align(Alignment.TopStart)) {
            Text("Campsites", color = contentColor, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("$count listings", color = contentColor)
        }
        Row(
            modifier = Modifier.align(Alignment.BottomStart),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Browse", color = contentColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(4.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = contentColor)
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
        val controller = remember {
            val admin = AdminController(SampleAdminRepository(), CoroutineScope(Dispatchers.Unconfined))
            admin.refresh()
            AdminHomeController(admin)
        }
        AdminHomeScreen(controller = controller)
    }
}
