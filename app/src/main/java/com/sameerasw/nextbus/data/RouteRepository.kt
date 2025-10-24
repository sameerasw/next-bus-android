package com.sameerasw.nextbus.data

import kotlinx.coroutines.flow.Flow

class RouteRepository(private val routeDao: RouteDao) {
    fun getAllRoutes(): Flow<List<RouteEntity>> {
        return routeDao.getAllRoutes()
    }

    suspend fun addRoute(routeNumber: String, start: String, end: String) {
        routeDao.insertRoute(RouteEntity(routeNumber = routeNumber, start = start, end = end))
    }

    suspend fun deleteRoute(route: RouteEntity) {
        routeDao.deleteRoute(route)
    }

    suspend fun getRouteByDetails(routeNumber: String, start: String, end: String): RouteEntity? {
        return routeDao.getRouteByDetails(routeNumber, start, end)
    }
}

