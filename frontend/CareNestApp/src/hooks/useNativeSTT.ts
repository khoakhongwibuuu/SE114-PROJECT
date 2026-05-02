import { useState, useEffect, useCallback } from 'react';
import Voice, {
  SpeechResultsEvent,
  SpeechErrorEvent,
  SpeechStartEvent,
  SpeechEndEvent,
} from '@react-native-voice/voice';
import { Platform, NativeModules } from 'react-native';

export interface UseNativeSTTReturn {
  isListening: boolean;
  results: string[];
  partialResults: string[];
  error: string | null;
  startListening: () => Promise<void>;
  stopListening: () => Promise<void>;
  cancelListening: () => Promise<void>;
  destroy: () => Promise<void>;
}

export function useNativeSTT(language: string = 'vi-VN'): UseNativeSTTReturn {
  const [isListening, setIsListening] = useState(false);
  const [results, setResults] = useState<string[]>([]);
  const [partialResults, setPartialResults] = useState<string[]>([]);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    function onSpeechStart(_e: SpeechStartEvent) {
      setIsListening(true);
      setError(null);
    }

    function onSpeechEnd(_e: SpeechEndEvent) {
      setIsListening(false);
    }

    function onSpeechError(e: SpeechErrorEvent) {
      setIsListening(false);
      setError(e.error?.message || 'Lỗi nhận diện giọng nói');
    }

    function onSpeechResults(e: SpeechResultsEvent) {
      if (e.value) {
        setResults(e.value);
      }
    }

    function onSpeechPartialResults(e: SpeechResultsEvent) {
      if (e.value) {
        setPartialResults(e.value);
      }
    }

    Voice.onSpeechStart = onSpeechStart;
    Voice.onSpeechEnd = onSpeechEnd;
    Voice.onSpeechError = onSpeechError;
    Voice.onSpeechResults = onSpeechResults;
    Voice.onSpeechPartialResults = onSpeechPartialResults;

    return () => {
      if (NativeModules.Voice) {
        void Voice.destroy().then(() => Voice.removeAllListeners()).catch(() => {});
      }
    };
  }, []);

  const startListening = useCallback(async () => {
    if (!NativeModules.Voice) {
      setError('Lỗi: Tính năng Voice chưa được nạp vào ứng dụng. Vui lòng chạy lệnh: npx react-native run-android');
      return;
    }
    try {
      setResults([]);
      setPartialResults([]);
      setError(null);
      await Voice.start(language);
    } catch (e) {
      setError(e instanceof Error ? e.message : String(e));
    }
  }, [language]);

  const stopListening = useCallback(async () => {
    if (!NativeModules.Voice) return;
    try {
      await Voice.stop();
    } catch (e) {
      setError(e instanceof Error ? e.message : String(e));
    }
  }, []);

  const cancelListening = useCallback(async () => {
    if (!NativeModules.Voice) return;
    try {
      await Voice.cancel();
    } catch (e) {
      setError(e instanceof Error ? e.message : String(e));
    }
  }, []);

  const destroy = useCallback(async () => {
    if (!NativeModules.Voice) return;
    try {
      await Voice.destroy();
      setResults([]);
      setPartialResults([]);
      setError(null);
      setIsListening(false);
    } catch (e) {
      setError(e instanceof Error ? e.message : String(e));
    }
  }, []);

  return {
    isListening,
    results,
    partialResults,
    error,
    startListening,
    stopListening,
    cancelListening,
    destroy,
  };
}
