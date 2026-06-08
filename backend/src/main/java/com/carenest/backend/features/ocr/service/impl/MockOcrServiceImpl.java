package com.carenest.backend.features.ocr.service.impl;

import com.carenest.backend.features.medication.enums.MedicationFrequency;
import com.carenest.backend.features.ocr.dto.request.ParseOcrRequest;
import com.carenest.backend.features.ocr.dto.response.ParsedMedicationDto;
import com.carenest.backend.features.ocr.service.OcrService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MockOcrServiceImpl implements OcrService {

    @Override
    public List<ParsedMedicationDto> parseRawTextToMedications(ParseOcrRequest request) {
        // [QUY TẮC 1]: Profile DEV trả về dữ liệu giả lập tĩnh.
        // Không tốn token API, test độc lập cho client.
        return List.of(
                ParsedMedicationDto.builder()
                        .medicineName("Paracetamol (Mock)")
                        .totalQuantity(20)
                        .unit("Viên")
                        .dosage("Sáng 1 viên, Tối 1 viên")
                        .frequency(MedicationFrequency.DAILY)
                        .durationDays(10)
                        .notes("Uống sau ăn")
                        .build(),
                ParsedMedicationDto.builder()
                        .medicineName("Amoxicillin (Mock)")
                        .totalQuantity(14)
                        .unit("Viên")
                        .dosage("Sáng 1 viên, Tối 1 viên")
                        .frequency(MedicationFrequency.DAILY)
                        .durationDays(7)
                        .notes("Kháng sinh, uống đủ liều")
                        .build()
        );
    }
}
