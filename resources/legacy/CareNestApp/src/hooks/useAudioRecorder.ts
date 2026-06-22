import { useCallback, useMemo, useState } from 'react';

export function useAudioRecorder() {
  const [isRecording] = useState(false);
  const [currentFilePath] = useState<string | null>(null);

  const startRecording = useCallback(async (): Promise<string | null> => {
    console.warn('useAudioRecorder: Audio recording is not supported in this build.');
    return null;
  }, []);

  const stopRecording = useCallback(async (): Promise<string | null> => {
    return currentFilePath;
  }, [currentFilePath]);

  return useMemo(
    () => ({
      isRecording,
      currentFilePath,
      startRecording,
      stopRecording,
    }),
    [currentFilePath, isRecording, startRecording, stopRecording],
  );
}
