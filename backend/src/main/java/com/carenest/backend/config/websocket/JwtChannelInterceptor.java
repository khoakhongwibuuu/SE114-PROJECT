package com.carenest.backend.config.websocket;

import com.carenest.backend.config.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

/**
 * Chặn và xác thực JWT Token khi Client gửi gói tin STOMP CONNECT.
 * WebSocket không hỗ trợ HTTP Authorization header tiêu chuẩn,
 * nên token phải được đính vào STOMP Connect Headers.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        // Chỉ kiểm tra khi Client gửi gói CONNECT
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            // Bước 1: Kiểm tra header
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("[WS] CONNECT bị từ chối: Thiếu hoặc sai format Authorization header.");
                throw new AccessDeniedException("Unauthorized WebSocket connection: Missing token.");
            }

            // Bước 2: Bóc JWT
            String jwt = authHeader.substring(7);

            // Bước 3: Lấy username (email) từ token
            String username;
            try {
                username = jwtService.extractUsername(jwt);
            } catch (Exception e) {
                log.warn("[WS] CONNECT bị từ chối: Không đọc được token — {}", e.getMessage());
                throw new AccessDeniedException("Unauthorized WebSocket connection: Malformed token.");
            }

            if (username == null) {
                throw new AccessDeniedException("Unauthorized WebSocket connection: Invalid token payload.");
            }

            // Bước 4: Load user và validate token
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            if (!jwtService.isTokenValid(jwt, userDetails)) {
                log.warn("[WS] CONNECT bị từ chối cho '{}': Token hết hạn hoặc không hợp lệ.", username);
                throw new AccessDeniedException("Unauthorized WebSocket connection: Token expired or invalid.");
            }

            // Bước 5: Gán Authentication vào STOMP session
            // Từ đây @AuthenticationPrincipal trong @MessageMapping sẽ hoạt động
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            accessor.setUser(authToken);
            SecurityContextHolder.getContext().setAuthentication(authToken);

            log.info("[WS] CONNECT thành công: user='{}'", username);
        }

        return message;
    }
}
