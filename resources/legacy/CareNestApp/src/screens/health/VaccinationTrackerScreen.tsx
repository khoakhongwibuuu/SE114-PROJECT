import React, { useCallback, useState } from 'react';
import {
  View,
  Text,
  ScrollView,
  TouchableOpacity,
  StyleSheet,
  StatusBar,
} from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { useFocusEffect, useRoute, useNavigation } from '@react-navigation/native';
import type { RouteProp } from '@react-navigation/native';
import { colors } from '../../theme/colors';
import { shadows } from '../../theme/spacing';
import { BOTTOM_NAV_HEIGHT } from '../../utils/constants';
import Icon from '../../components/common/Icon';
import FAB from '../../components/common/FAB';
import type { FamilyStackParamList } from '../../navigation/navigationTypes';
import { getVaccinationTracker, type VaccinationTrackerGroup } from '../../api/vaccinations';

type RouteT = RouteProp<FamilyStackParamList, 'VaccinationTracker'>;

const AGE_GROUP_ICONS: Record<string, string> = {
  'Sơ sinh': 'face',
};

export default function VaccinationTrackerScreen() {
  const route = useRoute<RouteT>();
  const navigation = useNavigation<any>();
  const insets = useSafeAreaInsets();
  const { memberId } = route.params;
  const [groups, setGroups] = useState<VaccinationTrackerGroup[]>([]);

  const loadTracker = useCallback(async () => {
    await getVaccinationTracker(Number(memberId))
      .then(setGroups)
      .catch(() => setGroups([]));
  }, [memberId]);

  useFocusEffect(
    useCallback(() => {
      void loadTracker();
      return undefined;
    }, [loadTracker]),
  );

  return (
    <View style={styles.root}>
      <StatusBar barStyle="dark-content" />
      <View style={[styles.customHeader, { paddingTop: insets.top + 10 }]}>
        <TouchableOpacity style={styles.backBtn} onPress={() => navigation.goBack()}>
          <Icon name="arrow_back" size={26} color="#1E293B" />
        </TouchableOpacity>

        <View style={styles.headerProfileRow}>
          <View style={styles.avatarWrap}>
            <Text style={styles.avatarText}>V</Text>
          </View>
          <View style={styles.headerTextWrap}>
            <Text style={styles.headerTitle}>Lịch tiêm chủng</Text>
            <Text style={styles.headerSubtitle}>HỒ SƠ #{memberId}</Text>
          </View>
        </View>
      </View>

      <ScrollView
        showsVerticalScrollIndicator={false}
        contentContainerStyle={[
          styles.scrollContent,
          { paddingBottom: 100 },
        ]}
      >
        {groups.length === 0 ? (
          <View style={styles.emptyState}>
            <Icon name="syringe" size={46} color={colors.outlineVariant} />
            <Text style={styles.emptyTitle}>Chưa có dữ liệu tiêm chủng</Text>
            <Text style={styles.emptyText}>Thêm mũi tiêm đầu tiên để bắt đầu theo dõi lịch sử vaccine.</Text>
          </View>
        ) : (
          groups.map(group => (
            <View key={group.stageLabel} style={styles.vaccineGroupCard}>
              <View style={styles.groupHeader}>
                <View style={styles.groupHeaderLeft}>
                  <View style={styles.groupIconCircle}>
                    <Icon name="syringe" size={20} color="#3B82F6" />
                  </View>
                  <Text style={styles.groupTitleText}>{group.stageLabel}</Text>
                </View>
              </View>

              <View style={styles.doseList}>
                {group.vaccinations.map((vaccine, index) => {
                  const isLast = index === group.vaccinations.length - 1;
                  return (
                    <TouchableOpacity
                      key={vaccine.id}
                      style={[
                        styles.doseRow,
                        !isLast && styles.doseDivider,
                      ]}
                      activeOpacity={0.7}
                      onPress={() => navigation.navigate('AddVaccinationSchedule', {
                        profileId: Number(memberId),
                        editVaccination: vaccine,
                      })}
                    >
                      <View style={[styles.vacIconWrap, vaccine.status === 'DONE' ? styles.vacIconWrapCompleted : undefined]}>
                        <Icon
                          name={vaccine.status === 'DONE' ? 'check' : 'calendar_today'}
                          size={18}
                          color={vaccine.status === 'DONE' ? '#fff' : colors.primary}
                        />
                      </View>

                      <View style={styles.vacInfo}>
                        <Text style={styles.vacDetail}>
                          Mũi {vaccine.doseNumber} • {vaccine.dateGiven || vaccine.plannedDate || 'Chưa có ngày'}
                        </Text>
                        {vaccine.clinicName ? (
                          <Text style={styles.vacFacility}>{vaccine.clinicName}</Text>
                        ) : (
                          <Text style={styles.vacNoFacility}>Chưa cập nhật địa điểm</Text>
                        )}
                      </View>

                      <Icon name="edit" size={16} color="#94A3B8" style={{ marginLeft: 'auto', paddingRight: 4 }} />
                    </TouchableOpacity>
                  );
                })}
              </View>
            </View>
          ))
        )}
      </ScrollView>

      <FAB iconName="add" onPress={() => navigation.navigate('AddVaccinationSchedule', { profileId: Number(memberId) })} />
    </View>
  );
}

const styles = StyleSheet.create({
  root: { flex: 1, backgroundColor: '#F8FAFC' },
  customHeader: { flexDirection: 'row', alignItems: 'center', paddingHorizontal: 16, paddingBottom: 20, backgroundColor: '#F8FAFC', gap: 12 },
  backBtn: { padding: 4 },
  headerProfileRow: { flex: 1, flexDirection: 'row', alignItems: 'center', gap: 12 },
  avatarWrap: { width: 60, height: 60, borderRadius: 30, alignItems: 'center', justifyContent: 'center', backgroundColor: '#DBEAFE', ...shadows.sm },
  avatarText: { fontSize: 24, fontWeight: '800', color: '#3B82F6' },
  headerTextWrap: { flex: 1 },
  headerTitle: { fontSize: 20, fontFamily: 'Manrope', fontWeight: '800', color: '#1E3A8A' },
  headerSubtitle: { fontSize: 12, fontFamily: 'Inter', fontWeight: '700', color: '#64748B', marginTop: 1, letterSpacing: 0.5 },
  scrollContent: { paddingHorizontal: 20, paddingTop: 10 },
  emptyState: { alignItems: 'center', justifyContent: 'center', paddingVertical: 80, gap: 10 },
  emptyTitle: { fontSize: 18, fontWeight: '700', color: colors.onSurface },
  emptyText: { fontSize: 14, color: colors.onSurfaceVariant, textAlign: 'center', lineHeight: 22 },
  vaccineGroupCard: {
    backgroundColor: '#FFF',
    borderRadius: 12,
    padding: 16,
    marginBottom: 16,
    borderWidth: 1,
    borderColor: '#F1F5F9',
    ...shadows.sm,
  },
  groupHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingBottom: 12,
    borderBottomWidth: 1,
    borderBottomColor: '#F1F5F9',
  },
  groupHeaderLeft: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 10,
  },
  groupIconCircle: {
    width: 32,
    height: 32,
    borderRadius: 16,
    backgroundColor: '#DBEAFE',
    alignItems: 'center',
    justifyContent: 'center',
  },
  groupTitleText: {
    fontSize: 16,
    fontFamily: 'Manrope',
    fontWeight: '800',
    color: '#1E3A8A',
  },
  doseList: {
    marginTop: 4,
  },
  doseRow: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 14,
    gap: 12,
  },
  doseDivider: {
    borderBottomWidth: 1,
    borderBottomColor: '#F1F5F9',
  },
  vacIconWrap: {
    width: 36,
    height: 36,
    borderRadius: 8,
    backgroundColor: '#EFF6FF',
    alignItems: 'center',
    justifyContent: 'center',
  },
  vacIconWrapCompleted: {
    backgroundColor: '#3B82F6',
  },
  vacInfo: {
    flex: 1,
  },
  vacDetail: {
    fontSize: 14,
    fontFamily: 'Inter',
    fontWeight: '700',
    color: '#1E293B',
  },
  vacFacility: {
    fontSize: 12,
    fontFamily: 'Inter',
    color: '#64748B',
    marginTop: 3,
  },
  vacNoFacility: {
    fontSize: 12,
    fontFamily: 'Inter',
    color: '#94A3B8',
    marginTop: 3,
    fontStyle: 'italic',
  },
});
