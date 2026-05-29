package com.example.carenest.feature.medical.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carenest.feature.medical.domain.model.Medicine
import com.example.carenest.feature.medical.domain.model.MedicineStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MedicineViewModel : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow("Tất cả")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    private val _medicines = MutableStateFlow<List<Medicine>>(emptyList())
    val medicines: StateFlow<List<Medicine>> = _medicines.asStateFlow()

    private val allMedicines = listOf(
        Medicine(
            id = "1",
            name = "Panadol Extra",
            quantity = 12,
            unit = "viên",
            expiryDate = "15/01/2024",
            status = MedicineStatus.EXPIRED
        ),
        Medicine(
            id = "2",
            name = "Amoxicillin",
            quantity = 8,
            unit = "gói",
            expiryDate = "Còn 12 ngày",
            status = MedicineStatus.EXPIRING_SOON
        ),
        Medicine(
            id = "3",
            name = "Berberin",
            quantity = 50,
            unit = "viên",
            expiryDate = "HSD: 12/2026",
            status = MedicineStatus.NORMAL
        ),
        Medicine(
            id = "4",
            name = "Efferalgan 500mg",
            quantity = 0,
            unit = "viên",
            expiryDate = "Dùng lần cuối: 2 ngày trước",
            status = MedicineStatus.OUT_OF_STOCK
        ),
        Medicine(
            id = "5",
            name = "Siro Ho Prospan",
            quantity = 1,
            unit = "chai (100ml)",
            expiryDate = "HSD: 09/2025",
            status = MedicineStatus.NORMAL
        )
    )

    init {
        _medicines.value = allMedicines
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        filterMedicines()
    }

    fun onFilterSelected(filter: String) {
        _selectedFilter.value = filter
        filterMedicines()
    }

    private fun filterMedicines() {
        var filteredList = allMedicines

        // Apply Search
        val query = _searchQuery.value
        if (query.isNotBlank()) {
            filteredList = filteredList.filter {
                it.name.contains(query, ignoreCase = true)
            }
        }

        // Apply Category Filter
        filteredList = when (_selectedFilter.value) {
            "Sắp hết hạn" -> filteredList.filter { it.status == MedicineStatus.EXPIRING_SOON }
            "Hết hàng" -> filteredList.filter { it.status == MedicineStatus.OUT_OF_STOCK }
            else -> filteredList // "Tất cả"
        }

        _medicines.value = filteredList
    }
}
