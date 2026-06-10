package com.carenest.backend.features.booking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActiveConsultationDto {
    private String code;
    private Long existingBookingId;
    private String status;
}
