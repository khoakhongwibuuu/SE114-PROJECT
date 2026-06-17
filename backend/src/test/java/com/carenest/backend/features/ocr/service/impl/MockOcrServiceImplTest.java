package com.carenest.backend.features.ocr.service.impl;

import com.carenest.backend.core.exception.BadRequestException;
import com.carenest.backend.features.ocr.dto.request.ParseOcrRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class MockOcrServiceImplTest {

    @Test
    void parseRawTextToMedications_doesNotReturnSampleMedicationInMvp() {
        MockOcrServiceImpl service = new MockOcrServiceImpl();
        ParseOcrRequest request = new ParseOcrRequest();
        request.setRawText("Paracetamol 500mg");

        assertThrows(BadRequestException.class, () -> service.parseRawTextToMedications(request));
    }
}
