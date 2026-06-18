package com.carenest.backend.features.ai;

import com.carenest.backend.core.exception.UpstreamServiceException;
import com.carenest.backend.features.aichat.dto.response.ChatResponse;
import com.carenest.backend.features.ocr.dto.response.StructuredOcrMedicationPayloadDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AiGatewayClient {

    private final ObjectMapper objectMapper;

    @Value("${app.ai.service-base-url:http://localhost:8000}")
    private String serviceBaseUrl;

    @Value("${app.ai.timeout-ms:15000}")
    private int timeoutMs;

    public ChatResponse chat(String message, Long conversationId) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("message", message);
        if (conversationId != null) {
            payload.put("conversationId", conversationId);
        }
        return post("/ai/chat", payload, ChatResponse.class);
    }

    public StructuredOcrMedicationPayloadDto parseMedicine(String rawText) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("raw_text", rawText);
        return post("/ai/ocr/medicine/parse", payload, StructuredOcrMedicationPayloadDto.class);
    }

    private <T> T post(String path, Object payload, Class<T> responseType) {
        try {
            Duration timeout = Duration.ofMillis(Math.max(timeoutMs, 1000));
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(timeout)
                    .build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(normalizedBaseUrl() + path))
                    .timeout(timeout)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new UpstreamServiceException(extractError(response.body(), response.statusCode()));
            }
            return objectMapper.readValue(response.body(), responseType);
        } catch (UpstreamServiceException ex) {
            throw ex;
        } catch (IOException ex) {
            throw new UpstreamServiceException("Không thể đọc phản hồi từ dịch vụ AI.", ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new UpstreamServiceException("Yêu cầu AI bị gián đoạn.", ex);
        } catch (IllegalArgumentException ex) {
            throw new UpstreamServiceException("Cấu hình AI service không hợp lệ.", ex);
        }
    }

    private String normalizedBaseUrl() {
        return serviceBaseUrl.endsWith("/")
                ? serviceBaseUrl.substring(0, serviceBaseUrl.length() - 1)
                : serviceBaseUrl;
    }

    private String extractError(String body, int statusCode) {
        if (body == null || body.isBlank()) {
            return "Dịch vụ AI trả lỗi " + statusCode + ".";
        }
        try {
            JsonNode root = objectMapper.readTree(body);
            JsonNode detail = root.get("detail");
            if (detail != null && !detail.asText().isBlank()) {
                return detail.asText();
            }
            JsonNode message = root.get("message");
            if (message != null && !message.asText().isBlank()) {
                return message.asText();
            }
        } catch (Exception ignored) {
            // Fall through to compact raw body below.
        }
        return body.length() > 300 ? body.substring(0, 300) : body;
    }
}
