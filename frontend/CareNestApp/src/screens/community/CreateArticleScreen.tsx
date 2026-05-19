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
import { useNavigation } from '@react-navigation/native';
import MaterialCommunityIcons from 'react-native-vector-icons/MaterialCommunityIcons';
import { createArticle } from '../../api/community';
import { colors } from '../../theme/colors';

export default function CreateArticleScreen() {
  const navigation = useNavigation<any>();
  const [title, setTitle] = useState('');
  const [tags, setTags] = useState('');
  const [content, setContent] = useState('');
  const [saving, setSaving] = useState(false);

  const canSubmit = title.trim().length > 0 && content.trim().length > 0 && !saving;

  const handleSubmit = async () => {
    if (!canSubmit) {
      Alert.alert('Thieu thong tin', 'Vui long nhap tieu de va noi dung bai viet.');
      return;
    }

    try {
      setSaving(true);
      await createArticle({
        title: title.trim(),
        tags: tags.trim() || undefined,
        content: content.trim(),
      });
      navigation.goBack();
    } catch (error) {
      Alert.alert(
        'Khong the tao bai viet',
        error instanceof Error ? error.message : 'Da co loi xay ra',
      );
      setSaving(false);
    }
  };

  return (
    <KeyboardAvoidingView
      style={styles.root}
      behavior={Platform.OS === 'ios' ? 'padding' : undefined}
    >
      <View style={styles.header}>
        <TouchableOpacity style={styles.iconButton} onPress={() => navigation.goBack()}>
          <MaterialCommunityIcons name="arrow-left" size={24} color="#0f172a" />
        </TouchableOpacity>
        <Text style={styles.headerTitle}>Tao bai viet</Text>
        <View style={styles.headerSpacer} />
      </View>

      <ScrollView
        contentContainerStyle={styles.content}
        keyboardShouldPersistTaps="handled"
        showsVerticalScrollIndicator={false}
      >
        <Text style={styles.label}>Tieu de</Text>
        <TextInput
          style={styles.input}
          value={title}
          onChangeText={setTitle}
          placeholder="Vi du: Cham soc tre sot tai nha"
          placeholderTextColor="#94a3b8"
        />

        <Text style={styles.label}>Category / tags</Text>
        <TextInput
          style={styles.input}
          value={tags}
          onChangeText={setTags}
          placeholder="Nhi khoa, sot, dinh duong"
          placeholderTextColor="#94a3b8"
        />

        <Text style={styles.label}>Noi dung</Text>
        <TextInput
          style={[styles.input, styles.textArea]}
          value={content}
          onChangeText={setContent}
          placeholder="Nhap noi dung bai viet..."
          placeholderTextColor="#94a3b8"
          multiline
          textAlignVertical="top"
        />

        <TouchableOpacity
          style={[styles.submitButton, !canSubmit && styles.submitButtonDisabled]}
          disabled={!canSubmit}
          onPress={() => void handleSubmit()}
          activeOpacity={0.86}
        >
          <Text style={styles.submitText}>{saving ? 'Dang luu...' : 'Dang bai'}</Text>
        </TouchableOpacity>
      </ScrollView>
    </KeyboardAvoidingView>
  );
}

const styles = StyleSheet.create({
  root: { flex: 1, backgroundColor: '#f8fafc' },
  header: {
    height: 60,
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 16,
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
  headerTitle: { flex: 1, fontSize: 18, fontWeight: '900', color: '#0f172a', textAlign: 'center' },
  headerSpacer: { width: 44 },
  content: { padding: 18, paddingBottom: 40 },
  label: { fontSize: 13, fontWeight: '800', color: '#475569', marginBottom: 8, marginTop: 14 },
  input: {
    minHeight: 52,
    borderRadius: 8,
    borderWidth: 1,
    borderColor: '#dbe3ee',
    backgroundColor: '#fff',
    paddingHorizontal: 14,
    paddingVertical: 12,
    fontSize: 15,
    color: '#0f172a',
  },
  textArea: { minHeight: 220, lineHeight: 21 },
  submitButton: {
    height: 54,
    borderRadius: 8,
    backgroundColor: colors.primary,
    alignItems: 'center',
    justifyContent: 'center',
    marginTop: 24,
  },
  submitButtonDisabled: { opacity: 0.55 },
  submitText: { color: '#fff', fontSize: 16, fontWeight: '900' },
});
