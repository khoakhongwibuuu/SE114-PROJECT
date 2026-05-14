package com.carenest.backend.module.chat.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

/**
 * JSON Contract — phải khớp CHÍNH XÁC với IMessage của react-native-gifted-chat.
 * Dùng @JsonProperty("_id") vì Gifted Chat yêu cầu "_id" chứ không phải "id".
 */
@Data
@Builder
public class ChatMessageResponse {

    @JsonProperty("_id")
    private Long id;

    private String text;          // "text" không phải "content" — Gifted Chat yêu cầu

    private Instant createdAt;

    private ChatUserDto user;

    @Data
    @Builder
    public static class ChatUserDto {

        @JsonProperty("_id")
        private Long id;

        private String name;

        private String avatar;    // null được chấp nhận — Gifted Chat xử lý gracefully
    }
}
