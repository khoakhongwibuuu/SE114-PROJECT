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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/ocr")
@RequiredArgsConstructor
public class OcrController {

    private final OcrService ocrService;

    @Operation(summary = "Phân tích đơn thuốc bằng AI", description = "Nhận dạng văn bản thô từ OCR và chuyển đổi thành cấu trúc JSON")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Phân tích thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Văn bản thô bị trống hoặc không hợp lệ"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Không có quyền truy cập"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Không thể xử lý văn bản (AI không nhận diện được đơn thuốc)")
    })
    @PostMapping("/parse")
    public ResponseEntity<com.carenest.backend.common.dto.ApiResponse<List<ParsedMedicationDto>>> parseOcrText(@RequestBody @Valid ParseOcrRequest request) {
        // [QUY TẮC 3]: Stateless & Safe - Chỉ nhận diện text ra JSON.
        List<ParsedMedicationDto> medications = ocrService.parseRawTextToMedications(request);
        return ResponseEntity.ok(ApiResponse.success("Phân tích đơn thuốc thành công", medications));
    }
}
