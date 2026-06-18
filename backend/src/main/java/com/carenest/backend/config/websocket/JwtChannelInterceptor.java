package com.carenest.backend.config.websocket;

import com.carenest.backend.config.security.JwtService;
import com.carenest.backend.features.auth.entity.User;
import com.carenest.backend.features.auth.enums.Role;
import com.carenest.backend.features.auth.repository.UserRepository;
import com.carenest.backend.features.community.repository.UserGroupMembershipRepository;
import com.carenest.backend.features.family.repository.FamilyMemberRepository;
import com.carenest.backend.features.booking.repository.ConsultationThreadRepository;
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

import java.security.Principal;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final UserGroupMembershipRepository userGroupMembershipRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final ConsultationThreadRepository consultationThreadRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            authenticateConnect(accessor);
        }

        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            validateTopicSubscription(accessor);
        }

        return message;
    }

    private void authenticateConnect(StompHeaderAccessor accessor) {
        String authHeader = accessor.getFirstNativeHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("[WS] CONNECT rejected: missing Authorization bearer token.");
            throw new AccessDeniedException("Unauthorized WebSocket connection: Missing token.");
        }

        String jwt = authHeader.substring(7);
        String username;
        try {
            username = jwtService.extractUsername(jwt);
        } catch (Exception e) {
            log.warn("[WS] CONNECT rejected: malformed token - {}", e.getMessage());
            throw new AccessDeniedException("Unauthorized WebSocket connection: Malformed token.");
        }

        if (username == null) {
            throw new AccessDeniedException("Unauthorized WebSocket connection: Invalid token payload.");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        if (!jwtService.isTokenValid(jwt, userDetails)) {
            log.warn("[WS] CONNECT rejected for '{}': token expired or invalid.", username);
            throw new AccessDeniedException("Unauthorized WebSocket connection: Token expired or invalid.");
        }
        if (!isAccountAllowed(userDetails)) {
            log.warn("[WS] CONNECT rejected for '{}': account is disabled or locked.", username);
            throw new AccessDeniedException("Unauthorized WebSocket connection: Account disabled or locked.");
        }

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        accessor.setUser(authToken);
        SecurityContextHolder.getContext().setAuthentication(authToken);
        log.info("[WS] CONNECT accepted: user='{}'", username);
    }

    private void validateTopicSubscription(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        if (destination == null || destination.isBlank()) {
            return;
        }

        User user = resolveUser(accessor.getUser());
        if (user == null) {
            throw new AccessDeniedException("Unauthorized WebSocket subscription.");
        }

        if (destination.startsWith("/topic/group/")) {
            Long groupId = parseId(destination, "/topic/group/");
            boolean isMember = userGroupMembershipRepository.existsByGroupIdAndUserId(groupId, user.getId());
            if (!isMember && user.getRole() != Role.ADMIN) {
                throw new AccessDeniedException("Forbidden WebSocket group subscription.");
            }
            return;
        }

        if (destination.startsWith("/topic/family/")) {
            Long familyId = parseId(destination, "/topic/family/");
            boolean isMember = familyMemberRepository.existsByFamilyIdAndUserId(familyId, user.getId());
            if (!isMember) {
                throw new AccessDeniedException("Forbidden WebSocket family subscription.");
            }
            return;
        }

        if (destination.startsWith("/topic/consultation/thread/")) {
            Long threadId = parseId(destination, "/topic/consultation/thread/");
            boolean isParticipant = consultationThreadRepository.existsByIdAndParticipantId(threadId, user.getId());
            if (!isParticipant && user.getRole() != Role.ADMIN) {
                throw new AccessDeniedException("Forbidden WebSocket consultation subscription.");
            }
        }
    }

    private User resolveUser(Principal principal) {
        if (principal instanceof UsernamePasswordAuthenticationToken token) {
            Object tokenPrincipal = token.getPrincipal();
            if (tokenPrincipal instanceof User user) {
                return user;
            }
            if (tokenPrincipal instanceof UserDetails userDetails) {
                return userRepository.findByEmail(userDetails.getUsername()).orElse(null);
            }
        }
        return principal != null ? userRepository.findByEmail(principal.getName()).orElse(null) : null;
    }

    private boolean isAccountAllowed(UserDetails userDetails) {
        return userDetails.isEnabled()
                && userDetails.isAccountNonLocked()
                && userDetails.isAccountNonExpired()
                && userDetails.isCredentialsNonExpired();
    }

    private Long parseId(String destination, String prefix) {
        try {
            return Long.parseLong(destination.substring(prefix.length()));
        } catch (NumberFormatException ex) {
            throw new AccessDeniedException("Invalid WebSocket topic destination.");
        }
    }
}
