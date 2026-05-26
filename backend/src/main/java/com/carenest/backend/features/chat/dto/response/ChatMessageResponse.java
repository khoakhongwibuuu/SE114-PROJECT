package com.carenest.backend.features.chat.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

/**
 * JSON Contract â€” pháº£i khá»›p CHÃNH XÃC vá»›i IMessage cá»§a react-native-gifted-chat.
 * DÃ¹ng @JsonProperty("_id") vÃ¬ Gifted Chat yÃªu cáº§u "_id" chá»© khÃ´ng pháº£i "id".
 */
@Data
@Builder
public class ChatMessageResponse {

    @JsonProperty("_id")
    private Long id;

    private String text;          // "text" khÃ´ng pháº£i "content" â€” Gifted Chat yÃªu cáº§u

    private Instant createdAt;

    private ChatUserDto user;

    @Data
    @Builder
    public static class ChatUserDto {

        @JsonProperty("_id")
        private Long id;

        private String name;

        private String avatar;    // null Ä‘Æ°á»£c cháº¥p nháº­n â€” Gifted Chat xá»­ lÃ½ gracefully
    }
}
