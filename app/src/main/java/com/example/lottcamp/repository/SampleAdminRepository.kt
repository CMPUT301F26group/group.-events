package com.example.lottcamp.repository

import com.example.lottcamp.domain.Campsite
import com.example.lottcamp.domain.CampsiteActivity
import com.example.lottcamp.domain.EntrantActivity
import com.example.lottcamp.domain.ImageType
import com.example.lottcamp.domain.MessageLog
import com.example.lottcamp.domain.NotificationLog
import com.example.lottcamp.domain.OwnerActivity
import com.example.lottcamp.domain.Review
import com.example.lottcamp.domain.UploadedImage
import com.example.lottcamp.domain.UserProfile
import com.example.lottcamp.domain.UserRole

/**
 * [AdminRepository] that keeps sample data in memory. Used by previews and tests
 * so they never read from or write to the real database.
 */
open class SampleAdminRepository : AdminRepository() {
    val users = mutableListOf(
        UserProfile("USR-1042", "Alex Morgan", "alex.morgan@example.com", "780-555-0142", UserRole.ENTRANT, "https://photo.invalid/alex.png"),
        UserProfile("USR-0871", "Jamie Chen", "jamie.chen@example.com", "780-555-0188", UserRole.OWNER),
        UserProfile("USR-1107", "Sam Patel", "sam.patel@example.com", "780-555-0119", UserRole.ENTRANT, "https://photo.invalid/sam.png"),
        UserProfile("USR-0932", "Robin Lee", "robin.lee@example.com", "780-555-0175", UserRole.OWNER),
        UserProfile("USR-1210", "Taylor Brooks", "taylor.b@example.com", "780-555-0101", UserRole.ENTRANT),
        UserProfile("USR-0001", "Jordan Wilson", "jordan.w@example.com", "780-555-0100", UserRole.ADMIN),
    )

    val campsites = mutableListOf(
        Campsite("CAMP-001", "USR-0871", "Pine Ridge Camp", "Hinton, AB", maxOccupancy = 6, pricePerNight = 85.0, imageUrl = "https://photo.invalid/pine.png"),
        Campsite("CAMP-002", "USR-0871", "Bow Valley Retreat", "Canmore, AB", maxOccupancy = 4, pricePerNight = 120.0, imageUrl = "https://photo.invalid/bow.png"),
        Campsite("CAMP-003", "USR-0932", "Meadow Ridge", "Banff, AB", maxOccupancy = 8, pricePerNight = 95.0),
    )

    val reviews = mutableListOf(
        Review("REV-1", "CAMP-001", "Alex Morgan", 5, "Great spot by the river. The cookhouse was spotless."),
        Review("REV-2", "CAMP-001", "Sam Patel", 4, "Quiet and well kept. Gravel parking is a little tight."),
        Review("REV-3", "CAMP-001", "Robin Lee", 1, "This review contains language that breaks app policy."),
    )

    private val notifications = listOf(
        NotificationLog(
            "NTF-1", "Lottery selection", NotificationLog.SYSTEM_SENDER, null, "Alex Morgan", "USR-1042",
            "You were selected in the Pine Ridge lottery. Choose from the remaining dates to reserve your stay.",
            1_794_407_400_000,
        ),
        NotificationLog(
            "NTF-2", "Reservation confirmed", NotificationLog.SYSTEM_SENDER, null, "Sam Patel", "USR-1107",
            "Check-in opens at 3 PM. Find the gate instructions in your reservation details.",
            1_794_402_900_000,
        ),
        NotificationLog(
            "NTF-3", "Check-in instructions", "Jamie Chen", "USR-0871", "Alex Morgan", "USR-1042",
            "Check-in opens at 3 PM. Find the gate instructions in your reservation details.",
            1_794_351_600_000,
        ),
    )

    private val messages = listOf(
        MessageLog(
            "MSG-1", "Jamie Chen", "Alex Morgan", "Pine Ridge Camp", "RES-2038",
            "Hi Alex, check-in starts at 3 PM. The cookhouse will be available throughout your stay.",
            1_794_409_320_000,
        ),
        MessageLog(
            "MSG-2", "Jamie Chen", "Sam Patel", "Bow Valley Retreat", "RES-2041",
            "Thanks for your reservation. Let me know if you have any questions before your stay.",
            1_794_334_800_000,
        ),
        MessageLog(
            "MSG-3", "Robin Lee", "Jordan Wilson", "Meadow Ridge", "RES-2027",
            "The north gate is open. Please use the gravel parking area beside your campsite.",
            1_794_134_700_000,
        ),
    )

    override suspend fun getUsers() = users.toList()
    override suspend fun getCampsites() = campsites.toList()
    override suspend fun getReviews() = reviews.toList()
    override suspend fun getNotifications() = notifications
    override suspend fun getMessages() = messages

    override suspend fun getEntrantActivity(userId: String) = EntrantActivity(2, 5, 3)

    override suspend fun getOwnerActivity(ownerId: String) =
        OwnerActivity(campsites.count { it.ownerId == ownerId }, 2, 4)

    override suspend fun getCampsiteActivity(campsiteId: String) = CampsiteActivity(2, 1)

    override suspend fun removeUser(user: UserProfile) {
        if (user.role == UserRole.OWNER) campsites.removeAll { it.ownerId == user.id }
        users.removeAll { it.id == user.id }
    }

    override suspend fun removeCampsite(campsiteId: String) {
        campsites.removeAll { it.id == campsiteId }
    }

    override suspend fun removeReview(review: Review) {
        reviews.removeAll { it.id == review.id }
    }

    override suspend fun removeImage(image: UploadedImage) {
        when (image.type) {
            ImageType.CAMPSITE -> campsites.replaceAll { if (it.id == image.sourceId) it.copy(imageUrl = null) else it }
            ImageType.PROFILE -> users.replaceAll { if (it.id == image.sourceId) it.copy(photoUrl = null) else it }
        }
    }
}
