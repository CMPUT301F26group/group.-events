package com.example.lottcamp.ui.screens.admin

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.lottcamp.controller.AdminController
import com.example.lottcamp.domain.UserRole
import com.example.lottcamp.ui.components.AppAvatar
import com.example.lottcamp.ui.components.AppImagePlaceholder
import com.example.lottcamp.ui.components.AppListRow

/**
 * Details of a user profile, with an option to remove it.
 * Owners get the "Owner details" variant with a link to the campsites.
 *
 * @param controller Supplies the user and handles removal.
 * @param userId User to show.
 * @param onBack Invoked when the back arrow is tapped, and after removal.
 * @param onViewCampsites Invoked by the owner variant's outlined button.
 * @param onViewNotifications Invoked by the entrant/admin variant's outlined button.
 */
@Composable
fun AdminUserDetailScreen(
    controller: AdminController,
    userId: String,
    onBack: () -> Unit,
    onViewCampsites: () -> Unit,
    onViewNotifications: () -> Unit,
) {
    val user = controller.user(userId) ?: return
    val isOwner = user.role == UserRole.OWNER

    AdminDetailPage(
        title = if (isOwner) "Owner details" else "User details",
        onBack = onBack,
        sections = listOf(
            DetailSection(
                "Personal information",
                listOf("Email" to user.email, "Phone" to user.phone, "Joined" to user.joined),
            ),
            DetailSection("Activity", controller.userActivity(user)),
        ),
        secondaryText = if (isOwner) "View campsites" else "View notification logs",
        onSecondary = if (isOwner) onViewCampsites else onViewNotifications,
        removeText = if (isOwner) "Remove owner" else "Remove profile",
        confirmTitle = "Remove profile?",
        confirmMessage = "${user.name}'s profile will be permanently removed. This can't be undone.",
        onRemove = {
            controller.removeUser(user.id)
            onBack()
        },
    ) {
        AppListRow(user.name, "${user.role.label} · ${user.id}") { AppAvatar(user.name, 56.dp, user.photoUrl) }
    }
}

/**
 * Details of a campsite, with an option to remove it.
 *
 * @param controller Supplies the campsite and handles removal.
 * @param campsiteId Campsite to show.
 * @param onBack Invoked when the back arrow is tapped, and after removal.
 * @param onViewReviews Invoked by the "View reviews & ratings" button.
 */
@Composable
fun AdminCampsiteDetailScreen(
    controller: AdminController,
    campsiteId: String,
    onBack: () -> Unit,
    onViewReviews: () -> Unit,
) {
    val campsite = controller.campsite(campsiteId) ?: return

    AdminDetailPage(
        title = "Campsite details",
        onBack = onBack,
        sections = listOf(
            DetailSection(
                "Campsite information",
                listOf(
                    "Location" to campsite.locationName,
                    "Campsite ID" to campsite.id,
                    "Owner" to controller.ownerName(campsite),
                    "Status" to if (controller.isRemoved(campsite.id)) "Removed" else "Active",
                ),
            ),
            DetailSection("Activity", controller.campsiteActivity(campsite)),
        ),
        secondaryText = "View reviews & ratings",
        onSecondary = onViewReviews,
        removeText = "Remove campsite",
        confirmTitle = "Remove campsite?",
        confirmMessage = "${campsite.name} will be removed from the app.",
        onRemove = {
            controller.removeCampsite(campsite.id)
            onBack()
        },
    ) {
        AppListRow(campsite.name, campsite.locationName) { AppImagePlaceholder() }
    }
}

/**
 * Details of an uploaded image, with an option to remove it.
 *
 * @param controller Supplies the image and handles removal.
 * @param imageId Image to show.
 * @param onBack Invoked when the back arrow is tapped, and after removal.
 * @param onViewUploader Invoked with the uploader's user ID by the "View uploader profile" button.
 */
@Composable
fun AdminImageDetailScreen(
    controller: AdminController,
    imageId: String,
    onBack: () -> Unit,
    onViewUploader: (String) -> Unit,
) {
    val image = controller.image(imageId) ?: return

    AdminDetailPage(
        title = "Image details",
        onBack = onBack,
        sections = listOf(
            DetailSection(
                null,
                listOf(
                    "Image ID" to image.id,
                    "Uploaded by" to image.uploaderName,
                    "Type" to image.type.label,
                    "Uploaded" to image.uploaded,
                ),
            ),
        ),
        secondaryText = "View uploader profile",
        onSecondary = { onViewUploader(image.uploaderId) },
        removeText = "Remove image",
        confirmTitle = "Remove image?",
        confirmMessage = "This image will be permanently removed from the app.",
        onRemove = {
            controller.removeImage(image.id)
            onBack()
        },
    ) {
        AppImagePlaceholder(Modifier.fillMaxWidth().height(200.dp))
        Text(image.title, style = MaterialTheme.typography.titleMedium)
    }
}
