package com.carenest.backend.features.ocr.service.impl;

import com.carenest.backend.features.ai.AiGatewayClient;
import com.carenest.backend.features.medication.enums.MedicationFrequency;
import com.carenest.backend.features.ocr.dto.request.ParseOcrRequest;
import com.carenest.backend.features.ocr.dto.response.ParsedMedicationDto;
import com.carenest.backend.features.ocr.dto.response.StructuredOcrMedicationItemDto;
import com.carenest.backend.features.ocr.dto.response.StructuredOcrMedicationPayloadDto;
import com.carenest.backend.features.ocr.service.OcrService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ProviderOcrServiceImpl implements OcrService {

    private final AiGatewayClient aiGatewayClient;

    @Override
    public List<ParsedMedicationDto> parseRawTextToMedications(ParseOcrRequest request) {
        StructuredOcrMedicationPayloadDto payload = aiGatewayClient.parseMedicine(request.getRawText());
        return payload.getMedications().stream()
                .map(this::toParsedMedication)
                .toList();
    }

    private ParsedMedicationDto toParsedMedication(StructuredOcrMedicationItemDto item) {
        return ParsedMedicationDto.builder()
                .medicineName(joinName(item.getName(), item.getStrength()))
                .unit(translateForm(item.getForm()))
                .dosage(item.getDoseInstruction())
                .totalQuantity(item.getTotalQuantity())
                .frequency(toFrequency(item.getFrequency()))
                .durationDays(item.getDurationDays())
                .notes(buildNotes(item))
                .build();
    }

    private String translateForm(String form) {
        if (form == null) return "Viên";
        return switch (form.toLowerCase(Locale.ROOT)) {
            case "tablet" -> "Viên";
            case "capsule" -> "Viên nang";
            case "syrup" -> "Siro";
            case "drop" -> "Giọt";
            default -> "Viên";
        };
    }

    private String joinName(String name, String strength) {
        if (strength == null || strength.isBlank()) {
            return name;
        }
        return name + " " + strength;
    }

    private MedicationFrequency toFrequency(String frequency) {
        if (frequency == null || frequency.isBlank()) {
            return MedicationFrequency.CUSTOM;
        }
        String normalized = frequency.trim().toUpperCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');
        return switch (normalized) {
            case "ONCE_DAILY", "ONCE_A_DAY", "1_TIME_DAILY", "1X_DAILY" -> MedicationFrequency.ONCE_DAILY;
            case "TWICE_DAILY", "TWO_TIMES_DAILY", "2_TIMES_DAILY", "2X_DAILY" -> MedicationFrequency.TWICE_DAILY;
            case "THREE_TIMES_DAILY", "3_TIMES_DAILY", "3X_DAILY" -> MedicationFrequency.THREE_TIMES_DAILY;
            case "DAILY", "EVERY_DAY" -> MedicationFrequency.DAILY;
            case "WEEKLY" -> MedicationFrequency.WEEKLY;
            case "EVERY_OTHER_DAY" -> MedicationFrequency.EVERY_OTHER_DAY;
            case "AS_NEEDED", "PRN" -> MedicationFrequency.AS_NEEDED;
            default -> MedicationFrequency.CUSTOM;
        };
    }

    private String buildNotes(StructuredOcrMedicationItemDto item) {
        StringBuilder notes = new StringBuilder();
        if (item.getRoute() != null && !item.getRoute().isBlank() && !"unknown".equalsIgnoreCase(item.getRoute())) {
            notes.append("Đường dùng: ").append(item.getRoute()).append(".");
        }
        if (item.getConfidence() != null) {
            if (!notes.isEmpty()) {
                notes.append(" ");
            }
            notes.append("Độ tin cậy OCR: ").append(Math.round(item.getConfidence() * 100)).append("%.");
        }
        if (item.getWarnings() != null && !item.getWarnings().isEmpty()) {
            if (!notes.isEmpty()) {
                notes.append(" ");
            }
            notes.append("Cần kiểm tra: ").append(String.join("; ", item.getWarnings()));
        }
        return notes.isEmpty() ? null : notes.toString();
    }
}
