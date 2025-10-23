package com.sameerasw.nextbus.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sameerasw.nextbus.data.BusScheduleEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BusScheduleCard(
    schedule: BusScheduleEntity,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Time
                Text(
                    text = SimpleDateFormat("HH:mm, MMM dd", Locale.getDefault()).format(Date(schedule.timestamp)),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                // Route
                Text(
                    text = schedule.route,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
                // Place/Address
                Text(
                    text = schedule.locationAddress ?: schedule.place,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
                // Seating Status
                Text(
                    text = schedule.seating ?: "Unknown",
                    style = MaterialTheme.typography.labelSmall,
                    color = when (schedule.seating) {
                        "Available" -> MaterialTheme.colorScheme.primary
                        "Almost full" -> MaterialTheme.colorScheme.tertiary
                        "Full" -> MaterialTheme.colorScheme.error
                        "Loaded" -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete schedule")
            }
        }
    }
}

