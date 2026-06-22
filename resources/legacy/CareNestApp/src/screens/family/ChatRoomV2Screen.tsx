import React, { useCallback, useEffect, useState, useRef } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
  ActivityIndicator,
  StatusBar,
  Image,
  FlatList,
  TextInput,
  KeyboardAvoidingView,
  Platform,
} from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { useNavigation, useRoute } from '@react-navigation/native';
import type { RouteProp } from '@react-navigation/native';
import MaterialCommunityIcons from 'react-native-vector-icons/MaterialCommunityIcons';
import { useWebSocket } from '../../hooks/useWebSocket';
import type { ChatMessagePayload } from '../../hooks/useWebSocket';
import { getChatHistory } from '../../api/chat';
import type { ChatMessage as ApiChatMessage } from '../../api/chat';
import { useAuth } from '../../context/AuthContext';
import { useFamily } from '../../context/FamilyContext';
import type { RootStackParamList } from '../../navigation/navigationTypes';

type ChatRoute = RouteProp<RootStackParamList, 'ChatRoomV2'>;

// Message type uses ChatMessagePayload shape for consistency with WS hook
type Message = ChatMessagePayload;

// Helper: map backend API response to local Message type
function mapApiMessage(m: ApiChatMessage): Message {
  return {
    _id: m._id,
    text: m.text,
    createdAt: new Date(m.createdAt),
    user: {
      _id: m.user._id,
      name: m.user.name,
      avatar: m.user.avatar ?? undefined,
    },
  };
}

export default function ChatRoomV2Screen() {
  const insets = useSafeAreaInsets();
  const navigation = useNavigation<any>();
  const route = useRoute<ChatRoute>();
  const { familyId, familyName } = route.params;
  const { user } = useAuth();
  const { members, familyImage } = useFamily();
  const memberCount = members ? members.length : 0;

  const [messages, setMessages] = useState<Message[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [currentPage, setCurrentPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [isLoadingMore, setIsLoadingMore] = useState(false);
  const [inputText, setInputText] = useState('');
  const [wsConnected, setWsConnected] = useState(false);
  const flatListRef = useRef<FlatList>(null);
  const isConnectedRef = useRef<(() => boolean) | null>(null);
  // Deduplication guard: track seen message IDs to prevent double-render
  const seenIds = useRef<Set<string | number>>(new Set());

  // ─── API: Load lịch sử chat (phân trang) ─────────────────────────────────
  const loadHistory = useCallback(async (page: number) => {
    try {
      const data = await getChatHistory(familyId, page);
      // Deduplicate: chỉ thêm tin nhắn chưa từng thấy
      const newMessages = data.content
        .map(mapApiMessage)
        .filter(m => {
          const key = String(m._id);
          if (seenIds.current.has(key)) return false;
          seenIds.current.add(key);
          return true;
        });

      if (page === 0) {
        // Reset toàn bộ khi tải trang đầu
        seenIds.current = new Set(newMessages.map(m => String(m._id)));
        setMessages(newMessages);
      } else {
        // Append cho infinite scroll (tin cũ hơn ở phía cuối mảng inverted)
        setMessages(prev => [...prev, ...newMessages]);
      }

      setHasMore(!data.last);
      setCurrentPage(data.number);
    } catch (e) {
      console.error('[ChatRoomV2] Lỗi tải lịch sử:', e);
    } finally {
      setIsLoading(false);
    }
  }, [familyId]);

  useEffect(() => {
    // Reset state khi đổi phòng chat
    seenIds.current = new Set();
    setMessages([]);
    setCurrentPage(0);
    setHasMore(true);
    setIsLoading(true);
    void loadHistory(0);
  }, [loadHistory]);

  // ─── Infinite Scroll: Load tin nhắn cũ hơn khi cuộn lên đầu ───────────────
  const handleLoadEarlier = useCallback(async () => {
    // Guard: tránh gọi API kép khi user cuộn nhanh
    if (!hasMore || isLoadingMore) return;
    setIsLoadingMore(true);
    await loadHistory(currentPage + 1);
    setIsLoadingMore(false);
  }, [hasMore, isLoadingMore, currentPage, loadHistory]);

  // ─── WebSocket: Nhận tin nhắn real-time ────────────────────────────────────
  const handleIncomingMessage = useCallback((msg: ChatMessagePayload) => {
    const key = String(msg._id);
    // Dedup guard: bỏ qua nếu WS echo lại tin mình vừa gửi
    if (seenIds.current.has(key)) return;
    seenIds.current.add(key);
    // Prepend vào đầu mảng (inverted FlatList hiển thị mới nhất lên trên)
    setMessages(prev => [msg, ...prev]);
  }, []);

  const { sendMessage, isConnected } = useWebSocket({
    familyId,
    onMessageReceived: handleIncomingMessage,
  });

  // Lưu ref để dùng trong interval mà không bị stale closure
  isConnectedRef.current = isConnected;

  // Cập nhật trạng thái kết nối WS: kiểm tra ngay lập tức + mỗi 2 giây
  useEffect(() => {
    // Kiểm tra ngay khi mount (không đợi 2 giây mới biết trạng thái)
    setWsConnected(isConnected());
    const timer = setInterval(() => {
      setWsConnected(isConnectedRef.current?.() ?? false);
    }, 2000);
    return () => clearInterval(timer);
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // ─── Gửi tin nhắn (Optimistic UI) ─────────────────────────────────────────
  const handleSend = useCallback(() => {
    const text = inputText.trim();
    if (!text) return;

    if (!wsConnected) {
      // Guard: Không xóa chữ nếu chưa kết nối, báo lỗi cho sếp đỡ tức!
      console.warn('[ChatRoom] Không thể gửi tin, mạng đang ngắt kết nối.');
      return;
    }

    // Mạng OK -> Xóa input ngay lập tức → cảm giác phản hồi tức thì
    setInputText('');
    sendMessage(text);
  }, [inputText, wsConnected, sendMessage]);

  // ID của user hiện tại để phân biệt tin của mình vs người khác
  const myId = user?.userId ?? 0;

  // Memoize toàn bộ render fn để FlatList không re-render không cần thiết
  const renderMessageItem = useCallback(({ item }: { item: Message }) => {
    const isMe = item.user._id === myId;
    const timeStr = item.createdAt.toLocaleTimeString([], {
      hour: '2-digit',
      minute: '2-digit',
    });

    return (
      <View style={[styles.messageRow, isMe ? styles.rowRight : styles.rowLeft]}>
        {!isMe && (
          <View style={styles.avatarContainer}>
            {item.user.avatar ? (
              <Image source={{ uri: item.user.avatar }} style={styles.avatar} />
            ) : (
              <View style={styles.avatarPlaceholder}>
                <Text style={styles.avatarText}>{item.user.name.charAt(0)}</Text>
              </View>
            )}
          </View>
        )}

        <View style={[styles.bubbleContainer, isMe ? styles.alignEnd : styles.alignStart]}>
          {!isMe && <Text style={styles.senderName}>{item.user.name}</Text>}
          <View style={[styles.bubble, isMe ? styles.bubbleMe : styles.bubbleOther]}>
            <Text style={[styles.bubbleText, isMe ? styles.textMe : styles.textOther]}>
              {item.text}
            </Text>
          </View>
          <Text style={styles.timestamp}>{timeStr}</Text>
        </View>
      </View>
    );
  }, [myId]);

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
            {wsConnected ? '🟢 ' : '🔴 '}
            {memberCount > 0 ? `${memberCount} Thành viên` : 'Chat nhóm gia đình'}
          </Text>
        </View>
      </View>

      {/* Chat Area */}
      {isLoading ? (
        <View style={styles.loadingContainer}>
          <ActivityIndicator size="large" color="#0369a1" />
          <Text style={styles.loadingText}>Đang tải tin nhắn...</Text>
        </View>
      ) : (
        <KeyboardAvoidingView
          style={styles.keyboardContainer}
          behavior={Platform.OS === 'ios' ? 'padding' : undefined}
          keyboardVerticalOffset={Platform.OS === 'ios' ? 90 : 0}
        >
          <FlatList
            ref={flatListRef}
            data={messages}
            keyExtractor={item => String(item._id)}
            renderItem={renderMessageItem}
            inverted={true}
            onEndReached={handleLoadEarlier}
            onEndReachedThreshold={0.2}
            contentContainerStyle={styles.chatContent}
            showsVerticalScrollIndicator={false}
            ListEmptyComponent={
              <View style={styles.emptyWrap}>
                <MaterialCommunityIcons name="home-heart" size={64} color="#94a3b8" />
                <Text style={styles.emptyTitle}>Chưa có tin nhắn nào</Text>
                <Text style={styles.emptySub}>
                  Hãy gửi lời chào đầu tiên đến tổ ấm của bạn!
                </Text>
              </View>
            }
          />

          {/* Custom Input Bar matching AI Chat style */}
          <View style={[styles.inputContainer, { paddingBottom: Math.max(insets.bottom, 8) }]}>
            <View style={styles.inputBar}>
              <TextInput
                style={styles.input}
                placeholder="Nhập tin nhắn..."
                placeholderTextColor="#94a3b8"
                value={inputText}
                onChangeText={setInputText}
                onSubmitEditing={handleSend}
              />
            </View>
            <TouchableOpacity
              style={[styles.sendBtn, !inputText.trim() && styles.sendBtnDisabled]}
              onPress={handleSend}
              disabled={!inputText.trim()}
            >
              <MaterialCommunityIcons name="send" size={22} color="#fff" />
            </TouchableOpacity>
          </View>
        </KeyboardAvoidingView>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  root: { flex: 1, backgroundColor: '#fff' },
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
  headerAvatarContainer: { marginHorizontal: 8 },
  headerAvatar: { width: 40, height: 40, borderRadius: 20 },
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
  loadingText: { fontSize: 14, color: '#64748b' },

  // Messages Area
  keyboardContainer: { flex: 1 },
  chatContent: { paddingHorizontal: 16, paddingVertical: 16 },

  messageRow: {
    flexDirection: 'row',
    marginBottom: 16,
    maxWidth: '80%',
  },
  rowLeft: { alignSelf: 'flex-start' },
  rowRight: { alignSelf: 'flex-end', flexDirection: 'row-reverse' },

  avatarContainer: { marginRight: 8, alignSelf: 'flex-end' },
  avatar: { width: 32, height: 32, borderRadius: 16 },
  avatarPlaceholder: {
    width: 32,
    height: 32,
    borderRadius: 16,
    backgroundColor: '#e2e8f0',
    alignItems: 'center',
    justifyContent: 'center',
  },
  avatarText: { fontSize: 14, fontWeight: 'bold', color: '#475569' },

  bubbleContainer: { flex: 1 },
  alignStart: { alignItems: 'flex-start' },
  alignEnd: { alignItems: 'flex-end' },

  senderName: { fontSize: 12, color: '#64748b', marginBottom: 4, marginLeft: 4 },
  bubble: {
    paddingHorizontal: 16,
    paddingVertical: 10,
    borderRadius: 20,
    maxWidth: '100%',
  },
  bubbleMe: {
    backgroundColor: '#1a73e8',
    borderBottomRightRadius: 4,
  },
  bubbleOther: {
    backgroundColor: '#f1f5f9',
    borderBottomLeftRadius: 4,
  },
  bubbleText: { fontSize: 15, lineHeight: 20 },
  textMe: { color: '#fff' },
  textOther: { color: '#1e293b' },
  timestamp: { fontSize: 10, color: '#94a3b8', marginTop: 4, marginHorizontal: 4 },

  // Empty State
  emptyWrap: {
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: 100,
  },
  emptyTitle: {
    fontSize: 16,
    fontWeight: 'bold',
    color: '#475569',
    marginTop: 10,
  },
  emptySub: {
    fontSize: 14,
    color: '#94a3b8',
    marginTop: 5,
    textAlign: 'center',
  },

  // Input Container
  inputContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 16,
    paddingVertical: 8,
    borderTopWidth: 1,
    borderTopColor: '#f1f5f9',
    backgroundColor: '#fff',
  },
  inputBar: {
    flex: 1,
    height: 46,
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#f1f5f9',
    borderRadius: 23,
    paddingHorizontal: 16,
  },
  input: { flex: 1, fontSize: 14, color: '#1e293b', padding: 0 },
  sendBtn: {
    width: 46,
    height: 46,
    borderRadius: 23,
    backgroundColor: '#1a73e8',
    alignItems: 'center',
    justifyContent: 'center',
    marginLeft: 8,
  },
  sendBtnDisabled: { opacity: 0.5 },
});
