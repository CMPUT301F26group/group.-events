package com.example.lottcamp.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.lottcamp.domain.UserRole
import com.example.lottcamp.ui.components.AppAvatar
import com.example.lottcamp.ui.components.AppChipRow
import com.example.lottcamp.ui.components.AppTextField

object RegistrationTestTags {
    const val PROFILE_PHOTO_PICKER = "registration_profile_photo_picker"
    const val NAME_FIELD = "registration_name_field"
    const val PHONE_FIELD = "registration_phone_field"
    const val EMAIL_FIELD = "registration_email_field"
    const val OPT_OUT_NOTIFICATIONS_SWITCH = "registration_opt_out_notifications_switch"
    const val CREATE_ACCOUNT_BUTTON = "registration_create_account_button"

    fun roleChip(role: UserRole) = "registration_role_chip_${role.name.lowercase()}"
}

/**
 * Callback-driven registration screen for creating a user account and selecting an initial role.
 */
@Composable
fun RegistrationScreen(
    name: String,
    onNameChange: (String) -> Unit,
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    optOutOfNotifications: Boolean,
    onOptOutOfNotificationsChange: (Boolean) -> Unit,
    profilePictureUrl: String?,
    onProfilePhotoSelected: (String?) -> Unit,
    selectedRole: UserRole,
    onRoleSelect: (UserRole) -> Unit,
    onCreateAccountClick: () -> Unit,
    modifier: Modifier = Modifier,
    nameError: String? = null,
    submitError: String? = null,
    isSubmitting: Boolean = false,
) {
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) {
            onProfilePhotoSelected(uri.toString())
        }
    }

    val roles = UserRole.entries
    val selectedIndex = roles.indexOf(selectedRole).coerceAtLeast(0)

    Scaffold(
        modifier = modifier.fillMaxSize(),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Create Account",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = "Set up your LottCamp profile to get started.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .clickable(enabled = !isSubmitting) {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                        .testTag(RegistrationTestTags.PROFILE_PHOTO_PICKER),
                    contentAlignment = Alignment.BottomEnd,
                ) {
                    AppAvatar(
                        name = name.ifBlank { "LottCamp User" },
                        size = 76.dp,
                        imageUrl = profilePictureUrl,
                    )
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CameraAlt,
                            contentDescription = "Choose profile photo",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(14.dp),
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Profile Picture (Optional)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = if (profilePictureUrl != null) {
                            "Photo selected. Tap avatar to change."
                        } else {
                            "Tap the photo icon to pick from your gallery."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            AppTextField(
                value = name,
                onValueChange = onNameChange,
                label = "Name *",
                placeholder = "Enter your full name",
                supportingText = nameError,
                isError = nameError != null,
                modifier = Modifier.testTag(RegistrationTestTags.NAME_FIELD),
            )

            AppTextField(
                value = phoneNumber,
                onValueChange = onPhoneNumberChange,
                label = "Phone Number (Optional)",
                placeholder = "e.g. 780-555-0199",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.testTag(RegistrationTestTags.PHONE_FIELD),
            )

            AppTextField(
                value = email,
                onValueChange = onEmailChange,
                label = "Email (Optional)",
                placeholder = "you@example.com",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.testTag(RegistrationTestTags.EMAIL_FIELD),
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "REGISTER AS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                AppChipRow(
                    labels = roles.map { it.label },
                    selected = selectedIndex,
                    onSelect = { index -> onRoleSelect(roles[index]) },
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .toggleable(
                        value = optOutOfNotifications,
                        enabled = !isSubmitting,
                        role = Role.Switch,
                        onValueChange = onOptOutOfNotificationsChange,
                    )
                    .padding(vertical = 4.dp)
                    .testTag(RegistrationTestTags.OPT_OUT_NOTIFICATIONS_SWITCH),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Opt out of notifications",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f),
                )
                Switch(
                    checked = optOutOfNotifications,
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

            if (submitError != null) {
                Text(
                    text = submitError,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Spacer(Modifier.height(4.dp))

            Button(
                onClick = onCreateAccountClick,
                enabled = !isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag(RegistrationTestTags.CREATE_ACCOUNT_BUTTON),
            ) {
                Text(if (isSubmitting) "Creating Account..." else "Create Account")
            }
        }
    }
}