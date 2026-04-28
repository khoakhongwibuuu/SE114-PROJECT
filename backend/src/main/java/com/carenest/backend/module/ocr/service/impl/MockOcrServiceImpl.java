package com.carenest.backend.module.ocr.service.impl;

import com.carenest.backend.module.medication.enums.MedicationFrequency;
import com.carenest.backend.module.ocr.dto.request.ParseOcrRequest;
import com.carenest.backend.module.ocr.dto.response.ParsedMedicationDto;
import com.carenest.backend.module.ocr.service.OcrService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Profile("dev")
public class MockOcrServiceImpl implements OcrService {

    @Override
    public List<ParsedMedicationDto> parseRawTextToMedications(ParseOcrRequest request) {
        // [QUY TẮC 1]: Profile DEV trả về dữ liệu giả lập tĩnh. 
        // Không tốn token API, test độc lập cho Client.
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
