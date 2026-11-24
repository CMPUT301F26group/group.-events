package com.example.lottcamp.controller

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.lottcamp.data.MessageDoc
import com.example.lottcamp.repository.CampsiteOwnerRepository
import com.example.lottcamp.repository.EntrantRepository
import com.example.lottcamp.repository.MessageRepository
import com.google.firebase.Timestamp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/** One message bubble. [mine] is true for messages the owner sent. */
data class ChatBubble(val id: String, val text: String, val time: String, val mine: Boolean)

/**
 * Controller for the message thread between the owner and the entrant of one reservation.
 * Every message is saved in the messages collection, which is also what the admin log reads.
 */
@RequiresApi(Build.VERSION_CODES.O)
class ChatController(
    private val ownerId: String,
    private val reservationId: String,
    private val owner: OwnerController,
    private val entrants: EntrantRepository,
    private val owners: CampsiteOwnerRepository,
    private val messages: MessageRepository,
    private val scope: CoroutineScope,
    private val back: () -> Unit = {},
) {

    var title by mutableStateOf("")
        private set
    var subtitle by mutableStateOf("")
        private set
    val bubbles = mutableStateListOf<ChatBubble>()
    var draft by mutableStateOf("")
        private set
    var sending by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    private var entrantId = ""
    private var entrantName = ""
    private var campsiteName = ""
    private var ownerName = "Campsite owner"

    fun onDraftChange(value: String) { draft = value }
    fun onBackClick() = back()
    fun clearError() { error = null }

    fun refresh() {
        scope.launch {
            try {
                load()
            } catch (e: Exception) {
                error = "Couldn't load the messages: ${e.message}"
            }
        }
    }

    fun onSendClick() {
        val text = draft.trim()
        if (sending || text.isEmpty() || entrantId.isEmpty()) return
        scope.launch {
            sending = true
            try {
                withTimeout(15_000) {
                    messages.send(
                        MessageDoc(
                            senderId = ownerId,
                            senderName = ownerName,
                            recipientId = entrantId,
                            recipientName = entrantName,
                            campsiteName = campsiteName,
                            reservationId = reservationId,
                            message = text,
                            sentAt = Timestamp.now(),
                        )
                    )
                }
                draft = ""
                load()
            } catch (e: Exception) {
                error = "Couldn't send the message: ${e.message}"
            } finally {
                sending = false
            }
        }
    }

    private suspend fun load() {
        val reservation = owner.reservations.find { it.id == reservationId }
            ?: throw IllegalStateException("Reservation not found")
        entrantId = reservation.entrantId
        entrantName = entrants.getEntrantByAndroidId(entrantId)?.name ?: "Entrant"
        campsiteName = owner.campsite(reservation.campsiteId)?.name ?: "Campsite"
        owners.getOwnerById(ownerId)?.let { ownerName = it.name }

        title = entrantName
        subtitle = "$campsiteName · ${reservationCode(reservationId)}"

        val docs = messages.getForReservation(reservationId)
        bubbles.clear()
        bubbles.addAll(docs.map { ChatBubble(it.id, it.message, formatTime(it.sentAt), it.senderId == ownerId) })
    }

    private fun formatTime(sentAt: Timestamp?): String {
        if (sentAt == null) return ""
        val date = sentAt.toDate()
        val now = Calendar.getInstance()
        val then = Calendar.getInstance().apply { time = date }
        val today = now.get(Calendar.YEAR) == then.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == then.get(Calendar.DAY_OF_YEAR)
        val pattern = if (today) "'Today' · h:mm a" else "MMM d · h:mm a"
        return SimpleDateFormat(pattern, Locale.getDefault()).format(date)
    }
}