package com.carenest.backend.features.ocr.controller;

import com.carenest.backend.core.api.ApiResponse;
import com.carenest.backend.features.ocr.dto.request.ParseOcrRequest;
import com.carenest.backend.features.ocr.dto.response.ParsedMedicationDto;
import com.carenest.backend.features.ocr.service.OcrService;
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
@org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('USER', 'DOCTOR', 'ADMIN')")
public class OcrController {

    private final OcrService ocrService;

    @Operation(summary = "PhÃ¢n tÃ­ch Ä‘Æ¡n thuá»‘c báº±ng AI", description = "Nháº­n dáº¡ng vÄƒn báº£n thÃ´ tá»« OCR vÃ  chuyá»ƒn Ä‘á»•i thÃ nh cáº¥u trÃºc JSON")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "PhÃ¢n tÃ­ch thÃ nh cÃ´ng"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "VÄƒn báº£n thÃ´ bá»‹ trá»‘ng hoáº·c khÃ´ng há»£p lá»‡"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "KhÃ´ng cÃ³ quyá»n truy cáº­p"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "KhÃ´ng thá»ƒ xá»­ lÃ½ vÄƒn báº£n (AI khÃ´ng nháº­n diá»‡n Ä‘Æ°á»£c Ä‘Æ¡n thuá»‘c)")
    })
    @PostMapping("/parse")
    public ResponseEntity<com.carenest.backend.core.api.ApiResponse<List<ParsedMedicationDto>>> parseOcrText(@RequestBody @Valid ParseOcrRequest request) {
        // [QUY Táº®C 3]: Stateless & Safe - Chá»‰ nháº­n diá»‡n text ra JSON.
        List<ParsedMedicationDto> medications = ocrService.parseRawTextToMedications(request);
        return ResponseEntity.ok(ApiResponse.success("PhÃ¢n tÃ­ch Ä‘Æ¡n thuá»‘c thÃ nh cÃ´ng", medications));
    }
}
