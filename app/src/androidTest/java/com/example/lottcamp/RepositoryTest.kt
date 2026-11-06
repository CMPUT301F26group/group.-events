package com.example.lottcamp
import com.example.lottcamp.domain.Campsite
import com.example.lottcamp.domain.CampsiteOwner
import com.example.lottcamp.domain.Entrant
import com.example.lottcamp.domain.Lottery
import com.example.lottcamp.domain.LotteryEntry
import com.example.lottcamp.repository.CampsiteOwnerRepository
import com.example.lottcamp.repository.CampsiteRepository
import com.example.lottcamp.repository.EntrantRepository
import com.example.lottcamp.repository.LotteryEntryRepository
import com.example.lottcamp.repository.LotteryRepository
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import org.junit.Test
import kotlinx.coroutines.runBlocking
import org.junit.AfterClass
import org.junit.BeforeClass
import java.time.Instant


class RepositoryTest {

    companion object {
        val farFuture = Instant.parse("2030-12-31T23:59:59Z")
        val entrant1 = Entrant(
            "entrant1",
            "geraldo",
            "780 666 6969",
            "yomum@gmail.com"
        )
        val entrant2 = Entrant(
            "entrant2",
            "bingus",
            "780 777 6969",
            "imtiredboss@hotmail.com"
        )
        val owner1 = CampsiteOwner(
            "owner1",
            "Mr. owner guy",
            "mrownerguy1@campsite.ai",
            "111 222 8888",
        )
        val campsite1 = Campsite(
            "campsite1",
            owner1.id,
            "Campsite 1",
            "Edmonton, AB",
            pricePerNight = 1000.0,
            maxOccupancy = 3
        )
        val campsite2 = Campsite(
            "campsite2",
            owner1.id,
            "Campsite 2",
            "St. Paul, AB",
            pricePerNight = 0.000005,
            maxOccupancy = 100000,
            tags = listOf("Terrifying", "Bone chilling", "Sunny")
        )
        val lottery = Lottery(
            "lottery1",
            owner1.id,
            listOf(campsite2, campsite1),
            Instant.now(),
            farFuture,
            Instant.now(),
            farFuture,
            1
        )
        val lotteryEntry1 = LotteryEntry(
            "entry1",
            lottery.id,
            entrant1.androidId
        )
        val lotteryEntry2 = LotteryEntry(
            "entry2",
            lottery.id,
            entrant2.androidId
        )

        val db = Firebase.firestore
        val entrant_repo = EntrantRepository(db)
        val campsite_owner_repo = CampsiteOwnerRepository(db)
        val campsite_repo = CampsiteRepository(db)
        val lottery_repo = LotteryRepository(db)
        val lottery_entry_repo = LotteryEntryRepository(db)

        @JvmStatic
        @BeforeClass
        /**
         * Populate the database with test data
        * */
        fun setupTestData(): Unit = runBlocking {
            entrant_repo.saveEntrant(entrant1)
            entrant_repo.saveEntrant(entrant2)
            campsite_owner_repo.saveOwner(owner1)
            campsite_repo.saveCampsite(campsite1)
            campsite_repo.saveCampsite(campsite2)
            lottery_repo.saveLottery(lottery)
            lottery_entry_repo.saveEntry(lotteryEntry1)
            lottery_entry_repo.saveEntry(lotteryEntry2)
        }

        @JvmStatic
        @AfterClass
        /**
         * Populate the database with test data
         * */
        fun deleteTestData(): Unit = runBlocking {
            entrant_repo.deleteEntrant(entrant1.androidId)
            entrant_repo.deleteEntrant(entrant2.androidId)
            campsite_owner_repo.deleteOwner(owner1.id)
            campsite_repo.deleteCampsite(campsite1.id)
            campsite_repo.deleteCampsite(campsite2.id)
            lottery_repo.deleteLottery(lottery.id)
            lottery_entry_repo.deleteEntry(lotteryEntry1.id)
            lottery_entry_repo.deleteEntry(lotteryEntry2.id)
        }
    }

    // --- EntrantRepository Tests ---

    @Test
    fun testGetEntrantByAndroidId() = runBlocking {
        val fetched = entrant_repo.getEntrantByAndroidId(entrant1.androidId)
        assertNotNull("Entrant 1 should exist in database", fetched)
        assertEquals("geraldo", fetched?.name)
        assertEquals("780 666 6969", fetched?.phoneNumber)
        assertEquals("yomum@gmail.com", fetched?.email)
    }

    @Test
    fun testUpdateEntrantProfile() = runBlocking {
        val updatedEntrant = entrant2.copy(name = "bingus_updated", notificationsEnabled = true)
        entrant_repo.saveEntrant(updatedEntrant)

        val fetched = entrant_repo.getEntrantByAndroidId(entrant2.androidId)
        assertNotNull(fetched)
        assertEquals("bingus_updated", fetched?.name)
        assertEquals(true, fetched?.notificationsEnabled)
    }


    // --- CampsiteOwnerRepository Tests ---

    @Test
    fun testGetOwnerById() = runBlocking {
        val fetchedOwner = campsite_owner_repo.getOwnerById(owner1.id)
        assertNotNull("Owner 1 should exist in database", fetchedOwner)
        assertEquals("Mr. owner guy", fetchedOwner?.name)
        assertEquals("mrownerguy1@campsite.ai", fetchedOwner?.email)
    }


    // --- CampsiteRepository Tests ---

    @Test
    fun testGetCampsiteById() = runBlocking {
        val fetchedCamp = campsite_repo.getCampsiteById(campsite1.id)
        assertNotNull("Campsite 1 should exist in database", fetchedCamp)
        assertEquals("Campsite 1", fetchedCamp?.name)
        assertEquals("Edmonton, AB", fetchedCamp?.locationName)
        assertEquals(1000.0, fetchedCamp?.pricePerNight ?: 0.0, 0.01)
    }

    @Test
    fun testGetCampsitesByOwner() = runBlocking {
        val ownerCampsites = campsite_repo.getCampsitesByOwner(owner1.id)
        assertEquals("Owner 1 should own 2 campsites", 2, ownerCampsites.size)

        val names = ownerCampsites.map { it.name }
        assertTrue(names.contains("Campsite 1"))
        assertTrue(names.contains("Campsite 2"))
    }


    // --- LotteryRepository & Hydration Tests ---

    @Test
    fun testGetLotteryByIdAndHydrateCampsites() = runBlocking {
        val fetchedLottery = lottery_repo.getLotteryById(lottery.id)
        assertNotNull("Lottery should exist in database", fetchedLottery)
        assertEquals(lottery.id, fetchedLottery?.id)
        assertEquals(owner1.id, fetchedLottery?.ownerId)

        // Verify relationship hydration (campsiteIds -> domain Campsite models)
        val campsites = fetchedLottery?.campsites ?: emptyList()
        assertEquals(2, campsites.size)

        val campNames = campsites.map { it.name }
        assertTrue(campNames.contains("Campsite 1"))
        assertTrue(campNames.contains("Campsite 2"))
    }


    // --- LotteryEntryRepository Tests ---

    @Test
    fun testGetEntriesForLottery() = runBlocking {
        val entries = lottery_entry_repo.getEntriesForLottery(lottery.id)
        assertEquals("Lottery 1 should have 2 entries", 2, entries.size)

        val entrantIds = entries.map { it.entrantId }
        assertTrue(entrantIds.contains(entrant1.androidId))
        assertTrue(entrantIds.contains(entrant2.androidId))
    }

    @Test
    fun testGetEntriesForEntrant(): Unit = runBlocking {
        val entrant1Entries = lottery_entry_repo.getEntriesForEntrant(entrant1.androidId)
        assertEquals(1, entrant1Entries.size)
        assertEquals(lottery.id, entrant1Entries.first().lotteryId)
    }
}