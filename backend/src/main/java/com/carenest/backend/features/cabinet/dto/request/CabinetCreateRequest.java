package com.carenest.backend.features.cabinet.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CabinetCreateRequest {

    @NotNull(message = "Vui lÃ²ng chá»n gia Ä‘Ã¬nh")
    private Long familyId;

    @Size(max = 100, message = "TÃªn tá»§ thuá»‘c khÃ´ng Ä‘Æ°á»£c vÆ°á»£t quÃ¡ 100 kÃ½ tá»±")
    private String name;
}
