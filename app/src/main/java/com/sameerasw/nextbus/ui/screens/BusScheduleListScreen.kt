package com.sameerasw.nextbus.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sameerasw.nextbus.data.BusScheduleEntity
import com.sameerasw.nextbus.location.LocationData
import com.sameerasw.nextbus.ui.components.BusScheduleCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusScheduleListScreen(
    schedules: List<BusScheduleEntity>,
    location: LocationData,
    onAddSchedule: (
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
    onDeleteSchedule: (BusScheduleEntity) -> Unit,
    onSelectSchedule: (BusScheduleEntity) -> Unit
) {
    var showNewScheduleSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Bus Schedules") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showNewScheduleSheet = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add schedule")
            }
        }
    ) { innerPadding ->
        if (schedules.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "No schedules yet",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Tap + to create your first schedule",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        } else {
            LazyColumn(modifier = Modifier.padding(innerPadding)) {
                items(schedules) { schedule ->
                    BusScheduleCard(
                        schedule = schedule,
                        onDelete = { onDeleteSchedule(schedule) },
                        onClick = { onSelectSchedule(schedule) }
                    )
                }
            }
        }

        if (showNewScheduleSheet) {
            NewScheduleSheet(
                location = location,
                onDismiss = { showNewScheduleSheet = false },
                onSave = onAddSchedule,
                onNavigateToRouteSearch = {}
            )
        }
    }
}

