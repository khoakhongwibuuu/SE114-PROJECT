import React, { useCallback, useMemo, useState, useEffect } from 'react';
import {
  Alert,
  ActivityIndicator,
  Image,
  StyleSheet,
  Text,
  TouchableOpacity,
  View,
} from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { useNavigation } from '@react-navigation/native';
import { CARENEST_LOGO_HOUSE } from '../../assets/branding';
import Icon from '../../components/common/Icon';
import { chatAi, speakText } from '../../api/ai';
import { useFamily } from '../../context/FamilyContext';
import { useAuth } from '../../context/AuthContext';
import { useNativeSTT } from '../../hooks/useNativeSTT';
import { useAudioPlayback } from '../../hooks/useAudioPlayback';

export default function VoiceAssistantScreen() {
  const navigation = useNavigation<any>();
  const insets = useSafeAreaInsets();
  const { selectedProfileId } = useFamily();
  const { user } = useAuth();
  
  // Use Native STT instead of AudioRecorder
  const { 
    isListening, 
    results, 
    partialResults, 
    error: sttError, 
    startListening, 
    stopListening,
    destroy: destroySTT
  } = useNativeSTT('vi-VN');
  
  const { isPlaying, playBase64, stopPlayback } = useAudioPlayback();

  const [transcript, setTranscript] = useState('');
  const [replyText, setReplyText] = useState('');
  const [replyAudioBase64, setReplyAudioBase64] = useState('');
  const [conversationId, setConversationId] = useState<number | null>(null);
  const [loading, setLoading] = useState(false);

  const activeProfileId = selectedProfileId || (user?.profileId ? Number(user.profileId) : null);

  // Handle STT errors
  useEffect(() => {
    if (sttError) {
      Alert.alert('Lỗi nhận diện giọng nói', sttError);
    }
  }, [sttError]);

  const statusLabel = useMemo(() => {
    if (loading) {
      return 'ĐANG XỬ LÝ AI';
    }
    if (isListening) {
      return 'ĐANG LẮNG NGHE...';
    }
    if (isPlaying) {
      return 'ĐANG PHÁT PHẢN HỒI';
    }
    return 'TRỢ LÝ GIỌNG NÓI SẴN SÀNG';
  }, [isPlaying, isListening, loading]);

  // Use partial results for real-time feedback
  const displayTranscript = useMemo(() => {
    if (transcript) return transcript;
    if (partialResults.length > 0) return partialResults[0];
    return '';
  }, [partialResults, transcript]);

  const submitChat = useCallback(async (text: string) => {
    setLoading(true);
    try {
      // 1. Send text to AI
      const chatResponse = await chatAi({
        message: text,
        profileId: activeProfileId,
        conversationId: conversationId,
      });

      setReplyText(chatResponse.reply);
      if (chatResponse.conversation_id) {
        setConversationId(chatResponse.conversation_id);
      }

      // 2. Convert reply to Speech (TTS)
      try {
        const audioB64 = await speakText(chatResponse.reply);
        setReplyAudioBase64(audioB64);
        
        // 3. Play the audio
        await playBase64(audioB64);
      } catch (ttsErr) {
        console.error('TTS Error:', ttsErr);
        // We still have the text reply, so just a warning or ignore
      }
    } catch (error) {
      Alert.alert(
        'Không thể xử lý yêu cầu',
        error instanceof Error ? error.message : 'Đã có lỗi xảy ra',
      );
    } finally {
      setLoading(false);
    }
  }, [activeProfileId, conversationId, playBase64]);

  const handlePrimaryAction = useCallback(async () => {
    if (loading) {
      return;
    }

    try {
      if (!isListening) {
        setReplyText('');
        setTranscript('');
        setReplyAudioBase64('');
        await startListening();
        return;
      }

      // Stop listening and wait for results
      await stopListening();
      
      // Note: results are usually available in the next render cycle 
      // via the 'results' state from useNativeSTT.
      // But we can trigger submission once results populate.
    } catch (error) {
      Alert.alert(
        'Lỗi',
        error instanceof Error ? error.message : 'Đã có lỗi xảy ra',
      );
    }
  }, [isListening, loading, startListening, stopListening]);

  // Auto-submit when results are finalized
  useEffect(() => {
    if (!isListening && results.length > 0 && !loading && !replyText) {
      const finalResult = results[0];
      setTranscript(finalResult);
      void submitChat(finalResult);
    }
  }, [isListening, results, loading, replyText, submitChat]);

  const handleReplay = useCallback(async () => {
    if (!replyAudioBase64 || loading) {
      return;
    }

    try {
      await playBase64(replyAudioBase64);
    } catch (error) {
      Alert.alert(
        'Không thể phát lại audio',
        error instanceof Error ? error.message : 'Đã có lỗi xảy ra',
      );
    }
  }, [loading, playBase64, replyAudioBase64]);

  const handleStopPlayback = useCallback(async () => {
    try {
      await stopPlayback();
    } catch {
      // ignore stop errors
    }
  }, [stopPlayback]);

  // Cleanup on unmount
  useEffect(() => {
    return () => {
      void destroySTT();
    };
  }, [destroySTT]);

  return (
    <View style={[styles.root, { paddingTop: insets.top, paddingBottom: insets.bottom }]}>
      <View style={styles.header}>
        <TouchableOpacity style={styles.closeBtn} onPress={() => navigation.goBack()}>
          <Icon name="close" size={24} color="#fff" />
        </TouchableOpacity>
      </View>

      <View style={styles.content}>
        <View style={[styles.mainMic, isListening && styles.mainMicActive]}>
          <Icon name={isListening ? 'graphic_eq' : 'mic'} size={40} color="#fff" />
        </View>

        <Text style={styles.badgeText}>{statusLabel}</Text>
        <Text style={styles.caption}>
          Bấm để bắt đầu nói. Ứng dụng sẽ tự động nhận diện và gửi yêu cầu của bạn đến AI.
        </Text>

        <TouchableOpacity
          style={[styles.submitBtn, (loading || isListening) && styles.submitBtnActive]}
          onPress={() => void handlePrimaryAction()}
          disabled={loading}
        >
          <Text style={styles.submitText}>
            {loading ? 'Đang xử lý...' : isListening ? 'Đang nghe... (Bấm để dừng)' : 'Bắt đầu nói'}
          </Text>
        </TouchableOpacity>

        {loading ? (
          <View style={styles.loadingWrap}>
            <ActivityIndicator size="small" color="#fff" />
            <Text style={styles.loadingText}>AI đang suy nghĩ...</Text>
          </View>
        ) : null}

        {isPlaying ? (
          <TouchableOpacity style={styles.secondaryBtn} onPress={() => void handleStopPlayback()}>
            <Text style={styles.secondaryBtnText}>Dừng phát audio</Text>
          </TouchableOpacity>
        ) : null}

        {replyAudioBase64 && !isPlaying ? (
          <TouchableOpacity style={styles.secondaryBtn} onPress={() => void handleReplay()}>
            <Text style={styles.secondaryBtnText}>Phát lại phản hồi</Text>
          </TouchableOpacity>
        ) : null}

        {displayTranscript ? (
          <View style={styles.transcriptCard}>
            <Text style={styles.cardLabel}>BẠN ĐÃ NÓI:</Text>
            <Text style={styles.cardText}>{displayTranscript}</Text>
          </View>
        ) : null}

        {replyText ? (
          <View style={styles.aiCard}>
            <View style={styles.aiHeader}>
              <View style={styles.aiAvatar}>
                <Image source={CARENEST_LOGO_HOUSE} style={styles.aiAvatarIcon} resizeMode="contain" />
              </View>
              <Text style={styles.aiName}>AI Care Assistant</Text>
            </View>
            <Text style={styles.aiText}>{replyText}</Text>
          </View>
        ) : null}
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  root: { flex: 1, backgroundColor: '#0f131a' },
  header: { height: 60, alignItems: 'flex-end', paddingHorizontal: 24, justifyContent: 'center' },
  closeBtn: {
    width: 44,
    height: 44,
    borderRadius: 22,
    backgroundColor: 'rgba(255,255,255,0.1)',
    alignItems: 'center',
    justifyContent: 'center',
  },
  content: { flex: 1, alignItems: 'center', paddingHorizontal: 24, gap: 18 },
  mainMic: {
    width: 110,
    height: 110,
    borderRadius: 55,
    backgroundColor: '#3498db',
    alignItems: 'center',
    justifyContent: 'center',
    marginTop: 24,
  },
  mainMicActive: { backgroundColor: '#e74c3c' },
  badgeText: { color: '#fff', fontSize: 13, fontWeight: '800', letterSpacing: 1.2 },
  caption: { color: 'rgba(255,255,255,0.6)', fontSize: 14, textAlign: 'center', lineHeight: 22 },
  submitBtn: {
    width: '100%',
    height: 54,
    borderRadius: 20,
    backgroundColor: '#3498db',
    alignItems: 'center',
    justifyContent: 'center',
  },
  submitBtnActive: { backgroundColor: '#e74c3c' },
  submitText: { color: '#fff', fontSize: 16, fontWeight: '800' },
  loadingWrap: { flexDirection: 'row', alignItems: 'center', gap: 8 },
  loadingText: { color: 'rgba(255,255,255,0.7)', fontSize: 13 },
  secondaryBtn: {
    width: '100%',
    height: 46,
    borderRadius: 16,
    borderWidth: 1,
    borderColor: 'rgba(255,255,255,0.16)',
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: 'rgba(255,255,255,0.05)',
  },
  secondaryBtnText: { color: '#fff', fontSize: 14, fontWeight: '700' },
  transcriptCard: {
    width: '100%',
    backgroundColor: 'rgba(255,255,255,0.08)',
    borderRadius: 20,
    padding: 18,
    borderWidth: 1,
    borderColor: 'rgba(255,255,255,0.1)',
  },
  cardLabel: {
    color: 'rgba(255,255,255,0.7)',
    fontSize: 11,
    fontWeight: '800',
    letterSpacing: 1,
    marginBottom: 8,
  },
  cardText: { color: '#fff', fontSize: 15, lineHeight: 22 },
  aiCard: {
    width: '100%',
    backgroundColor: 'rgba(255,255,255,0.06)',
    borderRadius: 24,
    padding: 24,
    borderWidth: 1,
    borderColor: 'rgba(255,255,255,0.08)',
  },
  aiHeader: { flexDirection: 'row', alignItems: 'center', gap: 12, marginBottom: 16 },
  aiAvatar: {
    width: 36,
    height: 36,
    borderRadius: 18,
    alignItems: 'center',
    justifyContent: 'center',
  },
  aiAvatarIcon: { width: 20, height: 20 },
  aiName: { flex: 1, color: '#fff', fontSize: 15, fontWeight: '700' },
  aiText: { color: 'rgba(255,255,255,0.85)', fontSize: 16, lineHeight: 24, fontFamily: 'Inter' },
});
