import React, { useCallback, useEffect, useRef, useState } from 'react';
import {
  ActivityIndicator,
  Animated,
  Modal,
  ScrollView,
  StatusBar,
  StyleSheet,
  Text,
  TouchableOpacity,
  TouchableWithoutFeedback,
  View,
} from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { useFocusEffect, useNavigation } from '@react-navigation/native';
import type { NativeStackNavigationProp } from '@react-navigation/native-stack';
import MaterialCommunityIcons from 'react-native-vector-icons/MaterialCommunityIcons';
import type { FamilyStackParamList } from '../../navigation/navigationTypes';
import { useFamily } from '../../context/FamilyContext';
import type { FamilySummary } from '../../api/family';
import { getMyFamilyList } from '../../api/family';
import { colors } from '../../theme/colors';
import { shadows } from '../../theme/spacing';

type Nav = NativeStackNavigationProp<FamilyStackParamList, 'FamilyPicker'>;

function RoleBadge({ role }: { role: string }) {
  const label = (() => {
    switch (role) {
      case 'OWNER':  return 'Chủ hộ';
      case 'FATHER': return 'Bố';
      case 'MOTHER': return 'Mẹ';
      case 'OLDER_BROTHER': return 'Anh';
      case 'OLDER_SISTER':  return 'Chị';
      case 'YOUNGER': return 'Em';
      default: return 'Thành viên';
    }
  })();
  const isOwner = role === 'OWNER';
  return (
    <View style={[styles.roleBadge, isOwner && styles.roleBadgeOwner]}>
      <Text style={[styles.roleBadgeText, isOwner && styles.roleBadgeTextOwner]}>
        {label}
      </Text>
    </View>
  );
}

function FamilyCard({
  item,
  isActive,
  onPress,
}: {
  item: FamilySummary;
  isActive: boolean;
  onPress: () => void;
}) {
  return (
    <TouchableOpacity
      style={[styles.card, isActive && styles.cardActive]}
      activeOpacity={0.85}
      onPress={onPress}
    >
      <View style={[styles.cardIconWrap, isActive && styles.cardIconWrapActive]}>
        <MaterialCommunityIcons
          name="home-heart"
          size={28}
          color={isActive ? '#fff' : colors.primary}
        />
      </View>
      <View style={styles.cardInfo}>
        <Text style={[styles.cardName, isActive && styles.cardNameActive]} numberOfLines={1}>
          {item.name}
        </Text>
        <Text style={styles.cardSub}>
          {item.memberCount} thành viên • {item.ownerName}
        </Text>
      </View>
      <View style={styles.cardRight}>
        <RoleBadge role={item.myRole} />
        {isActive
          ? <MaterialCommunityIcons name="check-circle" size={20} color={colors.primary} />
          : <MaterialCommunityIcons name="chevron-right" size={20} color="#cbd5e1" />
        }
      </View>
    </TouchableOpacity>
  );
}

// ─── Bottom Sheet: Add Family ────────────────────────────────────────────────

function AddFamilySheet({ visible, onClose, onCreateNew, onJoinByCode }: {
  visible: boolean;
  onClose: () => void;
  onCreateNew: () => void;
  onJoinByCode: () => void;
}) {
  const slideAnim = useRef(new Animated.Value(300)).current;

  useEffect(() => {
    Animated.spring(slideAnim, {
      toValue: visible ? 0 : 300,
      useNativeDriver: true,
      tension: 70,
      friction: 12,
    }).start();
  }, [visible, slideAnim]);

  if (!visible) return null;

  return (
    <Modal transparent animationType="none" visible={visible} onRequestClose={onClose}>
      <TouchableWithoutFeedback onPress={onClose}>
        <View style={styles.sheetOverlay} />
      </TouchableWithoutFeedback>
      <Animated.View style={[styles.sheet, { transform: [{ translateY: slideAnim }] }]}>
        <View style={styles.sheetHandle} />
        <Text style={styles.sheetTitle}>Thêm gia đình</Text>
        <TouchableOpacity style={styles.sheetOption} activeOpacity={0.8} onPress={onCreateNew}>
          <View style={[styles.sheetOptionIcon, { backgroundColor: '#EFF6FF' }]}>
            <MaterialCommunityIcons name="home-plus" size={24} color={colors.primary} />
          </View>
          <View style={styles.sheetOptionText}>
            <Text style={styles.sheetOptionLabel}>Tạo gia đình mới</Text>
            <Text style={styles.sheetOptionSub}>Bạn sẽ là Chủ hộ của gia đình này</Text>
          </View>
        </TouchableOpacity>
        <TouchableOpacity style={styles.sheetOption} activeOpacity={0.8} onPress={onJoinByCode}>
          <View style={[styles.sheetOptionIcon, { backgroundColor: '#F0FDF4' }]}>
            <MaterialCommunityIcons name="qrcode-scan" size={24} color="#16A34A" />
          </View>
          <View style={styles.sheetOptionText}>
            <Text style={styles.sheetOptionLabel}>Tham gia bằng mã</Text>
            <Text style={styles.sheetOptionSub}>Nhập code hoặc quét QR từ Chủ hộ</Text>
          </View>
        </TouchableOpacity>
      </Animated.View>
    </Modal>
  );
}

// ─── Main Screen ─────────────────────────────────────────────────────────────

export default function FamilyPickerScreen() {
  const insets = useSafeAreaInsets();
  const navigation = useNavigation<Nav>();
  const { activeFamilyId, setActiveFamilyId } = useFamily();

  const [families, setFamilies] = useState<FamilySummary[]>([]);
  const [loading, setLoading] = useState(true);
  const [sheetVisible, setSheetVisible] = useState(false);

  const loadFamilies = useCallback(async () => {
    try {
      setLoading(true);
      const list = await getMyFamilyList();
      setFamilies(list);
    } finally {
      setLoading(false);
    }
  }, []);

  useFocusEffect(useCallback(() => { void loadFamilies(); }, [loadFamilies]));

  const handleSelectFamily = async (family: FamilySummary) => {
    await setActiveFamilyId(family.id);
    navigation.navigate('FamilyManagement');
  };

  return (
    <View style={[styles.root, { paddingTop: insets.top }]}>
      <StatusBar barStyle="dark-content" backgroundColor="#fff" />

      {/* Header */}
      <View style={styles.header}>
        <Text style={styles.headerTitle}>Gia đình của tôi</Text>
        <TouchableOpacity
          style={styles.addBtn}
          activeOpacity={0.85}
          onPress={() => setSheetVisible(true)}
        >
          <MaterialCommunityIcons name="plus" size={22} color="#fff" />
        </TouchableOpacity>
      </View>

      {loading ? (
        <View style={styles.loadingWrap}>
          <ActivityIndicator size="large" color={colors.primary} />
        </View>
      ) : families.length === 0 ? (
        <View style={styles.emptyWrap}>
          <View style={styles.emptyIcon}>
            <MaterialCommunityIcons name="home-group" size={52} color="#94a3b8" />
          </View>
          <Text style={styles.emptyTitle}>Chưa có gia đình nào</Text>
          <Text style={styles.emptySub}>
            Tạo gia đình mới hoặc tham gia bằng mã mời từ chủ hộ nhé!
          </Text>
          <TouchableOpacity
            style={styles.emptyBtn}
            activeOpacity={0.85}
            onPress={() => setSheetVisible(true)}
          >
            <Text style={styles.emptyBtnText}>Bắt đầu ngay</Text>
          </TouchableOpacity>
        </View>
      ) : (
        <ScrollView
          showsVerticalScrollIndicator={false}
          contentContainerStyle={styles.list}
        >
          <Text style={styles.sectionLabel}>ĐANG THAM GIA</Text>
          {families.map(item => (
            <FamilyCard
              key={item.id}
              item={item}
              isActive={item.id === activeFamilyId}
              onPress={() => handleSelectFamily(item)}
            />
          ))}
          <TouchableOpacity
            style={styles.addMoreBtn}
            activeOpacity={0.85}
            onPress={() => setSheetVisible(true)}
          >
            <MaterialCommunityIcons name="plus-circle-outline" size={20} color={colors.primary} />
            <Text style={styles.addMoreText}>Thêm gia đình</Text>
          </TouchableOpacity>
        </ScrollView>
      )}

      <AddFamilySheet
        visible={sheetVisible}
        onClose={() => setSheetVisible(false)}
        onCreateNew={() => {
          setSheetVisible(false);
          navigation.navigate('FamilyManagement');
        }}
        onJoinByCode={() => {
          setSheetVisible(false);
          navigation.navigate('FamilyManagement');
        }}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  root: { flex: 1, backgroundColor: '#F8FAFC' },

  header: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: 20,
    paddingVertical: 16,
    backgroundColor: '#fff',
    borderBottomWidth: 1,
    borderBottomColor: '#F1F5F9',
    ...shadows.sm,
  },
  headerTitle: {
    fontSize: 20,
    fontFamily: 'Manrope',
    fontWeight: '800',
    color: '#1E293B',
  },
  addBtn: {
    width: 40,
    height: 40,
    borderRadius: 20,
    backgroundColor: colors.primary,
    alignItems: 'center',
    justifyContent: 'center',
  },

  loadingWrap: { flex: 1, alignItems: 'center', justifyContent: 'center' },

  emptyWrap: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    paddingHorizontal: 32,
  },
  emptyIcon: {
    width: 96,
    height: 96,
    borderRadius: 48,
    backgroundColor: '#F1F5F9',
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 20,
  },
  emptyTitle: {
    fontSize: 18,
    fontFamily: 'Manrope',
    fontWeight: '800',
    color: '#1E293B',
    marginBottom: 8,
    textAlign: 'center',
  },
  emptySub: {
    fontSize: 14,
    fontFamily: 'Inter',
    color: '#64748B',
    textAlign: 'center',
    lineHeight: 22,
    marginBottom: 24,
  },
  emptyBtn: {
    backgroundColor: colors.primary,
    paddingHorizontal: 28,
    paddingVertical: 14,
    borderRadius: 16,
  },
  emptyBtnText: {
    fontSize: 15,
    fontFamily: 'Inter',
    fontWeight: '700',
    color: '#fff',
  },

  list: { paddingHorizontal: 16, paddingTop: 20, paddingBottom: 40 },
  sectionLabel: {
    fontSize: 11,
    fontFamily: 'Inter',
    fontWeight: '800',
    color: '#94A3B8',
    letterSpacing: 1.2,
    marginBottom: 12,
  },

  card: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#fff',
    borderRadius: 20,
    padding: 16,
    marginBottom: 12,
    borderWidth: 1.5,
    borderColor: '#F1F5F9',
    ...shadows.sm,
  },
  cardActive: {
    borderColor: colors.primary,
    backgroundColor: '#EFF6FF',
  },
  cardIconWrap: {
    width: 52,
    height: 52,
    borderRadius: 18,
    backgroundColor: '#EFF6FF',
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 14,
  },
  cardIconWrapActive: { backgroundColor: colors.primary },
  cardInfo: { flex: 1 },
  cardName: {
    fontSize: 16,
    fontFamily: 'Manrope',
    fontWeight: '700',
    color: '#1E293B',
    marginBottom: 4,
  },
  cardNameActive: { color: colors.primary },
  cardSub: { fontSize: 12, fontFamily: 'Inter', color: '#64748B' },
  cardRight: { alignItems: 'flex-end', gap: 6 },

  roleBadge: {
    backgroundColor: '#F1F5F9',
    borderRadius: 8,
    paddingHorizontal: 8,
    paddingVertical: 3,
  },
  roleBadgeOwner: { backgroundColor: '#DBEAFE' },
  roleBadgeText: { fontSize: 11, fontFamily: 'Inter', fontWeight: '700', color: '#64748B' },
  roleBadgeTextOwner: { color: colors.primary },

  addMoreBtn: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 8,
    paddingVertical: 16,
    marginTop: 4,
    borderRadius: 16,
    borderWidth: 1.5,
    borderColor: '#E2E8F0',
    borderStyle: 'dashed',
  },
  addMoreText: {
    fontSize: 14,
    fontFamily: 'Inter',
    fontWeight: '700',
    color: colors.primary,
  },

  // Bottom Sheet
  sheetOverlay: { flex: 1, backgroundColor: 'rgba(0,0,0,0.4)' },
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
    fontSize: 18,
    fontFamily: 'Manrope',
    fontWeight: '800',
    color: '#1E293B',
    marginBottom: 16,
  },
  sheetOption: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 14,
    gap: 14,
  },
  sheetOptionIcon: {
    width: 52,
    height: 52,
    borderRadius: 16,
    alignItems: 'center',
    justifyContent: 'center',
  },
  sheetOptionText: { flex: 1 },
  sheetOptionLabel: {
    fontSize: 15,
    fontFamily: 'Inter',
    fontWeight: '700',
    color: '#1E293B',
    marginBottom: 3,
  },
  sheetOptionSub: { fontSize: 13, fontFamily: 'Inter', color: '#64748B' },
});
