package com.example.lottcamp.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.lottcamp.data.AdminDoc
import com.example.lottcamp.data.CampsiteDoc
import com.example.lottcamp.data.CampsiteOwnerDoc
import com.example.lottcamp.data.EntrantDoc
import com.example.lottcamp.data.EntryStatus
import com.example.lottcamp.data.LotteryDoc
import com.example.lottcamp.data.LotteryEntryDoc
import com.example.lottcamp.data.ReservationDoc
import com.example.lottcamp.data.ReservationStatus

import com.example.lottcamp.domain.*
import com.google.firebase.Timestamp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

// --- Date Converters ---
//@RequiresApi(Build.VERSION_CODES.O)
//fun Timestamp?.toInstant(): Instant? = this?.toDate()?.toInstant()
//@RequiresApi(Build.VERSION_CODES.O)
//fun Instant?.toTimestamp(): Timestamp? = this?.let { Timestamp(Date.from(it)) }
@RequiresApi(Build.VERSION_CODES.O)
fun Instant.toTimestamp(): Timestamp {
    return Timestamp(this.epochSecond, this.nano)
}

// Extension to map back from Firestore Timestamp to Instant
@RequiresApi(Build.VERSION_CODES.O)
fun Timestamp.toInstant(): Instant {
    return Instant.ofEpochSecond(this.seconds, this.nanoseconds.toLong())
}

@RequiresApi(Build.VERSION_CODES.O)
fun Timestamp?.toLocalDate(): LocalDate? = this?.toDate()
    ?.toInstant()
    ?.atZone(ZoneId.systemDefault())
    ?.toLocalDate()

@RequiresApi(Build.VERSION_CODES.O)
fun LocalDate?.toTimestamp(): Timestamp? = this?.let {
    val instant = it.atStartOfDay(ZoneId.systemDefault()).toInstant()
    Timestamp(Date.from(instant))
}

// --- Entrant Mapper ---
fun EntrantDoc.toDomain(): Entrant = Entrant(
    androidId = androidId,
    name = name,
    phoneNumber = phoneNumber,
    email = email,
    profilePictureUrl = profilePictureUrl,
    notificationsEnabled = isNotificationsEnabled
)

fun Entrant.toDoc(): EntrantDoc = EntrantDoc(
    androidId = androidId,
    name = name,
    phoneNumber = phoneNumber,
    email = email,
    profilePictureUrl = profilePictureUrl,
    isNotificationsEnabled = notificationsEnabled
)

// --- Campsite Mapper ---
fun CampsiteDoc.toDomain(): Campsite = Campsite(
    id = id,
    ownerId = ownersAndroidId,
    name = name,
    locationName = locationName,
    latitude = latitude,
    longitude = longitude,
    description = description,
    maxOccupancy = maxOccupancy,
    pricePerNight = pricePerNight,
    imageUrl = imageUrl,
    tags = tags,
    ratingSum = ratingSum,
    ratingCount = ratingCount,
    rulesAndPolicy = rulesAndPolicy
)

fun Campsite.toDoc(): CampsiteDoc = CampsiteDoc(
    id = id,
    ownersAndroidId = ownerId,
    name = name,
    locationName = locationName,
    latitude = latitude,
    longitude = longitude,
    description = description,
    maxOccupancy = maxOccupancy,
    pricePerNight = pricePerNight,
    imageUrl = imageUrl,
    tags = tags,
    ratingSum = ratingSum,
    ratingCount = ratingCount,
    rulesAndPolicy = rulesAndPolicy
)

// --- Lottery Mapper ---
@RequiresApi(Build.VERSION_CODES.O)
fun LotteryDoc.toDomain(campsites: List<Campsite>): Lottery = Lottery(
    id = id,
    ownerId = ownerId,
    campsites = campsites,
    registrationStart = registrationStart?.toInstant() ?: Instant.now(),
    registrationEnd = registrationEnd?.toInstant() ?: Instant.now(),
    bookingStart = bookingStart?.toInstant() ?: Instant.now(),
    bookingEnd = bookingEnd?.toInstant() ?: Instant.now(),
    winnerCount = winnerCount,
    waitlistCapacity = waitlistCapacity
)

@RequiresApi(Build.VERSION_CODES.O)
fun Lottery.toDoc(): LotteryDoc = LotteryDoc(
    id = id,
    ownerId = ownerId,
    campsiteIds = campsites.map { it.id },
    registrationStart = registrationStart.toTimestamp(),
    registrationEnd = registrationEnd.toTimestamp(),
    bookingStart = bookingStart.toTimestamp(),
    bookingEnd = bookingEnd.toTimestamp(),
    winnerCount = winnerCount,
    waitlistCapacity = waitlistCapacity
)

// --- LotteryEntry Mapper ---
@RequiresApi(Build.VERSION_CODES.O)
fun LotteryEntryDoc.toDomain(): LotteryEntry = LotteryEntry(
    id = id,
    lotteryId = lotteryId,
    entrantId = entrantId,
    status = try { EntryStatus.valueOf(status) } catch (e: Exception) { EntryStatus.WAITING },
    joinedAt = joinedAt.toInstant() ?: Instant.now()
)

@RequiresApi(Build.VERSION_CODES.O)
fun LotteryEntry.toDoc(): LotteryEntryDoc = LotteryEntryDoc(
    id = id,
    lotteryId = lotteryId,
    entrantId = entrantId,
    status = status.name,
    joinedAt = joinedAt.toTimestamp() ?: Timestamp.now()
)

// --- Reservation Mapper ---
@RequiresApi(Build.VERSION_CODES.O)
fun ReservationDoc.toDomain(): Reservation = Reservation(
    id = id,
    entrantId = entrantId,
    campsiteId = campsiteId,
    lotteryId = lotteryId,
    checkIn = checkIn.toLocalDate() ?: LocalDate.now(),
    checkOut = checkOut.toLocalDate() ?: LocalDate.now(),
    status = try { ReservationStatus.valueOf(status) } catch (e: Exception) { ReservationStatus.ACTIVE },
    createdAt = createdAt.toInstant() ?: Instant.now()
)

@RequiresApi(Build.VERSION_CODES.O)
fun Reservation.toDoc(): ReservationDoc = ReservationDoc(
    id = id,
    entrantId = entrantId,
    campsiteId = campsiteId,
    lotteryId = lotteryId,
    checkIn = checkIn.toTimestamp(),
    checkOut = checkOut.toTimestamp(),
    status = status.name,
    createdAt = createdAt.toTimestamp() ?: Timestamp.now()
)

// --- CampsiteOwner Mapper ---
fun CampsiteOwnerDoc.toDomain(): CampsiteOwner = CampsiteOwner(
    id = androidId,
    name = name,
    email = email,
    phoneNumber = phoneNumber
)

fun CampsiteOwner.toDoc(): CampsiteOwnerDoc = CampsiteOwnerDoc(
    androidId = id,
    name = name,
    email = email,
    phoneNumber = phoneNumber
)

// --- Admin Mapper ---
fun AdminDoc.toDomain(): Admin = Admin(id = id)
fun Admin.toDoc(): AdminDoc = AdminDoc(id = id)