import React, { useCallback, useEffect, useState } from 'react';
import {
  ActivityIndicator,
  Alert,
  FlatList,
  KeyboardAvoidingView,
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
  getPendingVerifications,
  rejectVerification,
  type DoctorVerification,
} from '../../api/doctorVerification';
import { colors } from '../../theme/colors';

export default function AdminVerificationScreen() {
  const navigation = useNavigation<any>();
  const insets = useSafeAreaInsets();
  const [items, setItems] = useState<DoctorVerification[]>([]);
  const [loading, setLoading] = useState(true);
  const [actionId, setActionId] = useState<number | null>(null);
  const [rejectTarget, setRejectTarget] = useState<DoctorVerification | null>(null);
  const [rejectionReason, setRejectionReason] = useState('');

  const loadPending = useCallback(async () => {
    try {
      setLoading(true);
      const result = await getPendingVerifications();
      setItems(result);
    } catch (error) {
      Alert.alert('Không thể tải danh sách', error instanceof Error ? error.message : 'Đã có lỗi xảy ra');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void loadPending();
  }, [loadPending]);

  const handleApprove = async (id: number) => {
    try {
      setActionId(id);
      await approveVerification(id);
      setItems(current => current.filter(item => item.id !== id));
      Alert.alert('Đã phê duyệt', 'Tài khoản đã được cấp quyền Bác sĩ.');
    } catch (error) {
      Alert.alert('Không thể phê duyệt', error instanceof Error ? error.message : 'Đã có lỗi xảy ra');
    } finally {
      setActionId(null);
    }
  };

  const openRejectModal = (item: DoctorVerification) => {
    setRejectTarget(item);
    setRejectionReason('');
  };

  const closeRejectModal = () => {
    if (actionId) {
      return;
    }
    setRejectTarget(null);
    setRejectionReason('');
  };

  const handleReject = async () => {
    if (!rejectTarget) {
      return;
    }

    const reason = rejectionReason.trim();
    if (!reason) {
      Alert.alert('Thiếu lý do', 'Vui lòng nhập lý do từ chối hồ sơ.');
      return;
    }

    try {
      setActionId(rejectTarget.id);
      await rejectVerification(rejectTarget.id, reason);
      setItems(current => current.filter(item => item.id !== rejectTarget.id));
      setRejectTarget(null);
      setRejectionReason('');
      Alert.alert('Đã từ chối', 'Lý do từ chối đã được lưu vào hồ sơ.');
    } catch (error) {
      Alert.alert('Không thể từ chối', error instanceof Error ? error.message : 'Đã có lỗi xảy ra');
    } finally {
      setActionId(null);
    }
  };

  const renderItem = ({ item }: { item: DoctorVerification }) => {
    const busy = actionId === item.id;

    return (
      <View style={styles.card}>
        <View style={styles.cardHeader}>
          <View style={styles.avatar}>
            <Text style={styles.avatarText}>{(item.userFullName || item.userEmail || '?').charAt(0).toUpperCase()}</Text>
          </View>
          <View style={styles.identity}>
            <Text style={styles.name}>{item.userFullName || 'Unknown user'}</Text>
            <Text style={styles.email}>{item.userEmail || 'No email'}</Text>
          </View>
          <View style={styles.pendingBadge}>
            <Text style={styles.pendingBadgeText}>PENDING</Text>
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
          <View style={styles.infoBlockWide}>
            <Text style={styles.infoLabel}>Tài liệu</Text>
            <Text style={styles.documentValue} numberOfLines={2}>{item.documentUrl}</Text>
          </View>
        </View>

        <View style={styles.actionRow}>
          <TouchableOpacity
            style={[styles.actionButton, styles.rejectButton]}
            onPress={() => openRejectModal(item)}
            disabled={busy}
            activeOpacity={0.86}
          >
            <MaterialCommunityIcons name="close-circle" size={18} color="#dc2626" />
            <Text style={styles.rejectText}>Từ chối</Text>
          </TouchableOpacity>
          <TouchableOpacity
            style={[styles.actionButton, styles.approveButton]}
            onPress={() => void handleApprove(item.id)}
            disabled={busy}
            activeOpacity={0.86}
          >
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

  return (
    <View style={styles.root}>
      <View style={[styles.header, { paddingTop: insets.top + 10 }]}>
        <TouchableOpacity style={styles.iconButton} onPress={() => navigation.goBack()} hitSlop={10}>
          <MaterialCommunityIcons name="arrow-left" size={24} color="#0f172a" />
        </TouchableOpacity>
        <Text style={styles.headerTitle}>Duyệt Bác sĩ</Text>
        <TouchableOpacity style={styles.iconButton} onPress={() => void loadPending()} hitSlop={10}>
          <MaterialCommunityIcons name="refresh" size={22} color={colors.primary} />
        </TouchableOpacity>
      </View>

      {loading ? (
        <View style={styles.centerState}>
          <ActivityIndicator color={colors.primary} size="large" />
          <Text style={styles.emptyText}>Đang tải danh sách hồ sơ...</Text>
        </View>
      ) : (
        <FlatList
          data={items}
          renderItem={renderItem}
          keyExtractor={item => String(item.id)}
          contentContainerStyle={[
            styles.listContent,
            items.length === 0 && styles.emptyListContent,
            { paddingBottom: insets.bottom + 24 },
          ]}
          ListEmptyComponent={
            <View style={styles.emptyState}>
              <MaterialCommunityIcons name="clipboard-check-outline" size={56} color="#94a3b8" />
              <Text style={styles.emptyTitle}>Không có hồ sơ chờ duyệt</Text>
              <Text style={styles.emptyText}>Tất cả yêu cầu hiện tại đã được xử lý.</Text>
            </View>
          }
        />
      )}

      <Modal transparent visible={!!rejectTarget} animationType="fade" onRequestClose={closeRejectModal}>
        <KeyboardAvoidingView
          style={styles.modalBackdrop}
          behavior={Platform.OS === 'ios' ? 'padding' : undefined}
        >
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
                disabled={!!actionId}
              >
                <Text style={styles.cancelText}>Hủy</Text>
              </TouchableOpacity>
              <TouchableOpacity
                style={[styles.modalButton, styles.confirmRejectButton, !!actionId && styles.disabledButton]}
                onPress={() => void handleReject()}
                disabled={!!actionId}
              >
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
  iconButton: {
    width: 44,
    height: 44,
    alignItems: 'center',
    justifyContent: 'center',
  },
  headerTitle: { flex: 1, textAlign: 'center', fontSize: 18, fontWeight: '900', color: '#0f172a' },
  listContent: { padding: 16, gap: 14 },
  emptyListContent: { flexGrow: 1, justifyContent: 'center' },
  card: {
    borderRadius: 8,
    backgroundColor: '#fff',
    borderWidth: 1,
    borderColor: '#e2e8f0',
    padding: 14,
  },
  cardHeader: { flexDirection: 'row', alignItems: 'center', gap: 12 },
  avatar: {
    width: 44,
    height: 44,
    borderRadius: 22,
    backgroundColor: '#dbeafe',
    alignItems: 'center',
    justifyContent: 'center',
  },
  avatarText: { fontSize: 17, fontWeight: '900', color: colors.primary },
  identity: { flex: 1 },
  name: { fontSize: 15, fontWeight: '900', color: '#0f172a' },
  email: { fontSize: 12, color: '#64748b', marginTop: 2 },
  pendingBadge: {
    paddingHorizontal: 9,
    paddingVertical: 5,
    borderRadius: 8,
    backgroundColor: '#fef3c7',
  },
  pendingBadgeText: { fontSize: 11, fontWeight: '900', color: '#92400e' },
  infoGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 10,
    marginTop: 16,
  },
  infoBlock: {
    width: '48%',
    borderRadius: 8,
    backgroundColor: '#f8fafc',
    padding: 10,
  },
  infoBlockWide: {
    width: '100%',
    borderRadius: 8,
    backgroundColor: '#f8fafc',
    padding: 10,
  },
  infoLabel: { fontSize: 11, fontWeight: '800', color: '#64748b', marginBottom: 4 },
  infoValue: { fontSize: 13, fontWeight: '800', color: '#0f172a' },
  documentValue: { fontSize: 13, color: colors.primary, lineHeight: 18 },
  actionRow: { flexDirection: 'row', gap: 10, marginTop: 16 },
  actionButton: {
    flex: 1,
    height: 44,
    borderRadius: 8,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 8,
  },
  rejectButton: {
    backgroundColor: '#fef2f2',
    borderWidth: 1,
    borderColor: '#fecaca',
  },
  approveButton: { backgroundColor: '#16a34a' },
  rejectText: { fontSize: 14, fontWeight: '900', color: '#dc2626' },
  approveText: { fontSize: 14, fontWeight: '900', color: '#fff' },
  centerState: { flex: 1, alignItems: 'center', justifyContent: 'center', padding: 24 },
  emptyState: { alignItems: 'center', padding: 24 },
  emptyTitle: { fontSize: 18, fontWeight: '900', color: '#0f172a', marginTop: 12, marginBottom: 6 },
  emptyText: { fontSize: 14, color: '#64748b', textAlign: 'center' },
  modalBackdrop: {
    flex: 1,
    backgroundColor: 'rgba(15,23,42,0.45)',
    justifyContent: 'center',
    padding: 20,
  },
  modalCard: {
    borderRadius: 8,
    backgroundColor: '#fff',
    padding: 18,
  },
  modalTitle: { fontSize: 18, fontWeight: '900', color: '#0f172a', marginBottom: 6 },
  modalSubtitle: { fontSize: 13, color: '#64748b', lineHeight: 19, marginBottom: 14 },
  reasonInput: {
    minHeight: 120,
    borderRadius: 8,
    borderWidth: 1,
    borderColor: '#dbe3ee',
    padding: 12,
    color: '#0f172a',
    fontSize: 14,
    lineHeight: 20,
  },
  modalActions: { flexDirection: 'row', gap: 10, marginTop: 16 },
  modalButton: {
    flex: 1,
    height: 46,
    borderRadius: 8,
    alignItems: 'center',
    justifyContent: 'center',
  },
  cancelButton: { backgroundColor: '#f1f5f9' },
  confirmRejectButton: { backgroundColor: '#dc2626' },
  disabledButton: { opacity: 0.65 },
  cancelText: { color: '#334155', fontWeight: '900', fontSize: 14 },
  confirmRejectText: { color: '#fff', fontWeight: '900', fontSize: 14 },
});
