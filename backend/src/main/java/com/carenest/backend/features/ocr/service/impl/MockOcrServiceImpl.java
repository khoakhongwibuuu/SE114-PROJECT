package com.carenest.backend.features.ocr.service.impl;

import com.carenest.backend.core.exception.BadRequestException;
import com.carenest.backend.features.ocr.dto.request.ParseOcrRequest;
import com.carenest.backend.features.ocr.dto.response.ParsedMedicationDto;
import com.carenest.backend.features.ocr.service.OcrService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MockOcrServiceImpl implements OcrService {

    @Override
    public List<ParsedMedicationDto> parseRawTextToMedications(ParseOcrRequest request) {
        throw new BadRequestException("OCR chua co provider that trong MVP va se duoc bat lai o phase cuoi.");
    }
}
