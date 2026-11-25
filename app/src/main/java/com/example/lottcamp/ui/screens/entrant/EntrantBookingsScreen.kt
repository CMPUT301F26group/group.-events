package com.example.lottcamp.ui.screens.entrant

import android.content.ContentValues.TAG
import android.util.Log
import android.util.Log.i
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.lottcamp.controller.BookingCard
import com.example.lottcamp.controller.EntrantBookingsController
import com.example.lottcamp.controller.WaitingListCard
import com.example.lottcamp.data.EntryStatus
import com.example.lottcamp.data.ReservationStatus
import com.example.lottcamp.domain.Reservation
import com.example.lottcamp.repository.SampleEntrantRepository
import com.example.lottcamp.ui.components.AppButton
import com.example.lottcamp.ui.components.AppButtonVariant
import com.example.lottcamp.ui.components.AppChipRow
import com.example.lottcamp.ui.components.AppDialog
import com.example.lottcamp.ui.components.AppMutedText
import com.example.lottcamp.ui.theme.LottCampTheme
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Entrant bookings screen displaying active reservations, past stays, and waiting lists
 * backed by [EntrantBookingsController].
 *
 * @param controller Controller owning bookings, waiting lists, and confirmation actions.
 * @param modifier Modifier applied to the screen.
 */
@Composable
fun EntrantBookingsScreen(
    controller: EntrantBookingsController,
    modifier: Modifier = Modifier,
) {
    BookingsContent(
        activeBookings = controller.activeBookings,
        pastBookings = controller.pastBookings,
        waitingLists = controller.waitingLists,
        isLoading = controller.isLoading,
        modifier = modifier,
        onCancelBooking = controller::onCancelBookingClick,
        onRateCampsite = controller::onRateCampsiteClick,
        onLeaveWaitingList = controller::onLeaveWaitingListClick,
    )

    controller.bookingToCancel?.let { booking ->
        AppDialog(
            title = "Cancel booking?",
            message = "Your reservation at ${booking.campsiteName} " +
                    "(${formatDateRange(booking.checkIn, booking.checkOut, includeYear = false)}) " +
                    "will be cancelled and refunded.",
            dismissText = "Keep it",
            onDismiss = controller::dismissCancelDialog,
            confirmText = "Cancel booking",
            onConfirm = controller::confirmCancelBooking,
        )
    }

    controller.waitingToLeave?.let { waiting ->
        AppDialog(
            title = "Leave waiting list?",
            message = "You'll leave the waiting list for ${waiting.campsiteName}. " +
                    "You can rejoin while registration is open.",
            dismissText = "Stay",
            onDismiss = controller::dismissLeaveDialog,
            confirmText = "Leave waiting list",
            onConfirm = controller::confirmLeaveWaitingList,
        )
    }
}

/**
 * Stateless bookings layout with the Active, Past and Waiting lists tabs.
 *
 * @param activeBookings Confirmed reservations that have not ended yet.
 * @param pastBookings Completed or ended reservations.
 * @param waitingLists Lotteries the entrant is currently waiting on.
 * @param modifier Applied to the whole screen.
 * @param isLoading Whether the booking data is still loading.
 * @param initialTab Tab selected when the screen first appears: 0 Active, 1 Past, 2 Waiting lists.
 * @param onCancelBooking Invoked by a "Cancel reservation" button.
 * @param onRateCampsite Invoked by a "Rate campsite" button.
 * @param onLeaveWaitingList Invoked by a "Leave waiting list" button.
 */
@Composable
private fun BookingsContent(
    activeBookings: List<BookingCard>,
    pastBookings: List<BookingCard>,
    waitingLists: List<WaitingListCard>,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    initialTab: Int = 0,
    onCancelBooking: (BookingCard) -> Unit = {},
    onRateCampsite: (BookingCard) -> Unit = {},
    onLeaveWaitingList: (WaitingListCard) -> Unit = {},
) {
    val tabs = listOf("Active", "Past", "Waiting lists")
    var selectedTab by rememberSaveable { mutableIntStateOf(initialTab) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
    ) {
        Spacer(Modifier.height(16.dp))

        Text(
            text = "Bookings",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )

        Spacer(Modifier.height(16.dp))

        AppChipRow(
            labels = tabs,
            selected = selectedTab,
            onSelect = { selectedTab = it },
        )

        Spacer(Modifier.height(16.dp))
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            when (selectedTab) {
                0 -> BookingList(
                    bookings = activeBookings,
                    emptyText = "No active bookings",
                    actionText = "Cancel reservation",
                    modifier = Modifier.weight(1f),
                    onAction = onCancelBooking,
                )
                1 -> BookingList(
                    bookings = pastBookings,
                    emptyText = "No past bookings",
                    actionText = "Rate campsite",
                    modifier = Modifier.weight(1f),
                    isPast = true,
                    onAction = onRateCampsite,
                )
                else -> WaitingList(
                    waitingLists = waitingLists,
                    modifier = Modifier.weight(1f),
                    onLeave = onLeaveWaitingList,
                )
            }
        }
    }
}

@Composable
private fun BookingList(
    bookings: List<BookingCard>,
    emptyText: String,
    actionText: String,
    modifier: Modifier = Modifier,
    isPast: Boolean = false,
    onAction: (BookingCard) -> Unit,
) {
    if (bookings.isEmpty()) {
        Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            AppMutedText(emptyText)
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 16.dp),
    ) {
        items(bookings, key = { it.reservationId }) { booking ->
            BookingCardItem(
                booking = booking,
                status = if (isPast) {
                    "Completed"
                } else {
                    "Confirmed · ${booking.reservationId}"
                },
                actionText = actionText,
                onAction = { onAction(booking) },
            )
        }
    }
}

@Composable
private fun BookingCardItem(
    booking: BookingCard,
    status: String,
    actionText: String,
    onAction: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = booking.campsiteName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Text(
                text = listOfNotNull(
                    booking.campsiteLocation.takeIf { it.isNotBlank() },
                    formatDateRange(booking.checkIn, booking.checkOut),
                ).joinToString(" · "),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text(
                text = status,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(Modifier.height(4.dp))

            AppButton(
                text = actionText,
                onClick = onAction,
                modifier = Modifier.fillMaxWidth(),
                variant = AppButtonVariant.Secondary,
            )
        }
    }
}

@Composable
private fun WaitingList(
    waitingLists: List<WaitingListCard>,
    modifier: Modifier = Modifier,
    onLeave: (WaitingListCard) -> Unit,
) {
    if (waitingLists.isEmpty()) {
        Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            AppMutedText("You're not on any waiting lists")
        }
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 16.dp),
    ) {
        items(waitingLists, key = { it.entryId }) { waiting ->
            WaitingListCardItem(
                waiting = waiting,
                onLeave = { onLeave(waiting) },
            )
        }
    }
}

@Composable
private fun WaitingListCardItem(
    waiting: WaitingListCard,
    onLeave: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = waiting.campsiteName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Text(
                text = "Lottery closes ${formatDate(waiting.closesOn)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text(
                text = "On the waiting list · ${waiting.entrantCount} entrants",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Spacer(Modifier.height(4.dp))

            AppButton(
                text = "Leave waiting list",
                onClick = onLeave,
                modifier = Modifier.fillMaxWidth(),
                variant = AppButtonVariant.Secondary,
            )
        }
    }
}

private val monthDayFormatter = DateTimeFormatter.ofPattern("MMM d", Locale.US)
private val monthDayYearFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US)

/** Formats a stay as "May 8 – May 10, 2027". */
private fun formatDateRange(
    start: LocalDate,
    end: LocalDate,
    includeYear: Boolean = true,
): String {
    val endText = if (includeYear || start.year != end.year) {
        end.format(monthDayYearFormatter)
    } else {
        end.format(monthDayFormatter)
    }
    return "${start.format(monthDayFormatter)} – $endText"
}

/** Formats an instant as a local calendar date, e.g. "Apr 20, 2027". */
private fun formatDate(instant: Instant): String =
    instant.atZone(ZoneId.systemDefault()).toLocalDate().format(monthDayYearFormatter)

@Preview(showBackground = true, heightDp = 900, name = "Bookings - Active")
@Composable
private fun BookingsActivePreview() = BookingsPreview(initialTab = 0)

@Preview(showBackground = true, heightDp = 900, name = "Bookings - Past")
@Composable
private fun BookingsPastPreview() = BookingsPreview(initialTab = 1)

@Preview(showBackground = true, heightDp = 900, name = "Bookings - Waiting lists")
@Composable
private fun BookingsWaitingPreview() = BookingsPreview(initialTab = 2)

@Composable
private fun BookingsPreview(initialTab: Int) {
    val data = remember { SampleEntrantRepository() }
    val campsites = data.campsites.associateBy { it.id }
    val today = LocalDate.now()

    fun Reservation.toCard(): BookingCard {
        val campsite = campsites[campsiteId]
        return BookingCard(
            reservationId = id,
            campsiteId = campsiteId,
            campsiteName = campsite?.name ?: "Campsite",
            campsiteLocation = campsite?.locationName ?: "",
            checkIn = checkIn,
            checkOut = checkOut,
        )
    }

    val active = data.reservations
        .filter { it.status == ReservationStatus.ACTIVE && !it.checkOut.isBefore(today) }
        .map { it.toCard() }
    val past = data.reservations
        .filter {
            it.status == ReservationStatus.COMPLETED ||
                    (it.status == ReservationStatus.ACTIVE && it.checkOut.isBefore(today))
        }
        .map { it.toCard() }
    val waiting = data.entries
        .filter { it.status == EntryStatus.WAITING }
        .mapNotNull { entry ->
            val lottery = data.lotteries.firstOrNull { it.id == entry.lotteryId }
                ?: return@mapNotNull null
            WaitingListCard(
                entryId = entry.id,
                campsiteName = lottery.campsites.firstOrNull()?.name ?: "Campsite",
                closesOn = lottery.registrationEnd,
                entrantCount = data.waitingListSizes[lottery.id]
                    ?: data.entries.count { it.lotteryId == lottery.id },
            )
        }

    LottCampTheme {
        BookingsContent(
            activeBookings = active,
            pastBookings = past,
            waitingLists = waiting,
            isLoading = true,
            initialTab = initialTab,
        )
    }
}