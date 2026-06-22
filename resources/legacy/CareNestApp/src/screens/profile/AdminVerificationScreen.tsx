import React, { useCallback, useEffect, useState } from 'react';
import {
  ActivityIndicator,
  Alert,
  FlatList,
  Image,
  KeyboardAvoidingView,
  Linking,
  Modal,
  Platform,
  StyleSheet,
  Text,
  TextInput,
  TouchableOpacity,
  View,
} from 'react-native';
import { useNavigation } from '@react-navigation/native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import MaterialCommunityIcons from 'react-native-vector-icons/MaterialCommunityIcons';
import {
  approveVerification,
  getAllDoctors,
  getPendingVerifications,
  rejectVerification,
  revokeDoctor,
  type DoctorSummary,
  type DoctorVerification,
} from '../../api/doctorVerification';
import { colors } from '../../theme/colors';

type Tab = 'pending' | 'doctors';

export default function AdminVerificationScreen() {
  const navigation = useNavigation<any>();
  const insets = useSafeAreaInsets();
  const [activeTab, setActiveTab] = useState<Tab>('pending');

  // ── Pending tab state ───────────────────────────────────────────────────────
  const [pendingItems, setPendingItems] = useState<DoctorVerification[]>([]);
  const [pendingLoading, setPendingLoading] = useState(true);
  const [actionId, setActionId] = useState<number | null>(null);
  const [rejectTarget, setRejectTarget] = useState<DoctorVerification | null>(null);
  const [rejectionReason, setRejectionReason] = useState('');

  // ── Doctors tab state ───────────────────────────────────────────────────────
  const [doctors, setDoctors] = useState<DoctorSummary[]>([]);
  const [doctorsLoading, setDoctorsLoading] = useState(false);
  const [revokeTarget, setRevokeTarget] = useState<DoctorSummary | null>(null);

  // ── Load pending verifications ──────────────────────────────────────────────
  const loadPending = useCallback(async () => {
    try {
      setPendingLoading(true);
      const result = await getPendingVerifications();
      setPendingItems(result);
    } catch (error) {
      Alert.alert('Không thể tải danh sách', error instanceof Error ? error.message : 'Đã có lỗi xảy ra');
    } finally {
      setPendingLoading(false);
    }
  }, []);

  // ── Load doctors ────────────────────────────────────────────────────────────
  const loadDoctors = useCallback(async () => {
    try {
      setDoctorsLoading(true);
      const result = await getAllDoctors();
      setDoctors(result);
    } catch (error) {
      Alert.alert('Không thể tải danh sách bác sĩ', error instanceof Error ? error.message : 'Đã có lỗi xảy ra');
    } finally {
      setDoctorsLoading(false);
    }
  }, []);

  useEffect(() => { void loadPending(); }, [loadPending]);

  useEffect(() => {
    if (activeTab === 'doctors') {
      void loadDoctors();
    }
  }, [activeTab, loadDoctors]);

  // ── Approve ─────────────────────────────────────────────────────────────────
  const handleApprove = async (id: number) => {
    try {
      setActionId(id);
      await approveVerification(id);
      setPendingItems(current => current.filter(item => item.id !== id));
      Alert.alert('Đã phê duyệt ✅', 'Tài khoản đã được cấp quyền Bác sĩ.');
    } catch (error) {
      Alert.alert('Không thể phê duyệt', error instanceof Error ? error.message : 'Đã có lỗi xảy ra');
    } finally {
      setActionId(null);
    }
  };

  // ── Reject ──────────────────────────────────────────────────────────────────
  const openRejectModal = (item: DoctorVerification) => {
    setRejectTarget(item);
    setRejectionReason('');
  };

  const closeRejectModal = () => {
    if (actionId) return;
    setRejectTarget(null);
    setRejectionReason('');
  };

  const handleReject = async () => {
    if (!rejectTarget) return;
    const reason = rejectionReason.trim();
    if (!reason) {
      Alert.alert('Thiếu lý do', 'Vui lòng nhập lý do từ chối hồ sơ.');
      return;
    }
    try {
      setActionId(rejectTarget.id);
      await rejectVerification(rejectTarget.id, reason);
      setPendingItems(current => current.filter(item => item.id !== rejectTarget.id));
      setRejectTarget(null);
      setRejectionReason('');
      Alert.alert('Đã từ chối', 'Lý do từ chối đã được lưu vào hồ sơ.');
    } catch (error) {
      Alert.alert('Không thể từ chối', error instanceof Error ? error.message : 'Đã có lỗi xảy ra');
    } finally {
      setActionId(null);
    }
  };

  // ── Revoke ──────────────────────────────────────────────────────────────────
  const confirmRevoke = (doctor: DoctorSummary) => {
    Alert.alert(
      'Thu hồi quyền Bác sĩ',
      `Bạn có chắc muốn thu hồi quyền Bác sĩ của ${doctor.fullName}? Họ sẽ phải nộp hồ sơ lại.`,
      [
        { text: 'Hủy', style: 'cancel' },
        { text: 'Thu hồi', style: 'destructive', onPress: () => void handleRevoke(doctor) },
      ],
    );
  };

  const handleRevoke = async (doctor: DoctorSummary) => {
    try {
      setRevokeTarget(doctor);
      await revokeDoctor(doctor.id);
      setDoctors(current => current.filter(d => d.id !== doctor.id));
      Alert.alert('Đã thu hồi', `Quyền Bác sĩ của ${doctor.fullName} đã bị thu hồi.`);
    } catch (error) {
      Alert.alert('Không thể thu hồi', error instanceof Error ? error.message : 'Đã có lỗi xảy ra');
    } finally {
      setRevokeTarget(null);
    }
  };

  // ── Render pending card ─────────────────────────────────────────────────────
  const renderPendingItem = ({ item }: { item: DoctorVerification }) => {
    const busy = actionId === item.id;
    return (
      <View style={styles.card}>
        <View style={styles.cardHeader}>
          <View style={styles.avatar}>
            <Text style={styles.avatarText}>
              {(item.userFullName || item.userEmail || '?').charAt(0).toUpperCase()}
            </Text>
          </View>
          <View style={styles.identity}>
            <Text style={styles.name}>{item.userFullName || 'Không rõ'}</Text>
            <Text style={styles.email}>{item.userEmail || ''}</Text>
          </View>
          <View style={styles.pendingBadge}>
            <Text style={styles.pendingBadgeText}>CHỜ DUYỆT</Text>
          </View>
        </View>

        <View style={styles.infoGrid}>
          <View style={styles.infoBlock}>
            <Text style={styles.infoLabel}>Số chứng chỉ</Text>
            <Text style={styles.infoValue}>{item.certificationNumber}</Text>
          </View>
          <View style={styles.infoBlock}>
            <Text style={styles.infoLabel}>Chuyên khoa</Text>
            <Text style={styles.infoValue}>{item.specialty}</Text>
          </View>
          <View style={styles.infoBlockWide}>
            <Text style={styles.infoLabel}>Đơn vị công tác</Text>
            <Text style={styles.infoValue}>{item.hospitalName}</Text>
          </View>
          {item.documentUrl ? (
            <View style={styles.infoBlockWide}>
              <Text style={styles.infoLabel}>Tài liệu chứng chỉ</Text>
              <TouchableOpacity onPress={() => Linking.openURL(item.documentUrl).catch(() => {})} activeOpacity={0.8}>
                <Image
                  source={{ uri: item.documentUrl }}
                  style={styles.docImage}
                  resizeMode="cover"
                />
                <Text style={styles.tapHint}>Nhấn để xem đầy đủ</Text>
              </TouchableOpacity>
            </View>
          ) : null}
        </View>

        <View style={styles.actionRow}>
          <TouchableOpacity
            style={[styles.actionButton, styles.rejectButton]}
            onPress={() => openRejectModal(item)}
            disabled={busy}
            activeOpacity={0.86}>
            <MaterialCommunityIcons name="close-circle" size={18} color="#dc2626" />
            <Text style={styles.rejectText}>Từ chối</Text>
          </TouchableOpacity>
          <TouchableOpacity
            style={[styles.actionButton, styles.approveButton]}
            onPress={() => void handleApprove(item.id)}
            disabled={busy}
            activeOpacity={0.86}>
            {busy ? (
              <ActivityIndicator size="small" color="#fff" />
            ) : (
              <>
                <MaterialCommunityIcons name="check-circle" size={18} color="#fff" />
                <Text style={styles.approveText}>Phê duyệt</Text>
              </>
            )}
          </TouchableOpacity>
        </View>
      </View>
    );
  };

  // ── Render doctor card ──────────────────────────────────────────────────────
  const renderDoctorItem = ({ item }: { item: DoctorSummary }) => {
    const isRevoking = revokeTarget?.id === item.id;
    return (
      <View style={styles.card}>
        <View style={styles.cardHeader}>
          {item.avatarUrl ? (
            <Image source={{ uri: item.avatarUrl }} style={styles.avatarImg} />
          ) : (
            <View style={[styles.avatar, { backgroundColor: '#dcfce7' }]}>
              <Text style={[styles.avatarText, { color: '#16a34a' }]}>
                {(item.fullName || item.email || '?').charAt(0).toUpperCase()}
              </Text>
            </View>
          )}
          <View style={styles.identity}>
            <Text style={styles.name}>{item.fullName}</Text>
            <Text style={styles.email}>{item.email}</Text>
          </View>
          <View style={[styles.pendingBadge, { backgroundColor: '#dcfce7' }]}>
            <MaterialCommunityIcons name="check-decagram" size={12} color="#16a34a" />
            <Text style={[styles.pendingBadgeText, { color: '#166534', marginLeft: 4 }]}>BÁC SĨ</Text>
          </View>
        </View>

        <View style={styles.infoGrid}>
          {item.specialty ? (
            <View style={styles.infoBlock}>
              <Text style={styles.infoLabel}>Chuyên khoa</Text>
              <Text style={styles.infoValue}>{item.specialty}</Text>
            </View>
          ) : null}
          {item.certificationNumber ? (
            <View style={styles.infoBlock}>
              <Text style={styles.infoLabel}>Số chứng chỉ</Text>
              <Text style={styles.infoValue}>{item.certificationNumber}</Text>
            </View>
          ) : null}
          {item.hospitalName ? (
            <View style={styles.infoBlockWide}>
              <Text style={styles.infoLabel}>Đơn vị công tác</Text>
              <Text style={styles.infoValue}>{item.hospitalName}</Text>
            </View>
          ) : null}
        </View>

        <TouchableOpacity
          style={[styles.revokeButton, isRevoking && { opacity: 0.6 }]}
          onPress={() => confirmRevoke(item)}
          disabled={isRevoking}
          activeOpacity={0.82}>
          {isRevoking ? (
            <ActivityIndicator size="small" color="#dc2626" />
          ) : (
            <>
              <MaterialCommunityIcons name="shield-remove" size={16} color="#dc2626" />
              <Text style={styles.revokeText}>Thu hồi quyền Bác sĩ</Text>
            </>
          )}
        </TouchableOpacity>
      </View>
    );
  };

  const refresh = () => {
    if (activeTab === 'pending') void loadPending();
    else void loadDoctors();
  };

  return (
    <View style={styles.root}>
      {/* Header */}
      <View style={[styles.header, { paddingTop: insets.top + 10 }]}>
        <TouchableOpacity style={styles.iconButton} onPress={() => navigation.goBack()} hitSlop={10}>
          <MaterialCommunityIcons name="arrow-left" size={24} color="#0f172a" />
        </TouchableOpacity>
        <Text style={styles.headerTitle}>Quản lý Bác sĩ</Text>
        <TouchableOpacity style={styles.iconButton} onPress={refresh} hitSlop={10}>
          <MaterialCommunityIcons name="refresh" size={22} color={colors.primary} />
        </TouchableOpacity>
      </View>

      {/* Tabs */}
      <View style={styles.tabBar}>
        <TouchableOpacity
          style={[styles.tab, activeTab === 'pending' && styles.tabActive]}
          onPress={() => setActiveTab('pending')}
          activeOpacity={0.8}>
          <MaterialCommunityIcons
            name="clock-outline"
            size={16}
            color={activeTab === 'pending' ? colors.primary : '#94a3b8'}
          />
          <Text style={[styles.tabText, activeTab === 'pending' && styles.tabTextActive]}>
            Chờ duyệt
          </Text>
          {pendingItems.length > 0 && (
            <View style={styles.badge}>
              <Text style={styles.badgeText}>{pendingItems.length}</Text>
            </View>
          )}
        </TouchableOpacity>
        <TouchableOpacity
          style={[styles.tab, activeTab === 'doctors' && styles.tabActive]}
          onPress={() => setActiveTab('doctors')}
          activeOpacity={0.8}>
          <MaterialCommunityIcons
            name="doctor"
            size={16}
            color={activeTab === 'doctors' ? colors.primary : '#94a3b8'}
          />
          <Text style={[styles.tabText, activeTab === 'doctors' && styles.tabTextActive]}>
            Danh sách Bác sĩ
          </Text>
          {doctors.length > 0 && activeTab === 'doctors' && (
            <View style={[styles.badge, { backgroundColor: '#dcfce7' }]}>
              <Text style={[styles.badgeText, { color: '#166534' }]}>{doctors.length}</Text>
            </View>
          )}
        </TouchableOpacity>
      </View>

      {/* Content */}
      {activeTab === 'pending' ? (
        pendingLoading ? (
          <View style={styles.centerState}>
            <ActivityIndicator color={colors.primary} size="large" />
            <Text style={styles.emptyText}>Đang tải hồ sơ chờ duyệt...</Text>
          </View>
        ) : (
          <FlatList
            data={pendingItems}
            renderItem={renderPendingItem}
            keyExtractor={item => String(item.id)}
            contentContainerStyle={[
              styles.listContent,
              pendingItems.length === 0 && styles.emptyListContent,
              { paddingBottom: insets.bottom + 24 },
            ]}
            ListEmptyComponent={
              <View style={styles.emptyState}>
                <MaterialCommunityIcons name="clipboard-check-outline" size={64} color="#94a3b8" />
                <Text style={styles.emptyTitle}>Không có hồ sơ chờ duyệt</Text>
                <Text style={styles.emptyText}>Tất cả yêu cầu hiện tại đã được xử lý.</Text>
              </View>
            }
          />
        )
      ) : (
        doctorsLoading ? (
          <View style={styles.centerState}>
            <ActivityIndicator color={colors.primary} size="large" />
            <Text style={styles.emptyText}>Đang tải danh sách bác sĩ...</Text>
          </View>
        ) : (
          <FlatList
            data={doctors}
            renderItem={renderDoctorItem}
            keyExtractor={item => String(item.id)}
            contentContainerStyle={[
              styles.listContent,
              doctors.length === 0 && styles.emptyListContent,
              { paddingBottom: insets.bottom + 24 },
            ]}
            ListEmptyComponent={
              <View style={styles.emptyState}>
                <MaterialCommunityIcons name="doctor" size={64} color="#94a3b8" />
                <Text style={styles.emptyTitle}>Chưa có Bác sĩ nào</Text>
                <Text style={styles.emptyText}>Các hồ sơ được phê duyệt sẽ hiển thị ở đây.</Text>
              </View>
            }
          />
        )
      )}

      {/* Reject Modal */}
      <Modal transparent visible={!!rejectTarget} animationType="fade" onRequestClose={closeRejectModal}>
        <KeyboardAvoidingView
          style={styles.modalBackdrop}
          behavior={Platform.OS === 'ios' ? 'padding' : undefined}>
          <View style={styles.modalCard}>
            <Text style={styles.modalTitle}>Lý do từ chối</Text>
            <Text style={styles.modalSubtitle}>
              Lý do này sẽ hiển thị cho người nộp hồ sơ để họ sửa và gửi lại.
            </Text>
            <TextInput
              style={styles.reasonInput}
              value={rejectionReason}
              onChangeText={setRejectionReason}
              placeholder="Nhập lý do từ chối..."
              placeholderTextColor="#94a3b8"
              multiline
              textAlignVertical="top"
            />
            <View style={styles.modalActions}>
              <TouchableOpacity
                style={[styles.modalButton, styles.cancelButton]}
                onPress={closeRejectModal}
                disabled={!!actionId}>
                <Text style={styles.cancelText}>Hủy</Text>
              </TouchableOpacity>
              <TouchableOpacity
                style={[styles.modalButton, styles.confirmRejectButton, !!actionId && styles.disabledButton]}
                onPress={() => void handleReject()}
                disabled={!!actionId}>
                {actionId ? (
                  <ActivityIndicator size="small" color="#fff" />
                ) : (
                  <Text style={styles.confirmRejectText}>Gửi từ chối</Text>
                )}
              </TouchableOpacity>
            </View>
          </View>
        </KeyboardAvoidingView>
      </Modal>
    </View>
  );
}

const styles = StyleSheet.create({
  root: { flex: 1, backgroundColor: '#f8fafc' },
  header: {
    minHeight: 64,
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 16,
    paddingBottom: 12,
    backgroundColor: '#fff',
    borderBottomWidth: 1,
    borderBottomColor: '#e2e8f0',
  },
  iconButton: { width: 44, height: 44, alignItems: 'center', justifyContent: 'center' },
  headerTitle: { flex: 1, textAlign: 'center', fontSize: 18, fontWeight: '900', color: '#0f172a' },

  // ── Tabs ──────────────────────────────────────────────────────────────────
  tabBar: {
    flexDirection: 'row',
    backgroundColor: '#fff',
    borderBottomWidth: 1,
    borderBottomColor: '#e2e8f0',
    paddingHorizontal: 8,
  },
  tab: {
    flex: 1,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 6,
    paddingVertical: 12,
    borderBottomWidth: 2,
    borderBottomColor: 'transparent',
  },
  tabActive: { borderBottomColor: colors.primary },
  tabText: { fontSize: 13, fontWeight: '700', color: '#94a3b8' },
  tabTextActive: { color: colors.primary },
  badge: {
    minWidth: 20,
    height: 20,
    borderRadius: 10,
    backgroundColor: '#fef3c7',
    alignItems: 'center',
    justifyContent: 'center',
    paddingHorizontal: 5,
  },
  badgeText: { fontSize: 11, fontWeight: '900', color: '#92400e' },

  // ── List ──────────────────────────────────────────────────────────────────
  listContent: { padding: 16, gap: 14 },
  emptyListContent: { flexGrow: 1, justifyContent: 'center' },

  // ── Card ──────────────────────────────────────────────────────────────────
  card: {
    borderRadius: 12,
    backgroundColor: '#fff',
    borderWidth: 1,
    borderColor: '#e2e8f0',
    padding: 14,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.05,
    shadowRadius: 4,
    elevation: 2,
  },
  cardHeader: { flexDirection: 'row', alignItems: 'center', gap: 12 },
  avatar: {
    width: 46,
    height: 46,
    borderRadius: 23,
    backgroundColor: '#dbeafe',
    alignItems: 'center',
    justifyContent: 'center',
  },
  avatarImg: { width: 46, height: 46, borderRadius: 23 },
  avatarText: { fontSize: 18, fontWeight: '900', color: colors.primary },
  identity: { flex: 1 },
  name: { fontSize: 15, fontWeight: '900', color: '#0f172a' },
  email: { fontSize: 12, color: '#64748b', marginTop: 2 },
  pendingBadge: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 9,
    paddingVertical: 5,
    borderRadius: 8,
    backgroundColor: '#fef3c7',
  },
  pendingBadgeText: { fontSize: 11, fontWeight: '900', color: '#92400e' },

  // ── Info grid ──────────────────────────────────────────────────────────────
  infoGrid: { flexDirection: 'row', flexWrap: 'wrap', gap: 10, marginTop: 14 },
  infoBlock: { width: '48%', borderRadius: 8, backgroundColor: '#f8fafc', padding: 10 },
  infoBlockWide: { width: '100%', borderRadius: 8, backgroundColor: '#f8fafc', padding: 10 },
  infoLabel: { fontSize: 11, fontWeight: '800', color: '#64748b', marginBottom: 4 },
  infoValue: { fontSize: 13, fontWeight: '700', color: '#0f172a' },
  docImage: {
    width: '100%',
    height: 180,
    borderRadius: 8,
    marginTop: 8,
    backgroundColor: '#e2e8f0',
  },
  tapHint: { fontSize: 11, color: colors.primary, marginTop: 6, textAlign: 'center' },

  // ── Action row (pending) ───────────────────────────────────────────────────
  actionRow: { flexDirection: 'row', gap: 10, marginTop: 14 },
  actionButton: {
    flex: 1,
    height: 44,
    borderRadius: 10,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 8,
  },
  rejectButton: { backgroundColor: '#fef2f2', borderWidth: 1, borderColor: '#fecaca' },
  approveButton: { backgroundColor: '#16a34a' },
  rejectText: { fontSize: 14, fontWeight: '900', color: '#dc2626' },
  approveText: { fontSize: 14, fontWeight: '900', color: '#fff' },

  // ── Revoke button (doctors) ────────────────────────────────────────────────
  revokeButton: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 8,
    marginTop: 14,
    height: 42,
    borderRadius: 10,
    borderWidth: 1.5,
    borderColor: '#fca5a5',
    backgroundColor: '#fef2f2',
  },
  revokeText: { fontSize: 13, fontWeight: '900', color: '#dc2626' },

  // ── Empty state ────────────────────────────────────────────────────────────
  centerState: { flex: 1, alignItems: 'center', justifyContent: 'center', padding: 24 },
  emptyState: { alignItems: 'center', padding: 32 },
  emptyTitle: { fontSize: 18, fontWeight: '900', color: '#0f172a', marginTop: 14, marginBottom: 8 },
  emptyText: { fontSize: 14, color: '#64748b', textAlign: 'center', lineHeight: 20 },

  // ── Modal ──────────────────────────────────────────────────────────────────
  modalBackdrop: {
    flex: 1,
    backgroundColor: 'rgba(15,23,42,0.5)',
    justifyContent: 'center',
    padding: 20,
  },
  modalCard: { borderRadius: 14, backgroundColor: '#fff', padding: 20 },
  modalTitle: { fontSize: 18, fontWeight: '900', color: '#0f172a', marginBottom: 6 },
  modalSubtitle: { fontSize: 13, color: '#64748b', lineHeight: 19, marginBottom: 14 },
  reasonInput: {
    minHeight: 120,
    borderRadius: 10,
    borderWidth: 1,
    borderColor: '#dbe3ee',
    padding: 12,
    color: '#0f172a',
    fontSize: 14,
    lineHeight: 20,
  },
  modalActions: { flexDirection: 'row', gap: 10, marginTop: 16 },
  modalButton: { flex: 1, height: 46, borderRadius: 10, alignItems: 'center', justifyContent: 'center' },
  cancelButton: { backgroundColor: '#f1f5f9' },
  confirmRejectButton: { backgroundColor: '#dc2626' },
  disabledButton: { opacity: 0.65 },
  cancelText: { color: '#334155', fontWeight: '900', fontSize: 14 },
  confirmRejectText: { color: '#fff', fontWeight: '900', fontSize: 14 },
});
