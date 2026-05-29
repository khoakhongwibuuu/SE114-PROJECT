package com.carenest.backend.features.ocr.service.impl;

import com.carenest.backend.features.medication.enums.MedicationFrequency;
import com.carenest.backend.features.ocr.dto.request.ParseOcrRequest;
import com.carenest.backend.features.ocr.dto.response.ParsedMedicationDto;
import com.carenest.backend.features.ocr.service.OcrService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MockOcrServiceImpl implements OcrService {

    @Override
    public List<ParsedMedicationDto> parseRawTextToMedications(ParseOcrRequest request) {
        // [QUY Táº®C 1]: Profile DEV tráº£ vá» dá»¯ liá»‡u giáº£ láº­p tÄ©nh.
        // KhÃ´ng tá»‘n token API, test Ä‘á»™c láº­p cho Client.
        return List.of(
            ParsedMedicationDto.builder()
                .medicineName("Paracetamol (Mock)")
                .totalQuantity(20)
                .unit("ViÃªn")
                .dosage("SÃ¡ng 1 viÃªn, Tá»‘i 1 viÃªn")
                .frequency(MedicationFrequency.DAILY)
                .durationDays(10)
                .notes("Uá»‘ng sau Äƒn")
                .build(),
            ParsedMedicationDto.builder()
                .medicineName("Amoxicillin (Mock)")
                .totalQuantity(14)
                .unit("ViÃªn")
                .dosage("SÃ¡ng 1 viÃªn, Tá»‘i 1 viÃªn")
                .frequency(MedicationFrequency.DAILY)
                .durationDays(7)
                .notes("KhÃ¡ng sinh, uá»‘ng Ä‘á»§ liá»u")
                .build()
        );
    }
}
