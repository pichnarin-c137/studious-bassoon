package com.gymapp.data.repository

import com.gymapp.data.api.GymApi
import com.gymapp.data.model.Achievement
import com.gymapp.data.model.BodyMetric
import com.gymapp.data.model.PersonalRecord
import com.gymapp.data.model.VolumePoint
import com.gymapp.data.model.WorkoutLogEntry
import com.gymapp.data.model.WorkoutSession
import javax.inject.Inject

interface ProgressRepository {
    suspend fun getWorkoutLogs(): List<WorkoutLogEntry>
    suspend fun getRecentSession(): WorkoutSession
    suspend fun getPersonalRecords(): List<PersonalRecord>
    suspend fun getVolumeTrend(days: Int): List<VolumePoint>
    suspend fun getBodyMetrics(): List<BodyMetric>
    suspend fun getAchievements(): List<Achievement>
}

class ProgressRepositoryImpl @Inject constructor(
    private val api: GymApi,
) : ProgressRepository {
    override suspend fun getWorkoutLogs(): List<WorkoutLogEntry> = api.getWorkoutLogs()
    override suspend fun getRecentSession(): WorkoutSession = api.getRecentSession()
    override suspend fun getPersonalRecords(): List<PersonalRecord> = api.getPersonalRecords()
    override suspend fun getVolumeTrend(days: Int): List<VolumePoint> = api.getVolumeTrend(days)
    override suspend fun getBodyMetrics(): List<BodyMetric> = api.getBodyMetrics()
    override suspend fun getAchievements(): List<Achievement> = api.getAchievements()
}
