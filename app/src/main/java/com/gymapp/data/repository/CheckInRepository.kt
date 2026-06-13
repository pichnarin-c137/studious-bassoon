package com.gymapp.data.repository

import com.gymapp.data.api.GymApi
import com.gymapp.data.model.CheckIn
import com.gymapp.data.model.StreakState
import com.gymapp.data.model.VisitStats
import com.gymapp.data.model.WeeklyActivity
import javax.inject.Inject

interface CheckInRepository {
    suspend fun getCheckIns(): List<CheckIn>
    suspend fun getVisitStats(): VisitStats
    suspend fun getWeeklyActivity(): WeeklyActivity

    /** The kind streak — weekly target progress + run of met weeks + banked freezes. */
    suspend fun getStreakState(): StreakState
}

class CheckInRepositoryImpl @Inject constructor(
    private val api: GymApi,
) : CheckInRepository {
    override suspend fun getCheckIns(): List<CheckIn> = api.getCheckIns()
    override suspend fun getVisitStats(): VisitStats = api.getVisitStats()
    override suspend fun getWeeklyActivity(): WeeklyActivity = api.getWeeklyActivity()
    override suspend fun getStreakState(): StreakState = api.getStreakState()
}
