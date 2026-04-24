package com.carenest.backend.module.ocr.controller;

import com.carenest.backend.common.dto.ApiResponse;
import com.carenest.backend.module.ocr.dto.request.ParseOcrRequest;
import com.carenest.backend.module.ocr.dto.response.ParsedMedicationDto;
import com.carenest.backend.module.ocr.service.OcrService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ocr")
@RequiredArgsConstructor
public class OcrController {

    private final OcrService ocrService;

    @PostMapping("/parse")
    public ResponseEntity<ApiResponse<List<ParsedMedicationDto>>> parseOcrText(@RequestBody @Valid ParseOcrRequest request) {
        // [QUY TẮC 3]: Stateless & Safe - Chỉ nhận diện text ra JSON.
        List<ParsedMedicationDto> medications = ocrService.parseRawTextToMedications(request);
        return ResponseEntity.ok(ApiResponse.success("Phân tích đơn thuốc thành công", medications));
    }
}
