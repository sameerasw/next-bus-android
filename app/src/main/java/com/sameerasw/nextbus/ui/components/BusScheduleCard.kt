package com.sameerasw.nextbus.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
            .padding(12.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with time and delete button
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    // Time
                    Text(
                        text = SimpleDateFormat("hh:mm", Locale.getDefault()).format(Date(schedule.timestamp)),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.padding(0.dp)) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete schedule",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            // Divider
            androidx.compose.material3.Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Route and Location
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = "Route",
                    modifier = Modifier.padding(end = 8.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Route: ${schedule.route}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = schedule.locationAddress ?: schedule.place,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Seating Status
            if (schedule.seating != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .background(
                            color = getSeatingBackgroundColor(schedule.seating),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Seating: ",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = schedule.seating,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = getSeatingTextColor(schedule.seating)
                    )
                }
            }

            // Bus Details
            if (schedule.bus != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${schedule.bus.type?.uppercase() ?: "N/A"} - ${schedule.bus.tier ?: "N/A"}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Rating
                    if (schedule.bus.rating != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = "Rating",
                                modifier = Modifier.padding(end = 4.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = String.format("%.1f", schedule.bus.rating),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun getSeatingBackgroundColor(seating: String?): androidx.compose.ui.graphics.Color {
    return when (seating) {
        "Available" -> MaterialTheme.colorScheme.primaryContainer
        "Almost full" -> MaterialTheme.colorScheme.tertiaryContainer
        "Full" -> MaterialTheme.colorScheme.errorContainer
        "Loaded" -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
}

@Composable
private fun getSeatingTextColor(seating: String?): androidx.compose.ui.graphics.Color {
    return when (seating) {
        "Available" -> MaterialTheme.colorScheme.onPrimaryContainer
        "Almost full" -> MaterialTheme.colorScheme.onTertiaryContainer
        "Full" -> MaterialTheme.colorScheme.onErrorContainer
        "Loaded" -> MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

