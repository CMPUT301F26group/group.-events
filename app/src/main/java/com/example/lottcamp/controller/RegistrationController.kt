package com.example.lottcamp.controller

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.lottcamp.domain.Admin
import com.example.lottcamp.domain.CampsiteOwner
import com.example.lottcamp.domain.Entrant
import com.example.lottcamp.domain.UserRole
import com.example.lottcamp.repository.AdminRepository
import com.example.lottcamp.repository.CampsiteOwnerRepository
import com.example.lottcamp.repository.EntrantRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Controller for account registration and role selection.
 *
 * Holds registration form fields, validation and submission state, selected role, and
 * notification opt-out preference, and persists new accounts through [entrantRepo],
 * [ownerRepo], and [adminRepo].
 *
 * @param androidId Device identifier used as the account and document ID.
 * @param scope Coroutine scope used to run repository operations.
 * @param entrantRepo Repository for reading and saving entrant profiles.
 * @param ownerRepo Repository for saving campsite owner profiles.
 * @param adminRepo Repository for saving administrator records.
 * @param onAccountCreated Invoked with the created role and available roles after persistence succeeds.
 */
class RegistrationController(
    private val androidId: String,
    private val scope: CoroutineScope,
    private val entrantRepo: EntrantRepository = EntrantRepository(),
    private val ownerRepo: CampsiteOwnerRepository = CampsiteOwnerRepository(),
    private val adminRepo: AdminRepository = AdminRepository(),
    private val onAccountCreated: (role: UserRole, availableRoles: List<UserRole>) -> Unit = { _, _ -> },
) {
    var name by mutableStateOf("")
        private set

    var phoneNumber by mutableStateOf("")
        private set

    var email by mutableStateOf("")
        private set

    /** True when the user opts out of notifications right away. */
    var optOutOfNotifications by mutableStateOf(false)
        private set

    var profilePictureUrl by mutableStateOf<String?>(null)
        private set

    var selectedRole by mutableStateOf(UserRole.ADMIN)
        private set

    var nameError by mutableStateOf<String?>(null)
        private set

    var submitError by mutableStateOf<String?>(null)
        private set

    var isSubmitting by mutableStateOf(false)
        private set

    var isCheckingAccount by mutableStateOf(false)
        private set

    fun onNameChange(value: String) {
        name = value
        if (nameError != null && value.isNotBlank()) {
            nameError = null
        }
        submitError = null
    }

    fun onPhoneNumberChange(value: String) {
        phoneNumber = value
        submitError = null
    }

    fun onEmailChange(value: String) {
        email = value
        submitError = null
    }

    fun onOptOutOfNotificationsChange(optedOut: Boolean) {
        optOutOfNotifications = optedOut
    }

    fun onProfilePhotoSelected(uriString: String?) {
        profilePictureUrl = uriString
    }

    fun onRoleSelect(role: UserRole) {
        selectedRole = role
        submitError = null
    }

    /** Checks whether [androidId] already has an account and logs the user in automatically. */
    fun checkExistingAccount() {
        if (androidId.isBlank()) return

        scope.launch {
            isCheckingAccount = true
            submitError = null
            try {
                if (adminRepo.isAdmin(androidId)) {
                    onAccountCreated(UserRole.ADMIN, buildAvailableRoles(UserRole.ADMIN))
                    return@launch
                }

                if (ownerRepo.getOwnerById(androidId) != null) {
                    onAccountCreated(UserRole.OWNER, buildAvailableRoles(UserRole.OWNER))
                    return@launch
                }

                if (entrantRepo.getEntrantByAndroidId(androidId) != null) {
                    onAccountCreated(UserRole.ENTRANT, buildAvailableRoles(UserRole.ENTRANT))
                    return@launch
                }
            } catch (e: Exception) {
                submitError = "Could not check existing account: ${e.message ?: "Unknown error"}"
            } finally {
                isCheckingAccount = false
            }
        }
    }

    /** Validates and saves the new user to Firestore under the selected [UserRole]. */
    fun createAccount() {
        val trimmedName = name.trim()
        if (trimmedName.isEmpty()) {
            nameError = "Name is required."
            return
        }

        val trimmedPhone = phoneNumber.trim()
        val trimmedEmail = email.trim()
        val notificationsEnabled = !optOutOfNotifications
        val roleToSave = selectedRole

        scope.launch {
            isSubmitting = true
            submitError = null
            try {
                when (roleToSave) {
                    UserRole.ENTRANT -> {
                        entrantRepo.saveEntrant(
                            Entrant(
                                androidId = androidId,
                                name = trimmedName,
                                phoneNumber = trimmedPhone,
                                email = trimmedEmail,
                                profilePictureId = profilePictureUrl,
                                notificationsEnabled = notificationsEnabled,
                            )
                        )
                    }

                    UserRole.OWNER -> {
                        ownerRepo.saveOwner(
                            CampsiteOwner(
                                id = androidId,
                                name = trimmedName,
                                email = trimmedEmail,
                                phoneNumber = trimmedPhone,
                            )
                        )
                    }

                    UserRole.ADMIN -> {
                        adminRepo.saveAdmin(Admin(id = androidId))
                    }
                }

                val roles = buildAvailableRoles(roleToSave)
                onAccountCreated(roleToSave, roles)
            } catch (e: Exception) {
                submitError = "Could not create account: ${e.message ?: "Unknown error"}"
            } finally {
                isSubmitting = false
            }
        }
    }

    private fun buildAvailableRoles(active: UserRole): List<UserRole> {
        return if (active == UserRole.ADMIN) {
            UserRole.entries
        } else {
            listOf(UserRole.ENTRANT, UserRole.OWNER)
        }
    }
}