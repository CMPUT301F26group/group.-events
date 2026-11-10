package com.example.lottcamp.ui.screens.admin

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.lottcamp.controller.AdminController
import com.example.lottcamp.domain.Campsite
import com.example.lottcamp.domain.ImageType
import com.example.lottcamp.domain.UploadedImage
import com.example.lottcamp.domain.UserProfile
import com.example.lottcamp.domain.UserRole
import com.example.lottcamp.ui.components.AppAvatar
import com.example.lottcamp.ui.components.AppImagePlaceholder
import com.example.lottcamp.ui.components.AppListRow
import com.example.lottcamp.ui.components.AppMutedText
import com.example.lottcamp.ui.components.AppRatingBar

private val userFilters = listOf("All users" to null, "Entrants" to UserRole.ENTRANT, "Owners" to UserRole.OWNER, "Admins" to UserRole.ADMIN)
private val campsiteFilters = listOf("All campsites" to null, "Active" to false, "Removed" to true)
private val imageFilters = listOf("All images" to null, "Campsites" to ImageType.CAMPSITE, "Profiles" to ImageType.PROFILE)
private val notificationFilters = listOf("All" to null, "System" to true, "Owner" to false)

/**
 * Lets an admin search and browse every user profile (US 03.05.01).
 *
 * @param controller Supplies the users.
 * @param onBack Invoked when the back arrow is tapped.
 * @param onUserClick Invoked with the tapped user.
 */
@Composable
fun AdminUsersScreen(controller: AdminController, onBack: () -> Unit, onUserClick: (UserProfile) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableIntStateOf(0) }
    val users = controller.searchUsers(query, userFilters[filter].second)

    AdminListPage(
        title = "Search users",
        onBack = onBack,
        query = query,
        onQueryChange = { query = it },
        placeholder = "Search name, email or user ID",
        filters = userFilters.map { it.first },
        selectedFilter = filter,
        onFilterSelect = { filter = it },
        sectionTitle = "Users",
        countText = "${users.size} user${if (users.size == 1) "" else "s"}",
    ) {
        items(users, key = { it.id }) { user ->
            AppListRow(user.name, user.email, { onUserClick(user) }, trailing = user.role.label) {
                AppAvatar(user.name, imageUrl = user.photoUrl)
            }
        }
    }
}

/**
 * Lets an admin search and browse every campsite, including removed ones (US 03.04.01).
 *
 * @param controller Supplies the campsites.
 * @param onBack Invoked when the back arrow is tapped.
 * @param onCampsiteClick Invoked with the tapped campsite.
 */
@Composable
fun AdminCampsitesScreen(controller: AdminController, onBack: () -> Unit, onCampsiteClick: (Campsite) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableIntStateOf(0) }
    val campsites = controller.searchCampsites(query, campsiteFilters[filter].second)

    AdminListPage(
        title = "Search campsites",
        onBack = onBack,
        query = query,
        onQueryChange = { query = it },
        placeholder = "Search name, location or campsite ID",
        filters = campsiteFilters.map { it.first },
        selectedFilter = filter,
        onFilterSelect = { filter = it },
        sectionTitle = "Campsites",
        countText = "${campsites.size} campsite${if (campsites.size == 1) "" else "s"}",
    ) {
        items(campsites, key = { it.id }) { campsite ->
            AppListRow(campsite.name, "${campsite.locationName} · ${campsite.id}", { onCampsiteClick(campsite) }) {
                AppImagePlaceholder()
            }
        }
    }
}

/**
 * Lets an admin browse uploaded images so they can be removed.
 *
 * @param controller Supplies the images.
 * @param onBack Invoked when the back arrow is tapped.
 * @param onImageClick Invoked with the tapped image.
 */
@Composable
fun AdminImagesScreen(controller: AdminController, onBack: () -> Unit, onImageClick: (UploadedImage) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableIntStateOf(0) }
    val images = controller.searchImages(query, imageFilters[filter].second)

    AdminListPage(
        title = "Search images",
        onBack = onBack,
        query = query,
        onQueryChange = { query = it },
        placeholder = "Search uploader, campsite or image ID",
        filters = imageFilters.map { it.first },
        selectedFilter = filter,
        onFilterSelect = { filter = it },
        sectionTitle = "Uploaded images",
        countText = "${images.size} shown",
    ) {
        // Two images per row
        items(images.chunked(2)) { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { image ->
                    Column(Modifier.weight(1f).clickable { onImageClick(image) }) {
                        AppImagePlaceholder(Modifier.fillMaxWidth().aspectRatio(1f))
                        Text(image.title, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 4.dp))
                        AppMutedText("${image.uploaderName} · ${image.type.label}")
                    }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

/**
 * Lets an admin review the log of every notification sent.
 *
 * @param controller Supplies the notification log.
 * @param onBack Invoked when the back arrow is tapped.
 * @param initialQuery Text the search bar starts with, e.g. a user ID to show only their notifications.
 */
@Composable
fun AdminNotificationsScreen(controller: AdminController, onBack: () -> Unit, initialQuery: String = "") {
    var query by rememberSaveable { mutableStateOf(initialQuery) }
    var filter by rememberSaveable { mutableIntStateOf(0) }
    val logs = controller.searchNotifications(query, notificationFilters[filter].second)

    AdminListPage(
        title = "Notification logs",
        onBack = onBack,
        query = query,
        onQueryChange = { query = it },
        placeholder = "Search recipient, sender or notification",
        filters = notificationFilters.map { it.first },
        selectedFilter = filter,
        onFilterSelect = { filter = it },
    ) {
        items(logs, key = { it.id }) { log ->
            LogEntry(log.title, "${log.sender} → ${log.recipient}", log.message, log.time, status = log.status)
            // Gap above the line matches the list's 16dp gap below it
            HorizontalDivider(Modifier.padding(top = 16.dp), color = MaterialTheme.colorScheme.outline)
        }
    }
}

/**
 * Lets an admin review messages owners sent to entrants with reservations.
 *
 * @param controller Supplies the message log.
 * @param onBack Invoked when the back arrow is tapped.
 */
@Composable
fun AdminMessagesScreen(controller: AdminController, onBack: () -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    val logs = controller.searchMessages(query)

    AdminListPage(
        title = "Message log",
        onBack = onBack,
        query = query,
        onQueryChange = { query = it },
        placeholder = "Search sender, recipient or campsite",
    ) {
        items(logs, key = { it.id }) { log ->
            LogEntry(
                title = "${log.sender} → ${log.recipient}",
                subtitle = "${log.campsiteName} · ${log.reservationId}",
                body = log.message,
                time = log.time,
                boxed = true,
            )
        }
    }
}

/**
 * Lets an admin read a campsite's reviews and remove ones that break policy.
 *
 * @param controller Supplies the reviews and handles removal.
 * @param campsiteId Campsite whose reviews are shown.
 * @param onBack Invoked when the back arrow is tapped.
 */
@Composable
fun AdminReviewsScreen(controller: AdminController, campsiteId: String, onBack: () -> Unit) {
    val campsite = controller.campsite(campsiteId) ?: return
    val rating = controller.averageRating(campsiteId)

    AdminListPage(title = "Reviews & ratings", onBack = onBack) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(campsite.name, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.width(8.dp))
                AppMutedText("%.1f".format(rating))
                Spacer(Modifier.width(4.dp))
                AppRatingBar(rating.toFloat(), starSize = 16.dp)
            }
        }
        items(controller.reviewsFor(campsiteId), key = { it.id }) { review ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.medium)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AppAvatar(review.authorName)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(review.authorName, style = MaterialTheme.typography.titleSmall)
                        AppRatingBar(review.rating.toFloat(), starSize = 16.dp)
                    }
                }
                AppMutedText(review.text)
                Text(
                    text = "Remove review",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.clickable { controller.removeReview(review.id) },
                )
            }
        }
    }
}
