package com.example.lottcamp.ui.screens.entrant

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.lottcamp.controller.EntrantCampsiteDetailsController
import com.example.lottcamp.ui.components.AppButton
import com.example.lottcamp.ui.components.AppButtonVariant
import com.example.lottcamp.ui.components.AppCalendar
import com.example.lottcamp.ui.components.AppRatingBar
import com.example.lottcamp.ui.components.AppStoredImage
import com.example.lottcamp.ui.components.AppTopBar
import java.time.format.DateTimeFormatter
import java.util.Locale

object EntrantDetailsTestTags {
    const val SCREEN_LIST = "details_screen_list"
    const val BACK_BUTTON = "details_back_button"
    const val TITLE_TEXT = "details_title_text"
    const val CALENDAR_SECTION = "details_calendar_section"
    const val SELECTED_DATES_TEXT = "details_selected_dates_text"
    const val STATUS_MESSAGE_TEXT = "details_status_message_text"
    const val JOIN_WAITLIST_BUTTON = "details_join_waitlist_button"
    const val VIEW_ON_MAP_BUTTON = "details_view_on_map_button"
}

/**
 * Displays campsite details, available lottery calendar windows, and waitlist registration
 * backed by [EntrantCampsiteDetailsController].
 *
 * @param controller Controller owning campsite, lottery, and enrollment state.
 * @param modifier Modifier applied to the root container.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EntrantCampsiteDetailsScreen(
    controller: EntrantCampsiteDetailsController,
    modifier: Modifier = Modifier,
) {
    val cs = controller.campsite ?: return
    val uriHandler = LocalUriHandler.current
    val zoneId = controller.zoneId
    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault()) }
    val shortDateFormatter = remember { DateTimeFormatter.ofPattern("MMM d", Locale.getDefault()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        AppTopBar(
            title = "Campsite details",
            onBack = controller::onBackClick,
            backButtonModifier = Modifier.testTag(EntrantDetailsTestTags.BACK_BUTTON),
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .testTag(EntrantDetailsTestTags.SCREEN_LIST),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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
                        imageRepo = controller.imageRepo,
                        contentDescription = cs.name,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = cs.name,
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.testTag(EntrantDetailsTestTags.TITLE_TEXT),
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("%.1f ".format(cs.averageRating), style = MaterialTheme.typography.bodyMedium)
                            AppRatingBar(cs.averageRating.toFloat())
                            Text(
                                " (${cs.ratingCount} ${if (cs.ratingCount == 1) "review" else "reviews"})",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            "${cs.locationName} · Sleeps up to ${cs.maxOccupancy} · \$${cs.pricePerNight.toInt()} / night",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = cs.description,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            }

            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(EntrantDetailsTestTags.CALENDAR_SECTION),
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        AppCalendar(
                            year = controller.displayedYearMonth.year,
                            month = controller.displayedYearMonth.monthValue,
                            availableDays = controller.availableDaysInMonth,
                            selectedDays = controller.selectedDaysInMonth,
                            onPreviousMonth = controller::onPreviousMonth,
                            onNextMonth = controller::onNextMonth,
                            onDayClick = controller::onDayClick,
                        )

                        val currentLottery = controller.selectedLottery
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
                                        color = MaterialTheme.colorScheme.onBackground,
                                        modifier = Modifier.testTag(EntrantDetailsTestTags.SELECTED_DATES_TEXT),
                                    )
                                }

                                if (controller.isSelectedNotYetOpen) {
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
                                            text = "${controller.selectedWaitlistCount} / $capacity",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = if (controller.upcomingLotteries.isEmpty()) {
                                    "No upcoming lotteries are currently scheduled for this campsite."
                                } else {
                                    "Tap a highlighted date range on the calendar to select a lottery."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        controller.statusMessage?.let { msg ->
                            Text(
                                text = msg,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (controller.isErrorMessage) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.primary
                                },
                                modifier = Modifier.testTag(EntrantDetailsTestTags.STATUS_MESSAGE_TEXT),
                            )
                        }
                    }
                }
            }

            item {
                val buttonLabel = when {
                    controller.isSubmitting -> "Joining waiting list..."
                    controller.alreadyJoinedSelected -> "Already on waiting list"
                    controller.isSelectedNotYetOpen -> {
                        val opens = controller.selectedLottery!!.registrationStart.atZone(zoneId).toLocalDate()
                        "Opens ${opens.format(shortDateFormatter)}"
                    }
                    controller.selectedLottery != null && !controller.isSelectedOpen -> "Registration closed"
                    controller.isSelectedFull -> "Waiting list full"
                    else -> "Join waiting list"
                }

                AppButton(
                    text = buttonLabel,
                    onClick = controller::onJoinWaitingListClick,
                    enabled = controller.canJoin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(EntrantDetailsTestTags.JOIN_WAITLIST_BUTTON),
                )
            }

            cs.latitude?.let { lat ->
                cs.longitude?.let { lon ->
                    item {
                        AppButton(
                            text = "View on map",
                            onClick = {
                                uriHandler.openUri("https://www.google.com/maps/search/?api=1&query=$lat,$lon")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag(EntrantDetailsTestTags.VIEW_ON_MAP_BUTTON),
                            variant = AppButtonVariant.Secondary,
                        )
                    }
                }
            }
        }
    }
}