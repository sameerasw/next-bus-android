package com.sameerasw.nextbus.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sameerasw.nextbus.data.BusEmbedded
import com.sameerasw.nextbus.data.BusScheduleEntity
import com.sameerasw.nextbus.data.BusScheduleRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BusScheduleViewModel(private val repository: BusScheduleRepository) : ViewModel() {
    val schedules: StateFlow<List<BusScheduleEntity>> = repository.getAllSchedules()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addSchedule(
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
    ) {
        viewModelScope.launch {
            val schedule = BusScheduleEntity(
                timestamp = timestamp,
                route = route,
                place = place,
                seating = seating,
                locationLat = latitude,
                locationLng = longitude,
                locationAddress = address,
                bus = BusEmbedded(
                    type = busType,
                    tier = busTier,
                    rating = busRating
                )
            )
            repository.insertSchedule(schedule)
        }
    }

    fun deleteSchedule(schedule: BusScheduleEntity) {
        viewModelScope.launch {
            repository.deleteSchedule(schedule)
        }
    }
}

