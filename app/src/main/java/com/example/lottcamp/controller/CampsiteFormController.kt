package com.example.lottcamp.controller

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.lottcamp.domain.Campsite

/**
 * Controller for the register and edit campsite screen.
 *
 * Holds the form fields, validates them, and saves through [owner].
 * With an [existing] campsite the form starts filled in and saving updates it.
 *
 * @param owner Shared owner controller that saves the campsite.
 * @param ownerId Android ID of the owner the campsite belongs to.
 * @param existing Campsite being edited, or null to register a new one.
 * @param back Called when the screen should close, after going back or after a successful save.
 */
class CampsiteFormController(
    private val owner: OwnerController,
    private val ownerId: String,
    private val existing: Campsite? = null,
    private val back: () -> Unit = {},
) {

    val title = if (existing == null) "Register campsite" else "Edit campsite"
    val submitText = if (existing == null) "Publish campsite" else "Save changes"

    /** Photo of the campsite being edited, or null. */
    val imageUrl: String? = existing?.imageUrl

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

    /** Tags the owner can choose from, including any the campsite already has. */
    val availableTags = (listOf("Fire pit", "Cookhouse", "Tennis") + (existing?.tags ?: emptyList())).distinct()

    // Errors only appear after the first submit attempt
    private var submitted by mutableStateOf(false)

    private val occupancyValue: Int?
        get() = maxOccupancy.trim().toIntOrNull()?.takeIf { it > 0 }

    private val priceValue: Double?
        get() = pricePerNight.trim().removePrefix("$").toDoubleOrNull()?.takeIf { it >= 0 }

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

    fun onTagClick(tag: String) {
        selectedTags = if (tag in selectedTags) selectedTags - tag else selectedTags + tag
    }

    fun onBackClick() = back()

    // TODO: open the photo picker once image upload exists.
    fun onPhotoClick() {}

    // TODO: open the map picker once geolocation exists.
    fun onMapClick() {}

    /** Validates the form and, if it is valid, saves the campsite and closes the screen. */
    fun onSubmitClick() {
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
        owner.saveCampsite(
            base.copy(
                name = name.trim(),
                locationName = location.trim(),
                description = description.trim(),
                maxOccupancy = occupancy,
                pricePerNight = price,
                tags = selectedTags.toList(),
            ),
            onSaved = back,
        )
    }
}