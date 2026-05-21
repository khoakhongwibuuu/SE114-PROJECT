import React, { useCallback, useMemo, useState } from 'react';
import {
  ActivityIndicator,
  Alert,
  FlatList,
  Image,
  KeyboardAvoidingView,
  Modal,
  Platform,
  RefreshControl,
  StyleSheet,
  Text,
  TextInput,
  TouchableOpacity,
  View,
} from 'react-native';
import { useFocusEffect, useNavigation } from '@react-navigation/native';
import type { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import MaterialCommunityIcons from 'react-native-vector-icons/MaterialCommunityIcons';
import FAB from '../../components/common/FAB';
import RoleGuard from '../../components/common/RoleGuard';
import {
  createArticleComment,
  getArticleComments,
  getArticles,
  toggleArticleLike,
  type Article,
  type ArticleComment,
} from '../../api/community';
import { useAuth } from '../../context/AuthContext';
import type { CommunityStackParamList } from '../../navigation/navigationTypes';
import { colors } from '../../theme/colors';
import { shadows } from '../../theme/spacing';

type Nav = NativeStackNavigationProp<CommunityStackParamList>;

function parseTags(tags?: string | null): string[] {
  if (!tags) {
    return ['Kiến thức'];
  }
  const values = tags.split(',').map(tag => tag.trim()).filter(Boolean);
  return values.length ? values.slice(0, 4) : ['Kiến thức'];
}

function formatTime(value?: string | null): string {
  if (!value) {
    return 'Vừa đăng';
  }
  const created = new Date(value);
  if (Number.isNaN(created.getTime())) {
    return 'Vừa đăng';
  }
  const diffMinutes = Math.max(0, Math.floor((Date.now() - created.getTime()) / 60000));
  if (diffMinutes < 1) return 'Vừa xong';
  if (diffMinutes < 60) return `${diffMinutes} phút`;
  const diffHours = Math.floor(diffMinutes / 60);
  if (diffHours < 24) return `${diffHours} giờ`;
  return created.toLocaleDateString('vi-VN');
}

function getInitial(name?: string | null): string {
  return (name || 'C').trim().charAt(0).toUpperCase();
}

function formatCount(value?: number): string {
  const count = value || 0;
  return count > 0 ? String(count) : '';
}

function ArticleCard({
  item,
  onToggleLike,
  onOpenComments,
}: {
  item: Article;
  onToggleLike: (article: Article) => void;
  onOpenComments: (article: Article) => void;
}) {
  const authorName = item.authorName || 'CareNest Doctor';
  const tags = parseTags(item.tags);
  const liked = Boolean(item.likedByMe);

  return (
    <View style={styles.feedCard}>
      <View style={styles.postHeader}>
        <View style={styles.avatar}>
          <Text style={styles.avatarText}>{getInitial(authorName)}</Text>
        </View>
        <View style={styles.authorMeta}>
          <Text style={styles.authorName} numberOfLines={1}>{authorName}</Text>
          <Text style={styles.postTime}>{formatTime(item.createdAt)}</Text>
        </View>
      </View>

      <Text style={styles.articleTitle} numberOfLines={2}>{item.title}</Text>
      <Text style={styles.articleContent} numberOfLines={3}>{item.content}</Text>

      {item.imageUrl ? (
        <Image source={{ uri: item.imageUrl }} style={styles.articleImage} resizeMode="cover" />
      ) : null}

      <View style={styles.tagRow}>
        {tags.map(tag => (
          <View key={tag} style={styles.tagChip}>
            <Text style={styles.tagText}>#{tag}</Text>
          </View>
        ))}
      </View>

      <View style={styles.metricRow}>
        <Text style={styles.metricText}>
          {item.likeCount ? `${item.likeCount} lượt thích` : 'Chưa có lượt thích'}
        </Text>
        <Text style={styles.metricText}>
          {item.commentCount ? `${item.commentCount} bình luận` : 'Chưa có bình luận'}
        </Text>
      </View>

      <View style={styles.divider} />

      <View style={styles.actionRow}>
        <TouchableOpacity
          style={styles.actionButton}
          activeOpacity={0.75}
          onPress={() => onToggleLike(item)}
        >
          <MaterialCommunityIcons
            name={liked ? 'heart' : 'heart-outline'}
            size={20}
            color={liked ? '#ef4444' : '#64748b'}
          />
          <Text style={[styles.actionText, liked && styles.likedActionText]}>
            Thích {formatCount(item.likeCount)}
          </Text>
        </TouchableOpacity>
        <TouchableOpacity
          style={styles.actionButton}
          activeOpacity={0.75}
          onPress={() => onOpenComments(item)}
        >
          <MaterialCommunityIcons name="message-outline" size={20} color="#64748b" />
          <Text style={styles.actionText}>Bình luận {formatCount(item.commentCount)}</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
}

function CommentItem({ item }: { item: ArticleComment }) {
  const authorName = item.authorName || 'Người dùng CareNest';
  return (
    <View style={styles.commentItem}>
      <View style={styles.commentAvatar}>
        <Text style={styles.commentAvatarText}>{getInitial(authorName)}</Text>
      </View>
      <View style={styles.commentBubble}>
        <View style={styles.commentHeader}>
          <Text style={styles.commentAuthor} numberOfLines={1}>{authorName}</Text>
          <Text style={styles.commentTime}>{formatTime(item.createdAt)}</Text>
        </View>
        <Text style={styles.commentContent}>{item.content}</Text>
      </View>
    </View>
  );
}

export default function CommunityWikiScreen() {
  const insets = useSafeAreaInsets();
  const navigation = useNavigation<Nav>();
  const { user } = useAuth();
  const [articles, setArticles] = useState<Article[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [selectedArticle, setSelectedArticle] = useState<Article | null>(null);
  const [comments, setComments] = useState<ArticleComment[]>([]);
  const [commentsLoading, setCommentsLoading] = useState(false);
  const [commentText, setCommentText] = useState('');
  const [sendingComment, setSendingComment] = useState(false);
  const [likingIds, setLikingIds] = useState<Set<number>>(() => new Set());

  const canCreateArticle = useMemo(
    () => user?.role === 'DOCTOR' || user?.role === 'ADMIN',
    [user?.role],
  );

  const loadArticles = useCallback(async (asRefresh = false) => {
    try {
      if (asRefresh) {
        setRefreshing(true);
      } else {
        setLoading(true);
      }
      setArticles(await getArticles());
    } catch (error) {
      Alert.alert('Không thể tải bài viết', error instanceof Error ? error.message : 'Đã có lỗi xảy ra');
      setArticles([]);
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, []);

  useFocusEffect(useCallback(() => {
    void loadArticles();
  }, [loadArticles]));

  const updateArticle = useCallback((articleId: number, patch: Partial<Article>) => {
    setArticles(current => current.map(article =>
      article.id === articleId ? { ...article, ...patch } : article,
    ));
    setSelectedArticle(current =>
      current?.id === articleId ? { ...current, ...patch } : current,
    );
  }, []);

  const handleToggleLike = useCallback(async (article: Article) => {
    if (likingIds.has(article.id)) {
      return;
    }

    const nextLiked = !article.likedByMe;
    const nextCount = Math.max(0, (article.likeCount || 0) + (nextLiked ? 1 : -1));
    updateArticle(article.id, { likedByMe: nextLiked, likeCount: nextCount });
    setLikingIds(current => new Set(current).add(article.id));

    try {
      const result = await toggleArticleLike(article.id);
      updateArticle(article.id, {
        likedByMe: result.likedByMe,
        likeCount: result.likeCount,
      });
    } catch (error) {
      updateArticle(article.id, {
        likedByMe: article.likedByMe,
        likeCount: article.likeCount,
      });
      Alert.alert('Không thể cập nhật lượt thích', error instanceof Error ? error.message : 'Đã có lỗi xảy ra');
    } finally {
      setLikingIds(current => {
        const next = new Set(current);
        next.delete(article.id);
        return next;
      });
    }
  }, [likingIds, updateArticle]);

  const handleOpenComments = useCallback(async (article: Article) => {
    setSelectedArticle(article);
    setComments([]);
    setCommentText('');
    setCommentsLoading(true);
    try {
      setComments(await getArticleComments(article.id));
    } catch (error) {
      Alert.alert('Không thể tải bình luận', error instanceof Error ? error.message : 'Đã có lỗi xảy ra');
    } finally {
      setCommentsLoading(false);
    }
  }, []);

  const handleSendComment = useCallback(async () => {
    const content = commentText.trim();
    if (!selectedArticle || !content || sendingComment) {
      return;
    }

    try {
      setSendingComment(true);
      const comment = await createArticleComment(selectedArticle.id, content);
      setComments(current => [...current, comment]);
      setCommentText('');
      updateArticle(selectedArticle.id, {
        commentCount: (selectedArticle.commentCount || 0) + 1,
      });
    } catch (error) {
      Alert.alert('Không thể gửi bình luận', error instanceof Error ? error.message : 'Đã có lỗi xảy ra');
    } finally {
      setSendingComment(false);
    }
  }, [commentText, selectedArticle, sendingComment, updateArticle]);

  if (loading) {
    return (
      <View style={styles.centerState}>
        <ActivityIndicator size="large" color={colors.primary} />
      </View>
    );
  }

  return (
    <View style={styles.root}>
      <FlatList
        data={articles}
        keyExtractor={item => String(item.id)}
        renderItem={({ item }) => (
          <ArticleCard
            item={item}
            onToggleLike={handleToggleLike}
            onOpenComments={handleOpenComments}
          />
        )}
        contentContainerStyle={[styles.listContent, { paddingBottom: insets.bottom + 108 }]}
        refreshControl={
          <RefreshControl refreshing={refreshing} onRefresh={() => void loadArticles(true)} />
        }
        ListHeaderComponent={
          <View style={styles.header}>
            <Text style={styles.title}>Cẩm nang sức khỏe</Text>
            <Text style={styles.subtitle}>Bài viết chuyên môn từ bác sĩ và cộng đồng CareNest.</Text>
          </View>
        }
        ListEmptyComponent={
          <View style={styles.emptyCard}>
            <MaterialCommunityIcons name="file-document-outline" size={40} color="#94a3b8" />
            <Text style={styles.emptyTitle}>Chưa có bài viết</Text>
            <Text style={styles.emptyText}>Nội dung wiki sẽ hiển thị tại đây khi bác sĩ đăng bài.</Text>
          </View>
        }
      />

      <Modal
        visible={Boolean(selectedArticle)}
        transparent
        animationType="slide"
        onRequestClose={() => setSelectedArticle(null)}
      >
        <View style={styles.modalBackdrop}>
          <KeyboardAvoidingView
            behavior={Platform.OS === 'ios' ? 'padding' : undefined}
            style={[styles.commentSheet, { paddingBottom: insets.bottom + 12 }]}
          >
            <View style={styles.commentSheetHeader}>
              <Text style={styles.commentSheetTitle}>Bình luận</Text>
              <TouchableOpacity
                style={styles.closeButton}
                onPress={() => setSelectedArticle(null)}
                hitSlop={10}
              >
                <MaterialCommunityIcons name="close" size={22} color="#0f172a" />
              </TouchableOpacity>
            </View>
            <Text style={styles.commentArticleTitle} numberOfLines={2}>
              {selectedArticle?.title}
            </Text>

            {commentsLoading ? (
              <View style={styles.commentLoading}>
                <ActivityIndicator color={colors.primary} />
              </View>
            ) : (
              <FlatList
                data={comments}
                keyExtractor={item => String(item.id)}
                renderItem={({ item }) => <CommentItem item={item} />}
                contentContainerStyle={styles.commentList}
                ListEmptyComponent={
                  <View style={styles.commentEmpty}>
                    <Text style={styles.commentEmptyTitle}>Chưa có bình luận</Text>
                    <Text style={styles.commentEmptyText}>Hãy là người đầu tiên đặt câu hỏi hoặc chia sẻ thêm.</Text>
                  </View>
                }
              />
            )}

            <View style={styles.commentComposer}>
              <TextInput
                style={styles.commentInput}
                value={commentText}
                onChangeText={setCommentText}
                placeholder="Viết bình luận..."
                placeholderTextColor="#94a3b8"
                multiline
              />
              <TouchableOpacity
                style={[styles.sendButton, (!commentText.trim() || sendingComment) && styles.sendButtonDisabled]}
                disabled={!commentText.trim() || sendingComment}
                onPress={() => void handleSendComment()}
                activeOpacity={0.85}
              >
                {sendingComment ? (
                  <ActivityIndicator color="#fff" size="small" />
                ) : (
                  <MaterialCommunityIcons name="send" size={20} color="#fff" />
                )}
              </TouchableOpacity>
            </View>
          </KeyboardAvoidingView>
        </View>
      </Modal>

      <RoleGuard allowedRoles={['DOCTOR', 'ADMIN']}>
        <FAB iconName="add" onPress={() => navigation.navigate('CreateArticle')} />
      </RoleGuard>
    </View>
  );
}

const styles = StyleSheet.create({
  root: { flex: 1, backgroundColor: '#f0f4f8' },
  centerState: { flex: 1, alignItems: 'center', justifyContent: 'center', backgroundColor: '#f0f4f8' },
  listContent: { paddingHorizontal: 12, paddingTop: 14 },
  header: { paddingHorizontal: 4, marginBottom: 12 },
  title: { fontSize: 24, fontWeight: '900', color: '#0f172a' },
  subtitle: { marginTop: 6, fontSize: 14, color: '#64748b', lineHeight: 20 },
  feedCard: {
    backgroundColor: '#fff',
    borderRadius: 8,
    paddingTop: 14,
    marginBottom: 12,
    borderWidth: 1,
    borderColor: '#e2e8f0',
    ...shadows.sm,
  },
  postHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 14,
  },
  avatar: {
    width: 42,
    height: 42,
    borderRadius: 21,
    backgroundColor: '#dbeafe',
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 10,
  },
  avatarText: { fontSize: 16, fontWeight: '900', color: colors.primary },
  authorMeta: { flex: 1, minWidth: 0 },
  authorName: { fontSize: 15, fontWeight: '900', color: '#0f172a' },
  postTime: { marginTop: 2, fontSize: 12, fontWeight: '600', color: '#94a3b8' },
  articleTitle: {
    marginTop: 14,
    paddingHorizontal: 14,
    fontSize: 19,
    fontWeight: '900',
    color: '#0f172a',
    lineHeight: 25,
  },
  articleContent: {
    marginTop: 8,
    paddingHorizontal: 14,
    fontSize: 14,
    color: '#475569',
    lineHeight: 21,
  },
  articleImage: {
    width: '100%',
    height: 210,
    marginTop: 12,
    backgroundColor: '#e2e8f0',
  },
  tagRow: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 8,
    paddingHorizontal: 14,
    marginTop: 12,
  },
  tagChip: {
    backgroundColor: '#f1f5f9',
    borderRadius: 8,
    paddingHorizontal: 10,
    paddingVertical: 5,
  },
  tagText: { fontSize: 12, fontWeight: '800', color: '#2563eb' },
  metricRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    gap: 12,
    paddingHorizontal: 14,
    marginTop: 12,
  },
  metricText: { flexShrink: 1, fontSize: 12, fontWeight: '700', color: '#64748b' },
  divider: { height: 1, backgroundColor: '#e2e8f0', marginTop: 12 },
  actionRow: { flexDirection: 'row', height: 46 },
  actionButton: {
    flex: 1,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 7,
  },
  actionText: { fontSize: 14, fontWeight: '800', color: '#64748b' },
  likedActionText: { color: '#ef4444' },
  emptyCard: {
    alignItems: 'center',
    backgroundColor: '#fff',
    borderRadius: 8,
    padding: 28,
    borderWidth: 1,
    borderColor: '#e2e8f0',
  },
  emptyTitle: { marginTop: 12, fontSize: 16, fontWeight: '900', color: '#0f172a' },
  emptyText: { marginTop: 6, fontSize: 14, color: '#64748b', textAlign: 'center', lineHeight: 20 },
  modalBackdrop: {
    flex: 1,
    backgroundColor: 'rgba(15, 23, 42, 0.48)',
    justifyContent: 'flex-end',
  },
  commentSheet: {
    maxHeight: '86%',
    minHeight: '62%',
    backgroundColor: '#fff',
    borderTopLeftRadius: 8,
    borderTopRightRadius: 8,
    paddingTop: 14,
  },
  commentSheetHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: 16,
  },
  commentSheetTitle: { fontSize: 18, fontWeight: '900', color: '#0f172a' },
  closeButton: {
    width: 34,
    height: 34,
    borderRadius: 17,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#f1f5f9',
  },
  commentArticleTitle: {
    paddingHorizontal: 16,
    marginTop: 8,
    fontSize: 13,
    fontWeight: '700',
    color: '#64748b',
    lineHeight: 18,
  },
  commentLoading: { minHeight: 180, alignItems: 'center', justifyContent: 'center' },
  commentList: { paddingHorizontal: 14, paddingTop: 14, paddingBottom: 12 },
  commentItem: { flexDirection: 'row', alignItems: 'flex-start', marginBottom: 12 },
  commentAvatar: {
    width: 34,
    height: 34,
    borderRadius: 17,
    backgroundColor: '#dbeafe',
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 9,
  },
  commentAvatarText: { fontSize: 13, fontWeight: '900', color: colors.primary },
  commentBubble: {
    flex: 1,
    minWidth: 0,
    backgroundColor: '#f8fafc',
    borderRadius: 8,
    paddingHorizontal: 11,
    paddingVertical: 9,
    borderWidth: 1,
    borderColor: '#e2e8f0',
  },
  commentHeader: { flexDirection: 'row', alignItems: 'center', gap: 8 },
  commentAuthor: { flex: 1, fontSize: 13, fontWeight: '900', color: '#0f172a' },
  commentTime: { fontSize: 11, fontWeight: '700', color: '#94a3b8' },
  commentContent: { marginTop: 4, fontSize: 14, color: '#334155', lineHeight: 20 },
  commentEmpty: { alignItems: 'center', paddingVertical: 34, paddingHorizontal: 24 },
  commentEmptyTitle: { fontSize: 15, fontWeight: '900', color: '#0f172a' },
  commentEmptyText: { marginTop: 6, fontSize: 13, color: '#64748b', textAlign: 'center', lineHeight: 19 },
  commentComposer: {
    flexDirection: 'row',
    alignItems: 'flex-end',
    gap: 9,
    paddingHorizontal: 14,
    paddingTop: 10,
    borderTopWidth: 1,
    borderTopColor: '#e2e8f0',
  },
  commentInput: {
    flex: 1,
    maxHeight: 104,
    minHeight: 42,
    borderRadius: 8,
    backgroundColor: '#f1f5f9',
    paddingHorizontal: 12,
    paddingVertical: 10,
    fontSize: 14,
    color: '#0f172a',
    textAlignVertical: 'top',
  },
  sendButton: {
    width: 42,
    height: 42,
    borderRadius: 21,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: colors.primary,
  },
  sendButtonDisabled: { opacity: 0.45 },
});
