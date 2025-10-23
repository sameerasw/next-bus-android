package com.sameerasw.nextbus.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.Send
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
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
                title = {
                    Text(
                        text = "Bus Schedules",
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showNewScheduleSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add schedule")
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        if (schedules.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = "No schedules",
                    modifier = Modifier.padding(bottom = 16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "No schedules yet",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Tap + to create your first schedule",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(start = 8.dp, top = 4.dp, end = 8.dp, bottom = 32.dp)
            ) {
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

