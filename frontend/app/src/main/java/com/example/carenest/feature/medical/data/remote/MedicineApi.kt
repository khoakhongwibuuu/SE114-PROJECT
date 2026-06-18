package com.example.carenest.feature.medical.data.remote

import com.example.carenest.core.data.network.ApiResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

data class CabinetResponse(
    val id: Long = 0,
    val familyId: Long? = null,
    val name: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val medicines: List<CabinetMedicineResponse> = emptyList()
)

data class CabinetMedicineResponse(
    val id: Long = 0,
    val medicineName: String = "",
    val quantity: Int = 0,
    val unit: String = "vien",
    val expiryDate: String? = null,
    val addedDate: String? = null,
    val notes: String? = null,
    val isExpired: Boolean = false,
    val isExpiring: Boolean = false,
    val isLowStock: Boolean = false
)

data class CreateCabinetRequest(
    val familyId: Long,
    val name: String? = null
)

data class CreateCabinetMedicineRequest(
    val medicineName: String,
    val quantity: Int,
    val unit: String,
    val expiryDate: String? = null,
    val notes: String? = null
)

data class UpdateCabinetMedicineRequest(
    val medicineName: String? = null,
    val quantity: Int? = null,
    val unit: String? = null,
    val expiryDate: String? = null,
    val notes: String? = null
)

data class MedicationLogResponse(
    val id: Long = 0,
    val medicationId: Long? = null,
    val medicineName: String = "",
    val dosage: String = "",
    val notes: String? = null,
    val status: String = "PENDING",
    val scheduledTime: String = "",
    val takenTime: String? = null
)

data class CheckInRequest(
    val status: String,
    val notes: String? = null
)

data class MedicationScheduleResponse(
    val id: Long = 0,
    val healthProfileId: Long? = null,
    val medicineName: String = "",
    val dosage: String = "",
    val frequency: String = "DAILY",
    val timesPerDay: Int = 1,
    val timeSlots: List<String> = emptyList(),
    val notes: String? = null,
    val startDate: String = "",
    val endDate: String? = null,
    val status: String = "ACTIVE"
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

data class PagedMedicationResponse(
    val content: List<MedicationScheduleResponse> = emptyList(),
    val page: Int = 0,
    val size: Int = 0,
    val totalElements: Long = 0,
    val totalPages: Int = 0,
    val last: Boolean = true
)

interface MedicineApi {
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
    suspend fun createCabinet(@Body request: CreateCabinetRequest): Response<ApiResponse<CabinetResponse>>

    @GET("/api/v1/medications/today")
    suspend fun getTodayLogs(@Query("profileId") profileId: Long): Response<ApiResponse<List<MedicationLogResponse>>>

    @POST("/api/v1/medication-logs/{logId}/check-in")
    suspend fun checkInDose(
        @Path("logId") logId: Long,
        @Body request: CheckInRequest
    ): Response<ApiResponse<Unit>>

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

    @PUT("/api/v1/medications/{scheduleId}/complete")
    suspend fun completeMedicationSchedule(@Path("scheduleId") scheduleId: Long): Response<ApiResponse<Unit>>

    @POST("/api/v1/ocr/parse")
    suspend fun parseOcrText(
        @Body request: ParseOcrRequest
    ): Response<ApiResponse<List<ParsedMedicationDto>>>
}

data class ParseOcrRequest(
    val rawText: String
)

data class ParsedMedicationDto(
    val medicineName: String,
    val totalQuantity: Int? = null,
    val unit: String? = null,
    val dosage: String? = null,
    val frequency: String? = null,
    val timesPerDay: Int? = null,
    val durationDays: Int? = null,
    val notes: String? = null
)
