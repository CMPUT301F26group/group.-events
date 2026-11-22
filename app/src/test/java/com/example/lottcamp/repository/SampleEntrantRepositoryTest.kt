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
