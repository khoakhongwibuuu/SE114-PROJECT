import React, { useCallback, useEffect, useMemo, useState } from 'react';
import {
  ActivityIndicator,
  Alert,
  KeyboardAvoidingView,
  Platform,
  ScrollView,
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
  getMyVerificationStatus,
  submitVerification,
  type DoctorVerification,
} from '../../api/doctorVerification';
import { useAuth } from '../../context/AuthContext';
import { colors } from '../../theme/colors';

type FormState = {
  certificationNumber: string;
  specialty: string;
  hospitalName: string;
  documentUrl: string;
};

const emptyForm: FormState = {
  certificationNumber: '',
  specialty: '',
  hospitalName: '',
  documentUrl: '',
};

function buildFormFromVerification(verification: DoctorVerification | null): FormState {
  if (!verification) {
    return emptyForm;
  }

  return {
    certificationNumber: verification.certificationNumber || '',
    specialty: verification.specialty || '',
    hospitalName: verification.hospitalName || '',
    documentUrl: verification.documentUrl || '',
  };
}

function Field({
  label,
  value,
  onChangeText,
  placeholder,
  multiline,
}: {
  label: string;
  value: string;
  onChangeText: (value: string) => void;
  placeholder: string;
  multiline?: boolean;
}) {
  return (
    <View style={styles.fieldWrap}>
      <Text style={styles.label}>{label}</Text>
      <TextInput
        style={[styles.input, multiline && styles.textArea]}
        value={value}
        onChangeText={onChangeText}
        placeholder={placeholder}
        placeholderTextColor="#94a3b8"
        multiline={multiline}
        textAlignVertical={multiline ? 'top' : 'center'}
        autoCapitalize="none"
      />
    </View>
  );
}

export default function DoctorVerificationScreen() {
  const navigation = useNavigation<any>();
  const insets = useSafeAreaInsets();
  const { refreshUser } = useAuth();
  const [verification, setVerification] = useState<DoctorVerification | null>(null);
  const [form, setForm] = useState<FormState>(emptyForm);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);

  const canSubmit = useMemo(() => {
    return (
      form.certificationNumber.trim().length > 0 &&
      form.specialty.trim().length > 0 &&
      form.hospitalName.trim().length > 0 &&
      form.documentUrl.trim().length > 0 &&
      !submitting
    );
  }, [form, submitting]);

  const loadStatus = useCallback(async () => {
    try {
      setLoading(true);
      const result = await getMyVerificationStatus();
      setVerification(result);
      setForm(buildFormFromVerification(result));
    } catch (error) {
      Alert.alert('Khong the tai ho so', error instanceof Error ? error.message : 'Da co loi xay ra');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void loadStatus();
  }, [loadStatus]);

  const updateForm = (key: keyof FormState, value: string) => {
    setForm(current => ({ ...current, [key]: value }));
  };

  const handleSubmit = async () => {
    if (!canSubmit) {
      Alert.alert('Thieu thong tin', 'Vui long dien day du thong tin chung chi hanh nghe.');
      return;
    }

    try {
      setSubmitting(true);
      const result = await submitVerification({
        certificationNumber: form.certificationNumber.trim(),
        specialty: form.specialty.trim(),
        hospitalName: form.hospitalName.trim(),
        documentUrl: form.documentUrl.trim(),
      });
      setVerification(result);
      setForm(buildFormFromVerification(result));
      Alert.alert('Da gui ho so', 'Ho so cua ban dang duoc xet duyet.');
    } catch (error) {
      Alert.alert('Khong the gui ho so', error instanceof Error ? error.message : 'Da co loi xay ra');
    } finally {
      setSubmitting(false);
    }
  };

  const goToCommunity = () => {
    void refreshUser().catch(() => undefined);
    navigation.getParent()?.navigate('CommunityTab');
  };

  const renderForm = () => (
    <View style={styles.formCard}>
      <Field
        label="So chung chi hanh nghe"
        value={form.certificationNumber}
        onChangeText={value => updateForm('certificationNumber', value)}
        placeholder="VD: CCHN-012345"
      />
      <Field
        label="Chuyen khoa"
        value={form.specialty}
        onChangeText={value => updateForm('specialty', value)}
        placeholder="VD: Nhi khoa"
      />
      <Field
        label="Benh vien / phong kham"
        value={form.hospitalName}
        onChangeText={value => updateForm('hospitalName', value)}
        placeholder="VD: Benh vien Nhi Dong"
      />
      <Field
        label="Anh chung chi / tai lieu"
        value={form.documentUrl}
        onChangeText={value => updateForm('documentUrl', value)}
        placeholder="Nhap URL tai lieu hoac chuoi Base64 tam thoi"
        multiline
      />
      <TouchableOpacity
        style={[styles.submitButton, !canSubmit && styles.submitButtonDisabled]}
        onPress={() => void handleSubmit()}
        disabled={!canSubmit}
        activeOpacity={0.86}
      >
        {submitting ? (
          <ActivityIndicator color="#fff" />
        ) : (
          <>
            <MaterialCommunityIcons name="send-check" size={20} color="#fff" />
            <Text style={styles.submitText}>Gui ho so xac thuc</Text>
          </>
        )}
      </TouchableOpacity>
    </View>
  );

  const renderContent = () => {
    if (loading) {
      return (
        <View style={styles.centerState}>
          <ActivityIndicator color={colors.primary} size="large" />
          <Text style={styles.stateText}>Dang tai trang thai ho so...</Text>
        </View>
      );
    }

    if (!verification) {
      return (
        <>
          <View style={styles.introCard}>
            <MaterialCommunityIcons name="doctor" size={28} color={colors.primary} />
            <View style={styles.introCopy}>
              <Text style={styles.introTitle}>Xac thuc Bac si CareNest</Text>
              <Text style={styles.introText}>Ho so se duoc admin kiem tra truoc khi cap quyen viet cam nang y te.</Text>
            </View>
          </View>
          {renderForm()}
        </>
      );
    }

    if (verification.status === 'PENDING') {
      return (
        <View style={styles.centerState}>
          <View style={[styles.bigIconCircle, styles.pendingCircle]}>
            <MaterialCommunityIcons name="timer-sand" size={58} color="#b45309" />
          </View>
          <Text style={styles.stateTitle}>Ho so dang duoc xet duyet</Text>
          <Text style={styles.stateText}>Ho so cua ban dang duoc xet duyet. Vui long cho 24h-48h.</Text>
        </View>
      );
    }

    if (verification.status === 'APPROVED') {
      return (
        <View style={styles.centerState}>
          <View style={[styles.bigIconCircle, styles.approvedCircle]}>
            <MaterialCommunityIcons name="check-circle" size={64} color="#16a34a" />
          </View>
          <Text style={styles.stateTitle}>Ban da la Bac si cua CareNest!</Text>
          <Text style={styles.stateText}>Tai khoan cua ban da duoc cap quyen tao bai viet chuyen mon.</Text>
          <TouchableOpacity style={styles.communityButton} onPress={goToCommunity} activeOpacity={0.86}>
            <Text style={styles.communityButtonText}>Den trang Cong dong</Text>
            <MaterialCommunityIcons name="arrow-right" size={20} color="#fff" />
          </TouchableOpacity>
        </View>
      );
    }

    return (
      <>
        <View style={styles.rejectedCard}>
          <MaterialCommunityIcons name="alert-circle" size={24} color="#dc2626" />
          <View style={styles.rejectedCopy}>
            <Text style={styles.rejectedTitle}>Ho so bi tu choi</Text>
            <Text style={styles.rejectedText}>
              {verification.rejectionReason || 'Admin chua cung cap ly do cu the.'}
            </Text>
          </View>
        </View>
        {renderForm()}
      </>
    );
  };

  return (
    <KeyboardAvoidingView
      style={styles.root}
      behavior={Platform.OS === 'ios' ? 'padding' : undefined}
    >
      <View style={[styles.header, { paddingTop: insets.top + 10 }]}>
        <TouchableOpacity style={styles.iconButton} onPress={() => navigation.goBack()} hitSlop={10}>
          <MaterialCommunityIcons name="arrow-left" size={24} color="#0f172a" />
        </TouchableOpacity>
        <Text style={styles.headerTitle}>Xac thuc Bac si</Text>
        <View style={styles.iconButton} />
      </View>

      <ScrollView
        contentContainerStyle={[styles.content, { paddingBottom: insets.bottom + 32 }]}
        keyboardShouldPersistTaps="handled"
        showsVerticalScrollIndicator={false}
      >
        {renderContent()}
      </ScrollView>
    </KeyboardAvoidingView>
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
  content: { padding: 18 },
  introCard: {
    flexDirection: 'row',
    gap: 14,
    padding: 16,
    borderRadius: 8,
    backgroundColor: '#e0f2fe',
    borderWidth: 1,
    borderColor: '#bae6fd',
    marginBottom: 16,
  },
  introCopy: { flex: 1 },
  introTitle: { fontSize: 16, fontWeight: '900', color: '#0f172a', marginBottom: 4 },
  introText: { fontSize: 13, lineHeight: 19, color: '#475569' },
  formCard: {
    borderRadius: 8,
    backgroundColor: '#fff',
    borderWidth: 1,
    borderColor: '#e2e8f0',
    padding: 16,
  },
  fieldWrap: { marginBottom: 14 },
  label: { fontSize: 13, fontWeight: '800', color: '#334155', marginBottom: 8 },
  input: {
    minHeight: 50,
    borderRadius: 8,
    borderWidth: 1,
    borderColor: '#dbe3ee',
    paddingHorizontal: 14,
    paddingVertical: 10,
    color: '#0f172a',
    backgroundColor: '#fff',
    fontSize: 15,
  },
  textArea: { minHeight: 92, lineHeight: 21 },
  submitButton: {
    height: 52,
    borderRadius: 8,
    backgroundColor: colors.primary,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 10,
    marginTop: 4,
  },
  submitButtonDisabled: { opacity: 0.55 },
  submitText: { color: '#fff', fontSize: 15, fontWeight: '900' },
  centerState: {
    minHeight: 420,
    alignItems: 'center',
    justifyContent: 'center',
    paddingHorizontal: 20,
  },
  bigIconCircle: {
    width: 112,
    height: 112,
    borderRadius: 56,
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 20,
  },
  pendingCircle: { backgroundColor: '#fef3c7' },
  approvedCircle: { backgroundColor: '#dcfce7' },
  stateTitle: { fontSize: 20, fontWeight: '900', color: '#0f172a', textAlign: 'center', marginBottom: 8 },
  stateText: { fontSize: 14, lineHeight: 21, color: '#64748b', textAlign: 'center' },
  rejectedCard: {
    flexDirection: 'row',
    gap: 12,
    padding: 16,
    borderRadius: 8,
    backgroundColor: '#fef2f2',
    borderWidth: 1,
    borderColor: '#fecaca',
    marginBottom: 16,
  },
  rejectedCopy: { flex: 1 },
  rejectedTitle: { fontSize: 16, fontWeight: '900', color: '#991b1b', marginBottom: 4 },
  rejectedText: { fontSize: 13, lineHeight: 19, color: '#7f1d1d' },
  communityButton: {
    marginTop: 24,
    height: 52,
    borderRadius: 8,
    backgroundColor: colors.primary,
    paddingHorizontal: 18,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 10,
  },
  communityButtonText: { color: '#fff', fontSize: 15, fontWeight: '900' },
});
