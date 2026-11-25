package com.example.lottcamp.repository

import com.example.lottcamp.data.EntryStatus
import com.example.lottcamp.data.ReservationStatus
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Unit tests for [SampleEntrantRepository]'s in-memory booking data. */
class SampleEntrantRepositoryTest {

    private val data = SampleEntrantRepository()

    @Test
    fun reservations_areSplitBetweenActiveAndPast() = runBlocking {
        val reservations = data.getReservations(SampleEntrantRepository.ENTRANT_ID)

        assertTrue(reservations.any { it.status == ReservationStatus.ACTIVE })
        assertTrue(reservations.any { it.status == ReservationStatus.COMPLETED })
    }

    @Test
    fun campsiteLookup_returnsSampleCampsite() = runBlocking {
        val campsite = data.getCampsite("CAMP-001")

        assertEquals("Pine Ridge Camp", campsite?.name)
        assertEquals("Hinton, AB", campsite?.locationName)
    }

    @Test
    fun submitReview_addsTheRatingToTheCampsiteAverage() = runBlocking {
        val before = data.getCampsite("CAMP-001")!!

        data.submitReview("CAMP-001", SampleEntrantRepository.ENTRANT_ID, 5.0, "Loved it")

        val after = data.getCampsite("CAMP-001")!!
        assertEquals(before.ratingSum + 5.0, after.ratingSum, 0.001)
        assertEquals(before.ratingCount + 1, after.ratingCount)
        assertTrue("CAMP-001" in data.getReviewedCampsiteIds(SampleEntrantRepository.ENTRANT_ID))
    }

    @Test
    fun submitReview_twice_updatesTheSameReview() = runBlocking {
        data.submitReview("CAMP-001", SampleEntrantRepository.ENTRANT_ID, 5.0, "First")
        data.submitReview("CAMP-001", SampleEntrantRepository.ENTRANT_ID, 2.0, "Updated")

        val reviews = data.getReviewsByEntrant(SampleEntrantRepository.ENTRANT_ID)
            .filter { it.campsiteId == "CAMP-001" }
        assertEquals(1, reviews.size)
        assertEquals(2.0, reviews.single().rating, 0.001)
        assertEquals("Updated", reviews.single().comment)

        val campsite = data.getCampsite("CAMP-001")!!
        assertEquals(27.0 + 2.0, campsite.ratingSum, 0.001)
        assertEquals(7, campsite.ratingCount)
    }

    @Test
    fun unknownCampsite_returnsNull() = runBlocking {
        assertNull(data.getCampsite("missing"))
    }

    @Test
    fun cancelReservation_marksItCancelled() = runBlocking {
        data.cancelReservation("RES-2038")

        val cancelled = data.reservations.first { it.id == "RES-2038" }
        assertEquals(ReservationStatus.CANCELLED, cancelled.status)
    }

    @Test
    fun waitingLists_haveLotteryAndEntrantCount() = runBlocking {
        val entry = data.getLotteryEntries(SampleEntrantRepository.ENTRANT_ID)
            .first { it.status == EntryStatus.WAITING }
        val lottery = data.getLottery(entry.lotteryId)

        assertEquals("Bow Valley Retreat", lottery?.campsites?.first()?.name)
        assertEquals(18, data.getLotteryEntryCount("LOT-1"))
    }

    @Test
    fun leaveWaitingList_removesTheEntryAndUpdatesTheCount() = runBlocking {
        data.leaveWaitingList("ENTRY-1")

        assertTrue(data.getLotteryEntries(SampleEntrantRepository.ENTRANT_ID).none { it.id == "ENTRY-1" })
        assertEquals(17, data.getLotteryEntryCount("LOT-1"))
    }
}
