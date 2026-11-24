package com.example.lottcamp.components

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** Formats [millis] like "Apr 20, 2027". Date pickers use UTC midnight, so UTC is the default. */
fun formatDate(millis: Long, pattern: String = "MMM d, yyyy", utc: Boolean = true): String =
    SimpleDateFormat(pattern, Locale.getDefault()).apply {
        if (utc) timeZone = TimeZone.getTimeZone("UTC")
    }.format(Date(millis))

