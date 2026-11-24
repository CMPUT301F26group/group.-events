package com.example.lottcamp
import android.net.Uri
import androidx.test.platform.app.InstrumentationRegistry
import com.example.lottcamp.domain.Campsite
import com.example.lottcamp.domain.CampsiteOwner
import com.example.lottcamp.domain.Entrant
import com.example.lottcamp.domain.Lottery
import com.example.lottcamp.domain.LotteryEntry
import com.example.lottcamp.repository.CampsiteOwnerRepository
import com.example.lottcamp.repository.CampsiteRepository
import com.example.lottcamp.repository.EntrantRepository
import com.example.lottcamp.repository.ImageRepository
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
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId


class RepositoryTest {

    companion object {
        /** Start of the given day in 2026, Edmonton time. */
        private fun date(month: Int, day: Int): Instant =
            LocalDate.of(2026, month, day)
                .atStartOfDay(ZoneId.of("America/Edmonton"))
                .toInstant()

        // Entrants
        val entrant1 = Entrant("entrant1", "Beloved Bingus", "780 420 6969", "bingus@cherished.ca")
        val entrant2 = Entrant("entrant2", "Door Bell Cat", "587 676 1234", "cat@door.ca")
        val entrant3 = Entrant("entrant3", "Senor Soggy", "403 111 0000", "the.sogster@soggy.ca")

        // Owners
        val owner1 = CampsiteOwner("owner1", "Harold Hibachi", "buffalo.lover@albertacampingenthusiasts.ca", "780 555 0111")
        val owner2 = CampsiteOwner("owner2", "Gerlado Gonzalez", "canyon.connoisseur@albertacampingenthusiasts.ca", "403 555 0122")

        // Campsites
        val elkIsland = Campsite(
            id = "elk-island",
            ownerId = owner1.id,
            name = "Elk Island Lakeside Camp",
            locationName = "Elk Island National Park, AB",
            latitude = 53.5950,
            longitude = -112.8650,
            description = "Quiet lakeside sites in the aspen parkland east of Edmonton. Bison and elk are regular visitors.",
            maxOccupancy = 6,
            pricePerNight = 45.0,
            tags = listOf("Lakeside", "Wildlife", "Fire pit", "Pet friendly"),
            rulesAndPolicy = "Quiet hours 10 PM to 7 AM. Do not feed wildlife. Pets must be leashed.",
        )
        val woodBuffalo = Campsite(
            id = "wood-buffalo",
            ownerId = owner1.id,
            name = "Wood Buffalo Dark Sky Camp",
            locationName = "Wood Buffalo National Park, AB",
            latitude = 59.3000,
            longitude = -112.9000,
            description = "Remote prairie and boreal sites in a dark sky preserve with almost no light pollution. Bring everything you need.",
            maxOccupancy = 10,
            pricePerNight = 35.0,
            tags = listOf("Remote", "Stargazing", "Wildlife", "Fire pit"),
            rulesAndPolicy = "No services on site. Pack out all garbage. Check fire bans before arrival.",
        )
        val maligneCanyon = Campsite(
            id = "maligne-canyon",
            ownerId = owner2.id,
            name = "Maligne Canyon Camp",
            locationName = "Jasper, AB",
            latitude = 52.9267,
            longitude = -117.9827,
            description = "Forested sites a short walk from the canyon trails and footbridges.",
            maxOccupancy = 8,
            pricePerNight = 65.0,
            tags = listOf("Hiking", "Canyon views", "Fire pit", "Bear lockers"),
            rulesAndPolicy = "Fires only in provided pits. Store all food in the bear lockers. Maximum two vehicles per site.",
        )
        val moraineLake = Campsite(
            id = "moraine-lake",
            ownerId = owner2.id,
            name = "Moraine Lake Shore Camp",
            locationName = "Banff, AB",
            latitude = 51.3217,
            longitude = -116.1860,
            description = "Alpine tent sites beneath the Valley of the Ten Peaks with the lake just steps away.",
            maxOccupancy = 4,
            pricePerNight = 120.0,
            tags = listOf("Lake views", "Mountain views", "Hiking", "Canoeing"),
            rulesAndPolicy = "Tents only, no RVs. No drones. Pack out all garbage.",
        )
        val watertonLookout = Campsite(
            id = "waterton-lookout",
            ownerId = owner2.id,
            name = "Prince of Wales Lookout Camp",
            locationName = "Waterton Park, AB",
            latitude = 49.0561,
            longitude = -113.9142,
            description = "Sites on the bluff above Waterton Lake with views of the historic hotel and the townsite.",
            maxOccupancy = 6,
            pricePerNight = 85.0,
            tags = listOf("Lake views", "Windy", "Fire pit", "Near town"),
            rulesAndPolicy = "Secure tents and gear, strong winds are common. Quiet hours 10 PM to 7 AM.",
        )

        // Some sample lotteries which are valid during time of demo
        val lottery1 = Lottery(
            "lottery1", owner1.id, listOf(elkIsland, woodBuffalo),
            date(11, 20), date(12, 2), date(12, 4), date(12, 11), 1,
        )
        val lottery2 = Lottery(
            "lottery2", owner2.id, listOf(moraineLake, maligneCanyon),
            date(11, 20), date(12, 4), date(12, 6), date(12, 13), 1,
        )
        val lottery3 = Lottery(
            "lottery3", owner2.id, listOf(watertonLookout),
            date(12, 1), date(12, 8), date(12, 10), date(12, 17), 1,
        )
        val lottery4 = Lottery(
            "lottery4", owner1.id, listOf(elkIsland),
            date(12, 8), date(12, 15), date(12, 18), date(12, 25), 1,
        )

        val db = Firebase.firestore
        val entrant_repo = EntrantRepository(db)
        val campsite_owner_repo = CampsiteOwnerRepository(db)
        val campsite_repo = CampsiteRepository(db)
        val lottery_repo = LotteryRepository(db)
        val lottery_entry_repo = LotteryEntryRepository(db)
        val image_repo = ImageRepository(db)

        /** Copies [fileName] from the test assets to the cache directory and saves it as an image document. */
        private suspend fun saveAsset(
            fileName: String,
            maxSide: Int = 800,
            maxBytes: Int = 150_000,
        ): String {
            val instrumentation = InstrumentationRegistry.getInstrumentation()
            val context = instrumentation.targetContext
            val file = File(context.cacheDir, fileName)
            instrumentation.context.assets.open(fileName).use { input ->
                file.outputStream().use { input.copyTo(it) }
            }
            return image_repo.saveFromUri(context, Uri.fromFile(file), maxSide, maxBytes)
        }

        @JvmStatic
        @BeforeClass
                /**
                 * Populate the database with test data
                 * */
        fun setupTestData(): Unit = runBlocking {
            // Profile photos are loaded in bulk by the admin screens, so keep them small
            entrant_repo.saveEntrant(entrant1.copy(profilePictureId = saveAsset("beloved_bingus.webp", 256, 30_000)))
            entrant_repo.saveEntrant(entrant2.copy(profilePictureId = saveAsset("doorbell_cat.webp", 256, 30_000)))
            entrant_repo.saveEntrant(entrant3.copy(profilePictureId = saveAsset("soggy_cat.webp", 256, 30_000)))

            campsite_owner_repo.saveOwner(owner1)
            campsite_owner_repo.saveOwner(owner2)

            campsite_repo.saveCampsite(elkIsland.copy(imageId = saveAsset("elk_island.webp")))
            campsite_repo.saveCampsite(woodBuffalo.copy(imageId = saveAsset("wood_buffalo_national_park.webp")))
            campsite_repo.saveCampsite(maligneCanyon.copy(imageId = saveAsset("maligne_canyon_jasper.webp")))
            campsite_repo.saveCampsite(moraineLake.copy(imageId = saveAsset("moraine_lake_banff.webp")))
            campsite_repo.saveCampsite(watertonLookout.copy(imageId = saveAsset("prince_of_wales_hotel_waterton.webp")))

            lottery_repo.saveLottery(lottery1)
            lottery_repo.saveLottery(lottery2)
            lottery_repo.saveLottery(lottery3)
            lottery_repo.saveLottery(lottery4)
        }
    }

    // --- EntrantRepository Tests ---

    @Test
    fun testGetEntrantByAndroidId() = runBlocking {
        val fetched = entrant_repo.getEntrantByAndroidId(entrant1.androidId)
        assertNotNull("Entrant 1 should exist in database", fetched)
        assertEquals(entrant1.name, fetched?.name)
        assertEquals(entrant1.phoneNumber, fetched?.phoneNumber)
        assertEquals(entrant1.email, fetched?.email)
    }

    @Test
    fun testUpdateEntrantProfile() = runBlocking {
        // Use a throwaway entrant so the seeded entrants (and their photos) aren't modified
        val temp = Entrant("entrant_update_test", "Temp Entrant", "780 555 0199", "temp@example.com")
        entrant_repo.saveEntrant(temp)
        try {
            entrant_repo.saveEntrant(temp.copy(name = "Temp Updated", notificationsEnabled = true))
            val fetched = entrant_repo.getEntrantByAndroidId(temp.androidId)
            assertNotNull(fetched)
            assertEquals("Temp Updated", fetched?.name)
            assertEquals(true, fetched?.notificationsEnabled)
        } finally {
            entrant_repo.deleteEntrant(temp.androidId)
        }
    }

    // --- CampsiteOwnerRepository Tests ---

    @Test
    fun testGetOwnerById() = runBlocking {
        val fetchedOwner = campsite_owner_repo.getOwnerById(owner1.id)
        assertNotNull("Owner 1 should exist in database", fetchedOwner)
        assertEquals(owner1.name, fetchedOwner?.name)
        assertEquals(owner1.email, fetchedOwner?.email)
    }

    // --- CampsiteRepository Tests ---

    @Test
    fun testGetCampsiteById() = runBlocking {
        val fetchedCamp = campsite_repo.getCampsiteById(moraineLake.id)
        assertNotNull("Moraine Lake camp should exist in database", fetchedCamp)
        assertEquals(moraineLake.name, fetchedCamp?.name)
        assertEquals(moraineLake.locationName, fetchedCamp?.locationName)
        assertEquals(moraineLake.pricePerNight, fetchedCamp?.pricePerNight ?: 0.0, 0.01)
        assertEquals(moraineLake.latitude ?: 0.0, fetchedCamp?.latitude ?: 0.0, 0.0001)
        assertEquals(moraineLake.longitude ?: 0.0, fetchedCamp?.longitude ?: 0.0, 0.0001)
        assertNotNull("Campsite should point at its saved image", fetchedCamp?.imageId)
    }

    @Test
    fun testGetCampsitesByOwner() = runBlocking {
        val owner1Names = campsite_repo.getCampsitesByOwner(owner1.id).map { it.name }
        assertEquals(2, owner1Names.size)
        assertTrue(owner1Names.containsAll(listOf(elkIsland.name, woodBuffalo.name)))

        val owner2Names = campsite_repo.getCampsitesByOwner(owner2.id).map { it.name }
        assertEquals(3, owner2Names.size)
        assertTrue(owner2Names.containsAll(listOf(maligneCanyon.name, moraineLake.name, watertonLookout.name)))
    }

    // --- LotteryRepository & Hydration Tests ---

    @Test
    fun testGetLotteryByIdAndHydrateCampsites() = runBlocking {
        val fetchedLottery = lottery_repo.getLotteryById(lottery1.id)
        assertNotNull("Lottery should exist in database", fetchedLottery)
        assertEquals(lottery1.id, fetchedLottery?.id)
        assertEquals(owner1.id, fetchedLottery?.ownerId)

        val campNames = fetchedLottery?.campsites?.map { it.name } ?: emptyList()
        assertEquals(2, campNames.size)
        assertTrue(campNames.containsAll(listOf(elkIsland.name, woodBuffalo.name)))
    }

    // --- LotteryEntryRepository Tests ---

    @Test
    fun testGetEntriesForLottery() = runBlocking {
        assertTrue(lottery_entry_repo.getEntriesForLottery(lottery1.id).isEmpty())
    }

    @Test
    fun testGetEntriesForEntrant() = runBlocking {
        assertTrue(lottery_entry_repo.getEntriesForEntrant(entrant1.androidId).isEmpty())
    }
}