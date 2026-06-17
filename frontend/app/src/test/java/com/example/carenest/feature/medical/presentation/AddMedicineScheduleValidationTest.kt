package com.example.carenest.feature.medical.presentation

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AddMedicineScheduleValidationTest {
    private val today: LocalDate = LocalDate.of(2026, 6, 14)

    @Test
    fun `valid input returns no error`() {
        val error = validateMedicationScheduleInput(
            selectedMemberId = "12",
            selectedMedicineId = 34L,
            dosage = "1 vien sau an",
            frequencyValue = 2,
            startDate = today,
            endDate = today.plusDays(6),
            today = today,
        )

        assertNull(error)
    }

    @Test
    fun `blank dosage is rejected`() {
        val error = validateMedicationScheduleInput(
            selectedMemberId = "12",
            selectedMedicineId = 34L,
            dosage = " ",
            frequencyValue = 2,
            startDate = today,
            endDate = today.plusDays(6),
            today = today,
        )

        assertEquals("Vui lòng nhập liều dùng.", error)
    }

    @Test
    fun `frequency outside supported slots is rejected`() {
        val error = validateMedicationScheduleInput(
            selectedMemberId = "12",
            selectedMedicineId = 34L,
            dosage = "1 vien sau an",
            frequencyValue = 4,
            startDate = today,
            endDate = today.plusDays(6),
            today = today,
        )

        assertEquals("Số lần uống mỗi ngày chỉ hỗ trợ từ 1 đến 3 lần.", error)
    }

    @Test
    fun `past start date is rejected`() {
        val error = validateMedicationScheduleInput(
            selectedMemberId = "12",
            selectedMedicineId = 34L,
            dosage = "1 vien sau an",
            frequencyValue = 1,
            startDate = today.minusDays(1),
            endDate = today.plusDays(6),
            today = today,
        )

        assertEquals("Ngày bắt đầu không được nằm trong quá khứ.", error)
    }

    @Test
    fun `end date before start date is rejected`() {
        val error = validateMedicationScheduleInput(
            selectedMemberId = "12",
            selectedMedicineId = 34L,
            dosage = "1 vien sau an",
            frequencyValue = 1,
            startDate = today.plusDays(2),
            endDate = today.plusDays(1),
            today = today,
        )

        assertEquals("Ngày kết thúc không được trước ngày bắt đầu.", error)
    }
}
