package com.carenest.backend.features.booking.service;

import com.carenest.backend.features.booking.dto.request.SendConsultationMessageRequest;
import com.carenest.backend.features.booking.dto.response.ConsultationMessageResponse;

import java.util.List;

public interface ConsultationMessageService {
    List<ConsultationMessageResponse> getMessages(Long threadId);
    ConsultationMessageResponse sendMessage(Long threadId, SendConsultationMessageRequest request);
}
