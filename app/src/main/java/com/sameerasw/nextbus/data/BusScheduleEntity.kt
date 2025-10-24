package com.sameerasw.nextbus.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bus_schedule")
data class BusScheduleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long, // epoch millis
    val route: String,
    val routeDirection: Boolean = true, // true = normal, false = flipped
    val place: String,
    val seating: String?, // "Available", "Almost full", "Full", "Loaded"
    val locationLat: Double?,
    val locationLng: Double?,
    val locationAddress: String?,
    @Embedded(prefix = "bus_") val bus: BusEmbedded?
)

data class BusEmbedded(
    val type: String? = null,
    val tier: String? = null,
    val rating: Double? = null
)

