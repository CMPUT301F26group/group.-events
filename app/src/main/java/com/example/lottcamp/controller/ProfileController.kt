package com.example.lottcamp.controller

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.lottcamp.domain.CampsiteOwner
import com.example.lottcamp.domain.Entrant
import com.example.lottcamp.repository.CampsiteOwnerRepository
import com.example.lottcamp.repository.EntrantRepository
import com.example.lottcamp.repository.ImageRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.milliseconds

/** Shared profile controller used across entrant, owner, and admin screens. */
class ProfileController(
    private val userId: String,
    private val scope: CoroutineScope,
    private val entrantRepo: EntrantRepository = EntrantRepository(),
    private val ownerRepo: CampsiteOwnerRepository? = null,
    val imageRepo: ImageRepository = ImageRepository(),
    private val syncOwner: Boolean = false,
    private val onOpenPolicies: () -> Unit = {},
) {
    private var existingEntrant: Entrant? = null
    private var existingOwner: CampsiteOwner? = null
    private var submitted by mutableStateOf(false)

    var name by mutableStateOf("")
        private set
    var email by mutableStateOf("")
        private set
    var phoneNumber by mutableStateOf("")
        private set
    var profilePictureId by mutableStateOf<String?>(null)
        private set
    var notificationsEnabled by mutableStateOf(true)
        private set

    var isSaving by mutableStateOf(false)
        private set
    var isUploadingPhoto by mutableStateOf(false)
        private set
    var showDeleteDialog by mutableStateOf(false)
        private set
    var statusMessage by mutableStateOf<String?>(null)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    val nameError: String?
        get() = if (submitted && name.isBlank()) "Required" else null

    val emailError: String?
        get() = when {
            !submitted -> null
            email.isBlank() -> "Required"
            !email.trim().contains("@") -> "Enter a valid email"
            else -> null
        }

    val phoneError: String?
        get() = if (submitted && phoneNumber.isBlank()) "Required" else null

    /** Loads the user's profile from Firestore. */
    fun refresh() = launchTask("Couldn't load profile") {
        withTimeout(10_000L.milliseconds) {
            val loadedEntrant = entrantRepo.getEntrantByAndroidId(userId)
            val loadedOwner = ownerRepo?.getOwnerById(userId)
            existingEntrant = loadedEntrant
            existingOwner = loadedOwner

            name = loadedEntrant?.name?.ifBlank { null } ?: loadedOwner?.name ?: ""
            email = loadedEntrant?.email?.ifBlank { null } ?: loadedOwner?.email ?: ""
            phoneNumber = loadedEntrant?.phoneNumber?.ifBlank { null } ?: loadedOwner?.phoneNumber ?: ""
            profilePictureId = loadedEntrant?.profilePictureId
            notificationsEnabled = loadedEntrant?.notificationsEnabled ?: true
        }
    }

    fun onNameChange(value: String) { name = value; statusMessage = null }
    fun onEmailChange(value: String) { email = value; statusMessage = null }
    fun onPhoneNumberChange(value: String) { phoneNumber = value; statusMessage = null }
    fun onNotificationsChange(enabled: Boolean) { notificationsEnabled = enabled; statusMessage = null }
    fun onPoliciesClick() = onOpenPolicies()
    fun onDeleteClick() { showDeleteDialog = true }
    fun dismissDeleteDialog() { showDeleteDialog = false }
    fun clearError() { error = null }

    /** Uploads [uri] as the profile photo and replaces any previous stored image. */
    fun onPhotoPicked(context: Context, uri: Uri) {
        if (isUploadingPhoto) return
        launchTask("Couldn't update photo") {
            isUploadingPhoto = true
            statusMessage = null
            try {
                val newImageId = withTimeout(15_000L.milliseconds) { imageRepo.saveFromUri(context, uri) }
                val previousId = profilePictureId
                profilePictureId = newImageId
                existingEntrant?.let {
                    val updated = it.copy(profilePictureId = newImageId)
                    entrantRepo.saveEntrant(updated)
                    existingEntrant = updated
                }
                if (previousId != null && previousId != newImageId) {
                    runCatching { imageRepo.delete(previousId) }
                }
            } finally {
                isUploadingPhoto = false
            }
        }
    }

    /** Clears the current profile photo. */
    fun onRemovePhotoClick() {
        val currentId = profilePictureId ?: return
        profilePictureId = null
        statusMessage = null
        launchTask("Couldn't remove photo") {
            existingEntrant?.let {
                val updated = it.copy(profilePictureId = null)
                entrantRepo.saveEntrant(updated)
                existingEntrant = updated
            }
            runCatching { imageRepo.delete(currentId) }
        }
    }

    /** Validates fields and persists profile changes. */
    fun onSaveClick() {
        submitted = true
        if (nameError != null || emailError != null || phoneError != null || isSaving) return

        launchTask("Couldn't save profile") {
            isSaving = true
            statusMessage = null
            try {
                val updatedEntrant = Entrant(
                    androidId = userId,
                    name = name.trim(),
                    phoneNumber = phoneNumber.trim(),
                    email = email.trim(),
                    profilePictureId = profilePictureId,
                    notificationsEnabled = notificationsEnabled,
                )
                withTimeout(10_000L.milliseconds) {
                    entrantRepo.saveEntrant(updatedEntrant)
                    if (ownerRepo != null && (syncOwner || existingOwner != null)) {
                        val updatedOwner = CampsiteOwner(
                            id = userId,
                            name = updatedEntrant.name,
                            email = updatedEntrant.email,
                            phoneNumber = updatedEntrant.phoneNumber,
                        )
                        ownerRepo.saveOwner(updatedOwner)
                        existingOwner = updatedOwner
                    }
                }
                existingEntrant = updatedEntrant
                statusMessage = "Changes saved"
            } finally {
                isSaving = false
            }
        }
    }

    /** Deletes the profile and resets the form. */
    fun confirmDeleteProfile() {
        showDeleteDialog = false
        if (isSaving) return
        launchTask("Couldn't delete profile") {
            isSaving = true
            statusMessage = null
            try {
                val oldPhotoId = profilePictureId
                withTimeout(10_000L.milliseconds) {
                    entrantRepo.deleteEntrant(userId)
                    if (ownerRepo != null && (syncOwner || existingOwner != null)) {
                        ownerRepo.deleteOwner(userId)
                    }
                }
                oldPhotoId?.let { runCatching { imageRepo.delete(it) } }
                existingEntrant = null
                existingOwner = null
                name = ""
                email = ""
                phoneNumber = ""
                profilePictureId = null
                notificationsEnabled = true
                submitted = false
                statusMessage = "Profile deleted"
            } finally {
                isSaving = false
            }
        }
    }

    private fun launchTask(errorMessage: String, block: suspend () -> Unit) {
        scope.launch {
            error = null
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                error = "$errorMessage: ${e.message ?: e::class.simpleName}"
            }
        }
    }
}
