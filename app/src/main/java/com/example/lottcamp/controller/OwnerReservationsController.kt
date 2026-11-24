package com.example.lottcamp.controller

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.lottcamp.data.NotificationDoc
import com.example.lottcamp.data.ReservationStatus
import com.example.lottcamp.domain.Reservation
import com.example.lottcamp.repository.EntrantRepository
import com.example.lottcamp.repository.NotificationRepository
import com.example.lottcamp.repository.ReservationRepository
import com.google.firebase.Timestamp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** One reservation card. [canAct] is true while the owner can still message or reject. */
data class ReservationRow(
    val id: String,
    val name: String,
    val subtitle: String,
    val status: String,
    val canAct: Boolean,
)

/** Short code shown on a reservation, e.g. "RES-2038", derived from its document ID. */
fun reservationCode(id: String) = "RES-" + id.takeLast(4).uppercase()

/**
 * Controller for the owner's reservations screen.
 *
 * Lists the reservations on the owner's campsites under Active, Past and Cancelled,
 * rejects reservations, and opens the message thread.
 */
class OwnerReservationsController(
    private val ownerId: String,
    private val owner: OwnerController,
    private val entrants: EntrantRepository,
    private val reservations: ReservationRepository,
    private val notifications: NotificationRepository,
    private val scope: CoroutineScope,
    private val navigate: (String) -> Unit = {},
) {

    val filters = listOf("Active", "Past", "Cancelled")

    var filter by mutableIntStateOf(0)
        private set

    /** Entrant names by Android ID. */
    private var names by mutableStateOf(emptyMap<String, String>())

    /** The reservation the owner is about to reject, or null when no dialog is open. */
    var rejecting by mutableStateOf<ReservationRow?>(null)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    /** Used by the screen to know when to look up entrant names again. */
    val reservationCount: Int get() = owner.reservations.size

    val rows: List<ReservationRow>
        get() {
            val today = LocalDate.now()
            return owner.reservations
                .filter { bucket(it, today) == filter }
                .sortedBy { it.checkIn }
                .map { toRow(it, today) }
        }

    val rejectMessage: String
        get() = "${rejecting?.name ?: ""} will be notified and refunded. This can't be undone."

    fun onFilterSelect(index: Int) { filter = index }
    fun onRejectClick(row: ReservationRow) { rejecting = row }
    fun onRejectDismiss() { rejecting = null }
    fun onMessageClick(row: ReservationRow) = navigate(OwnerRoutes.chat(row.id))
    fun clearError() { error = null }

    /** Looks up the names of the entrants who hold reservations. */
    fun loadNames() {
        scope.launch {
            try {
                val ids = owner.reservations.map { it.entrantId }
                names = entrants.getByIds(ids).associate { it.androidId to it.name }
            } catch (e: Exception) {
                error = "Couldn't load entrants: ${e.message}"
            }
        }
    }

    /** Rejects the reservation in the dialog and notifies the entrant. */
    fun onRejectConfirm() {
        val row = rejecting ?: return
        rejecting = null
        val reservation = owner.reservations.find { it.id == row.id } ?: return
        scope.launch {
            try {
                withTimeout(15_000) {
                    reservations.updateReservationStatus(row.id, ReservationStatus.REJECTED.name)
                    notifications.send(
                        listOf(
                            NotificationDoc(
                                title = "Reservation rejected",
                                senderId = ownerId,
                                senderName = "Campsite owner",
                                recipientId = reservation.entrantId,
                                recipientName = row.name,
                                message = "Your reservation ${reservationCode(row.id)} at " +
                                        "${campsiteName(reservation)} was rejected by the owner and refunded.",
                                sentAt = Timestamp.now(),
                            )
                        )
                    )
                }
                owner.refresh()
            } catch (e: Exception) {
                error = "Couldn't reject the reservation: ${e.message}"
            }
        }
    }

    /** 0 = active, 1 = past, 2 = cancelled or rejected. */
    private fun bucket(r: Reservation, today: LocalDate) = when (r.status) {
        ReservationStatus.ACTIVE -> if (r.checkOut.isBefore(today)) 1 else 0
        ReservationStatus.COMPLETED -> 1
        ReservationStatus.CANCELLED, ReservationStatus.REJECTED -> 2
    }

    private fun toRow(r: Reservation, today: LocalDate): ReservationRow {
        val label = when (r.status) {
            ReservationStatus.ACTIVE -> if (r.checkOut.isBefore(today)) "Completed" else "Confirmed"
            ReservationStatus.COMPLETED -> "Completed"
            ReservationStatus.CANCELLED -> "Cancelled by entrant"
            ReservationStatus.REJECTED -> "Rejected by owner"
        }
        return ReservationRow(
            id = r.id,
            name = names[r.entrantId] ?: "Unknown entrant",
            subtitle = "${campsiteName(r)} · ${dateRange(r)}",
            status = "$label · ${reservationCode(r.id)}",
            canAct = bucket(r, today) == 0,
        )
    }

    private fun campsiteName(r: Reservation) = owner.campsite(r.campsiteId)?.name ?: "Campsite"

    private fun dateRange(r: Reservation): String {
        val locale = Locale.getDefault()
        val start = r.checkIn.format(DateTimeFormatter.ofPattern("MMM d", locale))
        val end = r.checkOut.format(DateTimeFormatter.ofPattern("MMM d, yyyy", locale))
        return "$start – $end"
    }
}