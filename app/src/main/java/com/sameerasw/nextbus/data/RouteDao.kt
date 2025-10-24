package com.sameerasw.nextbus.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RouteDao {
    @Query("SELECT * FROM routes ORDER BY routeNumber ASC")
    fun getAllRoutes(): Flow<List<RouteEntity>>

    @Insert
    suspend fun insertRoute(route: RouteEntity): Long

    @Delete
    suspend fun deleteRoute(route: RouteEntity)

    @Query("SELECT * FROM routes WHERE routeNumber = :routeNumber AND start = :start AND end = :end LIMIT 1")
    suspend fun getRouteByDetails(routeNumber: String, start: String, end: String): RouteEntity?
}

