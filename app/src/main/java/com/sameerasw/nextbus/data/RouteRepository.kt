package com.sameerasw.nextbus.data

import kotlinx.coroutines.flow.Flow

class RouteRepository(private val routeDao: RouteDao) {
    fun getAllRoutes(): Flow<List<RouteEntity>> {
        return routeDao.getAllRoutes()
    }

    suspend fun addRoute(routeName: String) {
        routeDao.insertRoute(RouteEntity(name = routeName))
    }

    suspend fun deleteRoute(route: RouteEntity) {
        routeDao.deleteRoute(route)
    }

    suspend fun getRouteByName(routeName: String): RouteEntity? {
        return routeDao.getRouteByName(routeName)
    }
}

