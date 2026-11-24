package com.example.lottcamp.ui.screens.entrant

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
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
import com.example.lottcamp.ui.components.AppBackButton
import com.example.lottcamp.ui.components.AppButton
import com.example.lottcamp.ui.components.AppButtonVariant
import com.example.lottcamp.ui.components.AppCalendar
import com.example.lottcamp.ui.components.AppRatingBar
import com.example.lottcamp.ui.components.AppStoredImage
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
/**
 * Displays detailed information for a campsite and allows an entrant to enroll in an active lottery.
 *
 * Fetches campsite metadata and upcoming lotteries from Firestore, highlights available stay
 * windows on an interactive calendar, and enables joining the waiting list for a selected
 * lottery period.
 *
 * @param navController Navigation controller used to return to search or open the rating screen.
 * @param modifier Modifier applied to the root container.
 * @param campsiteId Unique Firestore document ID of the campsite to display.
 */
fun EntrantCampsiteDetailsScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    campsiteId: String,
) {
    // Hardcoded for collaborative testing; replace with device androidId later
    val entrantId = "entrant1"

    val campsiteRepo = remember { CampsiteRepository() }
    val lotteryRepo = remember { LotteryRepository() }
    val lotteryEntryRepo = remember { LotteryEntryRepository() }
    val entrantRepo = remember { EntrantRepository() }
    val imageRepo = remember { ImageRepository() }
    val uriHandler = LocalUriHandler.current
    val coroutineScope = rememberCoroutineScope()
    val zoneId = remember { ZoneId.systemDefault() }

    var campsite by remember { mutableStateOf<Campsite?>(null) }
    var upcomingLotteries by remember { mutableStateOf<List<Lottery>>(emptyList()) }
    var selectedLottery by remember { mutableStateOf<Lottery?>(null) }
    var entrantEntries by remember { mutableStateOf<List<LotteryEntry>>(emptyList()) }
    var selectedWaitlistCount by remember { mutableIntStateOf(0) }
    var displayedYearMonth by remember { mutableStateOf(YearMonth.now(zoneId)) }
    var isSubmitting by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isErrorMessage by remember { mutableStateOf(false) }

    LaunchedEffect(campsiteId) {
        campsite = campsiteRepo.getCampsiteById(campsiteId)

        val now = Instant.now()
        val fetchedLotteries = lotteryRepo.getLotteriesForCampsite(campsiteId)
            .filter { now.isBefore(it.registrationEnd) }
            .sortedBy { it.bookingStart }

        upcomingLotteries = fetchedLotteries
        entrantEntries = lotteryEntryRepo.getEntriesForEntrant(entrantId)

        // Jump the calendar to the month of the earliest upcoming/open lottery
        fetchedLotteries.firstOrNull()?.let { earliest ->
            displayedYearMonth = YearMonth.from(earliest.bookingStart.atZone(zoneId))
        }
    }

    LaunchedEffect(selectedLottery?.id) {
        val lottery = selectedLottery
        selectedWaitlistCount = if (lottery != null) {
            lotteryEntryRepo.getWaitingEntrantIds(lottery.id).size
        } else {
            0
        }
    }

    val cs = campsite ?: return

    // Map each LocalDate in a lottery's booking window to its Lottery
    val dateToLotteryMap = remember(upcomingLotteries, zoneId) {
        buildMap {
            for (lottery in upcomingLotteries) {
                for (date in lottery.bookingDates(zoneId)) {
                    put(date, lottery)
                }
            }
        }
    }

    val availableDaysInMonth = remember(upcomingLotteries, displayedYearMonth, zoneId) {
        upcomingLotteries
            .flatMap { it.bookingDates(zoneId) }
            .filter { YearMonth.from(it) == displayedYearMonth }
            .map { it.dayOfMonth }
            .toSet()
    }

    val selectedDaysInMonth = remember(selectedLottery, displayedYearMonth, zoneId) {
        selectedLottery
            ?.bookingDates(zoneId)
            ?.filter { YearMonth.from(it) == displayedYearMonth }
            ?.map { it.dayOfMonth }
            ?.toSet()
            ?: emptySet()
    }

    val alreadyJoinedSelected = remember(selectedLottery, entrantEntries) {
        val currentId = selectedLottery?.id ?: return@remember false
        entrantEntries.any {
            it.lotteryId == currentId &&
                    (it.status == EntryStatus.WAITING || it.status == EntryStatus.WON)
        }
    }

    val now = Instant.now()
    val isSelectedNotYetOpen = selectedLottery?.let { now.isBefore(it.registrationStart) } == true
    val isSelectedOpen = selectedLottery?.isRegistrationOpen(now) == true
    val isSelectedFull = selectedLottery?.isWaitlistFull(selectedWaitlistCount) == true
    val canJoin = selectedLottery != null && isSelectedOpen && !alreadyJoinedSelected && !isSelectedFull && !isSubmitting

    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault()) }
    val shortDateFormatter = remember { DateTimeFormatter.ofPattern("MMM d", Locale.getDefault()) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            AppBackButton("Back to Search", onClick = { navController.navigate("entrant-campsite-search-screen") })
        }

        item {
            val shape = MaterialTheme.shapes.large
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(shape)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline, shape),
                contentAlignment = Alignment.Center,
            ) {
                AppStoredImage(
                    imageId = cs.imageId,
                    imageRepo = imageRepo,
                    contentDescription = cs.name,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(cs.name, style = MaterialTheme.typography.headlineMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${cs.averageRating} ", style = MaterialTheme.typography.bodyMedium)
                    AppRatingBar(cs.averageRating.toFloat())
                    Text(
                        " (${cs.ratingCount} reviews)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    "${cs.locationName} · Sleeps up to ${cs.maxOccupancy} · \$${cs.pricePerNight.toInt()} / night",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            Text(cs.description, style = MaterialTheme.typography.bodyLarge)
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AppCalendar(
                    year = displayedYearMonth.year,
                    month = displayedYearMonth.monthValue,
                    availableDays = availableDaysInMonth,
                    selectedDays = selectedDaysInMonth,
                    onPreviousMonth = { displayedYearMonth = displayedYearMonth.minusMonths(1) },
                    onNextMonth = { displayedYearMonth = displayedYearMonth.plusMonths(1) },
                    onDayClick = { day ->
                        val clickedDate = displayedYearMonth.atDay(day)
                        val matchedLottery = dateToLotteryMap[clickedDate]
                        if (matchedLottery != null) {
                            statusMessage = null
                            selectedLottery = if (selectedLottery?.id == matchedLottery.id) {
                                null
                            } else {
                                matchedLottery
                            }
                        }
                    }
                )

                val currentLottery = selectedLottery
                if (currentLottery != null) {
                    val stayStart = currentLottery.bookingStart.atZone(zoneId).toLocalDate()
                    val stayEnd = currentLottery.bookingEnd.atZone(zoneId).toLocalDate()
                    val regStarts = currentLottery.registrationStart.atZone(zoneId).toLocalDate()
                    val regCloses = currentLottery.registrationEnd.atZone(zoneId).toLocalDate()

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Selected dates",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${stayStart.format(shortDateFormatter)} – ${stayEnd.format(dateFormatter)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        if (isSelectedNotYetOpen) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Registration opens",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = regStarts.format(dateFormatter),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Lottery closes",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = regCloses.format(dateFormatter),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        currentLottery.waitlistCapacity?.let { capacity ->
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Waitlist spots",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "$selectedWaitlistCount / $capacity",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        text = if (upcomingLotteries.isEmpty()) {
                            "No upcoming lotteries are currently scheduled for this campsite."
                        } else {
                            "Tap a highlighted date range on the calendar to select a lottery."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                statusMessage?.let { msg ->
                    Text(
                        text = msg,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isErrorMessage) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )
                }
            }
        }

        item {
            val buttonLabel = when {
                isSubmitting -> "Joining waiting list..."
                alreadyJoinedSelected -> "Already on waiting list"
                isSelectedNotYetOpen -> {
                    val opens = selectedLottery!!.registrationStart.atZone(zoneId).toLocalDate()
                    "Opens ${opens.format(shortDateFormatter)}"
                }
                selectedLottery != null && !isSelectedOpen -> "Registration closed"
                isSelectedFull -> "Waiting list full"
                else -> "Join waiting list"
            }

            AppButton(
                buttonLabel,
                onClick = {
                    val lottery = selectedLottery ?: return@AppButton
                    if (!canJoin) return@AppButton

                    coroutineScope.launch {
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

                            entrantEntries = entrantEntries + newEntry.copy(id = savedId)
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
                },
                enabled = canJoin,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            AppButton(
                "Rate this campsite",
                onClick = { navController.navigate("entrant-campsite-rating-screen/${cs.id}") },
                modifier = Modifier.fillMaxWidth(),
                variant = AppButtonVariant.Secondary,
            )
        }

        cs.latitude?.let { lat ->
            cs.longitude?.let { lon ->
                item {
                    AppButton(
                        "View on map",
                        onClick = {
                            uriHandler.openUri("https://www.google.com/maps/search/?api=1&query=$lat,$lon")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        variant = AppButtonVariant.Secondary,
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun Lottery.bookingDates(zoneId: ZoneId = ZoneId.systemDefault()): List<LocalDate> {
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