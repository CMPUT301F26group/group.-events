package com.example.lottcamp

import android.annotation.SuppressLint
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.example.lottcamp.controller.RegistrationController
import com.example.lottcamp.domain.UserRole
import com.example.lottcamp.ui.screens.RegistrationScreen
import com.example.lottcamp.ui.screens.admin.AdminNavGraph
import com.example.lottcamp.ui.screens.entrant.EntrantNavGraph
import com.example.lottcamp.ui.screens.owner.OwnerNavGraph
import com.example.lottcamp.ui.theme.LottCampTheme

class MainActivity : ComponentActivity() {
    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LottCampTheme {
                val androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
                val scope = rememberCoroutineScope()
                var availableRoles by remember { mutableStateOf<List<UserRole>>(emptyList()) }
                var activeRole by rememberSaveable { mutableStateOf<UserRole?>(null) }

                val registrationController = remember(androidId, scope) {
                    RegistrationController(
                        androidId = androidId,
                        scope = scope,
                        onAccountCreated = { role, roles ->
                            availableRoles = roles
                            activeRole = role
                        },
                    )
                }

                LaunchedEffect(registrationController) {
                    if (activeRole == null) {
                        registrationController.checkExistingAccount()
                    }
                }

                when (activeRole) {
                    null -> {
                        if (!registrationController.isCheckingAccount) {
                            RegistrationScreen(
                                name = registrationController.name,
                                onNameChange = registrationController::onNameChange,
                                phoneNumber = registrationController.phoneNumber,
                                onPhoneNumberChange = registrationController::onPhoneNumberChange,
                                email = registrationController.email,
                                onEmailChange = registrationController::onEmailChange,
                                optOutOfNotifications = registrationController.optOutOfNotifications,
                                onOptOutOfNotificationsChange = registrationController::onOptOutOfNotificationsChange,
                                profilePictureUrl = registrationController.profilePictureUrl,
                                onProfilePhotoSelected = registrationController::onProfilePhotoSelected,
                                selectedRole = registrationController.selectedRole,
                                onRoleSelect = registrationController::onRoleSelect,
                                onCreateAccountClick = registrationController::createAccount,
                                nameError = registrationController.nameError,
                                submitError = registrationController.submitError,
                                isSubmitting = registrationController.isSubmitting,
                            )
                        }
                    }

                    UserRole.ADMIN -> AdminNavGraph(
                        roles = availableRoles,
                        onRoleSelected = { activeRole = it },
                    )

                    UserRole.ENTRANT -> EntrantNavGraph(
                        roles = availableRoles,
                        onRoleSelected = { activeRole = it },
                    )

                    UserRole.OWNER -> OwnerNavGraph(
                        ownerId = androidId,
                        availableRoles = availableRoles,
                        onRoleChange = { activeRole = it },
                    )
                }
            }
        }
    }
}