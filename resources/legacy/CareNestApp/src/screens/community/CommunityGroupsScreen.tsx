import React, { useCallback, useEffect, useState } from 'react';
import {
  ActivityIndicator,
  Alert,
  FlatList,
  Modal,
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
import {
  getCommunityGroupPreview,
  getDiscoverCommunityGroups,
  getMyCommunityGroups,
  joinCommunityGroup,
  type CommunityGroup,
  type CommunityGroupPreview,
} from '../../api/community';
import type { CommunityStackParamList } from '../../navigation/navigationTypes';
import { colors } from '../../theme/colors';
import { shadows } from '../../theme/spacing';

type Nav = NativeStackNavigationProp<CommunityStackParamList>;
type GroupTab = 'mine' | 'discover';

function formatGroupTime(value?: string | null): string {
  if (!value) {
    return '';
  }
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return '';
  }
  const now = new Date();
  const sameDay = date.toDateString() === now.toDateString();
  return sameDay
    ? date.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' })
    : date.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit' });
}

function GroupAvatar({ group }: { group: CommunityGroup }) {
  return (
    <View style={[styles.groupAvatar, group.isPrivate && styles.doctorAvatar]}>
      <MaterialCommunityIcons name={group.isPrivate ? 'doctor' : 'account-group'} size={24} color={colors.primary} />
    </View>
  );
}

function MyGroupItem({ item, onPress }: { item: CommunityGroup; onPress: () => void }) {
  return (
    <TouchableOpacity style={styles.myItem} activeOpacity={0.82} onPress={onPress}>
      <GroupAvatar group={item} />
      <View style={styles.myContent}>
        <View style={styles.myTitleRow}>
          <Text style={styles.groupName} numberOfLines={1}>{item.name}</Text>
          <Text style={styles.timeText}>{formatGroupTime(item.latestActivityAt)}</Text>
        </View>
        <Text style={styles.latestMessage} numberOfLines={1}>
          {item.latestMessage || 'Nhóm vừa được tạo'}
        </Text>
      </View>
    </TouchableOpacity>
  );
}

function DiscoverGroupItem({
  item,
  joining,
  onPress,
  onJoin,
}: {
  item: CommunityGroup;
  joining: boolean;
  onPress: () => void;
  onJoin: () => void;
}) {
  return (
    <TouchableOpacity style={styles.discoverItem} activeOpacity={0.86} onPress={onPress}>
      <GroupAvatar group={item} />
      <View style={styles.discoverContent}>
        <Text style={styles.groupName} numberOfLines={1}>{item.name}</Text>
        <View style={styles.discoverMetaRow}>
          <MaterialCommunityIcons name="account-multiple-outline" size={14} color="#64748b" />
          <Text style={styles.discoverMeta}>{item.memberCount || 0} thành viên</Text>
          {item.leadDoctorName ? (
            <>
              <MaterialCommunityIcons name="check-decagram" size={14} color="#0ea5e9" />
              <Text style={styles.discoverMeta} numberOfLines={1}>{item.leadDoctorName}</Text>
            </>
          ) : null}
        </View>
        {item.category ? (
          <View style={styles.categoryChip}>
            <Text style={styles.categoryText}>{item.category}</Text>
          </View>
        ) : null}
      </View>
      <TouchableOpacity
        style={[styles.joinMiniButton, joining && styles.joinButtonDisabled]}
        disabled={joining}
        onPress={event => {
          event.stopPropagation();
          onJoin();
        }}
        activeOpacity={0.82}
      >
        {joining ? (
          <ActivityIndicator color="#fff" size="small" />
        ) : (
          <Text style={styles.joinMiniText}>Tham gia</Text>
        )}
      </TouchableOpacity>
    </TouchableOpacity>
  );
}

export default function CommunityGroupsScreen() {
  const insets = useSafeAreaInsets();
  const navigation = useNavigation<Nav>();
  const [activeTab, setActiveTab] = useState<GroupTab>('mine');
  const [myGroups, setMyGroups] = useState<CommunityGroup[]>([]);
  const [discoverGroups, setDiscoverGroups] = useState<CommunityGroup[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [search, setSearch] = useState('');
  const [debouncedSearch, setDebouncedSearch] = useState('');
  const [preview, setPreview] = useState<CommunityGroupPreview | null>(null);
  const [previewLoading, setPreviewLoading] = useState(false);
  const [joiningGroupId, setJoiningGroupId] = useState<number | null>(null);

  useEffect(() => {
    const timer = setTimeout(() => setDebouncedSearch(search.trim()), 300);
    return () => clearTimeout(timer);
  }, [search]);

  const loadGroups = useCallback(async (asRefresh = false) => {
    try {
      if (asRefresh) {
        setRefreshing(true);
      } else {
        setLoading(true);
      }
      const [mine, discover] = await Promise.all([
        getMyCommunityGroups(debouncedSearch),
        getDiscoverCommunityGroups(debouncedSearch),
      ]);
      setMyGroups(mine);
      setDiscoverGroups(discover);
    } catch {
      setMyGroups([]);
      setDiscoverGroups([]);
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, [debouncedSearch]);

  useFocusEffect(useCallback(() => {
    void loadGroups();
  }, [loadGroups]));

  const openPreview = async (group: CommunityGroup) => {
    try {
      setPreviewLoading(true);
      setPreview(await getCommunityGroupPreview(group.id));
    } catch {
      setPreview({
        ...group,
        memberCount: group.memberCount || 0,
        joined: Boolean(group.joined),
        rules: 'Không thể tải thông tin chi tiết. Vui lòng thử lại sau.',
      });
    } finally {
      setPreviewLoading(false);
    }
  };

  const enterGroup = (group: CommunityGroup | CommunityGroupPreview) => {
    setPreview(null);
    navigation.navigate('GroupDetail', {
      groupId: group.id,
      groupName: group.name,
    });
  };

  const handleJoin = async (group: CommunityGroup | CommunityGroupPreview, openAfterJoin = false) => {
    try {
      setJoiningGroupId(group.id);
      const joinedPreview = await joinCommunityGroup(group.id);
      const joinedGroup: CommunityGroup = {
        ...group,
        ...joinedPreview,
        joined: true,
        latestMessage: group.latestMessage || 'Nhóm vừa được tạo',
      };
      setDiscoverGroups(current => current.filter(item => item.id !== group.id));
      setMyGroups(current => [joinedGroup, ...current.filter(item => item.id !== group.id)]);
      setPreview(joinedPreview);
      if (openAfterJoin) {
        enterGroup(joinedPreview);
      }
    } catch (error) {
      Alert.alert('Không thể tham gia nhóm', error instanceof Error ? error.message : 'Đã có lỗi xảy ra');
    } finally {
      setJoiningGroupId(null);
    }
  };

  const currentData = activeTab === 'mine' ? myGroups : discoverGroups;

  const renderEmpty = () => (
    <View style={styles.emptyCard}>
      <MaterialCommunityIcons name={activeTab === 'mine' ? 'archive-outline' : 'magnify-close'} size={46} color="#94a3b8" />
      <Text style={styles.emptyTitle}>
        {activeTab === 'mine' ? 'Bạn chưa tham gia nhóm nào' : 'Không tìm thấy nhóm phù hợp'}
      </Text>
      <Text style={styles.emptyText}>
        {activeTab === 'mine'
          ? "Bạn chưa tham gia nhóm nào. Hãy chuyển sang tab 'Tất cả' để khám phá nhé!"
          : 'Hãy thử đổi từ khóa tìm kiếm hoặc quay lại sau.'}
      </Text>
    </View>
  );

  return (
    <View style={styles.root}>
      <View style={styles.fixedHeader}>
        <View style={styles.searchBox}>
          <MaterialCommunityIcons name="magnify" size={20} color="#64748b" />
          <TextInput
            style={styles.searchInput}
            value={search}
            onChangeText={setSearch}
            placeholder="Tìm nhóm, chuyên khoa hoặc bác sĩ..."
            placeholderTextColor="#94a3b8"
            returnKeyType="search"
          />
          {search ? (
            <TouchableOpacity onPress={() => setSearch('')} hitSlop={10}>
              <MaterialCommunityIcons name="close-circle" size={18} color="#94a3b8" />
            </TouchableOpacity>
          ) : null}
        </View>

        <View style={styles.tabBar}>
          <TouchableOpacity
            style={[styles.tabItem, activeTab === 'mine' && styles.tabItemActive]}
            onPress={() => setActiveTab('mine')}
            activeOpacity={0.84}
          >
            <Text style={[styles.tabText, activeTab === 'mine' && styles.tabTextActive]}>
              Nhóm của bạn
            </Text>
          </TouchableOpacity>
          <TouchableOpacity
            style={[styles.tabItem, activeTab === 'discover' && styles.tabItemActive]}
            onPress={() => setActiveTab('discover')}
            activeOpacity={0.84}
          >
            <Text style={[styles.tabText, activeTab === 'discover' && styles.tabTextActive]}>
              Tất cả
            </Text>
          </TouchableOpacity>
        </View>
      </View>

      {loading ? (
        <View style={styles.centerState}>
          <ActivityIndicator size="large" color={colors.primary} />
        </View>
      ) : (
        <FlatList
          data={currentData}
          keyExtractor={item => String(item.id)}
          renderItem={({ item }) => activeTab === 'mine' ? (
            <MyGroupItem item={item} onPress={() => enterGroup(item)} />
          ) : (
            <DiscoverGroupItem
              item={item}
              joining={joiningGroupId === item.id}
              onPress={() => void openPreview(item)}
              onJoin={() => void handleJoin(item)}
            />
          )}
          contentContainerStyle={[styles.listContent, { paddingBottom: insets.bottom + 92 }]}
          refreshControl={<RefreshControl refreshing={refreshing} onRefresh={() => void loadGroups(true)} />}
          ListEmptyComponent={renderEmpty}
        />
      )}

      <Modal visible={!!preview} transparent animationType="slide" onRequestClose={() => setPreview(null)}>
        <View style={styles.modalOverlay}>
          <View style={[styles.previewSheet, { paddingBottom: insets.bottom + 18 }]}>
            {previewLoading ? (
              <ActivityIndicator color={colors.primary} />
            ) : preview ? (
              <>
                <View style={styles.sheetHandle} />
                <View style={styles.previewHeader}>
                  <View style={styles.previewIcon}>
                    <MaterialCommunityIcons name={preview.isPrivate ? 'doctor' : 'account-group'} size={28} color={colors.primary} />
                  </View>
                  <TouchableOpacity style={styles.closeButton} onPress={() => setPreview(null)} hitSlop={10}>
                    <MaterialCommunityIcons name="close" size={22} color="#64748b" />
                  </TouchableOpacity>
                </View>
                <Text style={styles.previewTitle}>{preview.name}</Text>
                <Text style={styles.previewMeta}>
                  {preview.memberCount} thành viên{preview.leadDoctorName ? ` · Host: ${preview.leadDoctorName}` : ''}
                </Text>
                <Text style={styles.previewDescription}>
                  {preview.description || 'Không gian trao đổi kinh nghiệm chăm sóc sức khỏe trong cộng đồng CareNest.'}
                </Text>
                <View style={styles.rulesBox}>
                  <MaterialCommunityIcons name="shield-alert-outline" size={18} color="#b45309" />
                  <Text style={styles.rulesText}>{preview.rules}</Text>
                </View>
                <TouchableOpacity
                  style={[styles.joinButton, joiningGroupId === preview.id && styles.joinButtonDisabled]}
                  onPress={preview.joined ? () => enterGroup(preview) : () => void handleJoin(preview, true)}
                  disabled={joiningGroupId === preview.id}
                  activeOpacity={0.84}
                >
                  {joiningGroupId === preview.id ? (
                    <ActivityIndicator color="#fff" />
                  ) : (
                    <Text style={styles.joinButtonText}>{preview.joined ? 'Vào phòng chat' : 'Tham gia nhóm'}</Text>
                  )}
                </TouchableOpacity>
              </>
            ) : null}
          </View>
        </View>
      </Modal>
    </View>
  );
}

const styles = StyleSheet.create({
  root: { flex: 1, backgroundColor: '#f8fafc' },
  centerState: { flex: 1, alignItems: 'center', justifyContent: 'center', backgroundColor: '#f8fafc' },
  fixedHeader: {
    backgroundColor: '#fff',
    paddingHorizontal: 14,
    paddingTop: 12,
    borderBottomWidth: 1,
    borderBottomColor: '#e2e8f0',
  },
  searchBox: {
    minHeight: 44,
    flexDirection: 'row',
    alignItems: 'center',
    gap: 9,
    borderRadius: 8,
    backgroundColor: '#f8fafc',
    borderWidth: 1,
    borderColor: '#e2e8f0',
    paddingHorizontal: 12,
  },
  searchInput: { flex: 1, fontSize: 14, color: '#0f172a', paddingVertical: 10 },
  tabBar: { flexDirection: 'row', marginTop: 12 },
  tabItem: { flex: 1, alignItems: 'center', paddingBottom: 11, borderBottomWidth: 3, borderBottomColor: 'transparent' },
  tabItemActive: { borderBottomColor: colors.primary },
  tabText: { fontSize: 14, fontWeight: '800', color: '#64748b' },
  tabTextActive: { color: colors.primary },
  listContent: { paddingHorizontal: 14, paddingTop: 10 },
  myItem: {
    flexDirection: 'row',
    alignItems: 'center',
    minHeight: 72,
    backgroundColor: '#fff',
    borderRadius: 8,
    paddingHorizontal: 12,
    marginBottom: 8,
    borderWidth: 1,
    borderColor: '#eef2f7',
    ...shadows.sm,
  },
  discoverItem: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#fff',
    borderRadius: 8,
    padding: 12,
    marginBottom: 10,
    borderWidth: 1,
    borderColor: '#e2e8f0',
    ...shadows.sm,
  },
  groupAvatar: {
    width: 48,
    height: 48,
    borderRadius: 24,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#eff6ff',
    marginRight: 12,
  },
  doctorAvatar: { backgroundColor: '#e0f2fe' },
  myContent: { flex: 1, minWidth: 0 },
  discoverContent: { flex: 1, minWidth: 0 },
  myTitleRow: { flexDirection: 'row', alignItems: 'center', gap: 8 },
  groupName: { flex: 1, fontSize: 15, fontWeight: '900', color: '#0f172a' },
  timeText: { fontSize: 11, fontWeight: '700', color: '#94a3b8' },
  latestMessage: { marginTop: 5, fontSize: 13, color: '#64748b' },
  discoverMetaRow: { flexDirection: 'row', alignItems: 'center', gap: 4, marginTop: 5 },
  discoverMeta: { flexShrink: 1, fontSize: 12, fontWeight: '700', color: '#64748b' },
  categoryChip: { alignSelf: 'flex-start', marginTop: 8, borderRadius: 999, paddingHorizontal: 8, paddingVertical: 3, backgroundColor: '#eef6ff' },
  categoryText: { fontSize: 10, fontWeight: '900', color: colors.primary },
  joinMiniButton: {
    minWidth: 78,
    height: 34,
    borderRadius: 17,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: colors.primary,
    marginLeft: 10,
  },
  joinMiniText: { fontSize: 12, fontWeight: '900', color: '#fff' },
  joinButtonDisabled: { opacity: 0.6 },
  emptyCard: {
    alignItems: 'center',
    backgroundColor: '#fff',
    borderRadius: 8,
    padding: 28,
    borderWidth: 1,
    borderColor: '#e2e8f0',
  },
  emptyTitle: { marginTop: 12, fontSize: 16, fontWeight: '900', color: '#0f172a', textAlign: 'center' },
  emptyText: { marginTop: 6, fontSize: 14, color: '#64748b', textAlign: 'center', lineHeight: 20 },
  modalOverlay: { flex: 1, justifyContent: 'flex-end', backgroundColor: 'rgba(15,23,42,0.42)' },
  previewSheet: { backgroundColor: '#fff', borderTopLeftRadius: 18, borderTopRightRadius: 18, paddingHorizontal: 18, paddingTop: 10 },
  sheetHandle: { alignSelf: 'center', width: 44, height: 5, borderRadius: 999, backgroundColor: '#cbd5e1', marginBottom: 14 },
  previewHeader: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between' },
  previewIcon: { width: 54, height: 54, borderRadius: 12, alignItems: 'center', justifyContent: 'center', backgroundColor: '#eff6ff' },
  closeButton: { width: 40, height: 40, alignItems: 'center', justifyContent: 'center' },
  previewTitle: { marginTop: 12, fontSize: 20, fontWeight: '900', color: '#0f172a' },
  previewMeta: { marginTop: 5, fontSize: 13, fontWeight: '800', color: '#64748b' },
  previewDescription: { marginTop: 12, fontSize: 14, color: '#334155', lineHeight: 21 },
  rulesBox: { marginTop: 14, flexDirection: 'row', gap: 8, borderRadius: 8, padding: 12, backgroundColor: '#fffbeb', borderWidth: 1, borderColor: '#fde68a' },
  rulesText: { flex: 1, fontSize: 12, fontWeight: '700', color: '#92400e', lineHeight: 18 },
  joinButton: { marginTop: 18, height: 48, borderRadius: 8, alignItems: 'center', justifyContent: 'center', backgroundColor: colors.primary },
  joinButtonText: { fontSize: 15, fontWeight: '900', color: '#fff' },
});
