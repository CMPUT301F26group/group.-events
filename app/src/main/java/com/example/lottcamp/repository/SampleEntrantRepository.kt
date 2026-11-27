package com.example.lottcamp.repository

import com.example.lottcamp.data.EntryStatus
import com.example.lottcamp.data.ReservationStatus
import com.example.lottcamp.data.ReviewDoc
import com.example.lottcamp.domain.Campsite
import com.example.lottcamp.domain.Entrant
import com.example.lottcamp.domain.Lottery
import com.example.lottcamp.domain.LotteryEntry
import com.example.lottcamp.domain.Reservation
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * [EntrantRepository] that keeps sample data in memory. Used by previews and tests
 * so they never read from or write to the real database.
 *
 * The sample reservations and waiting-list entries belong to [ENTRANT_ID]. The
 * lookups ignore the requested entrant ID so the screens show data for whichever
 * device or emulator is running them.
 */
open class SampleEntrantRepository : EntrantRepository() {
    companion object {
        /** ID used by the sample reservations and lottery entries. */
        const val ENTRANT_ID = "sample-entrant"
    }

    private var entrant = Entrant(
        androidId = ENTRANT_ID,
        name = "Alex Morgan",
        phoneNumber = "780-555-0142",
        email = "alex.morgan@example.com",
        profilePictureId = "https://photo.invalid/alex.png",
    )

    val campsites = mutableListOf(
        Campsite(
            id = "CAMP-001",
            ownerId = "USR-0871",
            name = "Pine Ridge Camp",
            locationName = "Hinton, AB",
            description = "A quiet campsite surrounded by pines.",
            maxOccupancy = 6,
            pricePerNight = 85.0,
            imageId = "https://photo.invalid/pine.png",
            tags = listOf("Fire pit", "Pet friendly"),
            ratingSum = 27.0,
            ratingCount = 6,
        ),
        Campsite(
            id = "CAMP-002",
            ownerId = "USR-0871",
            name = "Bow Valley Retreat",
            locationName = "Canmore, AB",
            description = "Mountain views and easy trail access.",
            maxOccupancy = 4,
            pricePerNight = 120.0,
            imageId = "https://photo.invalid/bow.png",
            tags = listOf("Mountain view", "Hot tub"),
            ratingSum = 19.0,
            ratingCount = 4,
        ),
        Campsite(
            id = "CAMP-003",
            ownerId = "USR-0932",
            name = "Meadow Ridge",
            locationName = "Banff, AB",
            description = "Open meadow with room for the whole group.",
            maxOccupancy = 8,
            pricePerNight = 95.0,
            tags = listOf("Group friendly"),
            ratingSum = 13.0,
            ratingCount = 3,
        ),
        Campsite(
            id = "CAMP-004",
            ownerId = "USR-0932",
            name = "Moose Lake",
            locationName = "Kananaskis, AB",
            description = "Lakeside camping with canoe access.",
            maxOccupancy = 4,
            pricePerNight = 50.0,
            tags = listOf("Lake access"),
            ratingSum = 21.0,
            ratingCount = 5,
        ),
        Campsite(
            id = "CAMP-005",
            ownerId = "USR-0932",
            name = "Elk Island",
            locationName = "Fort Saskatchewan, AB",
            description = "Prairie camping near the lake.",
            maxOccupancy = 5,
            pricePerNight = 40.0,
            tags = listOf("Bird watching"),
            ratingSum = 9.0,
            ratingCount = 2,
        ),
        Campsite(
            id = "CAMP-006",
            ownerId = "USR-0932",
            name = "Lake Nordegg",
            locationName = "Nordegg, AB",
            description = "Quiet lakefront sites in the foothills.",
            maxOccupancy = 4,
            pricePerNight = 55.0,
            tags = listOf("Lake access", "Canoeing"),
            ratingSum = 16.0,
            ratingCount = 4,
        ),
    )

    val reservations = mutableListOf(
        Reservation(
            id = "RES-2038",
            entrantId = ENTRANT_ID,
            campsiteId = "CAMP-001",
            lotteryId = "LOT-1",
            checkIn = LocalDate.of(2027, 5, 8),
            checkOut = LocalDate.of(2027, 5, 10),
            status = ReservationStatus.ACTIVE,
        ),
        Reservation(
            id = "RES-2051",
            entrantId = ENTRANT_ID,
            campsiteId = "CAMP-002",
            lotteryId = "LOT-1",
            checkIn = LocalDate.of(2027, 6, 12),
            checkOut = LocalDate.of(2027, 6, 14),
            status = ReservationStatus.ACTIVE,
        ),
        Reservation(
            id = "RES-1998",
            entrantId = ENTRANT_ID,
            campsiteId = "CAMP-004",
            lotteryId = "LOT-2",
            checkIn = LocalDate.of(2026, 7, 2),
            checkOut = LocalDate.of(2026, 7, 4),
            status = ReservationStatus.COMPLETED,
        ),
        Reservation(
            id = "RES-1999",
            entrantId = ENTRANT_ID,
            campsiteId = "CAMP-005",
            lotteryId = "LOT-2",
            checkIn = LocalDate.of(2026, 8, 14),
            checkOut = LocalDate.of(2026, 8, 16),
            status = ReservationStatus.COMPLETED,
        ),
    )

    val lotteries = mutableListOf(
        Lottery(
            id = "LOT-1",
            ownerId = "USR-0871",
            campsites = listOf(campsites[1]),
            registrationStart = date(1, 1),
            registrationEnd = date(4, 20),
            bookingStart = date(4, 21),
            bookingEnd = date(4, 30),
            winnerCount = 2,
            waitlistCapacity = 25,
        ),
        Lottery(
            id = "LOT-2",
            ownerId = "USR-0932",
            campsites = listOf(campsites[5]),
            registrationStart = date(1, 15),
            registrationEnd = date(5, 2),
            bookingStart = date(5, 3),
            bookingEnd = date(5, 10),
            winnerCount = 1,
            waitlistCapacity = 15,
        ),
    )

    val entries = mutableListOf(
        LotteryEntry(
            id = "ENTRY-1",
            lotteryId = "LOT-1",
            entrantId = ENTRANT_ID,
            status = EntryStatus.WAITING,
        ),
        LotteryEntry(
            id = "ENTRY-2",
            lotteryId = "LOT-2",
            entrantId = ENTRANT_ID,
            status = EntryStatus.WAITING,
        ),
    )

    /** Reviews written through [submitReview], one per campsite. */
    val reviews = mutableListOf<ReviewDoc>()

    /** Entrants on each sample waiting list, including people not stored here. */
    val waitingListSizes = mutableMapOf(
        "LOT-1" to 18,
        "LOT-2" to 9,
    )

    override suspend fun getEntrantByAndroidId(androidId: String): Entrant =
        entrant.copy(androidId = androidId)

    override fun observeEntrant(androidId: String): Flow<Entrant?> =
        flowOf(entrant.copy(androidId = androidId))

    override suspend fun saveEntrant(entrant: Entrant) {
        this.entrant = entrant
    }

    override suspend fun deleteEntrant(androidId: String) = Unit

    override suspend fun getCampsites(): List<Campsite> = campsites.toList()

    override suspend fun getCampsite(campsiteId: String): Campsite? =
        campsites.firstOrNull { it.id == campsiteId }

    override suspend fun getReviewsByEntrant(entrantId: String): List<ReviewDoc> =
        reviews.filter { it.authorId == entrantId }

    override suspend fun submitReview(
        campsiteId: String,
        entrantId: String,
        rating: Double,
        comment: String,
    ) {
        val existing = reviews.firstOrNull {
            it.campsiteId == campsiteId && it.authorId == entrantId
        }
        val review = ReviewDoc(
            id = existing?.id ?: "REV-$campsiteId-$entrantId",
            campsiteId = campsiteId,
            authorId = entrantId,
            authorName = entrant.name,
            rating = rating,
            comment = comment.trim(),
            createdAt = existing?.createdAt,
        )
        if (existing != null) {
            reviews[reviews.indexOf(existing)] = review
        } else {
            reviews.add(review)
        }

        val index = campsites.indexOfFirst { it.id == campsiteId }
        if (index != -1) {
            val campsite = campsites[index]
            campsites[index] = if (existing != null) {
                campsite.copy(ratingSum = campsite.ratingSum - existing.rating + rating)
            } else {
                campsite.addRating(rating)
            }
        }
    }

    override suspend fun getReservations(entrantId: String): List<Reservation> =
        reservations.toList()

    override suspend fun cancelReservation(reservationId: String) {
        reservations.replaceAll {
            if (it.id == reservationId) it.copy(status = ReservationStatus.CANCELLED) else it
        }
    }

    override suspend fun getLotteryEntries(entrantId: String): List<LotteryEntry> =
        entries.toList()

    override suspend fun getLottery(lotteryId: String): Lottery? =
        lotteries.firstOrNull { it.id == lotteryId }

    override suspend fun getLotteryEntryCount(lotteryId: String): Int =
        waitingListSizes[lotteryId] ?: entries.count { it.lotteryId == lotteryId }

    override suspend fun leaveWaitingList(entryId: String) {
        val entry = entries.firstOrNull { it.id == entryId } ?: return
        entries.removeAll { it.id == entryId }
        waitingListSizes[entry.lotteryId] =
            ((waitingListSizes[entry.lotteryId] ?: 1) - 1).coerceAtLeast(0)
    }

    /** [month] and [day] in 2027 at the start of the day in the local time zone. */
    private fun date(month: Int, day: Int): Instant =
        LocalDate.of(2027, month, day)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
}
