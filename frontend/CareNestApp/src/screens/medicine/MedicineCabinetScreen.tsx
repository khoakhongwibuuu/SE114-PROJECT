import React, { useCallback, useMemo, useState } from 'react';
import {
  Alert,
  FlatList,
  Modal,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  TouchableOpacity,
  View,
} from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { useFocusEffect, useNavigation } from '@react-navigation/native';
import type { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { colors } from '../../theme/colors';
import { shadows } from '../../theme/spacing';
import { TOP_BAR_HEIGHT, BOTTOM_NAV_HEIGHT } from '../../utils/constants';
import Icon from '../../components/common/Icon';
import TopAppBar from '../../components/layout/TopAppBar';
import FAB from '../../components/common/FAB';
import type { MedicineStackParamList } from '../../navigation/navigationTypes';
import { getCabinetMedicines, updateCabinetMedicine, deleteCabinetMedicine, type MedicineItem } from '../../api/medicine';

type Nav = NativeStackNavigationProp<MedicineStackParamList, 'MedicineCabinet'>;
type FilterKey = 'all' | 'expired' | 'expiring' | 'low_stock' | 'out_of_stock';
type CabinetStatus = 'stable' | 'expiring' | 'expired' | 'out_of_stock' | 'low_stock';

const FILTERS: { key: FilterKey; label: string }[] = [
  { key: 'all', label: 'Tất cả' },
  { key: 'expired', label: 'Hết hạn' },
  { key: 'expiring', label: 'Sắp hết hạn' },
  { key: 'low_stock', label: 'Sắp hết hàng' },
  { key: 'out_of_stock', label: 'Hết hàng' },
];

export default function MedicineCabinetScreen() {
  const navigation = useNavigation<Nav>();
  const insets = useSafeAreaInsets();
  const [filter, setFilter] = useState<FilterKey>('all');
  const [medicines, setMedicines] = useState<MedicineItem[]>([]);
  const [selectedMedicine, setSelectedMedicine] = useState<MedicineItem | null>(null);
  const [sheetVisible, setSheetVisible] = useState(false);
  const [isEditingQuantity, setIsEditingQuantity] = useState(false);
  const [newQuantity, setNewQuantity] = useState('');

  const loadMedicines = useCallback(async () => {
    await getCabinetMedicines()
      .then(setMedicines)
      .catch(() => setMedicines([]));
  }, []);

  const handleOpenSheet = (item: MedicineItem) => {
    setSelectedMedicine(item);
    setNewQuantity(String(item.quantity));
    setIsEditingQuantity(false);
    setSheetVisible(true);
  };

  const handleQuickTake = async () => {
    if (!selectedMedicine) return;
    if (selectedMedicine.quantity <= 0) {
      Alert.alert('Hết hàng', 'Thuốc này đã hết trong tủ thuốc!');
      return;
    }
    try {
      const newQty = Math.max(0, selectedMedicine.quantity - 1);
      await updateCabinetMedicine(selectedMedicine.id, {
        quantity: newQty
      });
      setSheetVisible(false);
      void loadMedicines();
    } catch (e) {
      Alert.alert('Lỗi', 'Không thể cập nhật số lượng thuốc.');
    }
  };

  const handleSaveQuantity = async () => {
    if (!selectedMedicine) return;
    const qtyVal = Number(newQuantity);
    if (isNaN(qtyVal) || qtyVal < 0) {
      Alert.alert('Không hợp lệ', 'Vui lòng nhập số lượng hợp lệ.');
      return;
    }
    try {
      await updateCabinetMedicine(selectedMedicine.id, {
        quantity: qtyVal
      });
      setSheetVisible(false);
      void loadMedicines();
    } catch (e) {
      Alert.alert('Lỗi', 'Không thể cập nhật số lượng thuốc.');
    }
  };

  const handleDeleteMedicine = async () => {
    if (!selectedMedicine) return;
    Alert.alert(
      'Xóa thuốc',
      `Bạn có chắc chắn muốn xóa ${selectedMedicine.name} khỏi tủ thuốc không?`,
      [
        { text: 'Hủy', style: 'cancel' },
        { 
          text: 'Xóa', 
          style: 'destructive',
          onPress: async () => {
            try {
              await deleteCabinetMedicine(selectedMedicine.id);
              setSheetVisible(false);
              void loadMedicines();
            } catch (e) {
              Alert.alert('Lỗi', 'Không thể xóa thuốc khỏi tủ.');
            }
          }
        }
      ]
    );
  };

  useFocusEffect(
    useCallback(() => {
      void loadMedicines();
      return undefined;
    }, [loadMedicines]),
  );

  const filteredMedicines = useMemo(() => {
    if (filter === 'all') {
      return medicines;
    }

    return medicines.filter(item => mapStatus(item.status) === filter);
  }, [filter, medicines]);

  const alertCount = medicines.filter(item => {
    const status = mapStatus(item.status);
    return status === 'expired' || status === 'expiring';
  }).length;

  return (
    <View style={styles.root}>
      <TopAppBar variant="detail" title="Tủ thuốc gia đình" />
      <FlatList
        data={filteredMedicines.length > 0 ? filteredMedicines : []}
        keyExtractor={(item) => String(item.id)}
        contentContainerStyle={[
          styles.scroll,
          { paddingTop: TOP_BAR_HEIGHT + insets.top + 16, paddingBottom: BOTTOM_NAV_HEIGHT + 80 },
        ]}
        showsVerticalScrollIndicator={false}
        ListHeaderComponent={() => (
          <>
            {alertCount > 0 ? (
              <View style={styles.alertBanner}>
                <Icon name="warning" size={18} color={colors.onErrorContainer} />
                <Text style={styles.alertBannerText}>{alertCount} loại thuốc cần kiểm tra</Text>
              </View>
            ) : null}

            <TouchableOpacity
              style={styles.ocrCard}
              onPress={() => navigation.navigate('OcrScanner')}
              activeOpacity={0.85}
            >
              <View style={styles.ocrIconWrap}>
                <Icon name="document_scanner" size={24} color={colors.primary} />
              </View>
              <View style={styles.ocrInfo}>
                <Text style={styles.ocrTitle}>Quét toa thuốc</Text>
                <Text style={styles.ocrSub}>Dùng AI để thêm thuốc và lịch uống nhanh hơn</Text>
              </View>
              <Icon name="chevron_right" size={20} color={colors.outlineVariant} />
            </TouchableOpacity>

            <ScrollView horizontal showsHorizontalScrollIndicator={false} contentContainerStyle={styles.filterRow}>
              {FILTERS.map(item => (
                <TouchableOpacity
                  key={item.key}
                  style={[styles.filterChip, filter === item.key && styles.filterChipActive]}
                  onPress={() => setFilter(item.key)}
                  activeOpacity={0.8}
                >
                  <Text style={[styles.filterChipText, filter === item.key && styles.filterChipTextActive]}>
                    {item.label}
                  </Text>
                </TouchableOpacity>
              ))}
            </ScrollView>

            <View style={styles.section}>
              <Text style={styles.countText}>{filteredMedicines.length} loại thuốc</Text>
            </View>
          </>
        )}
        ListEmptyComponent={() => (
          <View style={[styles.card, shadows.sm]}>
            <View style={styles.emptyRow}>
              <Text style={styles.emptyText}>Chưa có thuốc nào trong tủ</Text>
            </View>
          </View>
        )}
        renderItem={({ item: medicine, index }) => {
          const status = mapStatus(medicine.status);
          const config = STATUS_CONFIG[status];
          const isFirst = index === 0;
          const isLast = index === filteredMedicines.length - 1;
          
          return (
            <TouchableOpacity 
              style={[
                styles.card, 
                shadows.sm, 
                { borderRadius: 0 },
                isFirst && { borderTopLeftRadius: 16, borderTopRightRadius: 16 },
                isLast && { borderBottomLeftRadius: 16, borderBottomRightRadius: 16, marginBottom: 16 }
              ]}
              onPress={() => handleOpenSheet(medicine)}
              activeOpacity={0.8}
            >
              <View
                style={[styles.medRow, !isLast && styles.medRowDivider]}
              >
                <View style={styles.medIconWrap}>
                  <Icon name="pill" size={22} color={colors.primary} />
                </View>
                <View style={styles.medContent}>
                  <Text style={styles.medName}>{medicine.name}</Text>
                  <Text style={styles.medMeta}>
                    {medicine.quantity} {medicine.unit}
                    {medicine.expiryDate ? ` · HSD: ${medicine.expiryDate}` : ''}
                  </Text>
                </View>
                <View style={[styles.statusBadge, { backgroundColor: config.bg }]}>
                  <Text style={[styles.statusText, { color: config.textColor }]}>{config.label}</Text>
                </View>
              </View>
            </TouchableOpacity>
          );
        }}
      />
      <FAB
        iconName="add"
        onPress={() => navigation.navigate('AddMedicineToCabinet', {})}
        bottomOffset={BOTTOM_NAV_HEIGHT - 55}
      />

      <Modal transparent visible={sheetVisible} animationType="slide" onRequestClose={() => setSheetVisible(false)}>
        <TouchableOpacity style={styles.sheetBackdrop} activeOpacity={1} onPress={() => setSheetVisible(false)}>
          <View style={styles.sheetCard} onStartShouldSetResponder={() => true}>
            <View style={styles.sheetHandle} />
            <Text style={styles.sheetTitle}>{selectedMedicine?.name}</Text>
            <Text style={styles.sheetSub}>
              Đơn vị: {selectedMedicine?.unit} · Số lượng hiện tại: <Text style={{ fontWeight: '700', color: colors.primary }}>{selectedMedicine?.quantity}</Text>
            </Text>

            {!isEditingQuantity ? (
              <View style={styles.sheetActions}>
                <TouchableOpacity style={styles.actionBtn} onPress={() => void handleQuickTake()} activeOpacity={0.75}>
                  <Icon name="local_hospital" size={20} color={colors.primary} />
                  <Text style={styles.actionBtnText}>Uống nhanh 1 {selectedMedicine?.unit || 'viên'}</Text>
                </TouchableOpacity>

                <TouchableOpacity style={styles.actionBtn} onPress={() => setIsEditingQuantity(true)} activeOpacity={0.75}>
                  <Icon name="edit" size={20} color={colors.primary} />
                  <Text style={styles.actionBtnText}>Chỉnh sửa số lượng</Text>
                </TouchableOpacity>

                <TouchableOpacity style={[styles.actionBtn, styles.deleteActionBtn]} onPress={() => void handleDeleteMedicine()} activeOpacity={0.75}>
                  <Icon name="delete" size={20} color="#C62828" />
                  <Text style={[styles.actionBtnText, { color: '#C62828' }]}>Xóa khỏi tủ thuốc</Text>
                </TouchableOpacity>
              </View>
            ) : (
              <View style={styles.editSection}>
                <Text style={styles.editLabel}>Nhập số lượng mới:</Text>
                <TextInput
                  style={styles.quantityInput}
                  value={newQuantity}
                  onChangeText={setNewQuantity}
                  keyboardType="numeric"
                  autoFocus
                />
                <View style={styles.editActions}>
                  <TouchableOpacity style={styles.cancelEditBtn} onPress={() => setIsEditingQuantity(false)} activeOpacity={0.8}>
                    <Text style={styles.cancelEditText}>Hủy</Text>
                  </TouchableOpacity>
                  <TouchableOpacity style={styles.saveEditBtn} onPress={() => void handleSaveQuantity()} activeOpacity={0.8}>
                    <Text style={styles.saveEditText}>Lưu</Text>
                  </TouchableOpacity>
                </View>
              </View>
            )}

            <TouchableOpacity style={styles.closeBtn} onPress={() => setSheetVisible(false)} activeOpacity={0.8}>
              <Text style={styles.closeBtnText}>Đóng</Text>
            </TouchableOpacity>
          </View>
        </TouchableOpacity>
      </Modal>
    </View>
  );
}

const STATUS_CONFIG: Record<CabinetStatus, { label: string; bg: string; textColor: string }> = {
  stable: { label: 'Ổn định', bg: '#E8F5E9', textColor: '#2E7D32' },
  expiring: { label: 'Sắp hết hạn', bg: '#FFEBEE', textColor: '#C62828' },
  expired: { label: 'Hết hạn', bg: colors.errorContainer, textColor: colors.onErrorContainer },
  out_of_stock: { label: 'Hết hàng', bg: '#ECEFF1', textColor: '#546E7A' },
  low_stock: { label: 'Sắp hết hàng', bg: '#FFF3E0', textColor: '#E65100' },
};

function mapStatus(status: string): CabinetStatus {
  const normalized = status.toUpperCase();
  if (normalized.includes('EXPIRED')) return 'expired';
  if (normalized.includes('EXPIRING')) return 'expiring';
  if (normalized.includes('LOW_STOCK')) return 'low_stock';
  if (normalized.includes('OUT')) return 'out_of_stock';
  return 'stable';
}

const styles = StyleSheet.create({
  root: { flex: 1, backgroundColor: colors.surface },
  scroll: { paddingHorizontal: 16, gap: 14 },
  alertBanner: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
    backgroundColor: colors.errorContainer,
    borderRadius: 12,
    paddingHorizontal: 14,
    paddingVertical: 10,
  },
  alertBannerText: { fontSize: 13, fontFamily: 'Inter', fontWeight: '600', color: colors.onErrorContainer },
  ocrCard: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 12,
    backgroundColor: colors.primaryFixed,
    borderRadius: 14,
    padding: 14,
  },
  ocrIconWrap: {
    width: 44,
    height: 44,
    borderRadius: 12,
    backgroundColor: '#fff',
    alignItems: 'center',
    justifyContent: 'center',
  },
  ocrInfo: { flex: 1 },
  ocrTitle: { fontSize: 14, fontFamily: 'Manrope', fontWeight: '700', color: colors.primary },
  ocrSub: { fontSize: 12, fontFamily: 'Inter', color: `${colors.primary}CC`, marginTop: 2 },
  filterRow: { flexDirection: 'row', gap: 8, paddingVertical: 2 },
  filterChip: {
    paddingHorizontal: 16,
    paddingVertical: 8,
    backgroundColor: colors.surfaceContainerHigh,
    borderRadius: 999,
  },
  filterChipActive: { backgroundColor: colors.primary },
  filterChipText: { fontSize: 13, fontFamily: 'Inter', fontWeight: '600', color: colors.onSurface },
  filterChipTextActive: { color: colors.onPrimary },
  section: { gap: 6 },
  countText: { fontSize: 12, fontFamily: 'Inter', color: colors.onSurfaceVariant, marginLeft: 2 },
  card: { backgroundColor: colors.surfaceContainerLowest, borderRadius: 16, overflow: 'hidden' },
  emptyRow: { padding: 24, alignItems: 'center' },
  emptyText: { fontSize: 14, fontFamily: 'Inter', color: colors.onSurfaceVariant, fontStyle: 'italic' },
  medRow: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 14,
    paddingVertical: 14,
    gap: 12,
  },
  medRowDivider: { borderBottomWidth: StyleSheet.hairlineWidth, borderBottomColor: colors.outlineVariant },
  medIconWrap: {
    width: 44,
    height: 44,
    borderRadius: 12,
    backgroundColor: colors.primaryFixed,
    alignItems: 'center',
    justifyContent: 'center',
  },
  medContent: { flex: 1 },
  medName: { fontSize: 14, fontFamily: 'Inter', fontWeight: '600', color: colors.onSurface },
  medMeta: { fontSize: 12, fontFamily: 'Inter', color: colors.onSurfaceVariant, marginTop: 2 },
  statusBadge: { borderRadius: 8, paddingHorizontal: 8, paddingVertical: 4, alignSelf: 'flex-start' },
  statusText: { fontSize: 11, fontFamily: 'Inter', fontWeight: '700' },
  sheetBackdrop: { flex: 1, backgroundColor: 'rgba(15, 23, 42, 0.45)', justifyContent: 'flex-end' },
  sheetCard: { backgroundColor: colors.surfaceContainerLowest, borderTopLeftRadius: 24, borderTopRightRadius: 24, padding: 24, gap: 16, paddingBottom: 40 },
  sheetHandle: { width: 36, height: 4, borderRadius: 2, backgroundColor: colors.outlineVariant, alignSelf: 'center', marginBottom: 4 },
  sheetTitle: { fontSize: 20, fontFamily: 'Manrope', fontWeight: '800', color: colors.onSurface, textAlign: 'center' },
  sheetSub: { fontSize: 13, fontFamily: 'Inter', color: colors.onSurfaceVariant, textAlign: 'center', marginBottom: 8 },
  sheetActions: { gap: 12 },
  actionBtn: { flexDirection: 'row', alignItems: 'center', height: 50, borderRadius: 12, backgroundColor: colors.surfaceContainerHigh, paddingHorizontal: 16, gap: 12 },
  deleteActionBtn: { backgroundColor: '#FFEBEE' },
  actionBtnText: { fontSize: 14, fontFamily: 'Inter', fontWeight: '700', color: colors.onSurface },
  editSection: { gap: 12 },
  editLabel: { fontSize: 13, fontFamily: 'Inter', fontWeight: '600', color: colors.onSurfaceVariant },
  quantityInput: { height: 48, borderRadius: 12, borderWidth: 1, borderColor: colors.outlineVariant, backgroundColor: colors.surfaceContainerHigh, paddingHorizontal: 16, fontSize: 16, color: colors.onSurface },
  editActions: { flexDirection: 'row', gap: 12, marginTop: 4 },
  cancelEditBtn: { flex: 1, height: 44, borderRadius: 8, backgroundColor: colors.surfaceContainerHigh, alignItems: 'center', justifyContent: 'center' },
  cancelEditText: { fontSize: 14, fontFamily: 'Inter', fontWeight: '700', color: colors.onSurfaceVariant },
  saveEditBtn: { flex: 1, height: 44, borderRadius: 8, backgroundColor: colors.primary, alignItems: 'center', justifyContent: 'center' },
  saveEditText: { fontSize: 14, fontFamily: 'Inter', fontWeight: '700', color: '#fff' },
  closeBtn: { height: 48, borderRadius: 999, borderWidth: 1, borderColor: colors.outlineVariant, alignItems: 'center', justifyContent: 'center', marginTop: 8 },
  closeBtnText: { fontSize: 14, fontFamily: 'Inter', fontWeight: '700', color: colors.onSurfaceVariant },
});
