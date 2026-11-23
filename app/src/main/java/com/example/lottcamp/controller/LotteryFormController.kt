package com.example.lottcamp.controller

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.lottcamp.domain.Campsite
import com.example.lottcamp.domain.Lottery
import com.example.lottcamp.repository.LotteryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.time.Instant

/**
 * Controller for the create and edit lottery screen.
 *
 * With a [lotteryId] the form is filled in by [load] and saving updates that lottery.
 *
 * @param owner Shared owner controller that knows the owner's campsites.
 * @param ownerId Android ID of the owner.
 * @param lotteries Where the lottery is read and saved.
 * @param scope Runs database work in the background.
 * @param lotteryId Lottery being edited, or null to create a new one.
 * @param preselectedCampsiteId Campsite that starts ticked when creating.
 * @param onSaved Called with the lottery ID after a successful save.
 * @param back Called when the screen should close.
 */
@RequiresApi(Build.VERSION_CODES.O)
class LotteryFormController(
    private val owner: OwnerController,
    private val ownerId: String,
    private val lotteries: LotteryRepository,
    private val scope: CoroutineScope,
    private val lotteryId: String?,
    preselectedCampsiteId: String?,
    private val onSaved: (String) -> Unit,
    private val back: () -> Unit,
) {

    val title = if (lotteryId == null) "Create lottery" else "Edit lottery"
    val submitText = if (lotteryId == null) "Publish lottery" else "Save changes"

    private var existing by mutableStateOf<Lottery?>(null)

    /** Campsites that can be ticked: the owner's own, plus any already in the lottery being edited. */
    val options: List<Campsite>
        get() = (owner.campsites + (existing?.campsites ?: emptyList())).distinctBy { it.id }

    var selectedIds by mutableStateOf(setOfNotNull(preselectedCampsiteId))
        private set
    var registrationStart by mutableStateOf<Long?>(null)
        private set
    var registrationEnd by mutableStateOf<Long?>(null)
        private set
    var bookingStart by mutableStateOf<Long?>(null)
        private set
    var bookingEnd by mutableStateOf<Long?>(null)
        private set
    var winners by mutableStateOf("")
        private set
    var waitlistLimit by mutableStateOf("")
        private set

    var saving by mutableStateOf(false)
        private set

    /** Problem with the form or the last save, shown above the button. */
    var formError by mutableStateOf<String?>(null)
        private set

    fun onCampsiteToggle(id: String) {
        selectedIds = if (id in selectedIds) selectedIds - id else selectedIds + id
    }

    fun onRegistrationStartChange(value: Long) { registrationStart = value }
    fun onRegistrationEndChange(value: Long) { registrationEnd = value }
    fun onBookingStartChange(value: Long) { bookingStart = value }
    fun onBookingEndChange(value: Long) { bookingEnd = value }
    fun onWinnersChange(value: String) { winners = value }
    fun onWaitlistLimitChange(value: String) { waitlistLimit = value }
    fun onBackClick() = back()

    /** Fills the form from the lottery being edited. Does nothing when creating. */
    fun load() {
        val id = lotteryId ?: return
        scope.launch {
            try {
                val lottery = lotteries.getLotteryById(id) ?: throw IllegalStateException("Lottery not found")
                existing = lottery
                selectedIds = lottery.campsites.map { it.id }.toSet()
                registrationStart = lottery.registrationStart.toEpochMilli()
                registrationEnd = lottery.registrationEnd.toEpochMilli()
                bookingStart = lottery.bookingStart.toEpochMilli()
                bookingEnd = lottery.bookingEnd.toEpochMilli()
                winners = lottery.winnerCount.toString()
                waitlistLimit = lottery.waitlistCapacity?.toString() ?: ""
            } catch (e: Exception) {
                formError = "Couldn't load the lottery: ${e.message}"
            }
        }
    }

    /** Validates the form and, if it is valid, saves the lottery. */
    fun onSubmitClick() {
        if (saving) return
        val regStart = registrationStart
        val regEnd = registrationEnd
        val bookStart = bookingStart
        val bookEnd = bookingEnd
        val winnerCount = winners.trim().toIntOrNull()?.takeIf { it > 0 }
        val capacityText = waitlistLimit.trim()
        val capacity = if (capacityText.isEmpty()) null else capacityText.toIntOrNull()?.takeIf { it > 0 }

        formError = when {
            selectedIds.isEmpty() -> "Pick at least one campsite"
            regStart == null || regEnd == null || bookStart == null || bookEnd == null ->
                "Pick all four dates"
            regStart >= regEnd -> "Registration must close after it opens"
            bookStart < regEnd -> "Booking can't start before registration closes"
            bookStart > bookEnd -> "Booking must end after it starts"
            winnerCount == null -> "Number of winners must be above 0"
            capacityText.isNotEmpty() && capacity == null -> "Waitlist limit must be above 0"
            else -> null
        }
        if (formError != null) return
        // The checks above make these non-null
        val campsites = options.filter { it.id in selectedIds }

        scope.launch {
            saving = true
            try {
                val id = withTimeout(15_000) {
                    lotteries.saveLottery(
                        Lottery(
                            id = lotteryId ?: "",
                            ownerId = ownerId,
                            campsites = campsites,
                            registrationStart = Instant.ofEpochMilli(regStart!!),
                            registrationEnd = Instant.ofEpochMilli(regEnd!!),
                            bookingStart = Instant.ofEpochMilli(bookStart!!),
                            bookingEnd = Instant.ofEpochMilli(bookEnd!!),
                            winnerCount = winnerCount!!,
                            waitlistCapacity = capacity,
                        )
                    )
                }
                owner.refresh()
                onSaved(id)
            } catch (e: Exception) {
                formError = "Couldn't save the lottery: ${e.message}"
            } finally {
                saving = false
            }
        }
    }
}