package com.sameerasw.nextbus.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import com.sameerasw.nextbus.data.BusScheduleEntity
import com.sameerasw.nextbus.location.LocationData
import com.sameerasw.nextbus.ui.components.BusScheduleCard
import java.util.Calendar

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
            val listState = rememberLazyListState()
            val currentTime = System.currentTimeMillis()

            // Helper function to get time of day in milliseconds (0 to 86400000)
            fun getTimeOfDay(timestamp: Long): Long {
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = timestamp
                val hour = calendar.get(Calendar.HOUR_OF_DAY)
                val minute = calendar.get(Calendar.MINUTE)
                val second = calendar.get(Calendar.SECOND)
                return (hour * 3600000L) + (minute * 60000L) + (second * 1000L)
            }

            // Get current time of day
            val currentTimeOfDay = getTimeOfDay(currentTime)

            // Sort schedules by time of day only (ignoring date)
            val sortedSchedules = schedules.sortedBy { getTimeOfDay(it.timestamp) }
            val pastSchedules = sortedSchedules.filter { getTimeOfDay(it.timestamp) < currentTimeOfDay }
            val upcomingSchedules = sortedSchedules.filter { getTimeOfDay(it.timestamp) >= currentTimeOfDay }

            // Find first upcoming schedule and auto-scroll to it
            val firstUpcomingIndex = pastSchedules.size
            LaunchedEffect(schedules) {
                if (upcomingSchedules.isNotEmpty()) {
                    listState.animateScrollToItem(maxOf(0, firstUpcomingIndex - 1))
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(start = 8.dp, top = 4.dp, end = 8.dp, bottom = 32.dp),
                state = listState
            ) {
                // Past schedules (faded)
                items(pastSchedules) { schedule ->
                    BusScheduleCard(
                        schedule = schedule,
                        onClick = { onSelectSchedule(schedule) },
                        modifier = Modifier.alpha(0.5f)
                    )
                }

                // Current time divider (only show if there are both past and upcoming schedules)
                if (pastSchedules.isNotEmpty() && upcomingSchedules.isNotEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            HorizontalDivider(
                                modifier = Modifier
                                    .fillMaxWidth(0.5f),
                                thickness = 4.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Upcoming schedules (full opacity)
                items(upcomingSchedules) { schedule ->
                    BusScheduleCard(
                        schedule = schedule,
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

