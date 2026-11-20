package com.example.lottcamp.controller

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import com.example.lottcamp.domain.Campsite
import java.util.Calendar

/** Price without trailing zeros: 65.0 becomes "65", 65.5 becomes "65.50". */
internal fun formatPrice(price: Double): String =
    if (price % 1.0 == 0.0) price.toInt().toString() else "%.2f".format(price)

/**
 * Controller for the owner's campsite details screen.
 *
 * @param owner Shared owner controller that owns the campsites.
 * @param campsiteId ID of the campsite being shown.
 * @param navigate Called with an [OwnerRoutes] route when the screen should change.
 * @param back Called when the screen should close.
 */
class CampsiteDetailsController(
    private val owner: OwnerController,
    private val campsiteId: String,
    private val navigate: (String) -> Unit = {},
    private val back: () -> Unit = {},
) {

    /** The campsite shown, or null while the list is still loading. */
    val campsite: Campsite? get() = owner.campsite(campsiteId)

    /** E.g. "8 guests · $65 / night". */
    val capacityAndPrice: String
        get() = campsite?.let { "${it.maxOccupancy} guests · \$${formatPrice(it.pricePerNight)} / night" } ?: ""

    private val today = Calendar.getInstance()

    /** Year of the month the calendar shows. */
    var year by mutableIntStateOf(today.get(Calendar.YEAR))
        private set

    /** Month of the calendar, 1 to 12. */
    var month by mutableIntStateOf(today.get(Calendar.MONTH) + 1)
        private set

    // TODO: fill from the campsite's reservations once they exist.
    /** Days of the shown month that are booked. */
    val bookedDays: Set<Int> get() = emptySet()

    fun onPreviousMonth() {
        if (month == 1) { month = 12; year-- } else month--
    }

    fun onNextMonth() {
        if (month == 12) { month = 1; year++ } else month++
    }

    fun onBackClick() = back()
    fun onEditClick() = navigate(OwnerRoutes.editCampsite(campsiteId))

    // TODO: open the create lottery screen once it exists.
    fun onCreateLotteryClick() {}

    // TODO: generate and save the QR code once a QR library is added.
    fun onDownloadQrClick() {}
}