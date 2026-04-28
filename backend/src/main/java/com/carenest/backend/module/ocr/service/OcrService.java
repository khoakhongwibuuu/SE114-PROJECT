package com.carenest.backend.module.ocr.service;

import com.carenest.backend.module.ocr.dto.request.ParseOcrRequest;
import com.carenest.backend.module.ocr.dto.response.ParsedMedicationDto;

import java.util.List;

public interface OcrService {
    /**
     * Bắn rawText lên Gemini kèm Prompt y tế để ép LLM trả về cấu trúc JSON.
     * Hàm này hoàn toàn Stateless và an toàn. Không lưu Data.
     */
    List<ParsedMedicationDto> parseRawTextToMedications(ParseOcrRequest request);
}
