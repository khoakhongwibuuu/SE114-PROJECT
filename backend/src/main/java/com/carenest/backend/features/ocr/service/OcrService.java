package com.carenest.backend.features.ocr.service;

import com.carenest.backend.features.ocr.dto.request.ParseOcrRequest;
import com.carenest.backend.features.ocr.dto.response.ParsedMedicationDto;

import java.util.List;

public interface OcrService {
    /**
     * Bắn rawText lên Gemini kèm prompt y tế để ép LLM trả về cấu trúc JSON.
     * Hàm này stateless và không lưu dữ liệu.
     */
    List<ParsedMedicationDto> parseRawTextToMedications(ParseOcrRequest request);
}
