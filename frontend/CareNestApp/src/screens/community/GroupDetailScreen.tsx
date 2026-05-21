import React, { useCallback, useEffect, useMemo, useState } from 'react';
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
import {
  createGroupPost,
  getCommunityGroupPreview,
  getGroupPosts,
  kickCommunityMember,
  leaveCommunityGroup,
  reportGroupPost,
  type CommunityGroupPreview,
  type GroupPost,
} from '../../api/community';
import { useAuth } from '../../context/AuthContext';
import { colors } from '../../theme/colors';

const USER_SLOW_MODE_SECONDS = 5;

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

function DoctorBadge() {
  return (
    <View style={styles.doctorBadge}>
      <MaterialCommunityIcons name="check-decagram" size={12} color="#fff" />
      <Text style={styles.doctorBadgeText}>Bác sĩ</Text>
    </View>
  );
}

function MessageBubble({
  message,
  isMine,
  onLongPress,
}: {
  message: GroupPost;
  isMine: boolean;
  onLongPress: () => void;
}) {
  const isDoctor = message.authorRole === 'DOCTOR';
  const authorName = message.authorName || 'Thành viên';

  return (
    <TouchableOpacity
      activeOpacity={0.92}
      onLongPress={onLongPress}
      delayLongPress={280}
      style={[styles.messageRow, isMine ? styles.messageRowMine : styles.messageRowOther]}
    >
      {!isMine ? (
        <View style={[styles.messageAvatar, isDoctor && styles.doctorAvatar]}>
          <Text style={[styles.messageAvatarText, isDoctor && styles.doctorAvatarText]}>{getInitial(authorName)}</Text>
        </View>
      ) : null}

      <View style={[styles.messageBlock, isMine && styles.messageBlockMine]}>
        {!isMine ? (
          <View style={styles.authorLine}>
            <Text style={styles.messageAuthor} numberOfLines={1}>{authorName}</Text>
            {isDoctor ? <DoctorBadge /> : null}
          </View>
        ) : null}

        <View style={[
          styles.bubble,
          isMine ? styles.bubbleMine : isDoctor ? styles.bubbleDoctor : styles.bubbleOther,
        ]}>
          {message.replyToPostId ? (
            <View style={[styles.replyStub, isMine && styles.replyStubMine]}>
              <Text style={[styles.replyText, isMine && styles.replyTextMine]}>Đang trả lời một tin nhắn</Text>
            </View>
          ) : null}
          <Text style={[
            styles.messageText,
            isMine ? styles.messageTextMine : styles.messageTextOther,
          ]}>
            {message.content}
          </Text>
          <Text style={[styles.messageTime, isMine ? styles.messageTimeMine : styles.messageTimeOther]}>
            {formatMessageTime(message.createdAt)}
          </Text>
        </View>
      </View>
    </TouchableOpacity>
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
  const bypassSlowMode = user?.role === 'DOCTOR' || user?.role === 'ADMIN';

  const [preview, setPreview] = useState<CommunityGroupPreview | null>(null);
  const [messages, setMessages] = useState<GroupPost[]>([]);
  const [draft, setDraft] = useState('');
  const [loading, setLoading] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);
  const [currentPage, setCurrentPage] = useState(0);
  const [hasMore, setHasMore] = useState(false);
  const [sending, setSending] = useState(false);
  const [slowCountdown, setSlowCountdown] = useState(0);

  const isHost = preview?.myRole === 'HOST' || user?.role === 'ADMIN';

  const canSend = useMemo(
    () => draft.trim().length > 0 && !sending && slowCountdown === 0,
    [draft, sending, slowCountdown],
  );

  const loadPreview = useCallback(async () => {
    if (!Number.isFinite(groupId)) {
      return;
    }
    try {
      setPreview(await getCommunityGroupPreview(groupId));
    } catch {
      setPreview(null);
    }
  }, [groupId]);

  const loadMessages = useCallback(async (page = 0) => {
    if (!Number.isFinite(groupId)) {
      setMessages([]);
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
      setMessages(current => page === 0 ? result.content : [...current, ...result.content]);
      setCurrentPage(result.page);
      setHasMore(!result.last);
    } catch (error) {
      if (page === 0) {
        setMessages([]);
      }
      Alert.alert('Không thể tải tin nhắn', error instanceof Error ? error.message : 'Đã có lỗi xảy ra');
    } finally {
      setLoading(false);
      setLoadingMore(false);
    }
  }, [groupId]);

  useFocusEffect(useCallback(() => {
    void loadPreview();
    void loadMessages();
  }, [loadMessages, loadPreview]));

  useEffect(() => {
    if (slowCountdown <= 0) {
      return undefined;
    }

    const timer = setTimeout(() => {
      setSlowCountdown(current => Math.max(0, current - 1));
    }, 1000);
    return () => clearTimeout(timer);
  }, [slowCountdown]);

  const handleLoadMore = useCallback(() => {
    if (!hasMore || loadingMore || loading) {
      return;
    }
    void loadMessages(currentPage + 1);
  }, [currentPage, hasMore, loadMessages, loading, loadingMore]);

  const handleSend = async () => {
    const content = draft.trim();
    if (!content || !canSend || !Number.isFinite(groupId)) {
      return;
    }

    try {
      setSending(true);
      setDraft('');
      const created = await createGroupPost(groupId, content);
      setMessages(current => [created, ...current]);
      if (!bypassSlowMode) {
        setSlowCountdown(USER_SLOW_MODE_SECONDS);
      }
    } catch (error) {
      setDraft(content);
      Alert.alert('Không thể gửi tin nhắn', error instanceof Error ? error.message : 'Đã có lỗi xảy ra');
    } finally {
      setSending(false);
    }
  };

  const handleLeave = () => {
    Alert.alert('Rời nhóm', 'Bạn có chắc chắn muốn rời khỏi nhóm này không?', [
      { text: 'Hủy', style: 'cancel' },
      {
        text: 'Rời nhóm',
        style: 'destructive',
        onPress: async () => {
          try {
            await leaveCommunityGroup(groupId);
            navigation.goBack();
          } catch (error) {
            Alert.alert('Không thể rời nhóm', error instanceof Error ? error.message : 'Đã có lỗi xảy ra');
          }
        },
      },
    ]);
  };

  const openHeaderMenu = () => {
    Alert.alert('Tùy chọn nhóm', groupName, [
      { text: 'Tắt thông báo', onPress: () => Alert.alert('Đã tắt thông báo', 'Bạn sẽ không nhận thông báo mới từ nhóm này trong phiên bản hiện tại.') },
      { text: 'Rời nhóm', style: 'destructive', onPress: handleLeave },
      { text: 'Đóng', style: 'cancel' },
    ]);
  };

  const handleReport = async (message: GroupPost) => {
    try {
      await reportGroupPost(message.id, 'Nội dung không phù hợp hoặc có dấu hiệu vi phạm nội quy cộng đồng');
      Alert.alert('Đã gửi báo cáo', 'CareNest sẽ xem xét nội dung này trong thời gian sớm nhất.');
    } catch (error) {
      Alert.alert('Không thể báo cáo', error instanceof Error ? error.message : 'Đã có lỗi xảy ra');
    }
  };

  const handleKick = async (message: GroupPost) => {
    if (!message.authorId) {
      return;
    }
    Alert.alert('Mời ra khỏi nhóm', `Bạn có chắc chắn muốn mời ${message.authorName || 'thành viên này'} ra khỏi nhóm?`, [
      { text: 'Hủy', style: 'cancel' },
      {
        text: 'Mời ra khỏi nhóm',
        style: 'destructive',
        onPress: async () => {
          try {
            await kickCommunityMember(groupId, message.authorId!);
            Alert.alert('Đã cập nhật', 'Thành viên đã được mời ra khỏi nhóm.');
          } catch (error) {
            Alert.alert('Không thể mời ra khỏi nhóm', error instanceof Error ? error.message : 'Đã có lỗi xảy ra');
          }
        },
      },
    ]);
  };

  const openMessageMenu = (message: GroupPost) => {
    const isMine = currentUserId !== null && message.authorId === currentUserId;
    const options = [];

    if (!isMine) {
      options.push({ text: 'Báo cáo', onPress: () => void handleReport(message) });
    }

    if (!isMine && isHost && message.authorId) {
      options.push({ text: 'Mời ra khỏi nhóm', style: 'destructive' as const, onPress: () => void handleKick(message) });
    }

    options.push({ text: 'Đóng', style: 'cancel' as const });

    if (options.length > 1) {
      Alert.alert('Tùy chọn tin nhắn', message.authorName || 'Thành viên', options);
    }
  };

  return (
    <KeyboardAvoidingView
      style={styles.root}
      behavior={Platform.OS === 'ios' ? 'padding' : undefined}
      keyboardVerticalOffset={Platform.OS === 'ios' ? 8 : 0}
    >
      <View style={[styles.header, { paddingTop: insets.top }]}>
        <TouchableOpacity style={styles.iconButton} onPress={() => navigation.goBack()} hitSlop={10}>
          <MaterialCommunityIcons name="arrow-left" size={24} color="#0f172a" />
        </TouchableOpacity>
        <View style={styles.headerTitleWrap}>
          <Text style={styles.headerTitle} numberOfLines={1}>{groupName}</Text>
          <Text style={styles.headerSubtitle}>
            {preview?.memberCount ? `${preview.memberCount} thành viên` : 'Phòng trò chuyện cộng đồng'}
          </Text>
        </View>
        <TouchableOpacity style={styles.iconButton} onPress={openHeaderMenu} hitSlop={10}>
          <MaterialCommunityIcons name="dots-vertical" size={24} color="#0f172a" />
        </TouchableOpacity>
      </View>

      <View style={styles.medicalDisclaimer}>
        <MaterialCommunityIcons name="shield-alert-outline" size={18} color="#b45309" />
        <Text style={styles.disclaimerText}>
          Nội dung trong phòng chat chỉ mang tính tham khảo, không thay thế tư vấn, chẩn đoán hoặc điều trị y khoa trực tiếp.
        </Text>
      </View>

      {loading ? (
        <View style={styles.centerState}>
          <ActivityIndicator color={colors.primary} size="large" />
        </View>
      ) : (
        <FlatList
          data={messages}
          keyExtractor={item => String(item.id)}
          renderItem={({ item }) => (
            <MessageBubble
              message={item}
              isMine={currentUserId !== null && item.authorId === currentUserId}
              onLongPress={() => openMessageMenu(item)}
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
              <Text style={styles.emptyText}>Hãy bắt đầu cuộc trò chuyện đầu tiên trong nhóm.</Text>
            </View>
          }
        />
      )}

      <View style={[styles.composerWrap, { paddingBottom: insets.bottom + 10 }]}>
        <TextInput
          style={styles.composerInput}
          value={draft}
          onChangeText={setDraft}
          placeholder={slowCountdown > 0 ? `Chờ ${slowCountdown}s để gửi tiếp...` : 'Nhập tin nhắn...'}
          placeholderTextColor="#94a3b8"
          multiline
        />
        <TouchableOpacity
          style={[styles.sendButton, !canSend && styles.sendButtonDisabled]}
          disabled={!canSend}
          onPress={() => void handleSend()}
          activeOpacity={0.82}
        >
          {sending ? (
            <ActivityIndicator size="small" color="#fff" />
          ) : slowCountdown > 0 ? (
            <Text style={styles.countdownText}>{slowCountdown}</Text>
          ) : (
            <MaterialCommunityIcons name="send" size={22} color="#fff" />
          )}
        </TouchableOpacity>
      </View>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  root: { flex: 1, backgroundColor: '#eef4f8' },
  header: {
    minHeight: 62,
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 12,
    paddingBottom: 8,
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
  medicalDisclaimer: {
    flexDirection: 'row',
    alignItems: 'flex-start',
    gap: 8,
    paddingHorizontal: 12,
    paddingVertical: 9,
    backgroundColor: '#fffbeb',
    borderBottomWidth: 1,
    borderBottomColor: '#fde68a',
  },
  disclaimerText: { flex: 1, fontSize: 12, fontWeight: '700', lineHeight: 17, color: '#92400e' },
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
    maxWidth: '88%',
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
    marginTop: 22,
  },
  doctorAvatar: { backgroundColor: '#e0f2fe' },
  messageAvatarText: { fontSize: 12, fontWeight: '900', color: colors.primary },
  doctorAvatarText: { color: '#0369a1' },
  messageBlock: { minWidth: 0, flexShrink: 1 },
  messageBlockMine: { alignItems: 'flex-end' },
  authorLine: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
    marginLeft: 4,
    marginBottom: 4,
    maxWidth: '100%',
  },
  messageAuthor: { flexShrink: 1, fontSize: 12, fontWeight: '800', color: '#64748b' },
  doctorBadge: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 3,
    borderRadius: 999,
    paddingHorizontal: 6,
    paddingVertical: 2,
    backgroundColor: '#0ea5e9',
  },
  doctorBadgeText: { fontSize: 10, fontWeight: '900', color: '#fff' },
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
  bubbleDoctor: {
    backgroundColor: '#ecfeff',
    borderBottomLeftRadius: 4,
    borderWidth: 1,
    borderColor: '#bae6fd',
  },
  replyStub: {
    borderLeftWidth: 3,
    borderLeftColor: '#93c5fd',
    paddingLeft: 7,
    marginBottom: 6,
  },
  replyStubMine: { borderLeftColor: 'rgba(255,255,255,0.7)' },
  replyText: { fontSize: 11, fontWeight: '800', color: '#0369a1' },
  replyTextMine: { color: 'rgba(255,255,255,0.82)' },
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
  },
  emptyTitle: { marginTop: 12, fontSize: 16, fontWeight: '900', color: '#0f172a' },
  emptyText: { marginTop: 6, fontSize: 14, color: '#64748b', textAlign: 'center', lineHeight: 20 },
  loadingMore: { paddingVertical: 12 },
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
    textAlignVertical: 'top',
  },
  sendButton: {
    width: 44,
    height: 44,
    borderRadius: 22,
    backgroundColor: colors.primary,
    alignItems: 'center',
    justifyContent: 'center',
  },
  sendButtonDisabled: { opacity: 0.45 },
  countdownText: { color: '#fff', fontSize: 14, fontWeight: '900' },
});
