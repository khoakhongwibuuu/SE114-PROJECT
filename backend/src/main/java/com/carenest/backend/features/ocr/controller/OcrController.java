package com.carenest.backend.features.ocr.controller;

import com.carenest.backend.core.api.ApiResponse;
import com.carenest.backend.core.exception.BadRequestException;
import com.carenest.backend.features.ocr.dto.request.ParseOcrRequest;
import com.carenest.backend.features.ocr.dto.response.ParsedMedicationDto;
import com.carenest.backend.features.ocr.service.OcrService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/ocr")
@RequiredArgsConstructor
@org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('USER', 'DOCTOR', 'ADMIN')")
public class OcrController {

    private final OcrService ocrService;

    @Value("${app.features.ocr-enabled:false}")
    private boolean ocrEnabled;

    @Operation(
            summary = "Phân tích đơn thuốc bằng AI",
            description = "Nhận dạng văn bản thô từ OCR thuốc và chuyển đổi thành cấu trúc thuốc để người dùng xác nhận trước khi lưu"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Phân tích thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Văn bản thô bị trống, không hợp lệ hoặc OCR đang tắt"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Không có quyền truy cập"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Không thể xử lý văn bản")
    })
    @PostMapping("/parse")
    public ResponseEntity<ApiResponse<List<ParsedMedicationDto>>> parseOcrText(
            @RequestBody @Valid ParseOcrRequest request) {
        if (!ocrEnabled) {
            // Return mock data for testing
            List<ParsedMedicationDto> mocks = List.of(
                    ParsedMedicationDto.builder()
                            .medicineName("Paracetamol 500mg")
                            .totalQuantity(10)
                            .unit("viên")
                            .dosage("Uống 1 viên mỗi lần")
                            .notes("ngày 2 lần sau ăn")
                            .build(),
                    ParsedMedicationDto.builder()
                            .medicineName("Amoxicillin 500mg")
                            .totalQuantity(21)
                            .unit("viên")
                            .dosage("Uống 1 viên mỗi lần")
                            .notes("ngày 3 lần")
                            .build(),
                    ParsedMedicationDto.builder()
                            .medicineName("Decolgen")
                            .totalQuantity(4)
                            .unit("viên")
                            .dosage("Uống khi nhức đầu")
                            .notes("")
                            .build()
            );
            return ResponseEntity.ok(ApiResponse.success("Phân tích thành công (Dữ liệu giả lập)", mocks));
        }
        
        List<ParsedMedicationDto> medications = ocrService.parseRawTextToMedications(request);
        return ResponseEntity.ok(ApiResponse.success("Phân tích đơn thuốc thành công", medications));
    }
}
