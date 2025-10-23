package com.sameerasw.nextbus.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sameerasw.nextbus.data.BusScheduleEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusScheduleDetailScreen(
    schedule: BusScheduleEntity,
    onBack: () -> Unit,
    onDelete: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onBack,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            // Header with title and delete button
            androidx.compose.foundation.layout.Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text(
                    "Schedule Details",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete schedule", tint = MaterialTheme.colorScheme.error)
                }
            }

            // Time Section
            DetailSection(title = "Time") {
                Text(
                    text = SimpleDateFormat("EEEE, MMMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
                        .format(Date(schedule.timestamp)),
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            // Route Section
            DetailSection(title = "Route") {
                Text(
                    text = schedule.route,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            // Pickup Location Section
            DetailSection(title = "Pickup Location") {
                Text(
                    text = schedule.place,
                    style = MaterialTheme.typography.bodyMedium
                )
                if (!schedule.locationAddress.isNullOrEmpty()) {
                    Text(
                        text = schedule.locationAddress,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                if (schedule.locationLat != null && schedule.locationLng != null) {
                    Text(
                        text = String.format(
                            "%.4f, %.4f",
                            schedule.locationLat,
                            schedule.locationLng
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Seating Section
            DetailSection(title = "Seating Status") {
                Text(
                    text = schedule.seating ?: "Unknown",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            // Bus Details Section
            schedule.bus?.let { bus ->
                DetailSection(title = "Bus Details") {
                    if (!bus.type.isNullOrEmpty()) {
                        DetailItem(label = "Type", value = bus.type)
                    }
                    if (!bus.tier.isNullOrEmpty()) {
                        DetailItem(label = "Tier", value = displayTier(bus.tier))
                    }
                    if (bus.rating != null) {
                        DetailItem(label = "Rating", value = String.format("%.1f", bus.rating))
                    }
                }
            }

            // Close button at bottom
            Button(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text("Close")
            }
        }
    }
}

@Composable
private fun DetailSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        content()
    }
}

@Composable
private fun DetailItem(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

private fun displayTier(tier: String?): String {
    if (tier == null) return "N/A"
    return when {
        tier.contains("x1") && !tier.contains("x1.5") -> "Normal (x1)"
        tier.contains("x1.5") -> "Semi-Luxury (x1.5)"
        tier.contains("x2") -> "Luxury (x2)"
        tier.contains("x4") -> "Express (x4)"
        else -> tier
    }
}

fun extractTierCode(tierDisplay: String?): String? {
    if (tierDisplay == null) return null
    return when {
        tierDisplay.contains("x1") && !tierDisplay.contains("x1.5") -> "x1"
        tierDisplay.contains("x1.5") -> "x1.5"
        tierDisplay.contains("x2") -> "x2"
        tierDisplay.contains("x4") -> "x4"
        else -> tierDisplay
    }
}

