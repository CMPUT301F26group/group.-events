package com.example.lottcamp.controller

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.lottcamp.data.EntryStatus
import com.example.lottcamp.domain.Campsite
import com.example.lottcamp.domain.Entrant
import com.example.lottcamp.domain.Lottery
import com.example.lottcamp.domain.LotteryEntry
import com.example.lottcamp.repository.CampsiteRepository
import com.example.lottcamp.repository.EntrantRepository
import com.example.lottcamp.repository.ImageRepository
import com.example.lottcamp.repository.LotteryEntryRepository
import com.example.lottcamp.repository.LotteryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

/**
 * Controller for the entrant campsite details screen and interactive lottery calendar.
 *
 * Loads campsite details, upcoming/open lotteries, existing entrant entries, and waitlist
 * counts, while handling date selection and waitlist enrollment.
 *
 * @param campsiteId Firestore ID of the displayed campsite.
 * @param entrantId Current entrant identifier (device Android ID or test ID).
 * @param scope Coroutine scope used for repository operations.
 * @param campsiteRepo Repository for campsite metadata.
 * @param lotteryRepo Repository for campsite lotteries.
 * @param lotteryEntryRepo Repository for lottery entries and waitlist counts.
 * @param entrantRepo Repository for entrant profiles.
 * @param imageRepo Repository for campsite images.
 * @param navigate Navigation callback accepting a route string.
 * @param back Navigation callback to return to the previous screen.
 * @param zoneId Time zone used when mapping lottery instants to calendar dates.
 */
class EntrantCampsiteDetailsController(
    private val campsiteId: String,
    private val entrantId: String,
    private val scope: CoroutineScope,
    private val campsiteRepo: CampsiteRepository = CampsiteRepository(),
    private val lotteryRepo: LotteryRepository = LotteryRepository(),
    private val lotteryEntryRepo: LotteryEntryRepository = LotteryEntryRepository(),
    private val entrantRepo: EntrantRepository = EntrantRepository(),
    val imageRepo: ImageRepository = ImageRepository(),
    private val navigate: (String) -> Unit = {},
    private val back: () -> Unit = {},
    val zoneId: ZoneId = ZoneId.systemDefault(),
) {
    var campsite by mutableStateOf<Campsite?>(null)
        private set

    val upcomingLotteries = mutableStateListOf<Lottery>()
    private val entrantEntries = mutableStateListOf<LotteryEntry>()

    var selectedLottery by mutableStateOf<Lottery?>(null)
        private set

    var selectedWaitlistCount by mutableIntStateOf(0)
        private set

    var displayedYearMonth by mutableStateOf(YearMonth.now(zoneId))
        private set

    var isSubmitting by mutableStateOf(false)
        private set

    var statusMessage by mutableStateOf<String?>(null)
        private set

    var isErrorMessage by mutableStateOf(false)
        private set

    private val dateToLotteryMap: Map<LocalDate, Lottery>
        get() = buildMap {
            for (lottery in upcomingLotteries) {
                for (date in lottery.bookingDates(zoneId)) {
                    put(date, lottery)
                }
            }
        }

    /** Days in [displayedYearMonth] belonging to any upcoming or open lottery. */
    val availableDaysInMonth: Set<Int>
        get() = upcomingLotteries
            .flatMap { it.bookingDates(zoneId) }
            .filter { YearMonth.from(it) == displayedYearMonth }
            .map { it.dayOfMonth }
            .toSet()

    /** Days in [displayedYearMonth] belonging to [selectedLottery]. */
    val selectedDaysInMonth: Set<Int>
        get() = selectedLottery
            ?.bookingDates(zoneId)
            ?.filter { YearMonth.from(it) == displayedYearMonth }
            ?.map { it.dayOfMonth }
            ?.toSet()
            ?: emptySet()

    /** True when the entrant already has a waiting or winning entry for [selectedLottery]. */
    val alreadyJoinedSelected: Boolean
        get() {
            val currentId = selectedLottery?.id ?: return false
            return entrantEntries.any {
                it.lotteryId == currentId &&
                        (it.status == EntryStatus.WAITING || it.status == EntryStatus.WON)
            }
        }

    val isSelectedNotYetOpen: Boolean
        get() = selectedLottery?.let { Instant.now().isBefore(it.registrationStart) } == true

    val isSelectedOpen: Boolean
        get() = selectedLottery?.isRegistrationOpen(Instant.now()) == true

    val isSelectedFull: Boolean
        get() = selectedLottery?.isWaitlistFull(selectedWaitlistCount) == true

    val canJoin: Boolean
        get() = selectedLottery != null && isSelectedOpen && !alreadyJoinedSelected && !isSelectedFull && !isSubmitting

    /** Loads campsite information, upcoming lotteries, and the entrant's entries. */
    fun refresh() {
        scope.launch {
            try {
                campsite = campsiteRepo.getCampsiteById(campsiteId)

                val now = Instant.now()
                val fetchedLotteries = lotteryRepo.getLotteriesForCampsite(campsiteId)
                    .filter { now.isBefore(it.registrationEnd) }
                    .sortedBy { it.bookingStart }

                upcomingLotteries.clear()
                upcomingLotteries.addAll(fetchedLotteries)

                val entries = lotteryEntryRepo.getEntriesForEntrant(entrantId)
                entrantEntries.clear()
                entrantEntries.addAll(entries)

                fetchedLotteries.firstOrNull()?.let { earliest ->
                    displayedYearMonth = YearMonth.from(earliest.bookingStart.atZone(zoneId))
                }
            } catch (e: Exception) {
                isErrorMessage = true
                statusMessage = e.message ?: "Unable to load campsite details."
            }
        }
    }

    fun onPreviousMonth() {
        displayedYearMonth = displayedYearMonth.minusMonths(1)
    }

    fun onNextMonth() {
        displayedYearMonth = displayedYearMonth.plusMonths(1)
    }

    /** Toggles selection of the lottery covering [day] in [displayedYearMonth]. */
    fun onDayClick(day: Int) {
        val clickedDate = displayedYearMonth.atDay(day)
        val matchedLottery = dateToLotteryMap[clickedDate] ?: return
        statusMessage = null
        selectedLottery = if (selectedLottery?.id == matchedLottery.id) {
            null
        } else {
            matchedLottery
        }
        refreshWaitlistCount()
    }

    /** Enrolls [entrantId] in [selectedLottery]'s waiting list. */
    fun onJoinWaitingListClick() {
        val lottery = selectedLottery ?: return
        if (!canJoin) return

        scope.launch {
            isSubmitting = true
            statusMessage = null
            try {
                val latestLottery = lotteryRepo.getLotteryById(lottery.id) ?: lottery
                val currentCount = lotteryEntryRepo.getWaitingEntrantIds(latestLottery.id).size
                val entrant = entrantRepo.getEntrantByAndroidId(entrantId)
                    ?: Entrant(
                        androidId = entrantId,
                        name = "",
                        phoneNumber = "",
                        email = ""
                    )

                val newEntry = latestLottery.createEntry(
                    entrant = entrant,
                    currentWaitlistSize = currentCount
                )
                val savedId = lotteryEntryRepo.saveEntry(newEntry)

                entrantEntries.add(newEntry.copy(id = savedId))
                selectedWaitlistCount = currentCount + 1
                isErrorMessage = false
                statusMessage = "Successfully joined the waiting list!"
            } catch (e: Exception) {
                isErrorMessage = true
                statusMessage = e.message ?: "Unable to join the waiting list."
            } finally {
                isSubmitting = false
            }
        }
    }

    fun onBackClick() = back()
    fun onRateCampsiteClick() = navigate(EntrantRoutes.rateCampsite(campsiteId))

    private fun refreshWaitlistCount() {
        val lottery = selectedLottery
        if (lottery == null) {
            selectedWaitlistCount = 0
            return
        }
        scope.launch {
            try {
                selectedWaitlistCount = lotteryEntryRepo.getWaitingEntrantIds(lottery.id).size
            } catch (_: Exception) {
                selectedWaitlistCount = 0
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun Lottery.bookingDates(zoneId: ZoneId): List<LocalDate> {
        val start = bookingStart.atZone(zoneId).toLocalDate()
        val end = bookingEnd.atZone(zoneId).toLocalDate()
        if (end.isBefore(start)) return listOf(start)

        val dates = mutableListOf<LocalDate>()
        var current = start
        while (!current.isAfter(end)) {
            dates.add(current)
            current = current.plusDays(1)
        }
        return dates
    }
}