package com.example.lottcamp.controller

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.lottcamp.data.ReservationStatus
import com.example.lottcamp.domain.Campsite
import com.example.lottcamp.ui.components.QrCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.Calendar

/** Price without trailing zeros: 65.0 becomes "65", 65.5 becomes "65.50". */
internal fun formatPrice(price: Double): String =
    if (price % 1.0 == 0.0) price.toInt().toString() else "%.2f".format(price)

@RequiresApi(Build.VERSION_CODES.O)
class CampsiteDetailsController(
    private val owner: OwnerController,
    private val campsiteId: String,
    private val appContext: Context,
    private val scope: CoroutineScope,
    private val navigate: (String) -> Unit = {},
    private val back: () -> Unit = {},
) {

    val campsite: Campsite? get() = owner.campsite(campsiteId)

    val capacityAndPrice: String
        get() = campsite?.let { "${it.maxOccupancy} guests · \$${formatPrice(it.pricePerNight)} / night" } ?: ""

    /** The promotional QR code, built from the campsite ID. */
    val qrBitmap: Bitmap by lazy { QrCode.generate(QrCode.campsiteContent(campsiteId)) }

    /** Message shown after tapping Download QR, or null. */
    var notice by mutableStateOf<String?>(null)
        private set

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
    fun dismissNotice() { notice = null }

    fun onLotteryClick() {
        val lottery = owner.lotteryFor(campsiteId)
        navigate(
            if (lottery != null) OwnerRoutes.waitingList(lottery.id)
            else OwnerRoutes.createLottery(campsiteId)
        )
    }

    /** Saves the QR code to the gallery. */
    fun onDownloadQrClick() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            notice = "Saving to the gallery needs Android 10 or newer."
            return
        }
        scope.launch {
            val saved = QrCode.saveToGallery(appContext, qrBitmap, "lottcamp-campsite-$campsiteId")
            notice = if (saved) "QR code saved to your gallery (Pictures/LottCamp)."
            else "Couldn't save the QR code."
        }
    }
}