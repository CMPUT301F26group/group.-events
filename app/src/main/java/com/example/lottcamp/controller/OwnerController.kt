package com.example.lottcamp.controller

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.lottcamp.domain.Campsite
import com.example.lottcamp.domain.Lottery
import com.example.lottcamp.domain.Reservation
import com.example.lottcamp.repository.CampsiteRepository
import com.example.lottcamp.repository.LotteryRepository
import com.example.lottcamp.repository.ReservationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.time.Instant

class OwnerController(
    private val ownerId: String,
    private val data: CampsiteRepository,
    private val lotteryData: LotteryRepository,
    private val reservationData: ReservationRepository,
    private val scope: CoroutineScope,
) {
    val campsites = mutableStateListOf<Campsite>()
    val lotteries = mutableStateListOf<Lottery>()
    val reservations = mutableStateListOf<Reservation>()

    var saving by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    fun refresh() {
        runInBackground("Couldn't load your campsites") { load() }
    }

    fun campsite(id: String) = campsites.find { it.id == id }

    fun latestLottery(): Lottery? = lotteries.maxByOrNull { it.registrationStart }

    fun lotteryFor(campsiteId: String): Lottery? {
        val now = Instant.now()
        return lotteries
            .filter { lottery -> lottery.campsites.any { it.id == campsiteId } && now.isBefore(lottery.bookingEnd) }
            .maxByOrNull { it.registrationStart }
    }

    fun status(campsite: Campsite): String =
        if (lotteryFor(campsite.id)?.isRegistrationOpen() == true) "Lottery open" else "Published"

    fun saveCampsite(campsite: Campsite, onSaved: () -> Unit) {
        if (saving) return
        runInBackground("Couldn't save the campsite") {
            saving = true
            try {
                withTimeout(10_000) { data.saveCampsite(campsite) }
                load()
                onSaved()
            } finally {
                saving = false
            }
        }
    }

    fun report(message: String) { error = message }
    fun clearError() { error = null }

    private suspend fun load() {
        val newCampsites = data.getCampsitesByOwner(ownerId)
        val newLotteries = lotteryData.getLotteriesByOwner(ownerId)
        val newReservations = reservationData.getReservationsForCampsites(newCampsites.map { it.id })

        campsites.clear(); campsites.addAll(newCampsites)
        lotteries.clear(); lotteries.addAll(newLotteries)
        reservations.clear(); reservations.addAll(newReservations)
    }

    private fun runInBackground(errorMessage: String, work: suspend () -> Unit) {
        scope.launch {
            try {
                work()
            } catch (e: Exception) {
                error = "$errorMessage: ${e.message}"
            }
        }
    }
}