package com.example.lottcamp.ui.screens.owner

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.lottcamp.controller.CampsiteDetailsController
import com.example.lottcamp.controller.CampsiteFormController
import com.example.lottcamp.controller.ChatController
import com.example.lottcamp.controller.LotteryFormController
import com.example.lottcamp.controller.OwnerController
import com.example.lottcamp.controller.OwnerHomeController
import com.example.lottcamp.controller.OwnerNotificationsController
import com.example.lottcamp.controller.OwnerReservationsController
import com.example.lottcamp.controller.OwnerRoutes
import com.example.lottcamp.controller.WaitingListController
import com.example.lottcamp.domain.UserRole
import com.example.lottcamp.repository.CampsiteOwnerRepository
import com.example.lottcamp.repository.CampsiteRepository
import com.example.lottcamp.repository.EntrantRepository
import com.example.lottcamp.repository.LotteryEntryRepository
import com.example.lottcamp.repository.LotteryRepository
import com.example.lottcamp.repository.MessageRepository
import com.example.lottcamp.repository.NotificationRepository
import com.example.lottcamp.repository.ReservationRepository
import com.example.lottcamp.ui.components.AppBottomNavOwner
import com.example.lottcamp.ui.components.MainTab

/**
 * Navigation between all owner screens, starting at the owner home screen.
 * Loads the owner's data when first shown, and shows a dialog if loading or saving fails.
 *
 * @param ownerId Android ID of the owner.
 * @param availableRoles Roles the user may switch to, shown in the role pill.
 * @param onRoleChange Called when the user picks a different role.
 * @param data Where the campsites come from. Tests can pass a fake.
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun OwnerNavGraph(
    ownerId: String,
    availableRoles: List<UserRole>,
    onRoleChange: (UserRole) -> Unit,
    data: CampsiteRepository = CampsiteRepository(),
) {
    val scope = rememberCoroutineScope()
    val lotteryRepo = remember { LotteryRepository() }
    val entryRepo = remember { LotteryEntryRepository() }
    val entrantRepo = remember { EntrantRepository() }
    val notificationRepo = remember { NotificationRepository() }
    val reservationRepo = remember { ReservationRepository() }
    val messageRepo = remember { MessageRepository() }
    val ownerRepo = remember { CampsiteOwnerRepository() }
    val controller = remember { OwnerController(ownerId, data, lotteryRepo, reservationRepo, scope) }
    val navController = rememberNavController()
    val go: (String) -> Unit = { navController.navigate(it) }
    val back: () -> Unit = { navController.popBackStack() }

    // The highlighted tab follows the screen that is showing
    val route = navController.currentBackStackEntryAsState().value?.destination?.route
    val tab = when (route) {
        OwnerRoutes.RESERVATIONS -> MainTab.Bookings
        OwnerRoutes.NOTIFICATIONS, OwnerRoutes.CHAT -> MainTab.Messages
        else -> MainTab.Explore
    }

    // Load everything once, when the owner screens first open
    LaunchedEffect(Unit) { controller.refresh() }

    controller.error?.let { message ->
        AlertDialog(
            onDismissRequest = { controller.clearError() },
            confirmButton = { TextButton(onClick = { controller.clearError() }) { Text("OK") } },
            title = { Text("Something went wrong") },
            text = { Text(message) },
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            AppBottomNavOwner(
                selected = tab,
                onSelect = { target ->
                    when (target) {
                        MainTab.Explore -> navController.navigate(OwnerRoutes.MY_CAMPSITES) {
                            popUpTo(OwnerRoutes.MY_CAMPSITES)
                            launchSingleTop = true
                        }
                        MainTab.Bookings -> navController.navigate(OwnerRoutes.RESERVATIONS) {
                            popUpTo(OwnerRoutes.MY_CAMPSITES)
                            launchSingleTop = true
                        }
                        MainTab.Messages -> {
                            // Notifications go to a lottery's waiting list, so use the newest lottery
                            val lottery = controller.latestLottery()
                            if (lottery == null) {
                                controller.report("Create a lottery first to send notifications.")
                            } else {
                                navController.navigate(OwnerRoutes.notifications(lottery.id)) {
                                    popUpTo(OwnerRoutes.MY_CAMPSITES)
                                    launchSingleTop = true
                                }
                            }
                        }
                        MainTab.Profile -> {} // no owner profile screen in the design
                    }
                },
            )
        },
    ) { innerPadding ->
        NavHost(navController, startDestination = OwnerRoutes.MY_CAMPSITES) {
            composable(OwnerRoutes.MY_CAMPSITES) {
                val screen = remember { OwnerHomeController(controller, availableRoles, onRoleChange, go) }
                MyCampsiteScreen(
                    campsites = screen.campsites,
                    subtitle = screen::subtitle,
                    role = screen.role,
                    availableRoles = screen.availableRoles,
                    onRoleSelected = screen::onRoleSelected,
                    onRegisterClick = screen::onRegisterClick,
                    onCampsiteClick = screen::onCampsiteClick,
                    modifier = Modifier.padding(innerPadding),
                )
            }
            composable(OwnerRoutes.REGISTER_CAMPSITE) {
                val screen = remember { CampsiteFormController(controller, ownerId, null, back) }
                CampsiteForm(screen, Modifier.padding(innerPadding))
            }
            composable(OwnerRoutes.CAMPSITE) { entry ->
                val id = entry.arguments?.getString("id") ?: ""
                val screen = remember(id) { CampsiteDetailsController(controller, id, go, back) }
                val campsite = screen.campsite ?: return@composable // still loading
                CampsiteDetailsScreen(
                    name = campsite.name,
                    imageUrl = campsite.imageUrl,
                    location = campsite.locationName,
                    capacityAndPrice = screen.capacityAndPrice,
                    year = screen.year,
                    month = screen.month,
                    bookedDays = screen.bookedDays,
                    lotteryText = screen.lotteryText,
                    onPreviousMonth = screen::onPreviousMonth,
                    onNextMonth = screen::onNextMonth,
                    onDownloadQr = screen::onDownloadQrClick,
                    onEdit = screen::onEditClick,
                    onCreateLottery = screen::onLotteryClick,
                    onBack = screen::onBackClick,
                    modifier = Modifier.padding(innerPadding),
                )
            }
            composable(OwnerRoutes.EDIT_CAMPSITE) { entry ->
                val id = entry.arguments?.getString("id") ?: ""
                val campsite = controller.campsite(id) ?: return@composable // still loading
                val screen = remember(id) { CampsiteFormController(controller, ownerId, campsite, back) }
                CampsiteForm(screen, Modifier.padding(innerPadding))
            }
            composable(OwnerRoutes.CREATE_LOTTERY) { entry ->
                val id = entry.arguments?.getString("id") ?: ""
                val screen = remember(id) {
                    LotteryFormController(
                        controller, ownerId, lotteryRepo, scope, null, id,
                        onSaved = { lotteryId ->
                            // Replace the form with the waiting list so Back returns to the details
                            navController.navigate(OwnerRoutes.waitingList(lotteryId)) {
                                popUpTo(OwnerRoutes.CREATE_LOTTERY) { inclusive = true }
                            }
                        },
                        back = back,
                    )
                }
                LotteryForm(screen, Modifier.padding(innerPadding))
            }
            composable(OwnerRoutes.EDIT_LOTTERY) { entry ->
                val id = entry.arguments?.getString("id") ?: ""
                val screen = remember(id) {
                    LotteryFormController(controller, ownerId, lotteryRepo, scope, id, null, { back() }, back)
                }
                LaunchedEffect(id) { screen.load() }
                LotteryForm(screen, Modifier.padding(innerPadding))
            }
            composable(OwnerRoutes.WAITING_LIST) { entry ->
                val id = entry.arguments?.getString("id") ?: ""
                val screen = remember(id) {
                    WaitingListController(id, lotteryRepo, entryRepo, entrantRepo, notificationRepo, scope, go, back)
                }
                LaunchedEffect(id) { screen.refresh() }
                WaitingListScreen(
                    campsiteNames = screen.campsiteNames,
                    summary = screen.summary,
                    drawText = screen.drawText,
                    rows = screen.rows,
                    error = screen.error,
                    onDismissError = screen::clearError,
                    onEdit = screen::onEditClick,
                    onSendNotification = screen::onSendNotificationClick,
                    onBack = screen::onBackClick,
                    modifier = Modifier.padding(innerPadding),
                )
            }
            composable(OwnerRoutes.NOTIFICATIONS) { entry ->
                val id = entry.arguments?.getString("id") ?: ""
                val screen = remember(id) {
                    OwnerNotificationsController(
                        ownerId, id, entryRepo, notificationRepo, ownerRepo, entrantRepo, scope, back,
                    )
                }
                LaunchedEffect(id) { screen.refresh() }
                OwnerNotificationsScreen(
                    message = screen.message,
                    onMessageChange = screen::onMessageChange,
                    recipientCount = screen.recipientCount,
                    sending = screen.sending,
                    sent = screen.sent,
                    error = screen.error,
                    onDismissError = screen::clearError,
                    onSend = screen::onSendClick,
                    onBack = screen::onBackClick,
                    modifier = Modifier.padding(innerPadding),
                )
            }
            composable(OwnerRoutes.RESERVATIONS) {
                val screen = remember {
                    OwnerReservationsController(
                        ownerId, controller, entrantRepo, reservationRepo, notificationRepo, scope, go,
                    )
                }
                LaunchedEffect(Unit) { controller.refresh() }
                LaunchedEffect(screen.reservationCount) { screen.loadNames() }
                ReservationsScreen(
                    filters = screen.filters,
                    selected = screen.filter,
                    onFilterSelect = screen::onFilterSelect,
                    rows = screen.rows,
                    rejecting = screen.rejecting,
                    rejectMessage = screen.rejectMessage,
                    onMessage = screen::onMessageClick,
                    onReject = screen::onRejectClick,
                    onRejectConfirm = screen::onRejectConfirm,
                    onRejectDismiss = screen::onRejectDismiss,
                    error = screen.error,
                    onDismissError = screen::clearError,
                    modifier = Modifier.padding(innerPadding),
                )
            }
            composable(OwnerRoutes.CHAT) { entry ->
                val id = entry.arguments?.getString("id") ?: ""
                val screen = remember(id) {
                    ChatController(ownerId, id, controller, entrantRepo, ownerRepo, messageRepo, scope, back)
                }
                LaunchedEffect(id) { screen.refresh() }
                ChatScreen(
                    title = screen.title,
                    subtitle = screen.subtitle,
                    bubbles = screen.bubbles,
                    draft = screen.draft,
                    onDraftChange = screen::onDraftChange,
                    sending = screen.sending,
                    onSend = screen::onSendClick,
                    error = screen.error,
                    onDismissError = screen::clearError,
                    onBack = screen::onBackClick,
                    modifier = Modifier.padding(innerPadding),
                )
            }
        }
    }
}

/** Connects [CampsiteFormScreen] to its controller. Shared by register and edit. */
@Composable
private fun CampsiteForm(screen: CampsiteFormController, modifier: Modifier) {
    CampsiteFormScreen(
        title = screen.title,
        submitText = screen.submitText,
        imageUrl = screen.imageUrl,
        name = screen.name,
        onNameChange = screen::onNameChange,
        nameError = screen.nameError,
        location = screen.location,
        onLocationChange = screen::onLocationChange,
        description = screen.description,
        onDescriptionChange = screen::onDescriptionChange,
        maxOccupancy = screen.maxOccupancy,
        onMaxOccupancyChange = screen::onMaxOccupancyChange,
        occupancyError = screen.occupancyError,
        pricePerNight = screen.pricePerNight,
        onPricePerNightChange = screen::onPricePerNightChange,
        priceError = screen.priceError,
        availableTags = screen.availableTags,
        selectedTags = screen.selectedTags,
        onTagClick = screen::onTagClick,
        onPhotoClick = screen::onPhotoClick,
        onMapClick = screen::onMapClick,
        onBack = screen::onBackClick,
        onSubmit = screen::onSubmitClick,
        modifier = modifier,
    )
}

/** Connects [LotteryFormScreen] to its controller. Shared by create and edit. */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun LotteryForm(screen: LotteryFormController, modifier: Modifier) {
    LotteryFormScreen(
        title = screen.title,
        submitText = screen.submitText,
        options = screen.options,
        selectedIds = screen.selectedIds,
        onCampsiteToggle = screen::onCampsiteToggle,
        registrationStart = screen.registrationStart,
        onRegistrationStartChange = screen::onRegistrationStartChange,
        registrationEnd = screen.registrationEnd,
        onRegistrationEndChange = screen::onRegistrationEndChange,
        bookingStart = screen.bookingStart,
        onBookingStartChange = screen::onBookingStartChange,
        bookingEnd = screen.bookingEnd,
        onBookingEndChange = screen::onBookingEndChange,
        winners = screen.winners,
        onWinnersChange = screen::onWinnersChange,
        waitlistLimit = screen.waitlistLimit,
        onWaitlistLimitChange = screen::onWaitlistLimitChange,
        formError = screen.formError,
        saving = screen.saving,
        onSubmit = screen::onSubmitClick,
        onBack = screen::onBackClick,
        modifier = modifier,
    )
}