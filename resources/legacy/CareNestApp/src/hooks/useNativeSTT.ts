import { useState, useCallback } from 'react';

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

export function useNativeSTT(_language: string = 'vi-VN'): UseNativeSTTReturn {
  const [isListening] = useState(false);
  const [results] = useState<string[]>([]);
  const [partialResults] = useState<string[]>([]);
  const [error] = useState<string | null>('Tính năng nhận diện giọng nói chưa được hỗ trợ trên thiết bị này.');

  const noop = useCallback(async () => {}, []);

  return {
    isListening,
    results,
    partialResults,
    error,
    startListening: noop,
    stopListening: noop,
    cancelListening: noop,
    destroy: noop,
  };
}
