package com.sameerasw.nextbus.navigation

sealed class Screen {
    data object ScheduleList : Screen()
    data class ScheduleDetail(val scheduleId: Long) : Screen()
    data object RouteSearch : Screen()
    data object MapPicker : Screen()
}

