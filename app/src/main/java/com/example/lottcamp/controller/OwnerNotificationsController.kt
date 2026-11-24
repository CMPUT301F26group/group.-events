package com.example.lottcamp.controller

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.lottcamp.data.NotificationDoc
import com.example.lottcamp.repository.CampsiteOwnerRepository
import com.example.lottcamp.repository.EntrantRepository
import com.example.lottcamp.repository.LotteryEntryRepository
import com.example.lottcamp.repository.NotificationRepository
import com.google.firebase.Timestamp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** One row of the "Sent notifications" list: a message sent to a group of entrants. */
data class SentNotification(val title: String, val subtitle: String, val date: String)

class OwnerNotificationsController(
    private val ownerId: String,
    private val lotteryId: String,
    private val entries: LotteryEntryRepository,
    private val notifications: NotificationRepository,
    private val owners: CampsiteOwnerRepository,
    private val entrants: EntrantRepository,
    private val scope: CoroutineScope,
    private val back: () -> Unit = {},
) {

    var message by mutableStateOf("")
        private set
    var recipientCount by mutableStateOf(0)
        private set
    var sending by mutableStateOf(false)
        private set
    val sent = mutableStateListOf<SentNotification>()
    var error by mutableStateOf<String?>(null)
        private set

    private var recipientIds = emptyList<String>()
    private var names = emptyMap<String, String>()
    private var ownerName = "Campsite owner"

    fun onMessageChange(value: String) { message = value }
    fun onBackClick() = back()
    fun clearError() { error = null }

    fun refresh() = run("Couldn't load notifications") { load() }

    fun onSendClick() {
        if (sending) return
        if (message.isBlank()) { error = "Write a message first"; return }
        if (recipientIds.isEmpty()) { error = "Nobody is on the waiting list yet"; return }

        val text = message.trim()
        run("Couldn't send the notification") {
            sending = true
            try {
                val now = Timestamp.now()
                val docs = recipientIds.map {
                    NotificationDoc(
                        title = TITLE,
                        senderId = ownerId,
                        senderName = ownerName,
                        recipientId = it,
                        recipientName = names[it] ?: "",
                        message = text,
                        sentAt = now,
                    )
                }
                withTimeout(15_000) { notifications.send(docs) }
                message = ""
                load()
            } finally {
                sending = false
            }
        }
    }

    private suspend fun load() {
        val ids = entries.getWaitingEntrantIds(lotteryId)
        val people = entrants.getByIds(ids).associateBy { it.androidId }
        recipientIds = ids.filter { people[it]?.notificationsEnabled != false }
        names = people.mapValues { it.value.name }
        recipientCount = recipientIds.size
        owners.getOwnerById(ownerId)?.let { ownerName = it.name }

        // One document exists per recipient, so group them back into one row per send
        val rows = notifications.getSentBy(ownerId)
            .filter { it.sentAt != null }
            .groupBy { it.title to it.sentAt!!.seconds }
            .map { (key, group) ->
                val first = group.first()
                val count = group.size
                SentNotification(
                    title = first.title,
                    subtitle = "$count ${if (count == 1) "entrant" else "entrants"}",
                    date = formatDate(first.sentAt!!.toDate()),
                ) to key.second
            }
            .sortedByDescending { it.second }
            .map { it.first }
        sent.clear()
        sent.addAll(rows)
    }

    private fun formatDate(date: Date): String {
        val now = Calendar.getInstance()
        val then = Calendar.getInstance().apply { time = date }
        val today = now.get(Calendar.YEAR) == then.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == then.get(Calendar.DAY_OF_YEAR)
        return if (today) "Today" else SimpleDateFormat("MMM d", Locale.getDefault()).format(date)
    }

    private fun run(errorMessage: String, work: suspend () -> Unit) {
        scope.launch {
            try {
                work()
            } catch (e: Exception) {
                error = "$errorMessage: ${e.message}"
            }
        }
    }

    private companion object {
        const val TITLE = "Lottery update"
    }
}