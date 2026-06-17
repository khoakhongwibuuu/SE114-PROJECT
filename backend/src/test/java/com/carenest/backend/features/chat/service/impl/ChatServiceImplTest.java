package com.carenest.backend.features.chat.service.impl;

import com.carenest.backend.features.auth.entity.User;
import com.carenest.backend.features.auth.enums.Role;
import com.carenest.backend.features.auth.repository.UserRepository;
import com.carenest.backend.features.chat.entity.ChatMessage;
import com.carenest.backend.features.chat.repository.ChatMessageRepository;
import com.carenest.backend.features.chat.repository.GroupChatMessageRepository;
import com.carenest.backend.features.community.entity.ChatGroup;
import com.carenest.backend.features.community.repository.ChatGroupRepository;
import com.carenest.backend.features.community.repository.ReportTicketRepository;
import com.carenest.backend.features.community.repository.UserGroupMembershipRepository;
import com.carenest.backend.features.family.entity.Family;
import com.carenest.backend.features.family.entity.FamilyMember;
import com.carenest.backend.features.family.repository.FamilyMemberRepository;
import com.carenest.backend.features.family.repository.FamilyRepository;
import com.carenest.backend.features.notification.enums.NotificationType;
import com.carenest.backend.features.notification.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatServiceImplTest {

    @Mock
    private ChatMessageRepository chatMessageRepository;
    @Mock
    private GroupChatMessageRepository groupChatMessageRepository;
    @Mock
    private FamilyRepository familyRepository;
    @Mock
    private FamilyMemberRepository familyMemberRepository;
    @Mock
    private ChatGroupRepository chatGroupRepository;
    @Mock
    private UserGroupMembershipRepository userGroupMembershipRepository;
    @Mock
    private ReportTicketRepository reportTicketRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ChatServiceImpl chatService;

    @Test
    void saveMessage_notifiesOtherFamilyMembers() {
        User sender = user(1L, "sender@example.com", "Sender");
        User recipient = user(2L, "recipient@example.com", "Recipient");
        Family family = Family.builder().name("CareNest Home").owner(sender).build();
        family.setId(100L);

        when(familyRepository.findById(100L)).thenReturn(Optional.of(family));
        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(familyMemberRepository.existsByFamilyIdAndUserId(100L, 1L)).thenReturn(true);
        when(chatMessageRepository.save(org.mockito.ArgumentMatchers.any(ChatMessage.class))).thenAnswer(invocation -> {
            ChatMessage message = invocation.getArgument(0);
            message.setId(500L);
            return message;
        });
        when(familyMemberRepository.findAllByFamilyId(100L)).thenReturn(List.of(
                FamilyMember.builder().family(family).user(sender).build(),
                FamilyMember.builder().family(family).user(recipient).build()
        ));

        chatService.saveMessage(100L, 1L, "Xin chào CareNest");

        verify(notificationService).createNotificationForUsers(
                argThat(users -> users.size() == 1 && users.get(0).getId().equals(2L)),
                eq("Tin nhắn gia đình mới"),
                contains("Sender: Xin chào CareNest"),
                eq(NotificationType.CHAT),
                eq("FAMILY_CHAT"),
                eq(100L)
        );
    }

    @Test
    void saveGroupMessage_rejectsFrozenGroup() {
        User sender = user(1L, "sender@example.com", "Sender");
        ChatGroup group = ChatGroup.builder()
                .name("Pediatrics")
                .isFrozen(true)
                .build();
        group.setId(22L);

        when(chatGroupRepository.findById(22L)).thenReturn(Optional.of(group));
        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(userGroupMembershipRepository.existsByGroupIdAndUserId(22L, 1L)).thenReturn(true);

        assertThatThrownBy(() -> chatService.saveGroupMessage(22L, 1L, "Xin chao"))
                .isInstanceOf(com.carenest.backend.core.exception.BadRequestException.class);
    }

    private static User user(Long id, String email, String fullName) {
        User user = User.builder()
                .email(email)
                .fullName(fullName)
                .role(Role.USER)
                .build();
        user.setId(id);
        return user;
    }
}
