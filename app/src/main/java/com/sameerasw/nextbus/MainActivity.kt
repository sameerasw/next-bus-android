package com.sameerasw.nextbus

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.SystemBarStyle
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.location.LocationServices
import com.sameerasw.nextbus.data.AppDatabase
import com.sameerasw.nextbus.data.BusScheduleRepository
import com.sameerasw.nextbus.location.LocationManager
import com.sameerasw.nextbus.ui.BusScheduleViewModel
import com.sameerasw.nextbus.ui.screens.BusScheduleDetailScreen
import com.sameerasw.nextbus.ui.screens.BusScheduleListScreen
import com.sameerasw.nextbus.ui.screens.NewScheduleSheet
import com.sameerasw.nextbus.ui.theme.NextBusTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable full edge-to-edge drawing for both status and navigation bars
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )

        // On Android 10+ disable forced high-contrast nav bar, so app can draw beneath gesture bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        setContent {
            NextBusTheme {
                MainApp(activity = this@MainActivity, modifier = Modifier.fillMaxSize())
            }
        }
    }
}

@Composable
fun MainApp(activity: MainActivity, modifier: Modifier = Modifier) {
    var hasLocationPermission by remember { mutableStateOf(false) }
    var appInitialized by remember { mutableStateOf(false) }
    var selectedScheduleId by remember { mutableStateOf<Long?>(null) }
    var showNewScheduleSheet by remember { mutableStateOf(false) }

    // Initialize location manager and repository
    val fusedLocationProviderClient = remember {
        LocationServices.getFusedLocationProviderClient(activity)
    }
    val locationManager = remember {
        LocationManager(activity, fusedLocationProviderClient)
    }

    val database = remember { AppDatabase.getInstance(activity) }
    val repository = remember { BusScheduleRepository(database.busScheduleDao()) }
    val viewModel: BusScheduleViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return BusScheduleViewModel(repository) as T
            }
        }
    )

    val schedules by viewModel.schedules.collectAsState()
    val location by locationManager.locationState
    val scope = rememberCoroutineScope()

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasLocationPermission = isGranted
        if (isGranted) {
            locationManager.startLocationUpdates()
            scope.launch {
                locationManager.updateAddressFromLocation()
            }
        }
        appInitialized = true
    }

    LaunchedEffect(Unit) {
        val permission = Manifest.permission.ACCESS_FINE_LOCATION

        hasLocationPermission = ContextCompat.checkSelfPermission(
            activity,
            permission
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasLocationPermission) {
            locationPermissionLauncher.launch(permission)
        } else {
            locationManager.startLocationUpdates()
            scope.launch {
                locationManager.updateAddressFromLocation()
            }
            appInitialized = true
        }
    }

    Box(modifier = modifier) {
        if (appInitialized) {
            val selectedSchedule = schedules.find { it.id == selectedScheduleId }

            // Main schedule list screen (always visible as base layer)
            BusScheduleListScreen(
                schedules = schedules,
                onSelectSchedule = { schedule ->
                    selectedScheduleId = schedule.id
                },
                onShowNewSchedule = { showNewScheduleSheet = true }
            )

            // Detail bottom sheet (shows when a schedule is selected)
            if (selectedSchedule != null) {
                BusScheduleDetailScreen(
                    schedule = selectedSchedule,
                    onBack = { selectedScheduleId = null },
                    onDelete = {
                        viewModel.deleteSchedule(selectedSchedule)
                        selectedScheduleId = null
                    }
                )
            }

            // New schedule bottom sheet
            if (showNewScheduleSheet) {
                NewScheduleSheet(
                    location = location,
                    onDismiss = { showNewScheduleSheet = false },
                    onSave = { timestamp, route, place, seating, latitude, longitude, address, busType, busTier, busRating ->
                        viewModel.addSchedule(
                            timestamp, route, place, seating,
                            latitude, longitude, address,
                            busType, busTier, busRating
                        )
                        showNewScheduleSheet = false
                    },
                    onNavigateToRouteSearch = {}
                )
            }
        }
    }
}
