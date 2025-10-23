package com.sameerasw.nextbus.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [BusScheduleEntity::class, RouteEntity::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun busScheduleDao(): BusScheduleDao
    abstract fun routeDao(): RouteDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bus_schedule_db"
                ).build().also { instance = it }
            }
        }
    }
}

