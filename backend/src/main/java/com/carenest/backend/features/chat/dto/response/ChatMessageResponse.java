package com.carenest.backend.features.chat.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ChatMessageResponse {

    @JsonProperty("_id")
    private Long id;

    private String text;

    private Instant createdAt;

    private ChatUserDto user;

    @Data
    @Builder
    public static class ChatUserDto {

        @JsonProperty("_id")
        private Long id;

        private String name;

        private String avatar;

        private String role;
    }
}
