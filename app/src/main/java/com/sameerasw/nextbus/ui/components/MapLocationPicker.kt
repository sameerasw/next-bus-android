package com.sameerasw.nextbus.ui.components

import android.location.Geocoder
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import java.util.Locale

@Composable
fun MapLocationPickerDialog(
    initialLatitude: Double?,
    initialLongitude: Double?,
    onLocationSelected: (latitude: Double, longitude: Double, address: String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val initialLat = initialLatitude ?: 0.0
    val initialLng = initialLongitude ?: 0.0
    val initialLocation = LatLng(initialLat, initialLng)

    val markerState = rememberMarkerState(position = initialLocation)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialLocation, 15f)
    }

    LaunchedEffect(initialLocation) {
        markerState.position = initialLocation
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        androidx.compose.material3.Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(12.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Header
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Pick Location on Map",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.padding(0.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Google Map
                GoogleMap(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp),
                    cameraPositionState = cameraPositionState,
                    onMapClick = { latLng ->
                        markerState.position = latLng
                    }
                ) {
                    Marker(
                        state = markerState,
                        title = "Selected Location"
                    )
                }

                // Location info and buttons
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Latitude: ${String.format(Locale.US, "%.4f", markerState.position.latitude)}\nLongitude: ${String.format(Locale.US, "%.4f", markerState.position.longitude)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Action buttons
                    androidx.compose.foundation.layout.Row(
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
                                val geocoder = Geocoder(context, Locale.getDefault())
                                try {
                                    @Suppress("DEPRECATION")
                                    val addresses = geocoder.getFromLocation(
                                        markerState.position.latitude,
                                        markerState.position.longitude,
                                        1
                                    )
                                    val address = if (addresses != null && addresses.isNotEmpty()) {
                                        addresses[0].getAddressLine(0)
                                    } else {
                                        "Location at ${String.format(Locale.US, "%.4f", markerState.position.latitude)}, ${String.format(Locale.US, "%.4f", markerState.position.longitude)}"
                                    }
                                    onLocationSelected(
                                        markerState.position.latitude,
                                        markerState.position.longitude,
                                        address
                                    )
                                } catch (_: Exception) {
                                    onLocationSelected(
                                        markerState.position.latitude,
                                        markerState.position.longitude,
                                        "Location at ${String.format(Locale.US, "%.4f", markerState.position.latitude)}, ${String.format(Locale.US, "%.4f", markerState.position.longitude)}"
                                    )
                                }
                            },
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text("Confirm Location")
                        }
                    }
                }
            }
        }
    }
}

