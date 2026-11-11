package com.example.lottcamp

import com.example.lottcamp.domain.Campsite
import com.example.lottcamp.repository.CampsiteRepository
import com.example.lottcamp.repository.LotteryRepository
import java.time.LocalDate

data class CampsiteFilter(
    val locationName: String? = null,
    val minOccupancy: Int? = null,
    val maxPricePerNight: Double? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val tags: List<String> = emptyList(),
) {
    suspend fun getMatches(campsiteRepo: CampsiteRepository, lotteryRepo: LotteryRepository): List<Campsite> {
        // TODO: need to query campsite according to availability, need the lottery repo for this
        var matches = emptyList<Campsite>()

        locationName?.let { name ->
            matches = campsiteRepo.searchCampsitesByName(name)
        }
        matches = matches.intersect(campsiteRepo.searchCampsitesByTags(tags)).toList()

        minOccupancy?.let { guests ->
            matches = matches.intersect(campsiteRepo.searchCampsitesByOccupancy(guests)).toList()
        }
        maxPricePerNight?.let { price ->
            matches = matches.intersect(campsiteRepo.searchCampsitesByMaxPrice(price)).toList()
        }
        return matches
    }
}