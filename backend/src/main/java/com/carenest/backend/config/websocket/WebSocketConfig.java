package com.carenest.backend.config.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Cáº¥u hÃ¬nh WebSocket + STOMP Broker cho module Family Chat.
 *
 * Luá»“ng tin nháº¯n:
 *   Client â†’ /app/chat.sendMessage â†’ ChatController.sendMessage()
 *   ChatController â†’ /topic/family/{id} â†’ Táº¥t cáº£ Client Ä‘ang subscribe
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtChannelInterceptor jwtChannelInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Server sáº½ broadcast Ä‘áº¿n cÃ¡c kÃªnh cÃ³ prefix /topic
        registry.enableSimpleBroker("/topic");

        // Client gá»­i lÃªn Ä‘á»‹a chá»‰ /app/...
        registry.setApplicationDestinationPrefixes("/app");

        // Prefix cho tin nháº¯n tá»›i user cá»¥ thá»ƒ (dá»± phÃ²ng)
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint handshake â€” React Native dÃ¹ng native WebSocket (ws://)
        // withSockJS() lÃ  fallback cho browser, Ä‘á»ƒ á»Ÿ Ä‘Ã¢y khÃ´ng áº£nh hÆ°á»Ÿng React Native
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");

        // Endpoint riÃªng cho SockJS (browser/testing tools)
        registry.addEndpoint("/ws-sockjs")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // ÄÄƒng kÃ½ JWT interceptor vÃ o Ä‘Ã¢y â€” Ä‘Ã¢y lÃ  "cá»­a" duy nháº¥t Ä‘á»ƒ vÃ o há»‡ thá»‘ng chat
        registration.interceptors(jwtChannelInterceptor);
    }
}
