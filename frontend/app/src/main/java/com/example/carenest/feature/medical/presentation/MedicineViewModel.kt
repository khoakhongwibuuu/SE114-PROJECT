package com.example.carenest.feature.medical.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.carenest.core.data.storage.SecureSessionManager
import com.example.carenest.feature.medical.data.remote.CabinetMedicineResponse
import com.example.carenest.feature.medical.data.remote.CheckInRequest
import com.example.carenest.feature.medical.data.remote.CreateCabinetRequest
import com.example.carenest.feature.medical.data.remote.CreateCabinetMedicineRequest
import com.example.carenest.feature.medical.data.remote.CreateMedicationScheduleRequest
import com.example.carenest.feature.medical.data.remote.MedicationLogResponse
import com.example.carenest.feature.medical.data.remote.MedicationScheduleResponse
import com.example.carenest.feature.medical.data.remote.MedicineApi
import com.example.carenest.feature.medical.data.remote.UpdateCabinetMedicineRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// ── Cabinet State ──────────────────────────────────────────────────────────
sealed class CabinetState {
    object Loading : CabinetState()
    data class Success(
        val cabinetId: Long,
        val medicines: List<CabinetMedicineResponse>
    ) : CabinetState()
    data class Error(val message: String) : CabinetState()
}

// ── Daily Schedule State ───────────────────────────────────────────────────
data class DoseSection(
    val session: String,   // "MORNING" | "NOON" | "EVENING"
    val label: String,
    val items: List<MedicationLogResponse>
)

sealed class ScheduleState {
    object Loading : ScheduleState()
    data class Success(
        val profileName: String,
        val sections: List<DoseSection>,
        val takenCount: Int,
        val totalCount: Int
    ) : ScheduleState()
    data class Error(val message: String) : ScheduleState()
    object Empty : ScheduleState()
}

data class ParsedMedicine(
    val name: String,
    val dosage: String,
    val frequency: String,
    val note: String = ""
)

// ── MedicineViewModel ──────────────────────────────────────────────────────
class MedicineViewModel(
    private val medicineApi: MedicineApi,
    private val secureSessionManager: SecureSessionManager
) : ViewModel() {

    // ── Cabinet ─────────────────────────────────────────────────────────────
    private val _cabinetState = MutableStateFlow<CabinetState>(CabinetState.Loading)
    val cabinetState: StateFlow<CabinetState> = _cabinetState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow("Tất cả")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    // ── Daily Schedule ───────────────────────────────────────────────────────
    private val _scheduleState = MutableStateFlow<ScheduleState>(ScheduleState.Loading)
    val scheduleState: StateFlow<ScheduleState> = _scheduleState.asStateFlow()

    // ── Long-term schedules (for form data) ─────────────────────────────────
    private val _schedules = MutableStateFlow<List<MedicationScheduleResponse>>(emptyList())
    val schedules: StateFlow<List<MedicationScheduleResponse>> = _schedules.asStateFlow()

    private val _isActionLoading = MutableStateFlow(false)
    val isActionLoading: StateFlow<Boolean> = _isActionLoading.asStateFlow()

    // ── Init ─────────────────────────────────────────────────────────────────
    init {
        viewModelScope.launch {
            secureSessionManager.familyIdFlow.collect { familyId ->
                if (familyId != null) {
                    fetchCabinet(familyId)
                }
            }
        }
    }

    // ── Cabinet operations ───────────────────────────────────────────────────

    fun fetchCabinet(familyId: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            _cabinetState.value = CabinetState.Loading
            try {
                val fid = familyId ?: secureSessionManager.familyIdFlow.value ?: run {
                    _cabinetState.value = CabinetState.Error("Chưa chọn gia đình")
                    return@launch
                }
                val response = medicineApi.getCabinet(fid)
                if (response.isSuccessful) {
                    val cabinet = response.body()?.data
                    if (cabinet != null) {
                        _cabinetState.value = CabinetState.Success(
                            cabinetId = cabinet.id,
                            medicines = cabinet.medicines
                        )
                    } else {
                        _cabinetState.value = CabinetState.Success(cabinetId = 0, medicines = emptyList())
                    }
                } else if (response.code() == 404) {
                    _cabinetState.value = CabinetState.Success(cabinetId = 0, medicines = emptyList())
                } else {
                    _cabinetState.value = CabinetState.Error("Khong the tai tu thuoc")
                }
            } catch (e: Exception) {
                _cabinetState.value = CabinetState.Error(e.localizedMessage ?: "Lỗi kết nối")
            }
        }
    }

    fun addMedicine(
        name: String,
        quantity: Int,
        unit: String,
        expiryDate: String?,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _isActionLoading.value = true
            try {
                val currentState = _cabinetState.value as? CabinetState.Success ?: run {
                    _isActionLoading.value = false
                    withContext(Dispatchers.Main) {
                        onError("Không thể lấy thông tin tủ thuốc hiện tại")
                    }
                    return@launch
                }
                var cabinetId = currentState.cabinetId
                // If cabinet doesn't exist yet, create one
                if (cabinetId == 0L) {
                    val fid = secureSessionManager.familyIdFlow.value ?: run {
                        _isActionLoading.value = false
                        withContext(Dispatchers.Main) {
                            onError("Chưa chọn gia đình")
                        }
                        return@launch
                    }
                    val familyIdLong = fid.toLongOrNull() ?: run {
                        _isActionLoading.value = false
                        withContext(Dispatchers.Main) {
                            onError("Ma gia dinh khong hop le")
                        }
                        return@launch
                    }
                    val createResponse = medicineApi.createCabinet(
                        CreateCabinetRequest(familyId = familyIdLong, name = "Tu thuoc gia dinh")
                    )
                    cabinetId = createResponse.body()?.data?.id ?: run {
                        _isActionLoading.value = false
                        withContext(Dispatchers.Main) {
                            onError("Không thể tạo tủ thuốc mới")
                        }
                        return@launch
                    }
                }
                val response = medicineApi.addMedicineToCabinet(
                    cabinetId,
                    CreateCabinetMedicineRequest(
                        medicineName = name,
                        quantity = quantity,
                        unit = unit,
                        expiryDate = expiryDate?.ifBlank { null }
                    )
                )
                if (response.isSuccessful) {
                    fetchCabinet()
                    withContext(Dispatchers.Main) {
                        onSuccess()
                    }
                } else {
                    val errorMsg = response.body()?.message ?: "Thêm thuốc thất bại"
                    withContext(Dispatchers.Main) {
                        onError(errorMsg)
                    }
                }
            } catch (e: Exception) {
                val errorMsg = e.localizedMessage ?: "Lỗi kết nối mạng"
                withContext(Dispatchers.Main) {
                    onError(errorMsg)
                }
            } finally {
                _isActionLoading.value = false
            }
        }
    }

    fun updateMedicineQuantity(medicineId: Long, newQuantity: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            _isActionLoading.value = true
            try {
                val cabinetId = (cabinetState.value as? CabinetState.Success)?.cabinetId ?: return@launch
                medicineApi.updateCabinetMedicine(
                    cabinetId, medicineId,
                    UpdateCabinetMedicineRequest(quantity = newQuantity)
                )
                fetchCabinet()
            } catch (e: Exception) {
                // ignore
            } finally {
                _isActionLoading.value = false
            }
        }
    }

    fun deleteMedicine(medicineId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val cabinetId = (cabinetState.value as? CabinetState.Success)?.cabinetId ?: return@launch
                medicineApi.deleteCabinetMedicine(cabinetId, medicineId)
                fetchCabinet()
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    fun confirmOcrPrescription(
        profileId: Long,
        clinicName: String,
        doctorName: String,
        prescriptionDate: String,
        medicines: List<ParsedMedicine>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _isActionLoading.value = true
            try {
                val currentState = _cabinetState.value as? CabinetState.Success ?: run {
                    _isActionLoading.value = false
                    withContext(Dispatchers.Main) {
                        onError("Không thể lấy thông tin tủ thuốc hiện tại")
                    }
                    return@launch
                }
                var cabinetId = currentState.cabinetId
                // If cabinet doesn't exist yet, create one
                if (cabinetId == 0L) {
                    val fid = secureSessionManager.familyIdFlow.value ?: run {
                        _isActionLoading.value = false
                        withContext(Dispatchers.Main) {
                            onError("Chưa chọn gia đình")
                        }
                        return@launch
                    }
                    val familyIdLong = fid.toLongOrNull() ?: run {
                        _isActionLoading.value = false
                        withContext(Dispatchers.Main) {
                            onError("Ma gia dinh khong hop le")
                        }
                        return@launch
                    }
                    val createResponse = medicineApi.createCabinet(
                        CreateCabinetRequest(familyId = familyIdLong, name = "Tu thuoc gia dinh")
                    )
                    cabinetId = createResponse.body()?.data?.id ?: run {
                        _isActionLoading.value = false
                        withContext(Dispatchers.Main) {
                            onError("Không thể tạo tủ thuốc mới")
                        }
                        return@launch
                    }
                }

                // Add each medicine to the cabinet and create a medication schedule
                for (med in medicines) {
                    // 1. Add to cabinet
                    val qty = 20 // Default value matching RN
                    medicineApi.addMedicineToCabinet(
                        cabinetId,
                        CreateCabinetMedicineRequest(
                            medicineName = med.name,
                            quantity = qty,
                            unit = "viên",
                            expiryDate = java.time.LocalDate.now().plusMonths(6).toString()
                        )
                    )

                    // 2. Create medication schedule
                    val frequencyVal = med.frequency.toIntOrNull() ?: 2
                    val startDate = java.time.LocalDate.now().toString()
                    val endDate = java.time.LocalDate.now().plusDays(6).toString()
                    
                    val timeSlots = when (frequencyVal) {
                        1 -> listOf("08:00")
                        2 -> listOf("08:00", "20:00")
                        else -> listOf("08:00", "13:00", "20:00")
                    }

                    medicineApi.createMedicationSchedule(
                        profileId,
                        CreateMedicationScheduleRequest(
                            medicineName = med.name,
                            dosage = med.dosage.ifBlank { "1 viên" },
                            timesPerDay = frequencyVal,
                            timeSlots = timeSlots,
                            startDate = startDate,
                            endDate = endDate,
                            notes = med.note.ifBlank { "Sau ăn" }
                        )
                    )
                }

                // Refresh states
                fetchCabinet()
                fetchTodaySchedule(profileId)

                withContext(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                val errorMsg = e.localizedMessage ?: "Lỗi kết nối mạng"
                withContext(Dispatchers.Main) {
                    onError(errorMsg)
                }
            } finally {
                _isActionLoading.value = false
            }
        }
    }

    // ── Filter ────────────────────────────────────────────────────────────────
    fun onSearchQueryChanged(query: String) { _searchQuery.value = query }
    fun onFilterSelected(filter: String) { _selectedFilter.value = filter }

    fun filteredMedicines(): List<CabinetMedicineResponse> {
        val all = (cabinetState.value as? CabinetState.Success)?.medicines ?: return emptyList()
        val q = _searchQuery.value
        var result = if (q.isBlank()) all else all.filter { it.medicineName.contains(q, ignoreCase = true) }
        result = when (_selectedFilter.value) {
            "Sắp hết hạn" -> result.filter { it.isExpiring }
            "Hết hàng" -> result.filter { it.quantity <= 0 }
            "Hết hạn" -> result.filter { it.isExpired }
            else -> result
        }
        return result
    }

    // ── Daily Schedule ────────────────────────────────────────────────────────

    fun fetchTodaySchedule(profileId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            _scheduleState.value = ScheduleState.Loading
            try {
                val response = medicineApi.getTodayLogs(profileId)
                if (response.isSuccessful) {
                    val logs = response.body()?.data ?: emptyList()
                    if (logs.isEmpty()) {
                        _scheduleState.value = ScheduleState.Empty
                        return@launch
                    }
                    val sortedLogs = logs.sortedBy { parseScheduledLocalTime(it.scheduledTime) ?: LocalTime.MAX }
                    _scheduleState.value = ScheduleState.Success(
                        profileName = "Hôm nay",
                        sections = groupIntoSessions(sortedLogs),
                        takenCount = sortedLogs.count { it.status == "TAKEN" },
                        totalCount = sortedLogs.size
                    )
                } else {
                    _scheduleState.value = ScheduleState.Error("Không thể tải lịch thuốc")
                }
            } catch (e: Exception) {
                _scheduleState.value = ScheduleState.Error(e.localizedMessage ?: "Lỗi kết nối")
            }
        }
    }

    private fun groupIntoSessions(logs: List<MedicationLogResponse>): List<DoseSection> {
        val morning = mutableListOf<MedicationLogResponse>()
        val noon = mutableListOf<MedicationLogResponse>()
        val evening = mutableListOf<MedicationLogResponse>()
        val unscheduled = mutableListOf<MedicationLogResponse>()

        logs.forEach { log ->
            val time = parseScheduledLocalTime(log.scheduledTime)
            when {
                time == null -> unscheduled.add(log)
                time.hour < 12 -> morning.add(log)
                time.hour < 17 -> noon.add(log)
                else -> evening.add(log)
            }
        }
        return buildList {
            if (morning.isNotEmpty()) add(DoseSection("MORNING", "Buổi sáng", morning))
            if (noon.isNotEmpty()) add(DoseSection("NOON", "Buổi trưa", noon))
            if (evening.isNotEmpty()) add(DoseSection("EVENING", "Buổi tối", evening))
            if (unscheduled.isNotEmpty()) add(DoseSection("UNSCHEDULED", "Chua xac dinh", unscheduled))
        }
    }

    private fun parseScheduledLocalTime(value: String): LocalTime? {
        val raw = value.trim()
        if (raw.isEmpty()) return null

        runCatching {
            return Instant.parse(raw).atZone(ZoneId.systemDefault()).toLocalTime()
        }
        runCatching {
            return OffsetDateTime.parse(raw).toLocalTime()
        }
        runCatching {
            return LocalDateTime.parse(raw).toLocalTime()
        }
        runCatching {
            return LocalTime.parse(raw, DateTimeFormatter.ofPattern("HH:mm"))
        }

        val match = Regex("""\b([01]\d|2[0-3]):([0-5]\d)\b""").find(raw)
        return match?.value?.let { LocalTime.parse(it, DateTimeFormatter.ofPattern("HH:mm")) }
    }

    fun toggleDose(logId: Long, currentTaken: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            // Optimistic update
            val current = _scheduleState.value as? ScheduleState.Success ?: return@launch
            _scheduleState.value = current.copy(
                sections = current.sections.map { section ->
                    section.copy(items = section.items.map { item ->
                        if (item.id == logId) item.copy(status = if (!currentTaken) "TAKEN" else "PENDING") else item
                    })
                },
                takenCount = if (!currentTaken) current.takenCount + 1 else current.takenCount - 1
            )
            try {
                medicineApi.checkInDose(logId, CheckInRequest(
                    status = if (!currentTaken) "TAKEN" else "PENDING"
                ))
            } catch (e: Exception) {
                // Revert on failure — re-fetch with the correct active profile id
                val profileId = secureSessionManager.getActiveProfileId() ?: return@launch
                fetchTodaySchedule(profileId)
            }
        }
    }

    // ── Long-term schedule (for AddMedicineScheduleScreen form data) ──────────

    fun fetchSchedules(profileId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = medicineApi.getMedicationSchedules(profileId)
                if (response.isSuccessful) {
                    _schedules.value = response.body()?.data?.content ?: emptyList()
                }
            } catch (e: Exception) {
                // ignore — form data fallback is empty list
            }
        }
    }

    fun createSchedule(
        profileId: Long,
        medicineName: String,
        dosage: String,
        timesPerDay: Int,
        startDate: String,
        endDate: String,
        notes: String?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _isActionLoading.value = true
            try {
                val timeSlots = when (timesPerDay) {
                    1 -> listOf("08:00")
                    2 -> listOf("08:00", "20:00")
                    else -> listOf("08:00", "13:00", "20:00")
                }
                val response = medicineApi.createMedicationSchedule(
                    profileId,
                    CreateMedicationScheduleRequest(
                        medicineName = medicineName,
                        dosage = dosage,
                        timesPerDay = timesPerDay,
                        timeSlots = timeSlots,
                        startDate = startDate,
                        endDate = endDate,
                        notes = notes?.ifBlank { null }
                    )
                )
                if (response.isSuccessful) {
                    fetchTodaySchedule(profileId)
                    fetchSchedules(profileId)
                    withContext(Dispatchers.Main) {
                        onSuccess()
                    }
                } else {
                    val errorMsg = response.body()?.message ?: "Không thể tạo lịch thuốc"
                    withContext(Dispatchers.Main) {
                        onError(errorMsg)
                    }
                }
            } catch (e: Exception) {
                val errorMsg = e.localizedMessage ?: "Lỗi kết nối"
                withContext(Dispatchers.Main) {
                    onError(errorMsg)
                }
            } finally {
                _isActionLoading.value = false
            }
        }
    }

    fun deleteSchedule(scheduleId: Long, profileId: Long? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                medicineApi.deleteMedicationSchedule(scheduleId)
                val resolvedProfileId = profileId
                    ?: secureSessionManager.getActiveProfileId()
                    ?: secureSessionManager.getProfileId()
                if (resolvedProfileId != null) {
                    fetchTodaySchedule(resolvedProfileId)
                    fetchSchedules(resolvedProfileId)
                }
            } catch (e: Exception) {
                // ignore
            }
        }
    }
}

class MedicineViewModelFactory(
    private val medicineApi: MedicineApi,
    private val secureSessionManager: SecureSessionManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MedicineViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MedicineViewModel(medicineApi, secureSessionManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
