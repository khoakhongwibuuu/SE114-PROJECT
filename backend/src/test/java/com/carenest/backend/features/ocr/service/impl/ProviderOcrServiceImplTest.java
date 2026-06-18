package com.carenest.backend.features.ocr.service.impl;

import com.carenest.backend.features.ai.AiGatewayClient;
import com.carenest.backend.features.medication.enums.MedicationFrequency;
import com.carenest.backend.features.ocr.dto.request.ParseOcrRequest;
import com.carenest.backend.features.ocr.dto.response.ParsedMedicationDto;
import com.carenest.backend.features.ocr.dto.response.StructuredOcrMedicationItemDto;
import com.carenest.backend.features.ocr.dto.response.StructuredOcrMedicationPayloadDto;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProviderOcrServiceImplTest {

    @Test
    void parseRawTextToMedications_mapsMedicineOnlyPayload() {
        AiGatewayClient aiGatewayClient = mock(AiGatewayClient.class);
        ParseOcrRequest request = new ParseOcrRequest();
        request.setRawText("Paracetamol 500mg, ngay 2 lan");
        StructuredOcrMedicationItemDto item = StructuredOcrMedicationItemDto.builder()
                .name("Paracetamol")
                .strength("500mg")
                .form("tablet")
                .doseInstruction("1 viên/lần")
                .frequency("twice_daily")
                .durationDays(3)
                .route("oral")
                .confidence(0.9)
                .warnings(List.of("Kiểm tra lại liều theo cân nặng"))
                .build();
        when(aiGatewayClient.parseMedicine(request.getRawText())).thenReturn(StructuredOcrMedicationPayloadDto.builder()
                .schemaVersion("ocr.medication.v1")
                .documentType("prescription")
                .confidence(0.9)
                .medications(List.of(item))
                .warnings(List.of())
                .rawText(request.getRawText())
                .build());

        ProviderOcrServiceImpl service = new ProviderOcrServiceImpl(aiGatewayClient);
        List<ParsedMedicationDto> result = service.parseRawTextToMedications(request);

        assertEquals(1, result.size());
        assertEquals("Paracetamol 500mg", result.get(0).getMedicineName());
        assertEquals(MedicationFrequency.TWICE_DAILY, result.get(0).getFrequency());
        assertEquals(3, result.get(0).getDurationDays());
    }
}
