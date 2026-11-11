package com.example.lottcamp

import com.example.lottcamp.domain.Campsite
import com.example.lottcamp.repository.CampsiteRepository
import com.example.lottcamp.repository.LotteryRepository
import java.time.LocalDate

data class CampsiteFilter(
    val name: String? = null,
    val locationName: String? = null,
    val minOccupancy: Int? = null,
    val maxPricePerNight: Double? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val tags: List<String> = emptyList(),
) {
    suspend fun getMatches(campsiteRepo: CampsiteRepository, lotteryRepo: LotteryRepository): List<Campsite> {
        var matches = if (name != null)
            campsiteRepo.searchCampsitesByName(locationName!!)
        else
            campsiteRepo.getAllCampsites()  // or however you fetch everything

        if (tags.isNotEmpty()) {
            matches = matches.intersect(campsiteRepo.searchCampsitesByTags(tags)).toList()
        }
        minOccupancy?.let { guests ->
            matches = matches.intersect(campsiteRepo.searchCampsitesByOccupancy(guests)).toList()
        }
        maxPricePerNight?.let { price ->
            matches = matches.intersect(campsiteRepo.searchCampsitesByMaxPrice(price)).toList()
        }
        locationName?.let { name ->
            matches = campsiteRepo.searchCampsitesByLocation(name)
        }
        return matches
    }
}