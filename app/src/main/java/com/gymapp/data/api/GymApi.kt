package com.gymapp.data.api

import com.gymapp.data.model.Achievement
import com.gymapp.data.model.ActivityFeedItem
import com.gymapp.data.model.Announcement
import com.gymapp.data.model.AuthSession
import com.gymapp.data.model.BodyMetric
import com.gymapp.data.model.BranchStatus
import com.gymapp.data.model.CheckIn
import com.gymapp.data.model.DetailedLogRequest
import com.gymapp.data.model.KudosRequest
import com.gymapp.data.model.LoginRequest
import com.gymapp.data.model.Member
import com.gymapp.data.model.Membership
import com.gymapp.data.model.Payment
import com.gymapp.data.model.PersonalRecord
import com.gymapp.data.model.LogSessionRequest
import com.gymapp.data.model.Plan
import com.gymapp.data.model.Referral
import com.gymapp.data.model.StreakState
import com.gymapp.data.model.TrainingDay
import com.gymapp.data.model.VisitStats
import com.gymapp.data.model.WeeklyTargetRequest
import com.gymapp.data.model.VolumePoint
import com.gymapp.data.model.WeeklyActivity
import com.gymapp.data.model.WorkoutLogEntry
import com.gymapp.data.model.WorkoutSession
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * The REST contract. For v1 a [FakeGymApi] backs this with in-memory data, so the app runs
 * fully offline. Swapping to a real server is just binding a Retrofit-created implementation
 * in [com.gymapp.di.ApiModule] instead of the fake.
 */
interface GymApi {
    @POST("auth/login")
    suspend fun signIn(@Body request: LoginRequest): AuthSession

    @GET("member/me")
    suspend fun getMember(): Member

    @GET("membership")
    suspend fun getMembership(): Membership

    @GET("membership/plans")
    suspend fun getPlans(): List<Plan>

    @GET("branch")
    suspend fun getBranch(): BranchStatus

    @GET("payments")
    suspend fun getPayments(): List<Payment>

    @GET("referral")
    suspend fun getReferral(): Referral

    @GET("checkins")
    suspend fun getCheckIns(): List<CheckIn>

    @GET("checkins/stats")
    suspend fun getVisitStats(): VisitStats

    @GET("checkins/week")
    suspend fun getWeeklyActivity(): WeeklyActivity

    @GET("streak")
    suspend fun getStreakState(): StreakState

    @POST("streak/target")
    suspend fun setWeeklyTarget(@Body request: WeeklyTargetRequest): StreakState

    @GET("progress/workouts")
    suspend fun getWorkoutLogs(): List<WorkoutLogEntry>

    @GET("progress/sessions/recent")
    suspend fun getRecentSession(): WorkoutSession

    @POST("progress/sessions")
    suspend fun logSession(@Body request: LogSessionRequest): StreakState

    @POST("progress/sessions/detailed")
    suspend fun logDetailedSession(@Body request: DetailedLogRequest): StreakState

    @GET("progress/records")
    suspend fun getPersonalRecords(): List<PersonalRecord>

    @GET("progress/volume")
    suspend fun getVolumeTrend(@Query("days") days: Int): List<VolumePoint>

    @GET("progress/training-days")
    suspend fun getTrainingDays(@Query("days") days: Int): List<TrainingDay>

    @GET("progress/body")
    suspend fun getBodyMetrics(): List<BodyMetric>

    @GET("achievements")
    suspend fun getAchievements(): List<Achievement>

    @GET("activity/announcements")
    suspend fun getAnnouncements(): List<Announcement>

    @GET("activity/feed")
    suspend fun getActivityFeed(): List<ActivityFeedItem>

    @POST("activity/kudos")
    suspend fun toggleKudos(@Body request: KudosRequest)
}
