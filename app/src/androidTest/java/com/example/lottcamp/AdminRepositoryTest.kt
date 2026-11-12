package com.example.lottcamp

import com.example.lottcamp.domain.Campsite
import com.example.lottcamp.domain.CampsiteOwner
import com.example.lottcamp.domain.Entrant
import com.example.lottcamp.domain.UserRole
import com.example.lottcamp.repository.AdminRepository
import com.example.lottcamp.repository.CampsiteOwnerRepository
import com.example.lottcamp.repository.CampsiteRepository
import com.example.lottcamp.repository.EntrantRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

/**
 * Runs [AdminRepository] against the real Firestore database.
 * Creates its own test documents in setUp and deletes them again in tearDown.
 */
class AdminRepositoryTest {

    private val admin = AdminRepository()
    private val entrantRepo = EntrantRepository()
    private val ownerRepo = CampsiteOwnerRepository()
    private val campsiteRepo = CampsiteRepository()

    private val entrant = Entrant("admin-test-entrant", "Test Entrant", "780-555-0100", "entrant@test.com")
    private val owner = CampsiteOwner("admin-test-owner", "Test Owner", "owner@test.com", "780-555-0101")
    private val campsite = Campsite(
        id = "admin-test-campsite",
        ownerId = owner.id,
        name = "Test Camp",
        locationName = "Test Location",
        maxOccupancy = 2,
        pricePerNight = 10.0,
    )

    @Before
    fun setUp(): Unit = runBlocking {
        entrantRepo.saveEntrant(entrant)
        ownerRepo.saveOwner(owner)
        campsiteRepo.saveCampsite(campsite)
    }

    @After
    fun tearDown(): Unit = runBlocking {
        entrantRepo.deleteEntrant(entrant.androidId)
        ownerRepo.deleteOwner(owner.id)
        campsiteRepo.deleteCampsite(campsite.id)
    }

    @Test
    fun getUsers_listsEntrantsAndOwnersWithTheirRole() = runBlocking {
        val users = admin.getUsers()
        assertEquals(UserRole.ENTRANT, users.first { it.id == entrant.androidId }.role)
        assertEquals(UserRole.OWNER, users.first { it.id == owner.id }.role)
    }

    @Test
    fun getCampsites_includesTheTestCampsite() = runBlocking {
        assertEquals(1, admin.getCampsites().count { it.id == campsite.id })
    }

    @Test
    fun removeUser_owner_alsoRemovesTheirCampsites() = runBlocking {
        val ownerProfile = admin.getUsers().first { it.id == owner.id }
        admin.removeUser(ownerProfile)
        assertNull(campsiteRepo.getCampsiteById(campsite.id))
        assertNull(ownerRepo.getOwnerById(owner.id))
    }

    @Test
    fun removeCampsite_removesIt() = runBlocking {
        admin.removeCampsite(campsite.id)
        assertNull(campsiteRepo.getCampsiteById(campsite.id))
    }

    @Test
    fun isAdmin_isFalseForAnIdThatIsNotInAdmins() = runBlocking {
        assertFalse(admin.isAdmin("admin-test-nobody"))
    }
}
