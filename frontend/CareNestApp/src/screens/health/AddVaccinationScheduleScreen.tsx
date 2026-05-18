import React, { useState } from 'react';
import {
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
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { useNavigation, useRoute } from '@react-navigation/native';
import type { RouteProp } from '@react-navigation/native';
import MaterialCommunityIcons from 'react-native-vector-icons/MaterialCommunityIcons';
import DateTimePicker, { DateTimePickerEvent, DateTimePickerAndroid } from '@react-native-community/datetimepicker';
import { colors } from '../../theme/colors';
import type { FamilyStackParamList } from '../../navigation/navigationTypes';
import { createVaccination } from '../../api/vaccinations';
import { formatLocalDate } from '../../utils/dateTime';

type AddVaccinationRoute = RouteProp<FamilyStackParamList, 'AddVaccinationSchedule'>;

const DOSE_OPTIONS = [
  { label: 'Mũi 1', value: 1 },
  { label: 'Mũi 2', value: 2 },
  { label: 'Mũi 3', value: 3 },
  { label: 'Mũi 4', value: 4 },
  { label: 'Nhắc lại', value: 99 },
];

export default function AddVaccinationScheduleScreen() {
  const insets = useSafeAreaInsets();
  const navigation = useNavigation<any>();
  const route = useRoute<AddVaccinationRoute>();
  const { profileId } = route.params;

  const [vaccineName, setVaccineName] = useState('');
  const [selectedDose, setSelectedDose] = useState(1);
  const [isCompleted, setIsCompleted] = useState(true);
  const [date, setDate] = useState(new Date());
  const [clinicName, setClinicName] = useState('');
  const [notes, setNotes] = useState('');
  const [showDatePicker, setShowDatePicker] = useState(false);

  const onDateChange = (_event: DateTimePickerEvent, selectedDate?: Date) => {
    setShowDatePicker(false);
    if (selectedDate) {
      setDate(selectedDate);
    }
  };

  const showDatePickerAndroid = () => {
    DateTimePickerAndroid.open({
      value: date,
      onChange: (_event, selectedDate) => {
        if (selectedDate) {
          setDate(selectedDate);
        }
      },
      mode: 'date',
      display: 'default',
    });
  };

  const handleOpenDatePicker = () => {
    if (Platform.OS === 'android') {
      showDatePickerAndroid();
    } else {
      setShowDatePicker(true);
    }
  };

  async function handleSave() {
    if (!vaccineName.trim()) {
      Alert.alert('Thiếu thông tin', 'Vui lòng nhập tên vắc xin.');
      return;
    }

    try {
      const dateValue = formatLocalDate(date);
      await createVaccination(profileId, {
        vaccineName: vaccineName.trim(),
        doseNumber: selectedDose,
        status: isCompleted ? 'COMPLETED' : 'PENDING',
        date: dateValue,
        location: clinicName.trim() || undefined,
        notes: notes.trim() || undefined,
      });

      Alert.alert('Đã lưu thành công', 'Mũi tiêm đã được thêm vào hồ sơ của bé.', [
        { text: 'OK', onPress: () => navigation.goBack() },
      ]);
    } catch (error) {
      Alert.alert('Không thể lưu dữ liệu', error instanceof Error ? error.message : 'Đã có lỗi xảy ra');
    }
  }

  return (
    <KeyboardAvoidingView
      behavior={Platform.OS === 'ios' ? 'padding' : undefined}
      style={[styles.root, { paddingTop: insets.top }]}
    >
      <View style={styles.header}>
        <TouchableOpacity style={styles.backBtn} onPress={() => navigation.goBack()}>
          <MaterialCommunityIcons name="arrow-left" size={24} color={colors.primary} />
        </TouchableOpacity>
        <Text style={styles.headerTitle}>Ghi nhận tiêm chủng</Text>
        <View style={styles.helpBtn} />
      </View>

      <ScrollView showsVerticalScrollIndicator={false} contentContainerStyle={styles.scrollContent}>
        <Text style={styles.introText}>
          Ghi nhận từng mũi tiêm cụ thể để theo dõi đầy đủ và chính xác lịch trình phòng bệnh của bé.
        </Text>

        <View style={styles.card}>
          <Text style={styles.cardTitle}>Thông tin vắc xin</Text>

          {/* Câu 1: Tên Vaccine */}
          <Text style={styles.inputLabel}>TÊN VẮC XIN</Text>
          <View style={styles.inputWrap}>
            <MaterialCommunityIcons name="needle" size={20} color="#64748b" />
            <TextInput
              style={styles.input}
              value={vaccineName}
              onChangeText={setVaccineName}
              placeholder="Ví dụ: Vắc xin 6 trong 1 Hexaxim"
              placeholderTextColor="#94a3b8"
            />
          </View>

          {/* Câu 2: Đây là mũi thứ mấy? */}
          <Text style={[styles.inputLabel, { marginTop: 24 }]}>ĐÂY LÀ MŨI THỨ MẤY?</Text>
          <View style={styles.doseGrid}>
            {DOSE_OPTIONS.map((opt) => {
              const isSelected = selectedDose === opt.value;
              return (
                <TouchableOpacity
                  key={opt.value}
                  style={[styles.doseChip, isSelected && styles.doseChipActive]}
                  onPress={() => setSelectedDose(opt.value)}
                  activeOpacity={0.8}
                >
                  <Text style={[styles.doseText, isSelected && styles.doseTextActive]}>
                    {opt.label}
                  </Text>
                </TouchableOpacity>
              );
            })}
          </View>

          {/* Câu 3: Trạng thái tiêm */}
          <Text style={[styles.inputLabel, { marginTop: 24 }]}>TRẠNG THÁI</Text>
          <View style={styles.choiceRow}>
            <TouchableOpacity
              style={[styles.choiceChip, isCompleted && styles.choiceChipActive]}
              onPress={() => setIsCompleted(true)}
              activeOpacity={0.8}
            >
              <MaterialCommunityIcons
                name="check-circle"
                size={16}
                color={isCompleted ? '#fff' : '#64748b'}
                style={{ marginRight: 6 }}
              />
              <Text style={[styles.choiceText, isCompleted && styles.choiceTextActive]}>Đã tiêm</Text>
            </TouchableOpacity>
            <TouchableOpacity
              style={[styles.choiceChip, !isCompleted && styles.choiceChipActive]}
              onPress={() => setIsCompleted(false)}
              activeOpacity={0.8}
            >
              <MaterialCommunityIcons
                name="calendar-clock"
                size={16}
                color={!isCompleted ? '#fff' : '#64748b'}
                style={{ marginRight: 6 }}
              />
              <Text style={[styles.choiceText, !isCompleted && styles.choiceTextActive]}>Lịch dự kiến</Text>
            </TouchableOpacity>
          </View>
        </View>

        <View style={styles.card}>
          <Text style={styles.cardTitle}>Chi tiết mũi tiêm</Text>

          {/* Câu 4: Ngày tiêm / Ngày hẹn */}
          <Text style={styles.inputLabel}>
            {isCompleted ? 'NGÀY TIÊM THỰC TẾ' : 'NGÀY HẸN / DỰ KIẾN'}
          </Text>
          <TouchableOpacity style={styles.inputWrap} onPress={handleOpenDatePicker} activeOpacity={0.7}>
            <MaterialCommunityIcons name="calendar-month-outline" size={20} color="#64748b" />
            <Text style={styles.input}>{date.toLocaleDateString('vi-VN')}</Text>
            <MaterialCommunityIcons name="calendar" size={20} color="#64748b" style={{ marginLeft: 'auto' }} />
          </TouchableOpacity>

          {/* Tùy chọn: Địa điểm tiêm */}
          <Text style={[styles.inputLabel, { marginTop: 24 }]}>ĐỊA ĐIỂM TIÊM (TÙY CHỌN)</Text>
          <View style={styles.inputWrap}>
            <MaterialCommunityIcons name="hospital-building" size={20} color="#64748b" />
            <TextInput
              style={styles.input}
              value={clinicName}
              onChangeText={setClinicName}
              placeholder="Ví dụ: Trung tâm Tiêm chủng VNVC"
              placeholderTextColor="#94a3b8"
            />
          </View>

          {/* Tùy chọn: Ghi chú */}
          <Text style={[styles.inputLabel, { marginTop: 24 }]}>GHI CHÚ (TÙY CHỌN)</Text>
          <View style={[styles.inputWrap, styles.notesInputWrap]}>
            <MaterialCommunityIcons name="file-document-edit-outline" size={20} color="#64748b" style={{ marginTop: 2 }} />
            <TextInput
              style={[styles.input, styles.notesInput]}
              value={notes}
              onChangeText={setNotes}
              placeholder="Phản ứng sau tiêm hoặc lưu ý theo dõi sức khỏe cho bé..."
              placeholderTextColor="#94a3b8"
              multiline
              textAlignVertical="top"
            />
          </View>
        </View>
      </ScrollView>

      <View style={[styles.footer, { paddingBottom: insets.bottom + 20 }]}>
        <TouchableOpacity style={styles.submitBtn} activeOpacity={0.8} onPress={() => void handleSave()}>
          <MaterialCommunityIcons name="check-circle-outline" size={24} color="#fff" />
          <Text style={styles.submitBtnText}>Lưu mũi tiêm</Text>
        </TouchableOpacity>
      </View>

      {showDatePicker ? <DateTimePicker value={date} mode="date" display="default" onChange={onDateChange} /> : null}
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  root: { flex: 1, backgroundColor: '#f0f7ff' },
  header: { height: 60, flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', paddingHorizontal: 20, backgroundColor: 'transparent' },
  backBtn: { width: 40, height: 40, alignItems: 'center', justifyContent: 'center' },
  helpBtn: { width: 40, height: 40 },
  headerTitle: { fontSize: 20, fontWeight: '800', color: colors.primary, fontFamily: 'Manrope' },
  scrollContent: { paddingHorizontal: 24, paddingBottom: 40 },
  introText: { fontSize: 13, color: '#64748b', textAlign: 'center', lineHeight: 18, marginTop: 16, marginBottom: 24, paddingHorizontal: 10, fontFamily: 'Inter' },
  card: { backgroundColor: '#fff', borderRadius: 24, padding: 20, marginBottom: 20, shadowColor: '#1a73e8', shadowOffset: { width: 0, height: 10 }, shadowOpacity: 0.03, shadowRadius: 20, elevation: 2 },
  cardTitle: { fontSize: 16, fontWeight: '800', color: '#1e293b', marginBottom: 20, fontFamily: 'Manrope' },
  inputLabel: { fontSize: 11, fontWeight: '800', color: '#64748b', marginBottom: 10, letterSpacing: 0.5, marginLeft: 4, fontFamily: 'Inter' },
  inputWrap: { flexDirection: 'row', alignItems: 'center', backgroundColor: '#f1f5f9', borderRadius: 16, paddingHorizontal: 16, height: 56 },
  input: { flex: 1, marginLeft: 12, fontSize: 15, color: '#1e293b', fontWeight: '600', fontFamily: 'Inter' },
  choiceRow: { flexDirection: 'row', gap: 12 },
  choiceChip: { flex: 1, height: 48, borderRadius: 16, backgroundColor: '#f1f5f9', flexDirection: 'row', alignItems: 'center', justifyContent: 'center' },
  choiceChipActive: { backgroundColor: colors.primary },
  choiceText: { fontSize: 13, fontWeight: '700', color: '#475569', fontFamily: 'Inter' },
  choiceTextActive: { color: '#fff' },
  doseGrid: { flexDirection: 'row', flexWrap: 'wrap', gap: 8 },
  doseChip: { paddingHorizontal: 16, height: 40, borderRadius: 20, backgroundColor: '#f1f5f9', alignItems: 'center', justifyContent: 'center', borderWidth: 1, borderColor: '#e2e8f0' },
  doseChipActive: { backgroundColor: colors.primary, borderColor: colors.primary },
  doseText: { fontSize: 13, fontWeight: '700', color: '#475569', fontFamily: 'Inter' },
  doseTextActive: { color: '#fff' },
  notesInputWrap: { height: 120, alignItems: 'flex-start', paddingVertical: 14 },
  notesInput: { marginLeft: 12, height: '100%', textAlignVertical: 'top' },
  footer: { paddingHorizontal: 24, paddingVertical: 10 },
  submitBtn: { height: 56, backgroundColor: colors.primary, borderRadius: 28, flexDirection: 'row', alignItems: 'center', justifyContent: 'center', gap: 10, shadowColor: '#2563eb', shadowOffset: { width: 0, height: 8 }, shadowOpacity: 0.2, shadowRadius: 16, elevation: 8 },
  submitBtnText: { color: '#fff', fontSize: 16, fontWeight: '800', fontFamily: 'Inter' },
});
