import React, { useCallback, useState } from 'react';
import {
  ActivityIndicator,
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
import { getCommunityGroups, type CommunityGroup } from '../../api/community';
import type { CommunityStackParamList } from '../../navigation/navigationTypes';
import { colors } from '../../theme/colors';
import { shadows } from '../../theme/spacing';

type Nav = NativeStackNavigationProp<CommunityStackParamList>;

function GroupCard({ item, onPress }: { item: CommunityGroup; onPress: () => void }) {
  return (
    <TouchableOpacity style={styles.card} activeOpacity={0.86} onPress={onPress}>
      <View style={styles.groupIcon}>
        <MaterialCommunityIcons name="account-heart" size={26} color={colors.primary} />
      </View>
      <View style={styles.groupInfo}>
        <Text style={styles.groupName} numberOfLines={1}>{item.name}</Text>
        <Text style={styles.groupDescription} numberOfLines={2}>
          {item.description || 'Trao đổi kinh nghiệm chăm sóc và theo dõi sức khỏe.'}
        </Text>
      </View>
      <MaterialCommunityIcons name="chevron-right" size={24} color="#94a3b8" />
    </TouchableOpacity>
  );
}

export default function CommunityGroupsScreen() {
  const insets = useSafeAreaInsets();
  const navigation = useNavigation<Nav>();
  const [groups, setGroups] = useState<CommunityGroup[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);

  const loadGroups = useCallback(async (asRefresh = false) => {
    try {
      if (asRefresh) {
        setRefreshing(true);
      } else {
        setLoading(true);
      }
      setGroups(await getCommunityGroups());
    } catch {
      setGroups([]);
    } finally {
      setLoading(false);
      setRefreshing(false);
    }
  }, []);

  useFocusEffect(useCallback(() => {
    void loadGroups();
  }, [loadGroups]));

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
        data={groups}
        keyExtractor={item => String(item.id)}
        renderItem={({ item }) => (
          <GroupCard
            item={item}
            onPress={() => navigation.navigate('GroupDetail', {
              groupId: item.id,
              groupName: item.name,
            })}
          />
        )}
        contentContainerStyle={[styles.listContent, { paddingBottom: insets.bottom + 96 }]}
        refreshControl={
          <RefreshControl refreshing={refreshing} onRefresh={() => void loadGroups(true)} />
        }
        ListHeaderComponent={
          <View style={styles.header}>
            <Text style={styles.title}>Hội nhóm bệnh lý</Text>
            <Text style={styles.subtitle}>Các nhóm thảo luận theo chủ đề sức khỏe và tình trạng bệnh lý.</Text>
          </View>
        }
        ListEmptyComponent={
          <View style={styles.emptyCard}>
            <MaterialCommunityIcons name="account-group-outline" size={42} color="#94a3b8" />
            <Text style={styles.emptyTitle}>Chưa có hội nhóm</Text>
            <Text style={styles.emptyText}>Danh sách nhóm sẽ xuất hiện khi backend có dữ liệu.</Text>
          </View>
        }
      />
    </View>
  );
}

const styles = StyleSheet.create({
  root: { flex: 1, backgroundColor: '#f8fafc' },
  centerState: { flex: 1, alignItems: 'center', justifyContent: 'center', backgroundColor: '#f8fafc' },
  listContent: { paddingHorizontal: 16, paddingTop: 18 },
  header: { marginBottom: 14 },
  title: { fontSize: 24, fontWeight: '900', color: '#0f172a' },
  subtitle: { marginTop: 6, fontSize: 14, color: '#64748b', lineHeight: 20 },
  card: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#fff',
    borderRadius: 8,
    padding: 14,
    marginBottom: 12,
    borderWidth: 1,
    borderColor: '#e2e8f0',
    ...shadows.sm,
  },
  groupIcon: {
    width: 52,
    height: 52,
    borderRadius: 8,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#eff6ff',
    marginRight: 12,
  },
  groupInfo: { flex: 1, minWidth: 0 },
  groupName: { fontSize: 16, fontWeight: '900', color: '#0f172a' },
  groupDescription: { marginTop: 4, fontSize: 13, color: '#64748b', lineHeight: 19 },
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
