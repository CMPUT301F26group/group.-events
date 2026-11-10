package com.example.lottcamp.controller

import com.example.lottcamp.domain.Campsite

/**
 * Controller for the admin home screen.
 *
 * Supplies the data the screen shows and handles its user actions.
 * Uses dummy data for now; later this will read from the repositories.
 */
class AdminHomeController {

    val userCount = 248
    val imageCount = 96
    val campsiteCount = 32

    val latestCampsites = listOf(
        Campsite(
            id = "1",
            ownerId = "owner1",
            name = "Pine Ridge Camp",
            locationName = "Hinton, AB",
            maxOccupancy = 6,
            pricePerNight = 85.0,
        ),
        Campsite(
            id = "2",
            ownerId = "owner2",
            name = "Bow Valley Retreat",
            locationName = "Canmore, AB",
            maxOccupancy = 4,
            pricePerNight = 120.0,
        ),
    )

    // TODO: hook these up once navigation and repositories are ready.
    fun onSearch(query: String) {}
    fun onBrowseCampsitesClick() {}
    fun onUsersClick() {}
    fun onImagesClick() {}
    fun onViewAllListingsClick() {}
    fun onCampsiteClick(campsite: Campsite) {}
    fun onNotificationsClick() {}
    fun onOwnerMessagesClick() {}
}
