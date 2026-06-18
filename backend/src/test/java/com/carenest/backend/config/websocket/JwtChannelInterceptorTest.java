package com.carenest.backend.config.websocket;

import com.carenest.backend.config.security.JwtService;
import com.carenest.backend.features.auth.entity.User;
import com.carenest.backend.features.auth.repository.UserRepository;
import com.carenest.backend.features.community.repository.UserGroupMembershipRepository;
import com.carenest.backend.features.booking.repository.ConsultationThreadRepository;
import com.carenest.backend.features.family.repository.FamilyMemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtChannelInterceptorTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserGroupMembershipRepository userGroupMembershipRepository;

    @Mock
    private FamilyMemberRepository familyMemberRepository;

    @Mock
    private ConsultationThreadRepository consultationThreadRepository;

    @Mock
    private MessageChannel channel;

    @Test
    void preSend_rejectsConnectForDisabledUserWithStillValidToken() {
        JwtChannelInterceptor interceptor = new JwtChannelInterceptor(
                jwtService,
                userDetailsService,
                userRepository,
                userGroupMembershipRepository,
                familyMemberRepository,
                consultationThreadRepository
        );
        User disabledUser = User.builder()
                .email("locked@example.com")
                .passwordHash("hashed")
                .fullName("Locked User")
                .isActive(false)
                .build();
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setNativeHeader("Authorization", "Bearer valid-token");
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());

        when(jwtService.extractUsername("valid-token")).thenReturn("locked@example.com");
        when(userDetailsService.loadUserByUsername("locked@example.com")).thenReturn(disabledUser);
        when(jwtService.isTokenValid("valid-token", disabledUser)).thenReturn(true);

        assertThatThrownBy(() -> interceptor.preSend(message, channel))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Account disabled or locked");
    }
}
