package com.example.lottcamp.ui.screens

import android.annotation.SuppressLint
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.lottcamp.controller.ProfileController
import com.example.lottcamp.repository.CampsiteOwnerRepository
import com.example.lottcamp.repository.EntrantRepository
import com.example.lottcamp.repository.ImageRepository
import com.example.lottcamp.ui.components.AppAvatar
import com.example.lottcamp.ui.components.AppButton
import com.example.lottcamp.ui.components.AppButtonVariant
import com.example.lottcamp.ui.components.AppDialog
import com.example.lottcamp.ui.components.AppSectionTitle
import com.example.lottcamp.ui.components.AppStoredImage
import com.example.lottcamp.ui.components.AppTextField
import com.example.lottcamp.ui.components.AppTopBar

/** Shared navigation routes for the profile and policies screens. */
object ProfileRoutes {
    const val PROFILE = "profile"
    const val POLICIES = "profile/policies"

    fun isProfileRoute(route: String?) = route == PROFILE || route == POLICIES
}

/** Registers the shared profile and policies screens in a navigation graph. */
fun NavGraphBuilder.profileRoutes(
    navController: NavController,
    userId: String? = null,
    entrantRepo: EntrantRepository? = null,
    ownerRepo: CampsiteOwnerRepository? = null,
    imageRepo: ImageRepository? = null,
    syncOwner: Boolean = false,
    modifier: Modifier = Modifier,
) {
    composable(ProfileRoutes.PROFILE) {
        ProfileScreen(
            userId = userId,
            entrantRepo = entrantRepo,
            ownerRepo = ownerRepo,
            imageRepo = imageRepo,
            syncOwner = syncOwner,
            onOpenPolicies = { navController.navigate(ProfileRoutes.POLICIES) },
            modifier = modifier,
        )
    }
    composable(ProfileRoutes.POLICIES) {
        PoliciesScreen(
            onBack = { navController.popBackStack() },
            modifier = modifier,
        )
    }
}

/** Shared profile screen for entrant, owner, and admin roles. */
@SuppressLint("HardwareIds")
@Composable
fun ProfileScreen(
    userId: String? = null,
    entrantRepo: EntrantRepository? = null,
    ownerRepo: CampsiteOwnerRepository? = null,
    imageRepo: ImageRepository? = null,
    syncOwner: Boolean = false,
    onOpenPolicies: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val resolvedId = remember(userId) {
        userId
            ?: Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            ?: ""
    }
    val controller = remember(resolvedId, scope, entrantRepo, ownerRepo, imageRepo, syncOwner) {
        ProfileController(
            userId = resolvedId,
            scope = scope,
            entrantRepo = entrantRepo ?: EntrantRepository(),
            ownerRepo = ownerRepo,
            imageRepo = imageRepo ?: ImageRepository(),
            syncOwner = syncOwner,
            onOpenPolicies = onOpenPolicies,
        )
    }

    LaunchedEffect(controller) { controller.refresh() }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) controller.onPhotoPicked(context, uri)
    }

    val displayName = controller.name.trim().ifEmpty { "New profile" }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(16.dp))

            Text(
                text = "Profile",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(68.dp)) {
                    AppAvatar(name = displayName.uppercase(), size = 68.dp)
                    AppStoredImage(
                        imageId = controller.profilePictureId,
                        imageRepo = controller.imageRepo,
                        contentDescription = "Profile photo",
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                    )
                }

                Spacer(Modifier.width(16.dp))

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(
                        text = "Identified by this device",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = if (controller.isUploadingPhoto) "Uploading..." else "Change photo",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable(enabled = !controller.isUploadingPhoto) {
                                photoPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        )
                        Text(
                            text = "Remove photo",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.clickable(
                                enabled = !controller.isUploadingPhoto,
                                onClick = controller::onRemovePhotoClick,
                            ),
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            AppTextField(
                value = controller.name,
                onValueChange = controller::onNameChange,
                label = "Name",
                placeholder = "Full name",
                supportingText = controller.nameError,
                isError = controller.nameError != null,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
            )

            Spacer(Modifier.height(20.dp))

            AppTextField(
                value = controller.email,
                onValueChange = controller::onEmailChange,
                label = "Email",
                placeholder = "name@example.com",
                supportingText = controller.emailError,
                isError = controller.emailError != null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            )

            Spacer(Modifier.height(20.dp))

            AppTextField(
                value = controller.phoneNumber,
                onValueChange = controller::onPhoneNumberChange,
                label = "Phone",
                placeholder = "780-555-0142",
                supportingText = controller.phoneError,
                isError = controller.phoneError != null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            )

            Spacer(Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .toggleable(
                        value = controller.notificationsEnabled,
                        role = Role.Switch,
                        onValueChange = controller::onNotificationsChange,
                    )
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Lottery and booking notifications",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f),
                )
                Switch(
                    checked = controller.notificationsEnabled,
                    onCheckedChange = null,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        checkedBorderColor = Color.Transparent,
                        uncheckedThumbColor = MaterialTheme.colorScheme.surface,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                        uncheckedBorderColor = MaterialTheme.colorScheme.outline,
                    ),
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = controller::onPoliciesClick)
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Policies & guidelines",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            controller.statusMessage?.let { message ->
                Spacer(Modifier.height(8.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Spacer(Modifier.height(16.dp))
        }

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AppButton(
                text = if (controller.isSaving) "Saving..." else "Save changes",
                onClick = controller::onSaveClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = !controller.isSaving,
            )
            AppButton(
                text = "Delete profile",
                onClick = controller::onDeleteClick,
                modifier = Modifier.fillMaxWidth(),
                variant = AppButtonVariant.Danger,
                enabled = !controller.isSaving,
            )
        }
    }

    if (controller.showDeleteDialog) {
        AppDialog(
            title = "Delete profile?",
            message = "Your profile and personal details will be permanently removed. This can't be undone.",
            dismissText = "Cancel",
            onDismiss = controller::dismissDeleteDialog,
            confirmText = "Delete profile",
            onConfirm = controller::confirmDeleteProfile,
        )
    }

    controller.error?.let { message ->
        AlertDialog(
            onDismissRequest = controller::clearError,
            confirmButton = { TextButton(onClick = controller::clearError) { Text("OK") } },
            title = { Text("Something went wrong") },
            text = { Text(message) },
        )
    }
}

/** Policies and guidelines page opened from [ProfileScreen]. */
@Composable
fun PoliciesScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        AppTopBar(title = "Policies & guidelines", onBack = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            PolicySection(
                title = "very cool section to do later",
                body = "something something blah blah blah no one reads the policies anyway"
            )
        }
    }
}

@Composable
private fun PolicySection(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        AppSectionTitle(title)
        Text(
            text = body,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
