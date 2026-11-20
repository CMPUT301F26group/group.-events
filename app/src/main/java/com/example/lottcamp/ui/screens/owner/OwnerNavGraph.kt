package com.example.lottcamp.ui.screens.owner

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.lottcamp.controller.CampsiteDetailsController
import com.example.lottcamp.controller.CampsiteFormController
import com.example.lottcamp.controller.OwnerController
import com.example.lottcamp.controller.OwnerHomeController
import com.example.lottcamp.controller.OwnerRoutes
import com.example.lottcamp.domain.UserRole
import com.example.lottcamp.repository.CampsiteRepository
import com.example.lottcamp.ui.components.AppBottomNavOwner
import com.example.lottcamp.ui.components.MainTab

/**
 * Navigation between all owner screens, starting at the owner home screen.
 * Loads the owner's campsites when first shown, and shows a dialog if loading or saving fails.
 *
 * @param ownerId Android ID of the owner.
 * @param availableRoles Roles the user may switch to, shown in the role pill.
 * @param onRoleChange Called when the user picks a different role.
 * @param data Where the campsites come from. Tests can pass a fake.
 */
@Composable
fun OwnerNavGraph(
    ownerId: String,
    availableRoles: List<UserRole>,
    onRoleChange: (UserRole) -> Unit,
    data: CampsiteRepository = CampsiteRepository(),
) {
    val scope = rememberCoroutineScope()
    val controller = remember { OwnerController(ownerId, data, scope) }
    val navController = rememberNavController()
    var tab by rememberSaveable { mutableStateOf(MainTab.Explore) }
    val go: (String) -> Unit = { navController.navigate(it) }
    val back: () -> Unit = { navController.popBackStack() }

    // Load the campsites once, when the owner screens first open
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
                onSelect = { tab = it }, // TODO: navigate once the other tabs exist
            )
        },
    ) { innerPadding ->
        NavHost(navController, startDestination = OwnerRoutes.MY_CAMPSITES) {
            composable(OwnerRoutes.MY_CAMPSITES) {
                val screen = remember { OwnerHomeController(controller, availableRoles, onRoleChange, go) }
                MyCampsiteScreen(
                    campsites = screen.campsites,
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
                    onPreviousMonth = screen::onPreviousMonth,
                    onNextMonth = screen::onNextMonth,
                    onDownloadQr = screen::onDownloadQrClick,
                    onEdit = screen::onEditClick,
                    onCreateLottery = screen::onCreateLotteryClick,
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