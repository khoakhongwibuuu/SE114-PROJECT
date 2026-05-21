import React, { useCallback, useState } from 'react';
import {
  ActivityIndicator,
  Alert,
  FlatList,
  KeyboardAvoidingView,
  Platform,
  StyleSheet,
  Text,
  TextInput,
  TouchableOpacity,
  View,
} from 'react-native';
import { useFocusEffect, useNavigation, useRoute } from '@react-navigation/native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import MaterialCommunityIcons from 'react-native-vector-icons/MaterialCommunityIcons';
import { createGroupPost, getGroupPosts, type GroupPost } from '../../api/community';
import { useAuth } from '../../context/AuthContext';
import { colors } from '../../theme/colors';

function getInitial(name?: string | null): string {
  return (name || 'U').trim().charAt(0).toUpperCase();
}

function formatMessageTime(value?: string | null): string {
  if (!value) {
    return '';
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return '';
  }
  return date.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });
}

function MessageBubble({ post, isMine }: { post: GroupPost; isMine: boolean }) {
  const authorName = post.authorName || 'Thanh vien';

  return (
    <View style={[styles.messageRow, isMine ? styles.messageRowMine : styles.messageRowOther]}>
      {!isMine ? (
        <View style={styles.messageAvatar}>
          <Text style={styles.messageAvatarText}>{getInitial(authorName)}</Text>
        </View>
      ) : null}
      <View style={styles.messageBlock}>
        {!isMine ? <Text style={styles.messageAuthor}>{authorName}</Text> : null}
        <View style={[styles.bubble, isMine ? styles.bubbleMine : styles.bubbleOther]}>
          <Text style={[styles.messageText, isMine ? styles.messageTextMine : styles.messageTextOther]}>
            {post.content}
          </Text>
          {post.createdAt ? (
            <Text style={[styles.messageTime, isMine ? styles.messageTimeMine : styles.messageTimeOther]}>
              {formatMessageTime(post.createdAt)}
            </Text>
          ) : null}
        </View>
      </View>
    </View>
  );
}

export default function GroupDetailScreen() {
  const navigation = useNavigation<any>();
  const route = useRoute<any>();
  const insets = useSafeAreaInsets();
  const { user } = useAuth();
  const groupId = Number(route.params?.groupId);
  const groupName = route.params?.groupName || 'Hội nhóm';
  const currentUserId = user?.userId ?? (user?.id ? Number(user.id) : null);

  const [posts, setPosts] = useState<GroupPost[]>([]);
  const [loading, setLoading] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);
  const [currentPage, setCurrentPage] = useState(0);
  const [hasMore, setHasMore] = useState(false);
  const [message, setMessage] = useState('');
  const [sending, setSending] = useState(false);

  const loadPosts = useCallback(async (page = 0) => {
    if (!Number.isFinite(groupId)) {
      setPosts([]);
      setLoading(false);
      return;
    }

    try {
      if (page === 0) {
        setLoading(true);
      } else {
        setLoadingMore(true);
      }
      const result = await getGroupPosts(groupId, page, 30);
      setPosts(current => page === 0 ? result.content : [...current, ...result.content]);
      setCurrentPage(result.page);
      setHasMore(!result.last);
    } catch {
      if (page === 0) {
        setPosts([]);
      }
    } finally {
      setLoading(false);
      setLoadingMore(false);
    }
  }, [groupId]);

  useFocusEffect(useCallback(() => {
    void loadPosts();
  }, [loadPosts]));

  const handleLoadMore = useCallback(() => {
    if (!hasMore || loadingMore || loading) {
      return;
    }
    void loadPosts(currentPage + 1);
  }, [currentPage, hasMore, loadPosts, loading, loadingMore]);

  const handleSend = async () => {
    const content = message.trim();
    if (!content || sending || !Number.isFinite(groupId)) {
      return;
    }

    try {
      setSending(true);
      setMessage('');
      const created = await createGroupPost(groupId, content);
      setPosts(current => [created, ...current]);
    } catch (error) {
      setMessage(content);
      Alert.alert(
        'Không thể gửi tin nhắn',
        error instanceof Error ? error.message : 'Đã có lỗi xảy ra',
      );
    } finally {
      setSending(false);
    }
  };

  return (
    <KeyboardAvoidingView
      style={styles.root}
      behavior={Platform.OS === 'ios' ? 'padding' : undefined}
      keyboardVerticalOffset={Platform.OS === 'ios' ? 8 : 0}
    >
      <View style={styles.header}>
        <TouchableOpacity style={styles.iconButton} onPress={() => navigation.goBack()}>
          <MaterialCommunityIcons name="arrow-left" size={24} color="#0f172a" />
        </TouchableOpacity>
        <View style={styles.headerTitleWrap}>
          <Text style={styles.headerTitle} numberOfLines={1}>{groupName}</Text>
          <Text style={styles.headerSubtitle}>Trò chuyện cộng đồng</Text>
        </View>
        <View style={styles.headerSpacer} />
      </View>

      {loading ? (
        <View style={styles.centerState}>
          <ActivityIndicator size="large" color={colors.primary} />
        </View>
      ) : (
        <FlatList
          data={posts}
          keyExtractor={item => String(item.id)}
          renderItem={({ item }) => (
            <MessageBubble
              post={item}
              isMine={currentUserId !== null && item.authorId === currentUserId}
            />
          )}
          inverted
          contentContainerStyle={styles.messageList}
          keyboardShouldPersistTaps="handled"
          onEndReached={handleLoadMore}
          onEndReachedThreshold={0.25}
          ListFooterComponent={
            loadingMore ? (
              <View style={styles.loadingMore}>
                <ActivityIndicator size="small" color={colors.primary} />
              </View>
            ) : null
          }
          ListEmptyComponent={
            <View style={styles.emptyState}>
              <MaterialCommunityIcons name="chat-outline" size={42} color="#94a3b8" />
              <Text style={styles.emptyTitle}>Chưa có tin nhắn</Text>
              <Text style={styles.emptyText}>Hãy bắt đầu cuộc trao đổi đầu tiên trong nhóm.</Text>
            </View>
          }
        />
      )}

      <View style={[styles.composerWrap, { paddingBottom: insets.bottom + 10 }]}>
        <TextInput
          style={styles.composerInput}
          value={message}
          onChangeText={setMessage}
          placeholder="Nhập tin nhắn..."
          placeholderTextColor="#94a3b8"
          multiline
        />
        <TouchableOpacity
          style={[styles.sendButton, (!message.trim() || sending) && styles.sendButtonDisabled]}
          disabled={!message.trim() || sending}
          onPress={() => void handleSend()}
          activeOpacity={0.82}
        >
          <MaterialCommunityIcons name="send" size={22} color="#fff" />
        </TouchableOpacity>
      </View>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  root: { flex: 1, backgroundColor: '#eef4f8' },
  header: {
    height: 62,
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 12,
    backgroundColor: '#fff',
    borderBottomWidth: 1,
    borderBottomColor: '#e2e8f0',
  },
  iconButton: {
    width: 44,
    height: 44,
    alignItems: 'center',
    justifyContent: 'center',
  },
  headerTitleWrap: { flex: 1, alignItems: 'center', minWidth: 0 },
  headerTitle: { fontSize: 17, fontWeight: '900', color: '#0f172a', maxWidth: '100%' },
  headerSubtitle: { marginTop: 2, fontSize: 12, fontWeight: '700', color: '#94a3b8' },
  headerSpacer: { width: 44 },
  centerState: { flex: 1, alignItems: 'center', justifyContent: 'center' },
  messageList: {
    flexGrow: 1,
    paddingHorizontal: 12,
    paddingTop: 18,
    paddingBottom: 14,
    justifyContent: 'flex-end',
  },
  messageRow: {
    flexDirection: 'row',
    marginVertical: 5,
    maxWidth: '86%',
  },
  messageRowMine: { alignSelf: 'flex-end', justifyContent: 'flex-end' },
  messageRowOther: { alignSelf: 'flex-start' },
  messageAvatar: {
    width: 30,
    height: 30,
    borderRadius: 15,
    backgroundColor: '#dbeafe',
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 7,
    marginTop: 18,
  },
  messageAvatarText: { fontSize: 12, fontWeight: '900', color: colors.primary },
  messageBlock: { minWidth: 0, flexShrink: 1 },
  messageAuthor: { marginLeft: 4, marginBottom: 3, fontSize: 12, fontWeight: '800', color: '#64748b' },
  bubble: {
    borderRadius: 16,
    paddingHorizontal: 12,
    paddingVertical: 9,
    minWidth: 54,
  },
  bubbleMine: {
    backgroundColor: colors.primary,
    borderBottomRightRadius: 4,
  },
  bubbleOther: {
    backgroundColor: '#fff',
    borderBottomLeftRadius: 4,
    borderWidth: 1,
    borderColor: '#e2e8f0',
  },
  messageText: { fontSize: 15, lineHeight: 21 },
  messageTextMine: { color: '#fff' },
  messageTextOther: { color: '#0f172a' },
  messageTime: { alignSelf: 'flex-end', marginTop: 4, fontSize: 10, fontWeight: '700' },
  messageTimeMine: { color: 'rgba(255,255,255,0.75)' },
  messageTimeOther: { color: '#94a3b8' },
  emptyState: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    paddingHorizontal: 32,
    transform: [{ scaleY: -1 }],
  },
  emptyTitle: { marginTop: 12, fontSize: 16, fontWeight: '900', color: '#0f172a' },
  emptyText: { marginTop: 6, fontSize: 14, color: '#64748b', textAlign: 'center', lineHeight: 20 },
  loadingMore: { paddingVertical: 12, transform: [{ scaleY: -1 }] },
  composerWrap: {
    flexDirection: 'row',
    alignItems: 'flex-end',
    gap: 10,
    paddingHorizontal: 12,
    paddingTop: 10,
    backgroundColor: '#fff',
    borderTopWidth: 1,
    borderTopColor: '#e2e8f0',
  },
  composerInput: {
    flex: 1,
    maxHeight: 112,
    minHeight: 44,
    borderRadius: 22,
    backgroundColor: '#f1f5f9',
    paddingHorizontal: 15,
    paddingTop: 11,
    paddingBottom: 10,
    fontSize: 15,
    color: '#0f172a',
  },
  sendButton: {
    width: 44,
    height: 44,
    borderRadius: 22,
    backgroundColor: colors.primary,
    alignItems: 'center',
    justifyContent: 'center',
  },
  sendButtonDisabled: { opacity: 0.55 },
});
