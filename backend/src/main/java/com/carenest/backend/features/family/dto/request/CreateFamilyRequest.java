package com.carenest.backend.features.family.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateFamilyRequest {

    @NotBlank(message = "TÃªn gia Ä‘Ã¬nh khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng")
    @Size(min = 2, max = 100, message = "TÃªn gia Ä‘Ã¬nh pháº£i tá»« 2 Ä‘áº¿n 100 kÃ½ tá»±")
    private String name;
}
