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
 * Cháº·n vÃ  xÃ¡c thá»±c JWT Token khi Client gá»­i gÃ³i tin STOMP CONNECT.
 * WebSocket khÃ´ng há»— trá»£ HTTP Authorization header tiÃªu chuáº©n,
 * nÃªn token pháº£i Ä‘Æ°á»£c Ä‘Ã­nh vÃ o STOMP Connect Headers.
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

        // Chá»‰ kiá»ƒm tra khi Client gá»­i gÃ³i CONNECT
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            // BÆ°á»›c 1: Kiá»ƒm tra header
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("[WS] CONNECT bá»‹ tá»« chá»‘i: Thiáº¿u hoáº·c sai format Authorization header.");
                throw new AccessDeniedException("Unauthorized WebSocket connection: Missing token.");
            }

            // BÆ°á»›c 2: BÃ³c JWT
            String jwt = authHeader.substring(7);

            // BÆ°á»›c 3: Láº¥y username (email) tá»« token
            String username;
            try {
                username = jwtService.extractUsername(jwt);
            } catch (Exception e) {
                log.warn("[WS] CONNECT bá»‹ tá»« chá»‘i: KhÃ´ng Ä‘á»c Ä‘Æ°á»£c token â€” {}", e.getMessage());
                throw new AccessDeniedException("Unauthorized WebSocket connection: Malformed token.");
            }

            if (username == null) {
                throw new AccessDeniedException("Unauthorized WebSocket connection: Invalid token payload.");
            }

            // BÆ°á»›c 4: Load user vÃ  validate token
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            if (!jwtService.isTokenValid(jwt, userDetails)) {
                log.warn("[WS] CONNECT bá»‹ tá»« chá»‘i cho '{}': Token háº¿t háº¡n hoáº·c khÃ´ng há»£p lá»‡.", username);
                throw new AccessDeniedException("Unauthorized WebSocket connection: Token expired or invalid.");
            }
            if (!isAccountAllowed(userDetails)) {
                log.warn("[WS] CONNECT bá»‹ tá»« chá»‘i cho '{}': TÃ i khoáº£n Ä‘Ã£ bá»‹ vÃ´ hiá»‡u hÃ³a hoáº·c khÃ³a.", username);
                throw new AccessDeniedException("Unauthorized WebSocket connection: Account disabled or locked.");
            }

            // BÆ°á»›c 5: GÃ¡n Authentication vÃ o STOMP session
            // Tá»« Ä‘Ã¢y @AuthenticationPrincipal trong @MessageMapping sáº½ hoáº¡t Ä‘á»™ng
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            accessor.setUser(authToken);
            SecurityContextHolder.getContext().setAuthentication(authToken);

            log.info("[WS] CONNECT thÃ nh cÃ´ng: user='{}'", username);
        }

        return message;
    }

    private boolean isAccountAllowed(UserDetails userDetails) {
        return userDetails.isEnabled()
                && userDetails.isAccountNonLocked()
                && userDetails.isAccountNonExpired()
                && userDetails.isCredentialsNonExpired();
    }
}
