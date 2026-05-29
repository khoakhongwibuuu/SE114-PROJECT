package com.example.carenest.feature.medical.data.remote

import com.example.carenest.core.data.network.ApiResponse
import retrofit2.Response
import retrofit2.http.*

// ── Cabinet (Tủ thuốc) ─────────────────────────────────────────────────────

data class CabinetResponse(
    val id: Long = 0,
    val medicines: List<CabinetMedicineResponse> = emptyList()
)

data class CabinetMedicineResponse(
    val id: Long = 0,
    val medicineName: String = "",
    val quantity: Int = 0,
    val unit: String = "viên",
    val expiryDate: String? = null,
    val status: String = "AVAILABLE",
    val isExpired: Boolean = false,
    val isExpiring: Boolean = false,
    val isLowStock: Boolean = false
)

data class CreateCabinetMedicineRequest(
    val medicineName: String,
    val quantity: Int,
    val unit: String,
    val expiryDate: String? = null,
    val status: String = "AVAILABLE"
)

data class UpdateCabinetMedicineRequest(
    val medicineName: String? = null,
    val quantity: Int? = null,
    val unit: String? = null,
    val expiryDate: String? = null,
    val notes: String? = null
)

// ── Daily Schedule (Lịch uống hôm nay) ────────────────────────────────────

data class MedicationLogResponse(
    val id: Long = 0,
    val medicationId: Long? = null,
    val medicineName: String = "",
    val dosage: String = "",
    val notes: String? = null,
    val status: String = "PENDING",
    val scheduledTime: String = ""
)

data class CheckInRequest(
    val status: String,   // "TAKEN" | "PENDING"
    val notes: String? = null
)

// ── Medication Schedule (Lịch dùng thuốc dài hạn) ─────────────────────────

data class MedicationScheduleResponse(
    val id: Long = 0,
    val medicineName: String = "",
    val dosage: String = "",
    val timesPerDay: Int = 1,
    val timeSlots: List<String> = emptyList(),
    val notes: String? = null,
    val startDate: String = "",
    val endDate: String = ""
)

data class CreateMedicationScheduleRequest(
    val medicineName: String,
    val dosage: String,
    val frequency: String = "DAILY",
    val timesPerDay: Int,
    val timeSlots: List<String>,
    val startDate: String,
    val endDate: String,
    val notes: String? = null
)

// ── Retrofit Interface ──────────────────────────────────────────────────────

interface MedicineApi {

    // Cabinet
    @GET("/api/v1/families/{familyId}/cabinets")
    suspend fun getCabinet(@Path("familyId") familyId: String): Response<ApiResponse<CabinetResponse>>

    @POST("/api/v1/cabinets/{cabinetId}/medicines")
    suspend fun addMedicineToCabinet(
        @Path("cabinetId") cabinetId: Long,
        @Body request: CreateCabinetMedicineRequest
    ): Response<ApiResponse<CabinetMedicineResponse>>

    @PUT("/api/v1/cabinets/{cabinetId}/medicines/{medicineId}")
    suspend fun updateCabinetMedicine(
        @Path("cabinetId") cabinetId: Long,
        @Path("medicineId") medicineId: Long,
        @Body request: UpdateCabinetMedicineRequest
    ): Response<ApiResponse<CabinetMedicineResponse>>

    @DELETE("/api/v1/cabinets/{cabinetId}/medicines/{medicineId}")
    suspend fun deleteCabinetMedicine(
        @Path("cabinetId") cabinetId: Long,
        @Path("medicineId") medicineId: Long
    ): Response<ApiResponse<Unit>>

    @POST("/api/v1/cabinets")
    suspend fun createCabinet(@Body body: Map<String, Any>): Response<ApiResponse<CabinetResponse>>

    // Daily logs
    @GET("/api/v1/medications/today")
    suspend fun getTodayLogs(@Query("profileId") profileId: Long): Response<ApiResponse<List<MedicationLogResponse>>>

    @POST("/api/v1/medication-logs/{logId}/check-in")
    suspend fun checkInDose(
        @Path("logId") logId: Long,
        @Body request: CheckInRequest
    ): Response<ApiResponse<Unit>>

    // Schedule (long-term)
    @GET("/api/v1/health-profiles/{profileId}/medications")
    suspend fun getMedicationSchedules(
        @Path("profileId") profileId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 30,
        @Query("sort") sort: String = "createdAt,desc"
    ): Response<ApiResponse<PagedMedicationResponse>>

    @POST("/api/v1/health-profiles/{profileId}/medications")
    suspend fun createMedicationSchedule(
        @Path("profileId") profileId: Long,
        @Body request: CreateMedicationScheduleRequest
    ): Response<ApiResponse<MedicationScheduleResponse>>

    @DELETE("/api/v1/medications/{scheduleId}")
    suspend fun deleteMedicationSchedule(@Path("scheduleId") scheduleId: Long): Response<ApiResponse<Unit>>
}

data class PagedMedicationResponse(
    val content: List<MedicationScheduleResponse> = emptyList(),
    val totalElements: Long = 0,
    val totalPages: Int = 0
)
