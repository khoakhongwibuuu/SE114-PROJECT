import React, { useCallback, useEffect, useState } from 'react';
import { View, Text, TouchableOpacity, StyleSheet, ActivityIndicator, StatusBar, Image } from 'react-native';
import { GiftedChat, type IMessage, Bubble, Send, InputToolbar, Composer } from 'react-native-gifted-chat';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { useNavigation, useRoute } from '@react-navigation/native';
import type { RouteProp } from '@react-navigation/native';
import MaterialCommunityIcons from 'react-native-vector-icons/MaterialCommunityIcons';
import Ionicons from 'react-native-vector-icons/Ionicons';
import { useWebSocket } from '../../hooks/useWebSocket';
import { getChatHistory } from '../../api/chat';
import { useAuth } from '../../context/AuthContext';
import { useFamily } from '../../context/FamilyContext';
import type { RootStackParamList } from '../../navigation/navigationTypes';

type ChatRoute = RouteProp<RootStackParamList, 'FamilyChatRoom'>;



// ─── Custom Render Helpers for Gifted Chat ───────────────────────────────────

const renderInputToolbar = (props: any) => (
  <InputToolbar
    {...props}
    containerStyle={{
      backgroundColor: 'white',
      borderTopWidth: 1,
      borderTopColor: '#E8E8E8',
      paddingVertical: 4,
    }}
  />
);

const renderComposer = (props: any) => (
  <Composer
    {...props}
    textInputStyle={{
      backgroundColor: '#F0F4F8',
      borderRadius: 20,
      paddingHorizontal: 16,
      paddingTop: 10,
      paddingBottom: 10,
      marginRight: 10,
    }}
  />
);

const renderSend = (props: any) => (
  <Send {...props} containerStyle={{ justifyContent: 'center', marginRight: 10 }}>
    <View style={{
      backgroundColor: '#2B6AEC',
      width: 36,
      height: 36,
      borderRadius: 18,
      justifyContent: 'center',
      alignItems: 'center',
    }}>
      <Text style={{ color: 'white', fontWeight: 'bold' }}>Gửi</Text>
    </View>
  </Send>
);

export default function FamilyChatRoomScreen() {
  const insets = useSafeAreaInsets(); // cache-bust-1
  const navigation = useNavigation<any>();
  const route = useRoute<ChatRoute>();
  const { familyId, familyName } = route.params;
  const { profile } = useAuth();
  const { members, familyImage } = useFamily();
  const memberCount = members.length;

  const [messages, setMessages] = useState<IMessage[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [currentPage, setCurrentPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [isLoadingMore, setIsLoadingMore] = useState(false);

  // ─── Load lịch sử ban đầu ───────────────────────────────────────────────────

  const loadHistory = useCallback(async (page = 0) => {
    try {
      const data = await getChatHistory(familyId, page);
      const mapped: IMessage[] = data.content.map(m => ({
        _id: m._id,
        text: m.text,
        createdAt: new Date(m.createdAt),
        user: { _id: m.user._id, name: m.user.name, avatar: m.user.avatar ?? undefined },
      }));

      if (page === 0) {
        setMessages(mapped);
      } else {
        setMessages(prev => GiftedChat.append(prev, mapped));
      }

      setHasMore(!data.last);
      setCurrentPage(data.number);
    } catch (e) {
      console.error('[Chat] Lỗi tải lịch sử:', e);
    } finally {
      setIsLoading(false);
    }
  }, [familyId]);

  useEffect(() => {
    void loadHistory(0);
  }, [loadHistory]);

  // ─── Load More (vuốt lên) ───────────────────────────────────────────────────

  const handleLoadEarlier = useCallback(async () => {
    if (!hasMore || isLoadingMore) return;
    setIsLoadingMore(true);
    await loadHistory(currentPage + 1);
    setIsLoadingMore(false);
  }, [hasMore, isLoadingMore, currentPage, loadHistory]);

  // ─── WebSocket real-time ────────────────────────────────────────────────────

  const handleIncomingMessage = useCallback((msg: IMessage) => {
    setMessages(prev => GiftedChat.prepend(prev, [msg]));
  }, []);

  const { sendMessage } = useWebSocket({ familyId, onMessageReceived: handleIncomingMessage });

  // ─── Gửi tin nhắn ──────────────────────────────────────────────────────────

  const handleSend = useCallback((newMessages: IMessage[] = []) => {
    const msg = newMessages[0];
    if (!msg?.text?.trim()) return;
    sendMessage(msg.text.trim());
  }, [sendMessage]);

  // ─── Me user object ─────────────────────────────────────────────────────────

  const me = {
    _id: profile?.id ?? 0,
    name: profile?.fullName ?? 'Tôi',
    avatar: profile?.avatarUrl ?? undefined,
  };

  // ─── Render ─────────────────────────────────────────────────────────────────

  return (
    <View style={[styles.root, { paddingTop: insets.top }]}>
      <StatusBar barStyle="dark-content" backgroundColor="#fff" />

      {/* Header */}
      <View style={styles.header}>
        <TouchableOpacity style={styles.backBtn} onPress={() => navigation.goBack()}>
          <MaterialCommunityIcons name="arrow-left" size={24} color="#0369a1" />
        </TouchableOpacity>

        <View style={styles.headerAvatarContainer}>
          {familyImage ? (
            <Image source={{ uri: familyImage }} style={styles.headerAvatar} />
          ) : (
            <View style={[styles.headerAvatar, styles.headerAvatarPlaceholder]}>
              <MaterialCommunityIcons name="home-heart" size={20} color="#0369a1" />
            </View>
          )}
        </View>

        <View style={styles.headerInfo}>
          <Text style={styles.headerTitle}>{familyName}</Text>
          <Text style={styles.headerSubtitle}>
            🟢 {memberCount > 0 ? `${memberCount} thành viên` : 'Chat nhóm gia đình'}
          </Text>
        </View>
      </View>

      {/* Chat */}
      {isLoading ? (
        <View style={styles.loadingContainer}>
          <ActivityIndicator size="large" color="#0369a1" />
          <Text style={styles.loadingText}>Đang tải tin nhắn...</Text>
        </View>
      ) : (
        <GiftedChat
          messages={messages}
          onSend={handleSend}
          user={me}
          locale="vi"
          bottomOffset={insets.bottom}
          alwaysShowSend={true}
          placeholder="Nhập tin nhắn..."
          loadEarlier={hasMore}
          isLoadingEarlier={isLoadingMore}
          onLoadEarlier={handleLoadEarlier}
          scrollToBottom={true}
          infiniteScroll={true}
          renderInputToolbar={renderInputToolbar}
          renderComposer={renderComposer}
          renderSend={renderSend}
          renderBubble={props => (
            <Bubble
              {...props}
              wrapperStyle={{
                right: { backgroundColor: '#0369a1' },
                left: { backgroundColor: '#f1f5f9' },
              }}
              textStyle={{
                right: { color: '#fff' },
                left: { color: '#1e293b' },
              }}
            />
          )}
          renderChatEmpty={() => (
            // Thêm marginTop để đẩy nó ra giữa màn hình (vì FlatList bị lật ngược)
            <View style={{ transform: [{ scaleY: -1 }], alignItems: 'center', justifyContent: 'center', marginTop: 250 }}>
              <View style={styles.emptyIconWrap}>
                <MaterialCommunityIcons name="home-heart" size={48} color="#94a3b8" />
              </View>
              <Text style={styles.emptyText}>Chưa có tin nhắn nào</Text>
              <Text style={styles.emptySubText}>
                Hãy gửi lời chào đầu tiên đến tổ ấm của bạn!
              </Text>
            </View>
          )}
        />
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  root: { flex: 1, backgroundColor: '#fff' },

  // Header
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 8,
    paddingVertical: 12,
    backgroundColor: '#fff',
    borderBottomWidth: 1,
    borderBottomColor: '#e2e8f0',
  },
  backBtn: { width: 40, height: 40, alignItems: 'center', justifyContent: 'center' },
  headerAvatarContainer: {
    marginHorizontal: 8,
  },
  headerAvatar: {
    width: 40,
    height: 40,
    borderRadius: 20,
  },
  headerAvatarPlaceholder: {
    backgroundColor: '#e0f2fe',
    alignItems: 'center',
    justifyContent: 'center',
  },
  headerInfo: { flex: 1, justifyContent: 'center' },
  headerTitle: { fontSize: 16, fontWeight: '700', color: '#1e293b' },
  headerSubtitle: { fontSize: 12, color: '#64748b', marginTop: 2 },

  // Loading
  loadingContainer: { flex: 1, alignItems: 'center', justifyContent: 'center', gap: 12 },
  loadingText: { fontSize: 14, color: '#64748b', fontFamily: 'Inter' },

  // Empty State
  emptyIconWrap: {
    width: 80,
    height: 80,
    borderRadius: 40,
    backgroundColor: '#f1f5f9',
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 16,
  },
  emptyText: {
    fontSize: 16,
    fontWeight: '700',
    color: '#475569',
    marginBottom: 8,
  },
  emptySubText: {
    fontSize: 14,
    color: '#94a3b8',
    textAlign: 'center',
    lineHeight: 20,
  },
});