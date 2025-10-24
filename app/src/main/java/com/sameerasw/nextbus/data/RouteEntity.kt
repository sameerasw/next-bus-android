package com.sameerasw.nextbus.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routes")
data class RouteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val routeNumber: String,
    val start: String,
    val end: String
) {
    // Display name for UI convenience
    fun getDisplayName(): String = "$routeNumber - $start → $end"
}

