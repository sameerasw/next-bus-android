package com.sameerasw.nextbus

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.sameerasw.nextbus.data.AppDatabase
import com.sameerasw.nextbus.data.BusScheduleRepository
import com.sameerasw.nextbus.data.RouteRepository
import com.sameerasw.nextbus.data.RouteEntity
import com.sameerasw.nextbus.location.LocationManager
import com.sameerasw.nextbus.ui.BusScheduleViewModel
import com.sameerasw.nextbus.ui.components.MapLocationPickerDialog
import com.sameerasw.nextbus.ui.theme.NextBusTheme
import java.util.Calendar
import java.util.Locale
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.launch

class NewScheduleActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }

        setContent {
            NextBusTheme {
                NewScheduleScreen(
                    activity = this@NewScheduleActivity,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewScheduleScreen(
    activity: NewScheduleActivity,
    modifier: Modifier = Modifier
) {
    val database = remember { AppDatabase.getInstance(activity) }
    val repository = remember { BusScheduleRepository(database.busScheduleDao()) }
    val routeRepository = remember { RouteRepository(database.routeDao()) }
    val viewModel: BusScheduleViewModel = remember {
        BusScheduleViewModel(repository)
    }

    var routesList by remember { mutableStateOf<List<RouteEntity>>(emptyList()) }
    var lastUsedRoute by remember { mutableStateOf<Pair<String, Pair<String, String>>?>(null) }

    // Load routes from database and last used route
    LaunchedEffect(Unit) {
        // Load routes
        routeRepository.getAllRoutes().collect { routes ->
            routesList = routes
        }
    }

    // Load last used route from preferences
    LaunchedEffect(Unit) {
        val sharedPref = activity.getSharedPreferences("bus_schedule_prefs", android.content.Context.MODE_PRIVATE)
        val lastRouteNumber = sharedPref.getString("last_route_number", "") ?: ""
        val lastRouteStart = sharedPref.getString("last_route_start", "") ?: ""
        val lastRouteEnd = sharedPref.getString("last_route_end", "") ?: ""
        if (lastRouteNumber.isNotEmpty() && lastRouteStart.isNotEmpty() && lastRouteEnd.isNotEmpty()) {
            lastUsedRoute = Pair(lastRouteNumber, Pair(lastRouteStart, lastRouteEnd))
        }
    }

    val fusedLocationProviderClient = remember {
        LocationServices.getFusedLocationProviderClient(activity)
    }
    val locationManager = remember {
        LocationManager(activity, fusedLocationProviderClient)
    }

    val location by locationManager.locationState

    val calendar = Calendar.getInstance()
    val timePickerState = rememberTimePickerState(
        initialHour = calendar.get(Calendar.HOUR_OF_DAY),
        initialMinute = calendar.get(Calendar.MINUTE)
    )

    var routeNumber by remember { mutableStateOf(lastUsedRoute?.first ?: "") }
    var routeStart by remember { mutableStateOf(lastUsedRoute?.second?.first ?: "") }
    var routeEnd by remember { mutableStateOf(lastUsedRoute?.second?.second ?: "") }
    var routeDirection by remember { mutableStateOf(true) }
    var place by remember { mutableStateOf(location.address ?: "") }
    var selectedLatitude by remember { mutableStateOf(location.latitude) }
    var selectedLongitude by remember { mutableStateOf(location.longitude) }
    var selectedAddress by remember { mutableStateOf(location.address) }
    var selectedSeating by remember { mutableStateOf("Available") }
    var selectedBusType by remember { mutableStateOf<String?>(null) }
    var selectedTier by remember { mutableStateOf("Normal (x1)") }
    var busRating by remember { mutableStateOf("") }
    var showRouteSearch by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showLocationPicker by remember { mutableStateOf(false) }
    var showCustomRouteInput by remember { mutableStateOf(false) }

    // Request location permission
    LaunchedEffect(Unit) {
        val permission = android.Manifest.permission.ACCESS_FINE_LOCATION
        val hasPermission = ContextCompat.checkSelfPermission(
            activity,
            permission
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            locationManager.startLocationUpdates()
        }
    }

    // Update place when location changes
    LaunchedEffect(location.address) {
        if (place.isEmpty() && location.address != null) {
            place = location.address ?: ""
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("New Schedule") },
                navigationIcon = {
                    IconButton(onClick = { activity.finish() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Time Selection
            Text(
                text = "Departure Time",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 12.dp, bottom = 6.dp)
            )
            TimePickerField(
                hour = timePickerState.hour,
                minute = timePickerState.minute,
                onPickTime = { showTimePicker = true }
            )

            if (showTimePicker) {
                TimePickerDialog(
                    timePickerState = timePickerState,
                    onDismiss = { showTimePicker = false }
                )
            }

            // Route Selection
            Text(
                text = "Route",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 12.dp, bottom = 6.dp)
            )
            OutlinedTextField(
                value = routeNumber,
                onValueChange = { routeNumber = it },
                label = { Text("Route Number") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = routeStart,
                onValueChange = { routeStart = it },
                label = { Text("From (Start Location)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = routeEnd,
                onValueChange = { routeEnd = it },
                label = { Text("To (End Location)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                singleLine = true
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showRouteSearch = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Browse Routes", style = MaterialTheme.typography.labelSmall)
                }
                Button(
                    onClick = { routeDirection = !routeDirection },
                    modifier = Modifier.weight(1f),
                    enabled = routeNumber.isNotEmpty() && routeStart.isNotEmpty() && routeEnd.isNotEmpty()
                ) {
                    Text(if (routeDirection) "Normal" else "Flipped", style = MaterialTheme.typography.labelSmall)
                }
            }

            // Pickup Location
            Text(
                text = "Pickup Location",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 12.dp, bottom = 6.dp)
            )
            OutlinedTextField(
                value = place,
                onValueChange = { place = it },
                label = { Text("From") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                leadingIcon = {
                    Icon(Icons.Default.LocationOn, contentDescription = "Location")
                }
            )

            // Location selection buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        if (location.latitude != null && location.longitude != null && location.address != null) {
                            place = location.address ?: ""
                            selectedLatitude = location.latitude
                            selectedLongitude = location.longitude
                            selectedAddress = location.address
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = location.latitude != null && location.longitude != null && location.address != null
                ) {
                    Text("Current Location", style = MaterialTheme.typography.labelSmall)
                }
                Button(
                    onClick = { showLocationPicker = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Pick on Map", style = MaterialTheme.typography.labelSmall)
                }
            }

            // Seating Status
            Text(
                text = "Seating Status",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 12.dp, bottom = 6.dp)
            )
            DropdownField(
                label = "Seating",
                options = listOf("Available", "Almost full", "Full", "Loaded"),
                selectedOption = selectedSeating,
                onOptionSelected = { selectedSeating = it }
            )

            // Bus Type
            Text(
                text = "Bus Details",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 12.dp, bottom = 6.dp)
            )
            DropdownField(
                label = "Type",
                options = listOf("sltb", "private"),
                selectedOption = selectedBusType,
                onOptionSelected = { selectedBusType = it }
            )

            // Bus Tier
            DropdownField(
                label = "Tier",
                options = listOf("Normal (x1)", "Semi-Luxury (x1.5)", "Luxury (x2)", "Express (x4)"),
                selectedOption = selectedTier,
                onOptionSelected = { selectedTier = it }
            )

            // Bus Rating
            OutlinedTextField(
                value = busRating,
                onValueChange = { busRating = it },
                label = { Text("Rating (0-5)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            // Save Button
            Button(
                onClick = {
                    if (routeNumber.isNotEmpty() && routeStart.isNotEmpty() && routeEnd.isNotEmpty() && place.isNotEmpty()) {
                        val cal = Calendar.getInstance()
                        cal.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        cal.set(Calendar.MINUTE, timePickerState.minute)
                        cal.set(Calendar.SECOND, 0)

                        // Extract tier code from display text
                        val tierCode = extractTierCode(selectedTier)

                        // Create route string from three parts
                        val routeString = "$routeNumber - $routeStart → $routeEnd"

                        viewModel.addSchedule(
                            cal.timeInMillis,
                            routeString,
                            true, // routeDirection (default true for new schedules)
                            place,
                            selectedSeating,
                            selectedLatitude,
                            selectedLongitude,
                            selectedAddress,
                            selectedBusType,
                            tierCode,
                            busRating.toDoubleOrNull()
                        )

                        // Save last used route (all three parts)
                        val sharedPref = activity.getSharedPreferences("bus_schedule_prefs", android.content.Context.MODE_PRIVATE)
                        sharedPref.edit().apply {
                            putString("last_route_number", routeNumber)
                            putString("last_route_start", routeStart)
                            putString("last_route_end", routeEnd)
                            apply()
                        }

                        activity.finish()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                enabled = routeNumber.isNotEmpty() && routeStart.isNotEmpty() && routeEnd.isNotEmpty() && place.isNotEmpty()
            ) {
                Text("Create Schedule")
            }
        }
    }

    // Route Search Screen
    if (showRouteSearch) {
        RouteSearchScreenDialog(
            routes = routesList,
            routeRepository = routeRepository,
            onSelectRoute = { number, start, end ->
                routeNumber = number
                routeStart = start
                routeEnd = end
                routeDirection = true
                showRouteSearch = false
            },
            onBack = { showRouteSearch = false },
            onShowCustomInput = { showCustomRouteInput = true }
        )
    }

    // Custom Route Input
    if (showCustomRouteInput) {
        CustomRouteInputDialog(
            routeRepository = routeRepository,
            onSave = { number, start, end ->
                routeNumber = number
                routeStart = start
                routeEnd = end
                routeDirection = true
                showCustomRouteInput = false
                showRouteSearch = false
            },
            onDismiss = { showCustomRouteInput = false }
        )
    }

    // Location Picker
    if (showLocationPicker) {
        MapLocationPickerDialog(
            initialLatitude = selectedLatitude,
            initialLongitude = selectedLongitude,
            onLocationSelected = { selectedLat, selectedLng, selectedAddr ->
                selectedLatitude = selectedLat
                selectedLongitude = selectedLng
                selectedAddress = selectedAddr
                place = selectedAddr
                showLocationPicker = false
            },
            onDismiss = { showLocationPicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteSearchScreenDialog(
    routes: List<RouteEntity>,
    routeRepository: RouteRepository,
    onSelectRoute: (String, String, String) -> Unit,
    onBack: () -> Unit,
    onShowCustomInput: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredRoutes = if (searchQuery.isEmpty()) {
        routes
    } else {
        routes.filter {
            it.routeNumber.contains(searchQuery, ignoreCase = true) ||
            it.start.contains(searchQuery, ignoreCase = true) ||
            it.end.contains(searchQuery, ignoreCase = true)
        }
    }

    Dialog(
        onDismissRequest = onBack,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        androidx.compose.material3.Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(16.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Select Route",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    IconButton(onClick = onShowCustomInput) {
                        Text("+", style = MaterialTheme.typography.headlineSmall)
                    }
                }

                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search routes...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    singleLine = true
                )

                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(filteredRoutes.size) { index ->
                        val route = filteredRoutes[index]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelectRoute(route.routeNumber, route.start, route.end)
                                    onBack()
                                }
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                route.getDisplayName(),
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            IconButton(
                                onClick = {
                                    kotlinx.coroutines.MainScope().launch {
                                        routeRepository.deleteRoute(route)
                                    }
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Delete route",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                TextButton(
                    onClick = onBack,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 8.dp)
                ) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
private fun CustomRouteInputDialog(
    routeRepository: RouteRepository,
    onSave: (String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var routeNumber by remember { mutableStateOf("") }
    var routeStart by remember { mutableStateOf("") }
    var routeEnd by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        androidx.compose.material3.Surface(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(16.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Enter Custom Route",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = routeNumber,
                    onValueChange = { routeNumber = it },
                    label = { Text("Route Number (e.g., 10A)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = routeStart,
                    onValueChange = { routeStart = it },
                    label = { Text("From (Start Location)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = routeEnd,
                    onValueChange = { routeEnd = it },
                    label = { Text("To (End Location)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    singleLine = true
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            if (routeNumber.isNotEmpty() && routeStart.isNotEmpty() && routeEnd.isNotEmpty()) {
                                kotlinx.coroutines.MainScope().launch {
                                    routeRepository.addRoute(routeNumber, routeStart, routeEnd)
                                    onSave(routeNumber, routeStart, routeEnd)
                                }
                            }
                        },
                        modifier = Modifier.padding(start = 8.dp),
                        enabled = routeNumber.isNotEmpty() && routeStart.isNotEmpty() && routeEnd.isNotEmpty()
                    ) {
                        Text("Add Route")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    timePickerState: androidx.compose.material3.TimePickerState,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        androidx.compose.material3.Surface(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(12.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Select Time",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                TimePicker(state = timePickerState)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    TextButton(onClick = onDismiss, modifier = Modifier.padding(start = 8.dp)) {
                        Text("OK")
                    }
                }
            }
        }
    }
}

@Composable
private fun TimePickerField(
    hour: Int,
    minute: Int,
    onPickTime: () -> Unit
) {
    val amPm = if (hour < 12) "AM" else "PM"
    val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour

    OutlinedTextField(
        value = String.format(Locale.US, "%02d:%02d %s", displayHour, minute, amPm),
        onValueChange = {},
        label = { Text("Time") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onPickTime() },
        readOnly = true,
        trailingIcon = {
            Button(onClick = onPickTime, modifier = Modifier.padding(4.dp)) {
                Text("Pick", style = MaterialTheme.typography.labelSmall)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownField(
    label: String,
    options: List<String>,
    selectedOption: String?,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        TextField(
            value = selectedOption ?: "",
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            options.forEach { option ->
                androidx.compose.material3.DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

private fun extractTierCode(tierDisplay: String?): String? {
    if (tierDisplay == null) return null
    return when {
        tierDisplay.contains("x1") && !tierDisplay.contains("x1.5") -> "x1"
        tierDisplay.contains("x1.5") -> "x1.5"
        tierDisplay.contains("x2") -> "x2"
        tierDisplay.contains("x4") -> "x4"
        else -> tierDisplay
    }
}
