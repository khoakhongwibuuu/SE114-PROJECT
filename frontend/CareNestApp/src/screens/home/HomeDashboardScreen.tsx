import React, { useCallback, useMemo, useRef, useState } from 'react';
import {
  Animated,
  Image,
  Modal,
  ScrollView,
  StatusBar,
  StyleSheet,
  Text,
  TouchableOpacity,
  TouchableWithoutFeedback,
  View,
  RefreshControl,
} from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { useFocusEffect, useNavigation, CompositeNavigationProp } from '@react-navigation/native';
import { BottomTabNavigationProp } from '@react-navigation/bottom-tabs';
import type { NativeStackNavigationProp } from '@react-navigation/native-stack';
import Svg, { Defs, LinearGradient, Rect, Stop } from 'react-native-svg';
import { shadows } from '../../theme/spacing';
import { colors } from '../../theme/colors';
import { BOTTOM_NAV_HEIGHT } from '../../utils/constants';
import { CARENEST_LOGO_HOUSE } from '../../assets/branding';
import Icon from '../../components/common/Icon';
import Avatar from '../../components/common/Avatar';
import NotificationBell from '../../components/common/NotificationBell';
import type { HomeStackParamList, MainTabParamList } from '../../navigation/navigationTypes';
import { useAuth } from '../../context/AuthContext';
import { useFamily } from '../../context/FamilyContext';
import { getDashboard, type DashboardPayload } from '../../api/dashboard';
import { invalidateApiGetCache } from '../../api/client';
import { getAppointmentOverview } from '../../api/appointments';
import { getDailySchedule } from '../../api/medicine';
import { getVaccinationTracker } from '../../api/vaccinations';
import { formatLocalDate } from '../../utils/dateTime';

type Nav = CompositeNavigationProp<
  NativeStackNavigationProp<HomeStackParamList, 'HomeDashboard'>,
  BottomTabNavigationProp<MainTabParamList>
>;

type TaskCard = {
  id: string;
  icon: string;
  iconBg: string;
  iconColor: string;
  title: string;
  subtitle: string;
  badge?: string;
};

type ProfileContext = {
  profile?: { id?: number; fullName?: string };
  dailyMedicine?: {
    sections?: Array<{
      session: string;
      items: Array<{
        id: number;
        medicineName: string;
        dosage: string;
        isTaken: boolean;
      }>;
    }>;
  };
  appointments?: {
    upcomingAppointments?: Array<{
      id: number;
      title: string;
      appointmentDate: string;
      location?: string | null;
      doctorName?: string | null;
    }>;
  };
  vaccinations?: Array<{
    stageLabel: string;
    vaccinations: Array<{
      id: number;
      vaccineName: string;
      plannedDate?: string | null;
      dateGiven?: string | null;
      status: string;
    }>;
  }>;
};

const AI_SUMMARY_FALLBACK =
  'CareNest AI sẽ tóm tắt nhanh các việc cần chú ý trong ngày của gia đình bạn.';

const AI_SUMMARY_NORMALIZERS: Array<{ pattern: RegExp; value: string }> = [
  {
    pattern:
      /^hom nay chua co canh bao lon\.? ban co the kiem tra lich thuoc, lich kham va hoi carenest ai neu can tra cuu nhanh\.?$/i,
    value:
      'Hôm nay chưa có cảnh báo lớn. Bạn có thể kiểm tra lịch thuốc, lịch khám và hỏi CareNest AI nếu cần tra cứu nhanh.',
  },
  {
    pattern:
      /^che do ca nha dang tong hop suc khoe cua toan bo thanh vien\.? ban co the xem nhac nho, lich kham va hoi carenest ai de tra cuu nhanh\.?$/i,
    value:
      'Chế độ Cả nhà đang tổng hợp sức khỏe của toàn bộ thành viên. Bạn có thể xem nhắc nhở, lịch khám và hỏi CareNest AI để tra cứu nhanh.',
  },
];

function normalizeAiSummaryText(summary?: string | null): string {
  if (!summary || !summary.trim()) {
    return AI_SUMMARY_FALLBACK;
  }

  const trimmed = summary.trim();
  const normalized = trimmed.toLowerCase().replace(/\s+/g, ' ');

  for (const item of AI_SUMMARY_NORMALIZERS) {
    if (item.pattern.test(normalized)) {
      return item.value;
    }
  }

  return trimmed;
}

function buildTasks(context?: ProfileContext): TaskCard[] {
  if (!context) {
    return [];
  }

  const nextTasks: TaskCard[] = [];
  const medicineSections = context.dailyMedicine?.sections || [];
  const firstDose = medicineSections.flatMap(section =>
    section.items.map(item => ({
      id: `dose-${item.id}`,
      icon: 'pill',
      iconBg: '#EFF6FF',
      iconColor: '#2563EB',
      title: item.medicineName,
      subtitle: `${section.session} · ${item.dosage}`,
      badge: item.isTaken ? 'ĐÃ UỐNG' : 'CHƯA UỐNG',
    })),
  )[0];

  if (firstDose) {
    nextTasks.push(firstDose);
  }

  const nextAppointment = context.appointments?.upcomingAppointments?.[0];
  if (nextAppointment) {
    nextTasks.push({
      id: `appt-${nextAppointment.id}`,
      icon: 'calendar_month',
      iconBg: '#F0FDF4',
      iconColor: '#16A34A',
      title: nextAppointment.title,
      subtitle: new Date(nextAppointment.appointmentDate).toLocaleString('vi-VN'),
    });
  }

  const nextVaccination = context.vaccinations
    ?.flatMap(group => group.vaccinations)
    .find(item => item.status !== 'DONE');

  if (nextVaccination) {
    nextTasks.push({
      id: `vac-${nextVaccination.id}`,
      icon: 'syringe',
      iconBg: '#FFF7ED',
      iconColor: '#EA580C',
      title: nextVaccination.vaccineName,
      subtitle:
        nextVaccination.plannedDate ||
        nextVaccination.dateGiven ||
        'Theo dõi lịch tiêm',
    });
  }

  return nextTasks;
}

export default function HomeDashboardScreen() {
  const navigation = useNavigation<Nav>();
  const insets = useSafeAreaInsets();
  const { user } = useAuth();
  const { members, selectedProfileId, setSelectedProfileId, ownProfileId, allFamilies, activeFamilyId, setActiveFamilyId } = useFamily();
  const [dashboard, setDashboard] = useState<DashboardPayload | null>(null);
  const [switcherVisible, setSwitcherVisible] = useState(false);
  const slideAnim = useRef(new Animated.Value(400)).current;
  const [refreshing, setRefreshing] = useState(false);

  const loadDashboard = useCallback(async () => {
    if (!activeFamilyId) return;
    await getDashboard(activeFamilyId, selectedProfileId || undefined)
      .then(setDashboard)
      .catch(() => setDashboard(null));
  }, [activeFamilyId, selectedProfileId]);

  const handleRefresh = useCallback(async () => {
    setRefreshing(true);
    invalidateApiGetCache(['/dashboard', '/notifications']);
    await loadDashboard();
    setRefreshing(false);
  }, [loadDashboard]);

  useFocusEffect(
    useCallback(() => {
      void loadDashboard();
      return undefined;
    }, [loadDashboard]),
  );

  const handleSelectMember = (profileId: number | null) => {
    if (selectedProfileId === profileId) {
      setSelectedProfileId(null);
    } else {
      setSelectedProfileId(profileId);
    }
  };

  const tasks = useMemo(() => {
    if (!dashboard || !dashboard.todayTasks) {
      return [];
    }

    return dashboard.todayTasks.map((t) => {
      let icon = 'check_circle';
      let iconBg = '#EFF6FF';
      let iconColor = '#2563EB';

      if (t.type === 'MEDICATION') {
        icon = 'pill';
        iconBg = '#EFF6FF';
        iconColor = '#2563EB';
      } else if (t.type === 'VACCINATION') {
        icon = 'syringe';
        iconBg = '#FFF7ED';
        iconColor = '#EA580C';
      } else if (t.type === 'APPOINTMENT') {
        icon = 'calendar_month';
        iconBg = '#F0FDF4';
        iconColor = '#16A34A';
      }

      let timeLabel = '';
      if (t.time) {
        try {
          const date = new Date(t.time);
          if (t.type === 'MEDICATION') {
            timeLabel = date.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });
          } else {
            timeLabel = date.toLocaleDateString('vi-VN');
          }
        } catch {
          timeLabel = t.time;
        }
      }

      const memberSub = t.memberName ? ` (${t.memberName})` : '';
      const subtitleText = `${timeLabel}${memberSub}`;

      return {
        id: `${t.type}-${t.referenceId}`,
        type: t.type,
        referenceId: t.referenceId,
        profileId: t.profileId,
        icon,
        iconBg,
        iconColor,
        title: t.title,
        subtitle: subtitleText,
        badge: t.subtitle || undefined,
      };
    });
  }, [dashboard]);

  const unreadCount = dashboard?.unreadNotifications ?? 0;

  const aiSummaryText = useMemo(() => {
    if (!dashboard || !dashboard.todayTasks) {
      return AI_SUMMARY_FALLBACK;
    }
    const count = dashboard.todayTasks.length;
    if (count > 0) {
      return `Hôm nay ${selectedProfileId ? 'thành viên' : 'cả nhà'} có ${count} việc cần chú ý thực hiện. Hãy lưu ý chuẩn bị đầy đủ nhé!`;
    }
    return 'Hôm nay chưa có cảnh báo lớn. Bạn có thể kiểm tra lịch thuốc, lịch khám và hỏi CareNest AI nếu cần tra cứu nhanh.';
  }, [dashboard, selectedProfileId]);

  const selectedProfileRouteId = String(
    selectedProfileId || ownProfileId || members[0]?.id || '',
  );
  const activeShortcutProfileId = Number(selectedProfileRouteId);

  const prefetchMedicineSchedule = useCallback(() => {
    if (!Number.isFinite(activeShortcutProfileId) || activeShortcutProfileId <= 0) {
      return;
    }

    const today = formatLocalDate(new Date());
    void getDailySchedule(activeShortcutProfileId, today).catch(() => {});
  }, [activeShortcutProfileId]);

  const prefetchAppointments = useCallback(() => {
    if (!Number.isFinite(activeShortcutProfileId) || activeShortcutProfileId <= 0) {
      return;
    }

    void getAppointmentOverview(activeShortcutProfileId).catch(() => {});
  }, [activeShortcutProfileId]);

  const prefetchVaccinations = useCallback(() => {
    if (!Number.isFinite(activeShortcutProfileId) || activeShortcutProfileId <= 0) {
      return;
    }

    void getVaccinationTracker(activeShortcutProfileId).catch(() => {});
  }, [activeShortcutProfileId]);

  const handleTaskPress = (task: any) => {
    const targetProfileId = task.profileId || activeShortcutProfileId;
    if (task.type === 'MEDICATION') {
      navigation.navigate('MedicineSchedule', { memberId: String(targetProfileId) });
    } else if (task.type === 'VACCINATION') {
      navigation.navigate('VaccinationTracker', { memberId: String(targetProfileId) });
    } else if (task.type === 'APPOINTMENT') {
      navigation.navigate('AppointmentList', { memberId: String(targetProfileId) });
    }
  };

  const handleOpenSwitcher = () => {
    setSwitcherVisible(true);
    Animated.spring(slideAnim, {
      toValue: 0, useNativeDriver: true, tension: 70, friction: 12,
    }).start();
  };

  const handleCloseSwitcher = () => {
    Animated.timing(slideAnim, {
      toValue: 400, useNativeDriver: true, duration: 220,
    }).start(() => setSwitcherVisible(false));
  };

  const handleSwitchFamily = async (id: number) => {
    handleCloseSwitcher();
    if (id !== activeFamilyId) {
      await setActiveFamilyId(id);
      await getDashboard(id, selectedProfileId || undefined)
        .then(setDashboard)
        .catch(() => setDashboard(null));
    }
  };

  const activeFamilyName = allFamilies.find(f => f.id === activeFamilyId)?.name
    ?? (allFamilies.length > 0 ? allFamilies[0].name : 'CareNest');

  return (
    <View style={styles.container}>
      <StatusBar barStyle="dark-content" backgroundColor="#fff" />

      <View style={[styles.header, { paddingTop: insets.top + 10 }]}>
        {/* Family Switcher — replaces static logo */}
        <TouchableOpacity
          style={styles.brandLeft}
          activeOpacity={0.8}
          onPress={handleOpenSwitcher}
          disabled={allFamilies.length <= 1}
        >
          <Image source={CARENEST_LOGO_HOUSE} style={styles.brandGlyph} resizeMode="contain" />
          <Text style={styles.logoText} numberOfLines={1}>
            {activeFamilyName}
          </Text>
          {allFamilies.length > 1 && (
            <Text style={styles.switcherCaret}>▾</Text>
          )}
        </TouchableOpacity>
        <View style={styles.headerActions}>
          <Avatar uri={user?.avatarUrl} name={user?.fullName || 'CareNest'} size="sm" bordered />
          <NotificationBell iconColor={colors.onSurfaceVariant} hasNotification={unreadCount > 0} />
        </View>
      </View>

      {/* Family Switcher Bottom Sheet */}
      <Modal transparent animationType="none" visible={switcherVisible} onRequestClose={handleCloseSwitcher}>
        <TouchableWithoutFeedback onPress={handleCloseSwitcher}>
          <View style={styles.sheetOverlay} />
        </TouchableWithoutFeedback>
        <Animated.View style={[styles.sheet, { transform: [{ translateY: slideAnim }] }]}>
          <View style={styles.sheetHandle} />
          <Text style={styles.sheetTitle}>Chọn gia đình</Text>
          {allFamilies.map(family => {
            const isActive = family.id === activeFamilyId;
            return (
              <TouchableOpacity
                key={family.id}
                style={[styles.sheetItem, isActive && styles.sheetItemActive]}
                activeOpacity={0.8}
                onPress={() => handleSwitchFamily(family.id)}
              >
                <View style={styles.sheetItemLeft}>
                  <Text style={[styles.sheetItemName, isActive && styles.sheetItemNameActive]}>
                    {family.name}
                  </Text>
                  <Text style={styles.sheetItemSub}>
                    {family.memberCount} thành viên • {family.myRole === 'OWNER' ? 'Chủ hộ' : 'Thành viên'}
                  </Text>
                </View>
                {isActive && <Text style={styles.sheetCheckmark}>✓</Text>}
              </TouchableOpacity>
            );
          })}
        </Animated.View>
      </Modal>

      <ScrollView
        contentContainerStyle={[
          styles.scroll,
          { paddingBottom: BOTTOM_NAV_HEIGHT + insets.bottom + 20 },
        ]}
        showsVerticalScrollIndicator={false}
        refreshControl={
          <RefreshControl
            refreshing={refreshing}
            onRefresh={handleRefresh}
            colors={[colors.primary]}
          />
        }
      >
        <View style={styles.greetingSection}>
          <Text style={styles.greetingTitle}>
            Xin chào, {user?.fullName || 'bạn'}!
          </Text>
          <Text style={styles.greetingSubtitle}>
            Hy vọng gia đình mình có một ngày khỏe mạnh.
          </Text>
        </View>

        <View style={styles.section}>
          <Text style={styles.sectionTitle}>THÀNH VIÊN</Text>
          <ScrollView
            horizontal
            showsHorizontalScrollIndicator={false}
            contentContainerStyle={styles.memberList}
          >
            <TouchableOpacity
              style={[styles.memberPill, selectedProfileId === null && styles.memberPillActive]}
              onPress={() => setSelectedProfileId(null)}
            >
              <Text
                style={[
                  styles.memberPillText,
                  selectedProfileId === null && styles.memberPillTextActive,
                ]}
              >
                Cả nhà
              </Text>
            </TouchableOpacity>
            {members.map(member => {
              const isSelf = member.userId === user?.userId;
              const trimmedName = (member.fullName || '').trim();
              const displayName = isSelf ? 'Tôi' : (trimmedName.split(/\s+/).pop() || 'Thành viên');
              
              return (
                <TouchableOpacity
                  key={member.id}
                  style={[
                    styles.memberPill,
                    selectedProfileId === member.id && styles.memberPillActive,
                  ]}
                  onPress={() => handleSelectMember(member.id)}
                >
                  <Text
                    style={[
                      styles.memberPillText,
                      selectedProfileId === member.id && styles.memberPillTextActive,
                    ]}
                  >
                    {displayName}
                  </Text>
                </TouchableOpacity>
              );
            })}
          </ScrollView>
        </View>

        <View style={styles.shortcutGrid}>
          <TouchableOpacity
            style={styles.shortcutCard}
            onPressIn={prefetchMedicineSchedule}
            onPress={() => navigation.navigate('MedicineSchedule')}
          >
            <View style={[styles.shortcutIconWrap, { backgroundColor: '#E0F2FE' }]}>
              <Icon name="pill" size={26} color="#0EA5E9" />
            </View>
            <Text style={styles.shortcutLabel}>Lịch thuốc</Text>
          </TouchableOpacity>
          <TouchableOpacity
            style={styles.shortcutCard}
            onPressIn={prefetchAppointments}
            onPress={() => navigation.navigate('AppointmentList')}
          >
            <View style={[styles.shortcutIconWrap, { backgroundColor: '#F3E8FF' }]}>
              <Icon name="calendar_month" size={26} color="#A855F7" />
            </View>
            <Text style={styles.shortcutLabel}>Lịch hẹn</Text>
          </TouchableOpacity>
          <TouchableOpacity
            style={styles.shortcutCard}
            onPressIn={prefetchVaccinations}
            onPress={() =>
              navigation.navigate('VaccinationTracker', { memberId: selectedProfileRouteId })
            }
          >
            <View style={[styles.shortcutIconWrap, { backgroundColor: '#E0F7FA' }]}>
              <Icon name="syringe" size={26} color="#0097A7" />
            </View>
            <Text style={styles.shortcutLabel}>Tiêm chủng</Text>
          </TouchableOpacity>
        </View>

        <View style={[styles.heroCard, shadows.lg]}>
          <View style={StyleSheet.absoluteFill}>
            <Svg height="100%" width="100%">
              <Defs>
                <LinearGradient id="grad" x1="0%" y1="0%" x2="100%" y2="100%">
                  <Stop offset="0%" stopColor="#007BFF" />
                  <Stop offset="100%" stopColor="#0047AB" />
                </LinearGradient>
              </Defs>
              <Rect width="100%" height="100%" fill="url(#grad)" />
            </Svg>
          </View>

          <View style={styles.heroHeader}>
            <View>
              <Text style={styles.heroDate}>
                {dashboard?.generatedAt || new Date().toLocaleDateString('vi-VN')}
              </Text>
              <Text style={styles.heroStatus}>
                {unreadCount > 0
                  ? 'Có việc cần chú ý'
                  : 'Mọi thứ đều ổn'}
              </Text>
            </View>
            <Icon name="sunny" size={40} color="rgba(255,255,255,0.8)" />
          </View>

          <View style={styles.glassStatsRow}>
            <View style={styles.glassModule}>
              <Icon name="group" size={18} color="#fff" />
              <Text
                style={styles.moduleLabel}
                numberOfLines={1}
                adjustsFontSizeToFit
                minimumFontScale={0.8}
              >
                Thành viên
              </Text>
              <Text style={styles.moduleValue}>{members.length}</Text>
            </View>
            <View style={styles.glassModule}>
              <Icon name="notifications" size={18} color="#fff" />
              <Text
                style={styles.moduleLabel}
                numberOfLines={1}
                adjustsFontSizeToFit
                minimumFontScale={0.8}
              >
                Nhắc nhở
              </Text>
              <Text style={styles.moduleValue}>{unreadCount}</Text>
            </View>
            <View style={styles.glassModule}>
              <Icon name="pill" size={18} color="#fff" />
              <Text
                style={styles.moduleLabel}
                numberOfLines={1}
                adjustsFontSizeToFit
                minimumFontScale={0.7}
              >
                Thuốc hôm nay
              </Text>
              <Text style={styles.moduleValue}>
                {tasks.filter(task => task.icon === 'pill').length}
              </Text>
            </View>
          </View>
        </View>

        <View style={styles.section}>
          <View style={styles.sectionHeaderRow}>
            <Text style={styles.sectionTitle}>HÔM NAY CẦN LÀM</Text>
          </View>

          {tasks.length === 0 ? (
            <View style={styles.taskCard}>
              <View style={[styles.taskIconWrap, { backgroundColor: '#EFF6FF' }]}>
                <Icon name="check_circle" size={24} color="#2563EB" />
              </View>
              <View style={styles.taskInfo}>
                <Text style={styles.taskTitle}>Chưa có việc nào cần xử lý</Text>
                <Text style={styles.taskTime}>
                  Dashboard sẽ tự cập nhật khi có lịch thuốc, khám
                  hoặc tiêm chủng.
                </Text>
              </View>
            </View>
          ) : (
            tasks.map(task => (
              <TouchableOpacity
                key={task.id}
                style={styles.taskCard}
                onPress={() => handleTaskPress(task)}
                activeOpacity={0.7}
              >
                <View style={[styles.taskIconWrap, { backgroundColor: task.iconBg }]}>
                  <Icon name={task.icon} size={24} color={task.iconColor} />
                </View>
                <View style={styles.taskInfo}>
                  <Text style={styles.taskTitle}>{task.title}</Text>
                  <Text style={styles.taskTime}>{task.subtitle}</Text>
                </View>
                {task.badge ? (
                  <View style={[
                    styles.badgeContainer,
                    task.badge.includes('Ngày mai') && styles.badgeTomorrow,
                    task.badge.includes('Ngày kia') && styles.badgeUpcoming,
                    task.badge.includes('Hôm nay') && styles.badgeToday,
                  ]}>
                    <Text style={[
                      styles.badgeText,
                      task.badge.includes('Ngày mai') && styles.badgeTextTomorrow,
                      task.badge.includes('Ngày kia') && styles.badgeTextUpcoming,
                      task.badge.includes('Hôm nay') && styles.badgeTextToday,
                    ]}>
                      {task.badge}
                    </Text>
                  </View>
                ) : (
                  <Icon name="chevron_right" size={20} color="#94A3B8" />
                )}
              </TouchableOpacity>
            ))
          )}
        </View>

        <View style={[styles.aiAdvisorCard, { backgroundColor: '#E1F5FE' }]}>
          <View style={styles.aiHeader}>
            <View style={styles.aiAvatar}>
              <Image source={CARENEST_LOGO_HOUSE} style={styles.aiAvatarIcon} resizeMode="contain" />
            </View>
            <Text style={styles.aiLabel}>AI CỐ VẤN</Text>
          </View>
          <Text style={styles.aiAdviceText}>
            "
            {aiSummaryText}
            "
          </Text>
        </View>
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#fff' },
  header: {
    paddingHorizontal: 20,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingBottom: 15,
  },
  brandLeft: { flexDirection: 'row', alignItems: 'center', gap: 12 },
  headerActions: { flexDirection: 'row', alignItems: 'center', gap: 8 },
  brandGlyph: { width: 22, height: 22 },
  logoText: {
    fontSize: 22,
    fontFamily: 'Manrope',
    fontWeight: '800',
    color: '#0047AB',
    letterSpacing: -0.5,
  },
  scroll: { paddingHorizontal: 20, paddingTop: 10 },
  greetingSection: { marginBottom: 24 },
  greetingTitle: {
    fontSize: 26,
    fontFamily: 'Manrope',
    fontWeight: '800',
    color: '#1E293B',
  },
  greetingSubtitle: {
    fontSize: 14,
    fontFamily: 'Inter',
    color: '#64748B',
    marginTop: 4,
  },
  section: { marginBottom: 24 },
  sectionHeaderRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 12,
  },
  sectionTitle: {
    fontSize: 12,
    fontFamily: 'Inter',
    fontWeight: '800',
    color: '#94A3B8',
    letterSpacing: 1.2,
    marginBottom: 12,
  },
  memberList: { paddingBottom: 5, gap: 12 },
  memberPill: {
    paddingHorizontal: 20,
    paddingVertical: 10,
    borderRadius: 25,
    backgroundColor: '#F1F5F9',
  },
  memberPillActive: {
    backgroundColor: '#0047AB',
    ...shadows.sm,
  },
  memberPillText: {
    fontSize: 14,
    fontFamily: 'Inter',
    fontWeight: '600',
    color: '#475569',
  },
  memberPillTextActive: { color: '#fff' },
  shortcutGrid: { flexDirection: 'row', gap: 12, marginBottom: 24 },
  shortcutCard: {
    flex: 1,
    backgroundColor: '#fff',
    borderRadius: 24,
    padding: 16,
    alignItems: 'center',
    justifyContent: 'center',
    borderWidth: 1,
    borderColor: '#F1F5F9',
    ...shadows.sm,
  },
  shortcutIconWrap: {
    width: 54,
    height: 54,
    borderRadius: 27,
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 10,
  },
  shortcutLabel: {
    fontSize: 12,
    fontFamily: 'Inter',
    fontWeight: '700',
    color: '#1E293B',
  },
  heroCard: {
    borderRadius: 28,
    padding: 24,
    height: 240,
    justifyContent: 'space-between',
    overflow: 'hidden',
    marginBottom: 24,
  },
  heroHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
  },
  heroDate: {
    fontSize: 14,
    fontFamily: 'Inter',
    color: 'rgba(255,255,255,0.7)',
  },
  heroStatus: {
    fontSize: 28,
    fontFamily: 'Manrope',
    fontWeight: '800',
    color: '#fff',
    marginTop: 4,
  },
  glassStatsRow: { flexDirection: 'row', gap: 10 },
  glassModule: {
    flex: 1,
    backgroundColor: 'rgba(255, 255, 255, 0.2)',
    padding: 12,
    borderRadius: 20,
    borderWidth: 1,
    borderColor: 'rgba(255, 255, 255, 0.3)',
    alignItems: 'center',
    gap: 4,
  },
  moduleLabel: {
    fontSize: 9,
    fontFamily: 'Inter',
    color: 'rgba(255,255,255,0.8)',
    textTransform: 'uppercase',
    textAlign: 'center',
    width: '100%',
  },
  moduleValue: {
    fontSize: 14,
    fontFamily: 'Manrope',
    fontWeight: '700',
    color: '#fff',
  },
  taskCard: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#fff',
    borderRadius: 24,
    padding: 16,
    marginBottom: 12,
    borderWidth: 1,
    borderColor: '#F1F5F9',
    ...shadows.sm,
  },
  taskIconWrap: {
    width: 52,
    height: 52,
    borderRadius: 18,
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 15,
  },
  taskInfo: { flex: 1 },
  taskTitle: {
    fontSize: 15,
    fontFamily: 'Manrope',
    fontWeight: '700',
    color: '#1E293B',
  },
  taskTime: {
    fontSize: 13,
    fontFamily: 'Inter',
    color: '#64748B',
    marginTop: 2,
  },
  tagChuaUong: {
    backgroundColor: '#EEF2FF',
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 10,
  },
  tagText: {
    fontSize: 10,
    fontFamily: 'Inter',
    fontWeight: '800',
    color: '#4F46E5',
  },
  aiAdvisorCard: {
    borderRadius: 24,
    padding: 20,
    borderWidth: 1,
    borderColor: 'rgba(0, 71, 171, 0.05)',
  },
  aiHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 10,
    marginBottom: 12,
  },
  aiAvatar: {
    width: 36,
    height: 36,
    borderRadius: 12,
    backgroundColor: '#0047AB',
    alignItems: 'center',
    justifyContent: 'center',
  },
  aiAvatarIcon: { width: 20, height: 20 },
  aiLabel: {
    fontSize: 12,
    fontFamily: 'Inter',
    fontWeight: '800',
    color: '#0047AB',
    letterSpacing: 1,
  },
  aiAdviceText: {
    fontSize: 14,
    fontFamily: 'Inter',
    color: '#1E293B',
    fontStyle: 'italic',
    lineHeight: 22,
  },

  // ── Family Switcher ─────────────────────────────────────────────────────────
  switcherCaret: {
    fontSize: 14,
    color: '#64748B',
    marginLeft: 2,
    marginTop: 1,
  },
  sheetOverlay: {
    flex: 1,
    backgroundColor: 'rgba(0,0,0,0.4)',
  },
  sheet: {
    position: 'absolute',
    bottom: 0,
    left: 0,
    right: 0,
    backgroundColor: '#fff',
    borderTopLeftRadius: 28,
    borderTopRightRadius: 28,
    padding: 24,
    paddingBottom: 40,
  },
  sheetHandle: {
    width: 40,
    height: 4,
    borderRadius: 2,
    backgroundColor: '#E2E8F0',
    alignSelf: 'center',
    marginBottom: 20,
  },
  sheetTitle: {
    fontSize: 17,
    fontFamily: 'Manrope',
    fontWeight: '800',
    color: '#1E293B',
    marginBottom: 12,
  },
  sheetItem: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 14,
    paddingHorizontal: 16,
    borderRadius: 16,
    marginBottom: 4,
  },
  sheetItemActive: {
    backgroundColor: '#EFF6FF',
  },
  sheetItemLeft: { flex: 1 },
  sheetItemName: {
    fontSize: 15,
    fontFamily: 'Inter',
    fontWeight: '700',
    color: '#1E293B',
    marginBottom: 3,
  },
  sheetItemNameActive: { color: colors.primary },
  sheetItemSub: {
    fontSize: 12,
    fontFamily: 'Inter',
    color: '#64748B',
  },
  sheetCheckmark: {
    fontSize: 18,
    fontWeight: '700',
    color: colors.primary,
  },
  badgeContainer: {
    backgroundColor: '#EEF2FF',
    paddingHorizontal: 10,
    paddingVertical: 5,
    borderRadius: 12,
  },
  badgeText: {
    fontSize: 11,
    fontFamily: 'Inter',
    fontWeight: '700',
    color: '#4F46E5',
  },
  badgeTomorrow: {
    backgroundColor: '#FFEDD5',
  },
  badgeTextTomorrow: {
    color: '#F97316',
  },
  badgeUpcoming: {
    backgroundColor: '#ECFDF5',
  },
  badgeTextUpcoming: {
    color: '#10B981',
  },
  badgeToday: {
    backgroundColor: '#FEE2E2',
  },
  badgeTextToday: {
    color: '#EF4444',
  },
});
