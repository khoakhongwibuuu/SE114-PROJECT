import React, { useCallback, useEffect, useRef, useState } from 'react';
import { View, Text, TouchableOpacity, StyleSheet, ActivityIndicator, StatusBar } from 'react-native';
import { GiftedChat, type IMessage, Bubble, Send, InputToolbar } from 'react-native-gifted-chat';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { useNavigation, useRoute } from '@react-navigation/native';
import type { RouteProp } from '@react-navigation/native';
import MaterialCommunityIcons from 'react-native-vector-icons/MaterialCommunityIcons';
import { useWebSocket } from '../../hooks/useWebSocket';
import { getChatHistory } from '../../api/chat';
import { useAuth } from '../../context/AuthContext';
import type { FamilyStackParamList } from '../../navigation/navigationTypes';

type ChatRoute = RouteProp<FamilyStackParamList, 'FamilyChat'>;

export default function FamilyChatScreen() {
  const insets = useSafeAreaInsets();
  const navigation = useNavigation<any>();
  const route = useRoute<ChatRoute>();
  const { familyId, familyName } = route.params;
  const { profile } = useAuth();

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
        <View style={styles.headerInfo}>
          <Text style={styles.headerTitle}>{familyName}</Text>
          <Text style={styles.headerSubtitle}>Chat nhóm gia đình</Text>
        </View>
        <View style={styles.onlineIndicator}>
          <View style={styles.onlineDot} />
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
          placeholder="Nhắn tin cho gia đình..."
          alwaysShowSend
          loadEarlier={hasMore}
          isLoadingEarlier={isLoadingMore}
          onLoadEarlier={handleLoadEarlier}
          scrollToBottom
          infiniteScroll
          renderBubble={props => (
            <Bubble
              {...props}
              wrapperStyle={{
                right: { backgroundColor: '#0369a1' },
                left: { backgroundColor: '#f1f5f9' },
              }}
              textStyle={{
                right: { color: '#fff', fontFamily: 'Inter' },
                left: { color: '#1e293b', fontFamily: 'Inter' },
              }}
            />
          )}
          renderSend={props => (
            <Send {...props}>
              <View style={styles.sendBtn}>
                <MaterialCommunityIcons name="send" size={22} color="#fff" />
              </View>
            </Send>
          )}
          renderInputToolbar={props => (
            <InputToolbar
              {...props}
              containerStyle={[styles.inputToolbar, { paddingBottom: insets.bottom + 4 }]}
              primaryStyle={styles.inputPrimary}
            />
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
    paddingHorizontal: 16,
    paddingVertical: 14,
    backgroundColor: '#fff',
    borderBottomWidth: 1,
    borderBottomColor: '#e2e8f0',
    gap: 12,
  },
  backBtn: { width: 40, height: 40, alignItems: 'center', justifyContent: 'center' },
  headerInfo: { flex: 1 },
  headerTitle: { fontSize: 18, fontWeight: '800', color: '#0369a1', fontFamily: 'Manrope' },
  headerSubtitle: { fontSize: 12, color: '#64748b', fontFamily: 'Inter', marginTop: 1 },
  onlineIndicator: { width: 36, height: 36, alignItems: 'center', justifyContent: 'center' },
  onlineDot: {
    width: 10, height: 10, borderRadius: 5,
    backgroundColor: '#22c55e',
    shadowColor: '#22c55e', shadowOpacity: 0.5, shadowRadius: 4, elevation: 2,
  },

  // Loading
  loadingContainer: { flex: 1, alignItems: 'center', justifyContent: 'center', gap: 12 },
  loadingText: { fontSize: 14, color: '#64748b', fontFamily: 'Inter' },

  // Input
  inputToolbar: {
    borderTopWidth: 1,
    borderTopColor: '#e2e8f0',
    backgroundColor: '#f8fafc',
    paddingHorizontal: 8,
    paddingTop: 8,
  },
  inputPrimary: { alignItems: 'center' },
  sendBtn: {
    width: 40, height: 40, borderRadius: 20,
    backgroundColor: '#0369a1',
    alignItems: 'center', justifyContent: 'center',
    marginRight: 4, marginBottom: 4,
  },
});
