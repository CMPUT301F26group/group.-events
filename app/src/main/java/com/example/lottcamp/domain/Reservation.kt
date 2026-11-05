package com.example.lottcamp.domain
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.lottcamp.data.ReservationStatus
import java.time.Instant
import java.time.LocalDate

/**
 * Domain model representing a confirmed campsite reservation.
 */
@RequiresApi(Build.VERSION_CODES.O)
data class Reservation(
    val id: String,
    val entrantId: String,
    val campsiteId: String,
    val lotteryId: String,
    val checkIn: LocalDate,
    val checkOut: LocalDate,
    val status: ReservationStatus = ReservationStatus.ACTIVE,
    val createdAt: Instant = Instant.now()
) {
    /** Returns a new cancelled [Reservation] if active. */
    fun cancel(): Reservation {
        check(status == ReservationStatus.ACTIVE) { "Only active reservations can be cancelled." }
        return copy(status = ReservationStatus.CANCELLED)
    }

    /** Returns a new rejected [Reservation] (e.g. by owner due to emergency). */
    fun rejectByOwner(): Reservation {
        check(status == ReservationStatus.ACTIVE) { "Only active reservations can be rejected." }
        return copy(status = ReservationStatus.REJECTED)
    }
}