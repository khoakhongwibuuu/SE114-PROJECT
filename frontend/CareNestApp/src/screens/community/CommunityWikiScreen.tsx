import React, { useCallback, useMemo, useState } from 'react';
import {
  ActivityIndicator,
  Alert,
  FlatList,
  RefreshControl,
  StyleSheet,
  Text,
  TouchableOpacity,
  View,
} from 'react-native';
import { useFocusEffect, useNavigation } from '@react-navigation/native';
import type { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import MaterialCommunityIcons from 'react-native-vector-icons/MaterialCommunityIcons';
import FAB from '../../components/common/FAB';
import { getArticles, type Article } from '../../api/community';
import { useAuth } from '../../context/AuthContext';
import type { CommunityStackParamList } from '../../navigation/navigationTypes';
import { colors } from '../../theme/colors';
import { shadows } from '../../theme/spacing';

type Nav = NativeStackNavigationProp<CommunityStackParamList>;

function parseTags(tags?: string | null): string[] {
  if (!tags) {
    return ['Kien thuc'];
  }
  const values = tags.split(',').map(tag => tag.trim()).filter(Boolean);
  return values.length ? values.slice(0, 4) : ['Kien thuc'];
}

function formatTime(value?: string | null): string {
  if (!value) {
    return 'Vua dang';
  }
  const created = new Date(value);
  if (Number.isNaN(created.getTime())) {
    return 'Vua dang';
  }
  const diffMinutes = Math.max(0, Math.floor((Date.now() - created.getTime()) / 60000));
  if (diffMinutes < 1) return 'Vua xong';
  if (diffMinutes < 60) return `${diffMinutes} phut`;
  const diffHours = Math.floor(diffMinutes / 60);
  if (diffHours < 24) return `${diffHours} gio`;
  return created.toLocaleDateString('vi-VN');
}

function getInitial(name?: string | null): string {
  return (name || 'C').trim().charAt(0).toUpperCase();
}

function ArticleCard({ item }: { item: Article }) {
  const authorName = item.authorName || 'CareNest Doctor';
  const tags = parseTags(item.tags);

  const handleInteraction = (label: string) => {
    Alert.alert(label, 'Tinh nang nay se duoc ket noi o phien ban tiep theo.');
  };

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

      <View style={styles.tagRow}>
        {tags.map(tag => (
          <View key={tag} style={styles.tagChip}>
            <Text style={styles.tagText}>#{tag}</Text>
          </View>
        ))}
      </View>

      <View style={styles.divider} />

      <View style={styles.actionRow}>
        <TouchableOpacity
          style={styles.actionButton}
          activeOpacity={0.75}
          onPress={() => handleInteraction('Thich')}
        >
          <MaterialCommunityIcons name="heart-outline" size={20} color="#64748b" />
          <Text style={styles.actionText}>Thich</Text>
        </TouchableOpacity>
        <TouchableOpacity
          style={styles.actionButton}
          activeOpacity={0.75}
          onPress={() => handleInteraction('Binh luan')}
        >
          <MaterialCommunityIcons name="message-outline" size={20} color="#64748b" />
          <Text style={styles.actionText}>Binh luan</Text>
        </TouchableOpacity>
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
    } catch {
      setArticles([]);
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, []);

  useFocusEffect(useCallback(() => {
    void loadArticles();
  }, [loadArticles]));

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
        renderItem={({ item }) => <ArticleCard item={item} />}
        contentContainerStyle={[styles.listContent, { paddingBottom: insets.bottom + 108 }]}
        refreshControl={
          <RefreshControl refreshing={refreshing} onRefresh={() => void loadArticles(true)} />
        }
        ListHeaderComponent={
          <View style={styles.header}>
            <Text style={styles.title}>Cam nang suc khoe</Text>
            <Text style={styles.subtitle}>Bai viet chuyen mon tu bac si va cong dong CareNest.</Text>
          </View>
        }
        ListEmptyComponent={
          <View style={styles.emptyCard}>
            <MaterialCommunityIcons name="file-document-outline" size={40} color="#94a3b8" />
            <Text style={styles.emptyTitle}>Chua co bai viet</Text>
            <Text style={styles.emptyText}>Noi dung wiki se hien thi tai day khi bac si dang bai.</Text>
          </View>
        }
      />
      {canCreateArticle ? (
        <FAB iconName="add" onPress={() => navigation.navigate('CreateArticle')} />
      ) : null}
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
  divider: { height: 1, backgroundColor: '#e2e8f0', marginTop: 14 },
  actionRow: { flexDirection: 'row', height: 46 },
  actionButton: {
    flex: 1,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 7,
  },
  actionText: { fontSize: 14, fontWeight: '800', color: '#64748b' },
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
});
