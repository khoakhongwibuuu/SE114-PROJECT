package com.carenest.backend.config.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Cấu hình WebSocket + STOMP Broker cho module Family Chat.
 *
 * Luồng tin nhắn:
 *   Client → /app/chat.sendMessage → ChatController.sendMessage()
 *   ChatController → /topic/family/{id} → Tất cả Client đang subscribe
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtChannelInterceptor jwtChannelInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Server sẽ broadcast đến các kênh có prefix /topic
        registry.enableSimpleBroker("/topic");

        // Client gửi lên địa chỉ /app/...
        registry.setApplicationDestinationPrefixes("/app");

        // Prefix cho tin nhắn tới user cụ thể (dự phòng)
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint handshake — React Native dùng native WebSocket (ws://)
        // withSockJS() là fallback cho browser, để ở đây không ảnh hưởng React Native
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");

        // Endpoint riêng cho SockJS (browser/testing tools)
        registry.addEndpoint("/ws-sockjs")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Đăng ký JWT interceptor vào đây — đây là "cửa" duy nhất để vào hệ thống chat
        registration.interceptors(jwtChannelInterceptor);
    }
}
