package com.example.lottcamp.controller

import android.os.Looper
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.lottcamp.data.EntryStatus
import com.example.lottcamp.data.ReservationStatus
import com.example.lottcamp.domain.Reservation
import com.example.lottcamp.repository.EntrantRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.time.Instant
import java.time.LocalDate
import kotlin.time.Duration.Companion.milliseconds

/** Reservation data ready to render in the active or past tab. */
data class BookingCard(
    val reservationId: String,
    val campsiteId: String,
    val campsiteName: String,
    val campsiteLocation: String,
    val checkIn: LocalDate,
    val checkOut: LocalDate,
    val hasReview: Boolean = false,
)

/** Lottery entry data ready to render in the waiting lists tab. */
data class WaitingListCard(
    val entryId: String,
    val campsiteName: String,
    val closesOn: Instant,
    val entrantCount: Int,
)

/**
 * Controller for the entrant bookings screen, managing active reservations, past stays,
 * waiting list entries, and confirmation dialogs.
 *
 * @param entrantId Current entrant identifier (device Android ID or test ID).
 * @param scope Coroutine scope used for repository operations.
 * @param data Entrant repository providing reservations, campsites, and lottery entries.
 * @param navigate Navigation callback accepting a route string.
 */
class EntrantBookingsController(
    private val entrantId: String,
    private val scope: CoroutineScope,
    private val data: EntrantRepository = EntrantRepository(),
    private val navigate: (String) -> Unit = {},
) {
    private val instanceId = System.identityHashCode(this)

    val activeBookings = mutableStateListOf<BookingCard>()
    val pastBookings = mutableStateListOf<BookingCard>()
    val waitingLists = mutableStateListOf<WaitingListCard>()

    var isLoading by mutableStateOf(true)
        private set

    var bookingToCancel by mutableStateOf<BookingCard?>(null)
        private set

    var waitingToLeave by mutableStateOf<WaitingListCard?>(null)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    init {
        Log.d(TAG, "Created controller#$instanceId for entrantId='$entrantId', mainThread=${Looper.myLooper() == Looper.getMainLooper()}")
    }

    /** Loads reservations and active waiting list entries for [entrantId]. */
    fun refresh() {
        Log.d(TAG, "controller#$instanceId refresh() called, isLoading=$isLoading, mainThread=${Looper.myLooper() == Looper.getMainLooper()}")
        scope.launch {
            isLoading = true
            error = null
            Log.d(TAG, "controller#$instanceId coroutine started, set isLoading=$isLoading on thread=${Thread.currentThread().name}")
            try {
                withTimeout(10_000L.milliseconds) {
                    val campsites = data.getCampsites().associateBy { it.id }
                    Log.d(TAG, "controller#$instanceId loaded ${campsites.size} campsites")
                    val today = LocalDate.now()

                    val reviewedCampsiteIds = try {
                        data.getReviewedCampsiteIds(entrantId)
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        Log.e(TAG, "controller#$instanceId couldn't load reviews", e)
                        emptySet()
                    }

                    val reservations = data.getReservations(entrantId)
                    Log.d(TAG, "controller#$instanceId loaded ${reservations.size} reservations")
                    val (past, active) = reservations.partition { reservation ->
                        reservation.status == ReservationStatus.COMPLETED ||
                                (reservation.status == ReservationStatus.ACTIVE &&
                                        reservation.checkOut.isBefore(today))
                    }

                    fun Reservation.toCard(): BookingCard {
                        val campsite = campsites[campsiteId]
                        return BookingCard(
                            reservationId = id,
                            campsiteId = campsiteId,
                            campsiteName = campsite?.name ?: "Campsite",
                            campsiteLocation = campsite?.locationName ?: "",
                            checkIn = checkIn,
                            checkOut = checkOut,
                            hasReview = campsiteId in reviewedCampsiteIds,
                        )
                    }

                    activeBookings.clear()
                    activeBookings.addAll(
                        active.filter { it.status == ReservationStatus.ACTIVE }.map { it.toCard() }
                    )

                    pastBookings.clear()
                    pastBookings.addAll(past.map { it.toCard() })

                    val entries = data.getLotteryEntries(entrantId)
                    Log.d(TAG, "controller#$instanceId loaded ${entries.size} lottery entries")
                    val waitingCards = entries
                        .filter { it.status == EntryStatus.WAITING }
                        .mapNotNull { entry ->
                            val lottery = data.getLottery(entry.lotteryId)
                                ?: return@mapNotNull null
                            WaitingListCard(
                                entryId = entry.id,
                                campsiteName = lottery.campsites.firstOrNull()?.name ?: "Campsite",
                                closesOn = lottery.registrationEnd,
                                entrantCount = data.getLotteryEntryCount(lottery.id),
                            )
                        }

                    waitingLists.clear()
                    waitingLists.addAll(waitingCards)
                    Log.d(TAG, "controller#$instanceId populated lists: active=${activeBookings.size}, past=${pastBookings.size}, waiting=${waitingLists.size}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "controller#$instanceId refresh() failed", e)
                error = "Couldn't load bookings: ${e.message ?: e::class.simpleName}"
            } finally {
                isLoading = false
                Log.d(TAG, "controller#$instanceId finally: set isLoading=$isLoading on thread=${Thread.currentThread().name}")
            }
        }
    }

    fun onCancelBookingClick(booking: BookingCard) {
        bookingToCancel = booking
    }

    fun dismissCancelDialog() {
        bookingToCancel = null
    }

    fun confirmCancelBooking() {
        val booking = bookingToCancel ?: return
        bookingToCancel = null
        scope.launch {
            try {
                data.cancelReservation(booking.reservationId)
                activeBookings.removeAll { it.reservationId == booking.reservationId }
            } catch (e: Exception) {
                error = "Couldn't cancel booking: ${e.message}"
            }
        }
    }

    fun onRateCampsiteClick(booking: BookingCard) {
        navigate(EntrantRoutes.rateCampsite(booking.campsiteId))
    }

    fun onLeaveWaitingListClick(waiting: WaitingListCard) {
        waitingToLeave = waiting
    }

    fun dismissLeaveDialog() {
        waitingToLeave = null
    }

    fun confirmLeaveWaitingList() {
        val waiting = waitingToLeave ?: return
        waitingToLeave = null
        scope.launch {
            try {
                data.leaveWaitingList(waiting.entryId)
                waitingLists.removeAll { it.entryId == waiting.entryId }
            } catch (e: Exception) {
                error = "Couldn't leave waiting list: ${e.message}"
            }
        }
    }

    fun clearError() {
        error = null
    }

    companion object {
        private const val TAG = "EntrantBookings"
    }
}