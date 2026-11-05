package com.example.lottcamp.domain

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.Instant
import com.example.lottcamp.data.EntryStatus


/**
 * Represents an entrant's participation entry on a specific lottery's waitlist.
 */
@RequiresApi(Build.VERSION_CODES.O)
data class LotteryEntry(
    val id: String,
    val lotteryId: String,
    val entrantId: String,
    val status: EntryStatus = EntryStatus.WAITING,
    val joinedAt: Instant = Instant.now()
) {
    /** Transitions state when entrant declines a win. */
    fun decline(): LotteryEntry {
        check(status == EntryStatus.WON) { "Can only decline if selected as a winner." }
        return copy(status = EntryStatus.DECLINED)
    }
}
