package com.gymapp.data.repository

import com.gymapp.data.api.GymApi
import com.gymapp.data.model.Achievement
import com.gymapp.data.model.BodyMetric
import com.gymapp.data.model.LogSessionRequest
import com.gymapp.data.model.PersonalRecord
import com.gymapp.data.model.SessionType
import com.gymapp.data.model.StreakState
import com.gymapp.data.model.TrainingDay
import com.gymapp.data.model.VisitStats
import com.gymapp.data.model.VolumePoint
import com.gymapp.data.model.WeeklyTargetRequest
import com.gymapp.data.model.WorkoutLogEntry
import com.gymapp.data.model.WorkoutSession
import javax.inject.Inject

interface ProgressRepository {
    suspend fun getWorkoutLogs(): List<WorkoutLogEntry>
    suspend fun getRecentSession(): WorkoutSession
    suspend fun getPersonalRecords(): List<PersonalRecord>
    suspend fun getVolumeTrend(days: Int): List<VolumePoint>

    /** Per-day training history over the window — powers the consistency strip + time/mix totals. */
    suspend fun getTrainingDays(days: Int): List<TrainingDay>

    /** Visit stats — totals/this-month counters behind the Home metric row. */
    suspend fun getVisitStats(): VisitStats
    suspend fun getBodyMetrics(): List<BodyMetric>
    suspend fun getAchievements(): List<Achievement>

    /** The kind streak — Progress reads this for the week-streak hero + weekly goal. */
    suspend fun getStreakState(): StreakState

    /** Changes the member's weekly session target; returns the recomputed streak state. */
    suspend fun setWeeklyTarget(target: Int): StreakState

    /** Quick-logs a session; returns the updated streak state (the weekly goal moves forward). */
    suspend fun logSession(type: SessionType, durationMin: Int): StreakState
}

class ProgressRepositoryImpl @Inject constructor(
    private val api: GymApi,
) : ProgressRepository {
    override suspend fun getWorkoutLogs(): List<WorkoutLogEntry> = api.getWorkoutLogs()
    override suspend fun getRecentSession(): WorkoutSession = api.getRecentSession()
    override suspend fun getPersonalRecords(): List<PersonalRecord> = api.getPersonalRecords()
    override suspend fun getVolumeTrend(days: Int): List<VolumePoint> = api.getVolumeTrend(days)
    override suspend fun getTrainingDays(days: Int): List<TrainingDay> = api.getTrainingDays(days)
    override suspend fun getVisitStats(): VisitStats = api.getVisitStats()
    override suspend fun getBodyMetrics(): List<BodyMetric> = api.getBodyMetrics()
    override suspend fun getAchievements(): List<Achievement> = api.getAchievements()
    override suspend fun getStreakState(): StreakState = api.getStreakState()
    override suspend fun setWeeklyTarget(target: Int): StreakState =
        api.setWeeklyTarget(WeeklyTargetRequest(target))
    override suspend fun logSession(type: SessionType, durationMin: Int): StreakState =
        api.logSession(LogSessionRequest(type, durationMin))
}
