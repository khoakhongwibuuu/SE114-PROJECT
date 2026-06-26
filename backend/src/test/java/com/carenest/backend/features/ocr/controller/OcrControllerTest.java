package com.carenest.backend.features.ocr.controller;

import com.carenest.backend.core.exception.BadRequestException;
import com.carenest.backend.features.ocr.dto.request.ParseOcrRequest;
import com.carenest.backend.features.ocr.service.OcrService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class OcrControllerTest {

    @Mock
    private OcrService ocrService;

    @InjectMocks
    private OcrController ocrController;

    @Test
    void parseOcrText_returnsMockDataWhenOcrIsDisabledByDefault() {
        ParseOcrRequest request = new ParseOcrRequest();
        request.setRawText("Paracetamol 500mg");

        var response = ocrController.parseOcrText(request);

        org.junit.jupiter.api.Assertions.assertEquals(200, response.getStatusCodeValue());
        verifyNoInteractions(ocrService);
    }
}
