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
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sameerasw.nextbus.location.LocationData
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewScheduleSheet(
    location: LocationData,
    onDismiss: () -> Unit,
    onSave: (
        timestamp: Long,
        route: String,
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
    var selectedHour by remember { mutableStateOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) }
    var selectedMinute by remember { mutableStateOf(Calendar.getInstance().get(Calendar.MINUTE)) }
    var route by remember { mutableStateOf("") }
    var place by remember { mutableStateOf(location.address ?: "") }
    var selectedSeating by remember { mutableStateOf<String?>(null) }
    var selectedBusType by remember { mutableStateOf<String?>(null) }
    var selectedTier by remember { mutableStateOf<String?>(null) }
    var busRating by remember { mutableStateOf("") }
    var showRouteSearch by remember { mutableStateOf(false) }

    if (showRouteSearch) {
        RouteSearchScreen(
            onSelectRoute = { selectedRoute ->
                route = selectedRoute
                showRouteSearch = false
                onNavigateToRouteSearch()
            },
            onBack = { showRouteSearch = false }
        )
        return
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "New Schedule",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 8.dp)
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            // Time Selection
            SectionTitle("Time")
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = selectedHour.toString().padStart(2, '0'),
                    onValueChange = { if (it.length <= 2) selectedHour = it.toIntOrNull() ?: 0 },
                    label = { Text("Hour") },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )
                OutlinedTextField(
                    value = selectedMinute.toString().padStart(2, '0'),
                    onValueChange = { if (it.length <= 2) selectedMinute = it.toIntOrNull() ?: 0 },
                    label = { Text("Minute") },
                    modifier = Modifier.weight(1f)
                )
            }

            // Route Selection
            SectionTitle("Route")
            OutlinedTextField(
                value = route,
                onValueChange = { route = it },
                label = { Text("Select Route") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable { showRouteSearch = true },
                readOnly = true,
                trailingIcon = {
                    Button(onClick = { showRouteSearch = true }) {
                        Text("Browse")
                    }
                }
            )

            // Pickup Location
            SectionTitle("Pickup Location")
            OutlinedTextField(
                value = place,
                onValueChange = { place = it },
                label = { Text("From") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

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
                options = listOf("x1", "x1.5", "x2", "x4"),
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
                    if (route.isNotEmpty() && place.isNotEmpty()) {
                        val calendar = Calendar.getInstance()
                        calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
                        calendar.set(Calendar.MINUTE, selectedMinute)

                        onSave(
                            calendar.timeInMillis,
                            route,
                            place,
                            selectedSeating,
                            location.latitude,
                            location.longitude,
                            location.address,
                            selectedBusType,
                            selectedTier,
                            busRating.toDoubleOrNull()
                        )
                        onDismiss()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Text("Create Schedule")
            }
        }
    }
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
                .menuAnchor()
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
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp)
    )
}

