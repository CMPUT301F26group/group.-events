package com.example.lottcamp.controller

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.lottcamp.domain.Campsite
import com.example.lottcamp.repository.ImageRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

/**
 * Controller for the register and edit campsite screen.
 *
 * Holds the form fields, validates them, uploads a newly picked photo, and saves through [owner].
 * With an [existing] campsite the form starts filled in and saving updates it.
 */
@RequiresApi(Build.VERSION_CODES.O)
class CampsiteFormController(
    private val owner: OwnerController,
    private val ownerId: String,
    private val images: ImageRepository,
    private val appContext: Context,
    private val scope: CoroutineScope,
    private val existing: Campsite? = null,
    private val back: () -> Unit = {},
) {

    val title = if (existing == null) "Register campsite" else "Edit campsite"
    val submitText = if (existing == null) "Publish campsite" else "Save changes"

    /** Stored photo of the campsite being edited, or null. */
    val imageId: String? = existing?.imageId

    /** Photo picked in this session. It is uploaded when the form is submitted. */
    var pickedUri by mutableStateOf<Uri?>(null)
        private set

    var name by mutableStateOf(existing?.name ?: "")
        private set
    var location by mutableStateOf(existing?.locationName ?: "")
        private set
    var description by mutableStateOf(existing?.description ?: "")
        private set
    var maxOccupancy by mutableStateOf(existing?.maxOccupancy?.toString() ?: "")
        private set
    var pricePerNight by mutableStateOf(existing?.let { "\$" + formatPrice(it.pricePerNight) } ?: "")
        private set
    var selectedTags by mutableStateOf(existing?.tags?.toSet() ?: emptySet())
        private set

    var latitudeText by mutableStateOf(existing?.latitude?.toString() ?: "")
        private set
    var longitudeText by mutableStateOf(existing?.longitude?.toString() ?: "")
        private set
    var mapDialogOpen by mutableStateOf(false)
        private set
    var mapError by mutableStateOf<String?>(null)
        private set

    /** True while the photo is uploading. */
    private var busy by mutableStateOf(false)

    /** Tags the owner can choose from, including any the campsite already has. */
    val availableTags = (listOf("Fire pit", "Cookhouse", "Tennis") + (existing?.tags ?: emptyList())).distinct()

    // Errors only appear after the first submit attempt
    private var submitted by mutableStateOf(false)

    private val occupancyValue: Int?
        get() = maxOccupancy.trim().toIntOrNull()?.takeIf { it > 0 }

    private val priceValue: Double?
        get() = pricePerNight.trim().removePrefix("$").toDoubleOrNull()?.takeIf { it >= 0 }

    private val latitudeValue: Double?
        get() = latitudeText.trim().toDoubleOrNull()?.takeIf { it in -90.0..90.0 }

    private val longitudeValue: Double?
        get() = longitudeText.trim().toDoubleOrNull()?.takeIf { it in -180.0..180.0 }

    val nameError: String?
        get() = if (submitted && name.isBlank()) "Required" else null

    val occupancyError: String?
        get() = if (submitted && occupancyValue == null) "Enter a number above 0" else null

    val priceError: String?
        get() = if (submitted && priceValue == null) "Enter a valid price" else null

    fun onNameChange(value: String) { name = value }
    fun onLocationChange(value: String) { location = value }
    fun onDescriptionChange(value: String) { description = value }
    fun onMaxOccupancyChange(value: String) { maxOccupancy = value }
    fun onPricePerNightChange(value: String) { pricePerNight = value }
    fun onLatitudeChange(value: String) { latitudeText = value }
    fun onLongitudeChange(value: String) { longitudeText = value }
    fun onPhotoPicked(uri: Uri) { pickedUri = uri }

    fun onTagClick(tag: String) {
        selectedTags = if (tag in selectedTags) selectedTags - tag else selectedTags + tag
    }

    fun onBackClick() = back()

    fun onMapDismiss() { mapDialogOpen = false }

    fun onMapClick() {
        mapError = null
        mapDialogOpen = true
    }

    /** Closes the dialog if both fields are empty or valid numbers in range. */
    fun onMapConfirm() {
        val bothEmpty = latitudeText.isBlank() && longitudeText.isBlank()
        if (!bothEmpty && (latitudeValue == null || longitudeValue == null)) {
            mapError = "Latitude must be -90 to 90 and longitude -180 to 180"
            return
        }
        mapDialogOpen = false
    }

    /** Validates the form, uploads the picked photo if any, saves the campsite and closes the screen. */
    fun onSubmitClick() {
        if (busy || owner.saving) return
        submitted = true
        val occupancy = occupancyValue
        val price = priceValue
        if (name.isBlank() || occupancy == null || price == null) return

        val base = existing ?: Campsite(
            id = "",
            ownerId = ownerId,
            name = "",
            locationName = "",
            maxOccupancy = occupancy,
            pricePerNight = price,
        )
        val uri = pickedUri
        val oldImageId = existing?.imageId

        scope.launch {
            busy = true
            try {
                val newImageId = uri?.let { withTimeout(30_000) { images.saveFromUri(appContext, it) } }
                owner.saveCampsite(
                    base.copy(
                        name = name.trim(),
                        locationName = location.trim(),
                        description = description.trim(),
                        maxOccupancy = occupancy,
                        pricePerNight = price,
                        tags = selectedTags.toList(),
                        latitude = latitudeValue,
                        longitude = longitudeValue,
                        imageId = newImageId ?: base.imageId,
                    ),
                    onSaved = {
                        // The photo was replaced, so the old one is no longer used
                        if (newImageId != null && oldImageId != null) {
                            scope.launch { runCatching { images.delete(oldImageId) } }
                        }
                        back()
                    },
                )
            } catch (e: Exception) {
                owner.report("Couldn't upload the photo: ${e.message}")
            } finally {
                busy = false
            }
        }
    }
}