package com.example.lottcamp.controller


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.lottcamp.components.formatDate
import com.example.lottcamp.data.EntryStatus
import com.example.lottcamp.data.NotificationDoc
import com.example.lottcamp.domain.Entrant
import com.example.lottcamp.domain.Lottery
import com.example.lottcamp.domain.LotteryEntry
import com.example.lottcamp.repository.EntrantRepository
import com.example.lottcamp.repository.LotteryEntryRepository
import com.example.lottcamp.repository.LotteryRepository
import com.example.lottcamp.repository.NotificationRepository
import com.google.firebase.Timestamp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.Instant


/** One entrant on the waiting list. [status] is empty while they are still waiting. */
data class WaitingRow(val id: String, val name: String, val subtitle: String, val status: String)

/**
 * Controller for the waiting list screen.
 *
 * Shows everyone who joined the lottery [lotteryId]. When registration has closed and no draw
 * has happened yet, it draws the winners and notifies every entrant.
 */
@RequiresApi(Build.VERSION_CODES.O)
class WaitingListController(
    private val lotteryId: String,
    private val lotteries: LotteryRepository,
    private val entries: LotteryEntryRepository,
    private val entrants: EntrantRepository,
    private val notifications: NotificationRepository,
    private val scope: CoroutineScope,
    private val navigate: (String) -> Unit = {},
    private val back: () -> Unit = {},
) {

    var lottery by mutableStateOf<Lottery?>(null)
        private set

    val rows = mutableStateListOf<WaitingRow>()

    var error by mutableStateOf<String?>(null)
        private set

    val campsiteNames: String get() = lottery?.campsites?.joinToString(", ") { it.name } ?: ""

    val summary: String
        get() = lottery?.let { "${rows.size} entrants · Closes ${formatDate(it.registrationEnd.toEpochMilli())}" } ?: ""

    val drawText: String
        get() = lottery?.let {
            "${it.winnerCount} ${if (it.winnerCount == 1) "winner" else "winners"} drawn automatically"
        } ?: ""

    fun refresh() {
        scope.launch {
            try {
                load()
            } catch (e: Exception) {
                error = "Couldn't load the waiting list: ${e.message}"
            }
        }
    }

    fun clearError() { error = null }
    fun onBackClick() = back()
    fun onSendNotificationClick() = navigate(OwnerRoutes.notifications(lotteryId))
    fun onEditClick() = navigate(OwnerRoutes.editLottery(lotteryId))

    private suspend fun load() {
        val loaded = lotteries.getLotteryById(lotteryId) ?: throw IllegalStateException("Lottery not found")
        var list = entries.getEntriesForLottery(lotteryId)
        val people = entrants.getByIds(list.map { it.entrantId }).associateBy { it.androidId }
        list = drawIfDue(loaded, list, people)

        lottery = loaded
        rows.clear()
        rows.addAll(
            list.sortedBy { it.joinedAt }.map { entry ->
                val person = people[entry.entrantId]
                val joined = "Joined ${formatDate(entry.joinedAt.toEpochMilli(), utc = false)}"
                WaitingRow(
                    id = entry.id,
                    name = person?.name ?: "Unknown entrant",
                    subtitle = if (person?.email.isNullOrBlank()) joined else "$joined · ${person?.email}",
                    status = statusLabel(entry.status),
                )
            }
        )
    }

    /**
     * Draws the winners if registration has closed and nobody has been drawn yet.
     * Runs on the owner's device, so it only happens once the owner opens this screen.
     */
    private suspend fun drawIfDue(
        lottery: Lottery,
        list: List<LotteryEntry>,
        people: Map<String, Entrant>,
    ): List<LotteryEntry> {
        val waiting = list.filter { it.status == EntryStatus.WAITING }
        val alreadyDrawn = list.any { it.status != EntryStatus.WAITING }
        if (alreadyDrawn || waiting.isEmpty() || Instant.now().isBefore(lottery.registrationEnd)) return list

        val waitingIds = waiting.map { it.id }.toSet()
        val winnerIds = waiting.shuffled().take(lottery.winnerCount).map { it.id }.toSet()
        val updated = list.map {
            if (it.id !in waitingIds) it
            else it.copy(status = if (it.id in winnerIds) EntryStatus.WON else EntryStatus.LOST)
        }
        val drawn = updated.filter { it.id in waitingIds }
        drawn.forEach { entries.saveEntry(it) }

        // Tell everyone the result, unless they opted out of notifications
        val now = Timestamp.now()
        val names = lottery.campsites.joinToString(", ") { it.name }
        val docs = drawn
            .filter { people[it.entrantId]?.notificationsEnabled != false }
            .map { entry ->
                val won = entry.status == EntryStatus.WON
                NotificationDoc(
                    title = if (won) "You won the lottery" else "Lottery result",
                    senderId = null,
                    senderName = "System",
                    recipientId = entry.entrantId,
                    recipientName = people[entry.entrantId]?.name ?: "",
                    message = if (won) "You won a spot at $names. Open Bookings to reserve your dates."
                    else "You weren't selected for $names this time.",
                    sentAt = now,
                )
            }
        notifications.send(docs)
        return updated
    }

    private fun statusLabel(status: EntryStatus) = when (status) {
        EntryStatus.WAITING -> ""
        EntryStatus.WON -> "Won"
        EntryStatus.LOST -> "Not selected"
        EntryStatus.DECLINED -> "Declined"
        EntryStatus.BOOKED -> "Booked"
    }
}