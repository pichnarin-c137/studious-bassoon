package com.gymapp.data.api

import com.gymapp.data.model.Achievement
import com.gymapp.data.model.AuthSession
import com.gymapp.data.model.BodyMetric
import com.gymapp.data.model.BranchStatus
import com.gymapp.data.model.CheckIn
import com.gymapp.data.model.LoginRequest
import com.gymapp.data.model.Member
import com.gymapp.data.model.Membership
import com.gymapp.data.model.Payment
import com.gymapp.data.model.PersonalRecord
import com.gymapp.data.model.Plan
import com.gymapp.data.model.Referral
import com.gymapp.data.model.VisitStats
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

    @GET("progress/workouts")
    suspend fun getWorkoutLogs(): List<WorkoutLogEntry>

    @GET("progress/sessions/recent")
    suspend fun getRecentSession(): WorkoutSession

    @GET("progress/records")
    suspend fun getPersonalRecords(): List<PersonalRecord>

    @GET("progress/volume")
    suspend fun getVolumeTrend(@Query("days") days: Int): List<VolumePoint>

    @GET("progress/body")
    suspend fun getBodyMetrics(): List<BodyMetric>

    @GET("achievements")
    suspend fun getAchievements(): List<Achievement>
}
