package com.example.lottcamp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.text.DateFormatSymbols
import java.util.Calendar

/**
 * Month calendar that highlights booked days.
 *
 * @param year Year shown.
 * @param month Month shown, 1 to 12.
 * @param bookedDays Days of the month to highlight.
 * @param onPreviousMonth Invoked when the left arrow is tapped.
 * @param onNextMonth Invoked when the right arrow is tapped.
 * @param modifier Applied to the calendar.
 */
@Composable
fun AppCalendar(
    year: Int,
    month: Int,
    bookedDays: Set<Int>,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val first = remember(year, month) {
        Calendar.getInstance().apply { clear(); set(year, month - 1, 1) }
    }
    val offset = first.get(Calendar.DAY_OF_WEEK) - 1 // Sunday is 0
    val daysInMonth = first.getActualMaximum(Calendar.DAY_OF_MONTH)
    val weeks = (offset + daysInMonth + 6) / 7
    val monthName = DateFormatSymbols().months[month - 1]

    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "$monthName $year",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onPreviousMonth) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous month")
            }
            IconButton(onClick = onNextMonth) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next month")
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("S", "M", "T", "W", "T", "F", "S").forEach { letter ->
                Text(
                    text = letter,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        for (week in 0 until weeks) {
            if (week > 0) Spacer(Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                for (column in 0 until 7) {
                    val day = week * 7 + column - offset + 1
                    val booked = day in bookedDays
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .then(if (booked) Modifier.background(MaterialTheme.colorScheme.primary) else Modifier),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (day in 1..daysInMonth) {
                            Text(
                                text = day.toString(),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (booked) FontWeight.Bold else FontWeight.Normal,
                                color = if (booked) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}