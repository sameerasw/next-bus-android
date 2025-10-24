package com.sameerasw.nextbus.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.sameerasw.nextbus.location.LocationData
import com.sameerasw.nextbus.ui.components.MapLocationPickerDialog
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewScheduleSheet(
    location: LocationData,
    onDismiss: () -> Unit,
    onSave: (
        timestamp: Long,
        route: String,
        routeDirection: Boolean,
        place: String,
        seating: String?,
        latitude: Double?,
        longitude: Double?,
        address: String?,
        busType: String?,
        busTier: String?,
        busRating: Double?
    ) -> Unit,
    onNavigateToRouteSearch: () -> Unit
) {
    val calendar = Calendar.getInstance()
    val timePickerState = rememberTimePickerState(
        initialHour = calendar.get(Calendar.HOUR_OF_DAY),
        initialMinute = calendar.get(Calendar.MINUTE)
    )

    var routeNumber by remember { mutableStateOf("") }
    var routeStart by remember { mutableStateOf("") }
    var routeEnd by remember { mutableStateOf("") }
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

    if (showRouteSearch) {
        RouteSearchScreen(
            onSelectRoute = { number, start, end ->
                routeNumber = number
                routeStart = start
                routeEnd = end
                routeDirection = true
                showRouteSearch = false
                onNavigateToRouteSearch()
            },
            onBack = { showRouteSearch = false },
            onShowCustomInput = { showCustomRouteInput = true }
        )
        return
    }

    if (showCustomRouteInput) {
        CustomRouteInputDialog(
            onSave = { number, start, end ->
                routeNumber = number
                routeStart = start
                routeEnd = end
                routeDirection = true
                showCustomRouteInput = false
            },
            onDismiss = { showCustomRouteInput = false }
        )
        return
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth(),
        scrimColor = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.32f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "New Schedule",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 8.dp)
                )
                IconButton(onClick = onDismiss, modifier = Modifier.padding(0.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            // Time Selection
            SectionTitle("Departure Time")
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
            SectionTitle("Route")

            // Route Number
            OutlinedTextField(
                value = routeNumber,
                onValueChange = { routeNumber = it },
                label = { Text("Route Number") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                singleLine = true
            )

            // Route Start
            OutlinedTextField(
                value = routeStart,
                onValueChange = { routeStart = it },
                label = { Text("From (Start Location)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                singleLine = true
            )

            // Route End
            OutlinedTextField(
                value = routeEnd,
                onValueChange = { routeEnd = it },
                label = { Text("To (End Location)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                singleLine = true
            )

            // Browse and Flip buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
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
            SectionTitle("Pickup Location")
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
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        if (location.latitude != null && location.longitude != null && location.address != null) {
                            place = location.address
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
            SectionTitle("Seating Status")
            DropdownField(
                label = "Seating",
                options = listOf("Available", "Almost full", "Full", "Loaded"),
                selectedOption = selectedSeating,
                onOptionSelected = { selectedSeating = it }
            )

            // Bus Type
            SectionTitle("Bus Details")
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

                        // Create route string from three parts
                        val routeString = "$routeNumber - $routeStart → $routeEnd"

                        onSave(
                            cal.timeInMillis,
                            routeString,
                            routeDirection,
                            place,
                            selectedSeating,
                            selectedLatitude,
                            selectedLongitude,
                            selectedAddress,
                            selectedBusType,
                            extractTierCode(selectedTier),
                            busRating.toDoubleOrNull()
                        )
                        onDismiss()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                enabled = routeNumber.isNotEmpty() && routeStart.isNotEmpty() && routeEnd.isNotEmpty() && place.isNotEmpty()
            ) {
                Text("Create Schedule")
            }

            // Location Picker Dialog
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
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End
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

@OptIn(ExperimentalMaterial3Api::class)
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
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                androidx.compose.material3.DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 12.dp, bottom = 6.dp)
    )
}

@Composable
private fun CustomRouteInputDialog(
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
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            if (routeNumber.isNotEmpty() && routeStart.isNotEmpty() && routeEnd.isNotEmpty()) {
                                onSave(routeNumber, routeStart, routeEnd)
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

private fun extractTierCode(tier: String): String {
    return when (tier) {
        "Normal (x1)" -> "normal"
        "Semi-Luxury (x1.5)" -> "semi_luxury"
        "Luxury (x2)" -> "luxury"
        "Express (x4)" -> "express"
        else -> "normal"
    }
}

@Composable
private fun RouteSearchScreen(
    onSelectRoute: (String, String, String) -> Unit,
    onBack: () -> Unit,
    onShowCustomInput: () -> Unit = {}
) {
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
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add or Browse Route",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    IconButton(onClick = onShowCustomInput) {
                        Text("+", style = MaterialTheme.typography.headlineSmall)
                    }
                }

                Text(
                    text = "Note: Create a custom route to add it to the database.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

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
