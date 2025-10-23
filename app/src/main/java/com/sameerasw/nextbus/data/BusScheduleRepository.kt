package com.sameerasw.nextbus.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class BusScheduleRepository(private val dao: BusScheduleDao) {
    fun getAllSchedules(): Flow<List<BusScheduleEntity>> = flow {
        emit(dao.getAll())
    }.flowOn(Dispatchers.IO)

    suspend fun insertSchedule(schedule: BusScheduleEntity): Long {
        return dao.insert(schedule)
    }

    suspend fun deleteSchedule(schedule: BusScheduleEntity) {
        dao.delete(schedule)
    }

    suspend fun deleteScheduleById(id: Long) {
        dao.deleteById(id)
    }

    suspend fun getScheduleById(id: Long): BusScheduleEntity? {
        return dao.getById(id)
    }
}

