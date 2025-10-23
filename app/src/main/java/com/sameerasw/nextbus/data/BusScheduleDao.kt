package com.sameerasw.nextbus.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BusScheduleDao {
    @Query("SELECT * FROM bus_schedule ORDER BY timestamp DESC")
    fun getAllSchedules(): Flow<List<BusScheduleEntity>>

    @Query("SELECT * FROM bus_schedule ORDER BY timestamp DESC")
    suspend fun getAll(): List<BusScheduleEntity>

    @Query("SELECT * FROM bus_schedule WHERE id = :id")
    suspend fun getById(id: Long): BusScheduleEntity?

    @Insert
    suspend fun insert(schedule: BusScheduleEntity): Long

    @Delete
    suspend fun delete(schedule: BusScheduleEntity)

    @Query("DELETE FROM bus_schedule WHERE id = :id")
    suspend fun deleteById(id: Long)
}

