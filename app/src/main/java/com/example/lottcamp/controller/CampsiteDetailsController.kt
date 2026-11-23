package com.example.lottcamp.controller

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import com.example.lottcamp.data.ReservationStatus
import com.example.lottcamp.domain.Campsite
import java.util.Calendar

/** Price without trailing zeros: 65.0 becomes "65", 65.5 becomes "65.50". */
internal fun formatPrice(price: Double): String =
    if (price % 1.0 == 0.0) price.toInt().toString() else "%.2f".format(price)

@RequiresApi(Build.VERSION_CODES.O)
class CampsiteDetailsController(
    private val owner: OwnerController,
    private val campsiteId: String,
    private val navigate: (String) -> Unit = {},
    private val back: () -> Unit = {},
) {

    val campsite: Campsite? get() = owner.campsite(campsiteId)

    val capacityAndPrice: String
        get() = campsite?.let { "${it.maxOccupancy} guests · \$${formatPrice(it.pricePerNight)} / night" } ?: ""

    private val today = Calendar.getInstance()

    var year by mutableIntStateOf(today.get(Calendar.YEAR))
        private set

    var month by mutableIntStateOf(today.get(Calendar.MONTH) + 1)
        private set

    /** Days of the shown month that have an active reservation. Check-out day isn't counted. */
    val bookedDays: Set<Int>
        get() {
            val days = mutableSetOf<Int>()
            owner.reservations
                .filter { it.campsiteId == campsiteId && it.status == ReservationStatus.ACTIVE }
                .forEach { reservation ->
                    var day = reservation.checkIn
                    while (day.isBefore(reservation.checkOut)) {
                        if (day.year == year && day.monthValue == month) days.add(day.dayOfMonth)
                        day = day.plusDays(1)
                    }
                }
            return days
        }

    /** "View lottery" when the campsite already has one, otherwise "Create lottery". */
    val lotteryText: String
        get() = if (owner.lotteryFor(campsiteId) != null) "View lottery" else "Create lottery"

    fun onPreviousMonth() {
        if (month == 1) { month = 12; year-- } else month--
    }

    fun onNextMonth() {
        if (month == 12) { month = 1; year++ } else month++
    }

    fun onBackClick() = back()
    fun onEditClick() = navigate(OwnerRoutes.editCampsite(campsiteId))

    fun onLotteryClick() {
        val lottery = owner.lotteryFor(campsiteId)
        navigate(
            if (lottery != null) OwnerRoutes.waitingList(lottery.id)
            else OwnerRoutes.createLottery(campsiteId)
        )
    }

    // TODO: generate and save the QR code once a QR library is added.
    fun onDownloadQrClick() {}
}