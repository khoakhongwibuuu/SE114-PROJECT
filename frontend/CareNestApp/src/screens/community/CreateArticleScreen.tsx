import React, { useState } from 'react';
import {
  ActivityIndicator,
  Alert,
  Image,
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
import { launchImageLibrary } from 'react-native-image-picker';
import MaterialCommunityIcons from 'react-native-vector-icons/MaterialCommunityIcons';
import { createArticle } from '../../api/community';
import { uploadMedia } from '../../api/media';
import { colors } from '../../theme/colors';

export default function CreateArticleScreen() {
  const navigation = useNavigation<any>();
  const [title, setTitle] = useState('');
  const [tags, setTags] = useState('');
  const [content, setContent] = useState('');
  const [imageUrl, setImageUrl] = useState('');
  const [uploadingImage, setUploadingImage] = useState(false);
  const [saving, setSaving] = useState(false);

  const canSubmit = title.trim().length > 0 && content.trim().length > 0 && !saving && !uploadingImage;

  const handlePickImage = async () => {
    try {
      setUploadingImage(true);
      const result = await launchImageLibrary({
        mediaType: 'photo',
        maxWidth: 1400,
        maxHeight: 1400,
        quality: 0.7,
        selectionLimit: 1,
      });

      if (result.didCancel) {
        return;
      }

      const asset = result.assets?.[0];
      if (!asset?.uri) {
        Alert.alert('Không có ảnh', 'Vui lòng chọn một ảnh hợp lệ.');
        return;
      }

      const uploaded = await uploadMedia(
        asset.uri,
        asset.fileName || `article-${Date.now()}.jpg`,
        asset.type || 'image/jpeg',
        'articles',
      );
      setImageUrl(uploaded.url);
    } catch (error) {
      Alert.alert('Không thể tải ảnh lên', error instanceof Error ? error.message : 'Đã có lỗi xảy ra');
    } finally {
      setUploadingImage(false);
    }
  };

  const handleSubmit = async () => {
    if (!canSubmit) {
      Alert.alert('Thiếu thông tin', 'Vui lòng nhập tiêu đề và nội dung bài viết.');
      return;
    }

    try {
      setSaving(true);
      await createArticle({
        title: title.trim(),
        tags: tags.trim() || undefined,
        content: content.trim(),
        imageUrl: imageUrl.trim() || undefined,
      });
      navigation.goBack();
    } catch (error) {
      Alert.alert(
        'Không thể tạo bài viết',
        error instanceof Error ? error.message : 'Đã có lỗi xảy ra',
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
        <Text style={styles.headerTitle}>Tạo bài viết</Text>
        <View style={styles.headerSpacer} />
      </View>

      <ScrollView
        contentContainerStyle={styles.content}
        keyboardShouldPersistTaps="handled"
        showsVerticalScrollIndicator={false}
      >
        <Text style={styles.label}>Tiêu đề</Text>
        <TextInput
          style={styles.input}
          value={title}
          onChangeText={setTitle}
          placeholder="Ví dụ: Chăm sóc trẻ sốt tại nhà"
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

        <Text style={styles.label}>Ảnh minh họa</Text>
        <TouchableOpacity
          style={styles.imagePicker}
          onPress={() => void handlePickImage()}
          disabled={uploadingImage || saving}
          activeOpacity={0.86}
        >
          {imageUrl ? (
            <Image source={{ uri: imageUrl }} style={styles.previewImage} resizeMode="cover" />
          ) : (
            <View style={styles.imagePlaceholder}>
              {uploadingImage ? (
                <ActivityIndicator color={colors.primary} />
              ) : (
                <>
                  <MaterialCommunityIcons name="image-plus" size={30} color={colors.primary} />
                  <Text style={styles.imagePlaceholderText}>Chọn ảnh và nén trước khi tải lên</Text>
                </>
              )}
            </View>
          )}
        </TouchableOpacity>
        {imageUrl ? (
          <TouchableOpacity
            style={styles.removeImageButton}
            onPress={() => setImageUrl('')}
            disabled={uploadingImage || saving}
          >
            <MaterialCommunityIcons name="close-circle" size={18} color="#dc2626" />
            <Text style={styles.removeImageText}>Gỡ ảnh</Text>
          </TouchableOpacity>
        ) : null}

        <Text style={styles.label}>Nội dung</Text>
        <TextInput
          style={[styles.input, styles.textArea]}
          value={content}
          onChangeText={setContent}
          placeholder="Nhập nội dung bài viết..."
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
          <Text style={styles.submitText}>
            {saving ? 'Đang lưu...' : uploadingImage ? 'Đang tải ảnh...' : 'Đăng bài'}
          </Text>
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
  imagePicker: {
    height: 190,
    borderRadius: 8,
    borderWidth: 1,
    borderStyle: 'dashed',
    borderColor: '#bfdbfe',
    backgroundColor: '#eff6ff',
    overflow: 'hidden',
  },
  previewImage: { width: '100%', height: '100%' },
  imagePlaceholder: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    padding: 16,
    gap: 8,
  },
  imagePlaceholderText: { fontSize: 13, fontWeight: '800', color: colors.primary, textAlign: 'center' },
  removeImageButton: {
    alignSelf: 'flex-start',
    marginTop: 8,
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
    paddingVertical: 6,
  },
  removeImageText: { fontSize: 13, fontWeight: '900', color: '#dc2626' },
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
