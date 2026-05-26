package com.carenest.backend.features.ocr.service;

import com.carenest.backend.features.ocr.dto.request.ParseOcrRequest;
import com.carenest.backend.features.ocr.dto.response.ParsedMedicationDto;

import java.util.List;

public interface OcrService {
    /**
     * Báº¯n rawText lÃªn Gemini kÃ¨m Prompt y táº¿ Ä‘á»ƒ Ã©p LLM tráº£ vá» cáº¥u trÃºc JSON.
     * HÃ m nÃ y hoÃ n toÃ n Stateless vÃ  an toÃ n. KhÃ´ng lÆ°u Data.
     */
    List<ParsedMedicationDto> parseRawTextToMedications(ParseOcrRequest request);
}
